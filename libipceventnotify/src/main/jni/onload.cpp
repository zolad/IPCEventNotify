#include "onload.h"
#include "base_util.h"

#define LOCAL_DEBUG 0

extern int register_ipcevnetnotify(JNIEnv *env);

jint JNI_OnLoad(JavaVM *vm, void *reserved) {
#if LOCAL_DEBUG
    LOGD("JNI_OnLoad");
#endif

    JNIEnv *env;
    if (vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }
    // register native methods
    int result = register_ipcevnetnotify(env);
	setVM(vm);
#if LOCAL_DEBUG
    LOGD("JNI_OnLoad:finshed:result=%d", result);
#endif
    return JNI_VERSION_1_6;
}
