#!/usr/bin/env python3

import os
import shlex
import shutil
import stat
import subprocess
import sys
import tempfile


def which_or_name(name: str) -> str:
    return shutil.which(name) or name


def _pretty(cmd: list) -> str:
    if os.name == "nt":
        return subprocess.list2cmdline(cmd)
    return " ".join(shlex.quote(str(c)) for c in cmd)


def run(cmd: list) -> int:
    print("+ " + _pretty(cmd), file=sys.stderr)
    # Windows: .bat/.cmd files must go through the shell
    if os.name == "nt" and cmd and str(cmd[0]).lower().endswith((".bat", ".cmd")):
        return subprocess.run(subprocess.list2cmdline(cmd), shell=True).returncode
    return subprocess.run(cmd).returncode


def main(argv: list) -> int:
    if not argv:
        print("Usage: ancc file1.java [file2.java ...]")
        return 1

    java_files = list(argv)
    main_class_name = os.path.splitext(os.path.basename(java_files[0]))[0]

    android_jar = os.environ.get("android_jar", "").strip()
    if not android_jar:
        print("Build FAILED: environment variable 'android_jar' is not set")
        return 1
    if not os.path.isfile(android_jar):
        print(f"Build FAILED: android.jar not found: {android_jar}")
        return 1

    for f in java_files:
        if not os.path.isfile(f):
            print(f"Build FAILED: Java source not found: {f}")
            return 1

    javac = which_or_name("javac")
    d8 = which_or_name("d8")

    temp_dir = tempfile.mkdtemp(prefix="ancc_")
    try:
        # ---- 1. compile ----
        rc = run([javac, "--release", "11", "-cp", android_jar, "-d", temp_dir, *java_files])
        if rc != 0:
            print("Build FAILED: Java compilation error")
            return 1

        # ---- 2. collect .class files (recursive) ----
        class_files = []
        for root, _dirs, files in os.walk(temp_dir):
            for name in files:
                if name.endswith(".class"):
                    class_files.append(os.path.join(root, name))
        class_files.sort()

        if not class_files:
            print(f"Build FAILED: No .class files generated in {temp_dir}")
            print("Directory contents:")
            for root, _dirs, files in os.walk(temp_dir):
                for name in files:
                    print(os.path.join(root, name))
            return 1

        print(f"Packaging {len(class_files)} class files")

        # ---- 3. run d8 ----
        if os.path.exists("classes.dex"):
            os.remove("classes.dex")

        rc = run([d8, "--release", "--min-api", "24", "--lib", android_jar,
                  "--output", ".", *class_files])
        if rc != 0:
            print("Build FAILED: D8 error")
            return 1

        if not os.path.isfile("classes.dex"):
            print("Build FAILED: classes.dex not found")
            return 1

        # ---- 4. rename to <MainClass>.dex, then chmod 444 ----
        out_name = main_class_name + ".dex"
        if os.path.exists(out_name):
            os.chmod(out_name, stat.S_IWRITE | stat.S_IREAD)  # 解锁只读
            os.remove(out_name)
        os.replace("classes.dex", out_name)
        try:
            os.chmod(out_name, stat.S_IRUSR | stat.S_IRGRP | stat.S_IROTH)
        except OSError:
            pass

        print(f"Built {out_name} from {len(java_files)} Java file(s)")

        # ---- 5. multi-file hint ----
        if len(java_files) > 1:
            print("Available classes in dex:")
            for jf in java_files:
                print(f"  {os.path.splitext(os.path.basename(jf))[0]}")

        return 0
    finally:
        shutil.rmtree(temp_dir, ignore_errors=True)


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))