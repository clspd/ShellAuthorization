#include <jni.h>
#include <unistd.h>
#include <string>
#include "sa_config.h"
using namespace std;

extern "C"
[[maybe_unused]] JNIEXPORT jstring JNICALL
Java_app_AppLogic_MainAppLogic_MyShellAuthorization_MyNative_DataHelper_getShizukuPackageName(
        JNIEnv *env, jclass clazz) {
    return env->NewStringUTF("moe.shizuku.privileged.api");
}

extern "C"
[[maybe_unused]] JNIEXPORT jstring JNICALL
Java_app_AppLogic_MainAppLogic_MyShellAuthorization_MyNative_DataHelper_getShizukuDownloadPage(
        JNIEnv *env, jclass clazz) {
    return env->NewStringUTF("https://github.com/RikkaApps/Shizuku/releases\0https://rikka.app/\0");
}

extern "C"
[[maybe_unused]] JNIEXPORT jstring JNICALL
Java_app_AppLogic_MainAppLogic_MyShellAuthorization_MyNative_DataHelper_getShizukuShellMainClass(
        JNIEnv *env, jclass clazz) {
    return env->NewStringUTF("rikka.shizuku.shell.ShizukuShellLoader");
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_app_AppLogic_MainAppLogic_MyShellAuthorization_MyNative_DataHelper_checkWhetherKnownShizukuSignature(
        JNIEnv *env, jclass clazz, jstring _signature) {
    const char *signature = env->GetStringUTFChars(_signature, nullptr);
    if (signature == nullptr) {
        return -1;
    }

    string sig = signature;
    env->ReleaseStringUTFChars(_signature, signature);

    if (sig == "268b5590e868fb08bae7e0ac413564cd1ff88f5ccff74af9dbd0dc918e30db30") return true; // original from gh release
    if (sig == "947e81ed49d6cde4006bd64b94ab69e63f50074ce01451d1bf30acb14e591f37") return true; // thedjchi fork

    return false;
}

