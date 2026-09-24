// Write C++ code here.
//
// Do not forget to dynamically load the C++ library into your application.
//
// For instance,
//
// In MainActivity.java:
//    static {
//       System.loadLibrary("shellauthorization");
//    }
//
// Or, in MainActivity.kt:
//    companion object {
//      init {
//         System.loadLibrary("shellauthorization")
//      }
//    }

#include <jni.h>
#include <csignal>
#include <unistd.h>
#include <sys/stat.h>
#include <functional>
#include <string>
#include <filesystem>
#include <utility>
#include <sys/wait.h>
#include "sa_config.h"
using namespace std;

class RAIIHelper {
protected:
    using cb_t = std::function<void()>;
    cb_t cb;
public:
    RAIIHelper(cb_t cb): cb(std::move(cb)) {}
    ~RAIIHelper() {
        if (cb) cb();
    }
    RAIIHelper(const RAIIHelper&) = delete;
    RAIIHelper& operator=(const RAIIHelper&) = delete;
};
using PCSTR = const char*;
#define LOAD_STR_PARAM(name) \
PCSTR name = env->GetStringUTFChars(_ ## name, nullptr); \
if (!name) return ENOMEM; \
RAIIHelper _strRemover__ ## name ([env, name, _ ## name] { env->ReleaseStringUTFChars(_ ## name, name); })
inline bool E_FAILED(int result) { return result != 0; }

// ----------

int SetupDataDirectoryStructure(PCSTR lpszDataDirectory);

extern "C"
[[maybe_unused]] JNIEXPORT jint JNICALL
Java_app_AppLogic_MainAppLogic_MyShellAuthorization_MyNative_AppNativeStartupInitialization_DoInitialize(
    JNIEnv *env, jobject thiz, jstring _jni, jstring _lib, jstring _apk, jstring _data
) {
    LOAD_STR_PARAM(jni);
    LOAD_STR_PARAM(lib);
    LOAD_STR_PARAM(apk);
    LOAD_STR_PARAM(data);

    // check jni lib itself
    struct stat buf{};
    if (E_FAILED(stat(jni, &buf))) return errno;
    if (!S_ISREG(buf.st_mode)) return EIO;

    // check executable lib
    pid_t libChecker = fork();
    if (libChecker < 0) return errno;
    if (libChecker == 0) {
        execl(lib, lib, "1", NULL);
        _exit(127);
    }
    int pstatus = 1;
    if (waitpid(libChecker, &pstatus, 0) == -1) return errno;
    if (!WIFEXITED(pstatus)) return EBUSY;
    if (WEXITSTATUS(pstatus) != 0) return ENOEXEC;

    // check apk installation
    if (E_FAILED(stat(apk, &buf))) return errno;
    if (!S_ISREG(buf.st_mode)) return EIO;

    // check data directory
    if (E_FAILED(stat(data, &buf))) return errno;
    if (!S_ISDIR(buf.st_mode)) return EIO;

    // setup data directory structure
    if (int error = SetupDataDirectoryStructure(data)) return error;

    // init success
    return 0;
}

extern "C"
[[maybe_unused]] JNIEXPORT jint JNICALL
Java_app_AppLogic_MainAppLogic_MyShellAuthorization_MyNative_SignalSender_SendSignal(
    JNIEnv *env, jobject thiz, jint pid, jint sig
) {
    return kill(pid, sig);
}

extern "C"
[[maybe_unused]] JNIEXPORT jstring JNICALL
Java_app_AppLogic_MainAppLogic_MyShellAuthorization_MyDataDirectory__1get(JNIEnv *env, jobject thiz, jstring _data) {
    PCSTR data = env->GetStringUTFChars(_data, nullptr);
    if (!data) return nullptr;
    RAIIHelper _strRemover__data ([env, data, _data] { env->ReleaseStringUTFChars(_data, data); });

    std::filesystem::path p(data);
    p /= APP_DATA_DIR_VER;

    return env->NewStringUTF(p.c_str());
}

// -----------

int SetupDataDirectoryStructure(PCSTR lpszDataDirectory) {
    struct stat buf{};

    filesystem::path dataDir(lpszDataDirectory);
    filesystem::path appDataDir = dataDir / APP_DATA_DIR_VER;

    if (E_FAILED(stat(appDataDir.c_str(), &buf))) {
        if (errno != ENOENT) return errno;
        if (E_FAILED(mkdir(appDataDir.c_str(), 0700))) return errno;
    }
    else if (!S_ISDIR(buf.st_mode)) return EIO;

    return 0;
}


