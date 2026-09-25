#include <jni.h>
#include <cstdlib>
#include <unistd.h>
#include "sa_config.h"
using namespace std;

static jint state1 = 0xFFFFFF;

extern "C"
JNIEXPORT jint JNICALL
Java_app_AppLogic_MainAppLogic_MyShellAuthorization_MyNative_AntiTamper_AntiTamper_1ComputeValue(
        JNIEnv *env, jobject thiz, jint type, jint input) {
    switch (type) {
        case 1:
            return 2;
        case 2:
            return 0x124f;
        case 0x331d:
            return (0x432 * input) + (input | 0x00010000) - type;
        case 0x1301e4:
            return uint8_t (jint(input) * jint(type));
        case 33550336:
            state1 += (input) & 0x7FFFFFFF;
            return type / input;
        default:
            return -1;
    }
    kill(getpid(), 9);
    return -1;
}

extern "C"
JNIEXPORT jint JNICALL
Java_app_AppLogic_MainAppLogic_MyShellAuthorization_MyNative_AntiTamper_AntiTamper_1ComputeValue2(
        JNIEnv *env, jclass clazz, jint type, jint input, jint input2) {
    switch (type) {
        case 0x231841da:
            if (input / 0x51274 == input2 * 0x12) return 0;
            break;
        case 0x28a92adf:
            if (input2 == ((input & 0x123124) + 1)) return input2 * 2;
            break;
        case 3:
            kill(getpid(), 9);
            break;
        case 0x131a4235:
            state1 -= type | input;
            return state1 + 3;
        case 0x131a2597:
            if (type - input2 > 0x10203040) return input2 ^ (type + input);
            break;
        case 0x666:
            if (!(input2 + input - 3)) return 0x888;
            break;
        case 0x7912dfa1:
            if (state1 - input2 == input) return getpid();
            break;
        default:
            kill(getpid(), 31);
    }
    kill(getpid(), 9);
    return 1;
}
