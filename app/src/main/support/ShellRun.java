package top.clspd.shellauthorization.Public.Shell;

import android.content.pm.PackageInfo;
import android.os.IBinder;
import android.os.Process;
import android.system.Os;

import java.lang.reflect.Method;

public class ShellRun {

    private static final String DEFAULT_PKG = "top.clspd.shellauthorization";
    private static final String LAUNCHER = "top.clspd.shellauthorization.Public.Shell.ShellLauncher";

    public static void main(String[] args) {
        String pkg = System.getenv().getOrDefault(
                "SHELL_AUTHORIZATION_APPLICATION_NAME", DEFAULT_PKG);
        try {
            String base = baseApkPath(pkg);

            String path = "/system/bin/app_process";
            String[] argv = new String[4 + args.length];
            argv[0] = "app_process";
            argv[1] = "-Djava.class.path=" + base;
            argv[2] = "/system/bin";
            argv[3] = LAUNCHER;
            System.arraycopy(args, 0, argv, 4, args.length);
            Os.execve(path, argv, Os.environ());
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static String baseApkPath(String pkg) throws Exception {
        PackageInfo pi = getPackageInfo(pkg);
        if (pi == null || pi.applicationInfo == null) {
            throw new IllegalStateException("package not found: " + pkg);
        }
        return pi.applicationInfo.sourceDir;
    }

    private static PackageInfo getPackageInfo(String pkg) throws Exception {
        Object ipm = ipm();
        Class<?> ic = Class.forName("android.content.pm.IPackageManager");

        for (Method m : ic.getDeclaredMethods()) {
            if (!m.getName().equals("getPackageInfo")) continue;
            Class<?>[] pt = m.getParameterTypes();
            if (pt.length != 3) continue;
            m.setAccessible(true);
            return (PackageInfo) m.invoke(ipm, pkg, zeroFlags(pt[1]), userId());
        }
        throw new NoSuchMethodException("IPackageManager.getPackageInfo(String,?,int)");
    }

    private static Object ipm() throws Exception {
        try {
            Method m = Class.forName("android.app.AppGlobals")
                    .getDeclaredMethod("getPackageManager");
            m.setAccessible(true);
            Object r = m.invoke(null);
            if (r != null) return r;
        } catch (Throwable ignored) {}

        Class<?> sm = Class.forName("android.os.ServiceManager");
        Method getService = sm.getDeclaredMethod("getService", String.class);
        getService.setAccessible(true);
        IBinder binder = (IBinder) getService.invoke(null, "package");
        if (binder == null) throw new IllegalStateException("no package service");

        Class<?> stub = Class.forName("android.content.pm.IPackageManager$Stub");
        Method asInterface = stub.getDeclaredMethod("asInterface", IBinder.class);
        asInterface.setAccessible(true);
        return asInterface.invoke(null, binder);
    }

    private static int userId() {
        try {
            return (Integer) Class.forName("android.os.UserHandle")
                    .getMethod("getUserId", int.class)
                    .invoke(null, Process.myUid());
        } catch (Throwable t) {
            return Process.myUid() / 100000;
        }
    }

    private static Object zeroFlags(Class<?> t) throws Exception {
        if (t == int.class || t == Integer.class) return 0;
        if (t == long.class || t == Long.class) return 0L;

        if (t.getName().endsWith("PackageInfoFlags")) {
            try {
                return Class.forName("android.content.pm.PackageManager$PackageInfoFlags")
                        .getMethod("of", long.class).invoke(null, 0L);
            } catch (Throwable ignored) {}
            return Class.forName("android.content.pm.PackageInfoFlags")
                    .getMethod("of", long.class).invoke(null, 0L);
        }
        throw new IllegalArgumentException("unexpected AIDL param: " + t.getName());
    }
}
