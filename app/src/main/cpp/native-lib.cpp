#include <jni.h>
#include <pthread.h>

JavaVM* mJvm = NULL;
jclass cls_MainActivity = NULL;

void cacheJClasses(JavaVM *vm) {
    JNIEnv *env;
    vm->GetEnv((void**)&env, JNI_VERSION_1_6);
    jclass jcls = env->FindClass("com/czf/nativeapp/MainActivity");
    cls_MainActivity = (jclass)env->NewGlobalRef(jcls);
}

void log(JNIEnv *env, char const *tag, char const *log)
{
    if (cls_MainActivity == NULL) return;

    jmethodID mid = env->GetStaticMethodID(cls_MainActivity, "nativeLog",
                                           "(Ljava/lang/String;Ljava/lang/String;)V");
    if (mid == NULL) return;

    jstring jtag = env->NewStringUTF(tag);
    jstring jlog = env->NewStringUTF(log);
    env->CallStaticVoidMethod(cls_MainActivity, mid, jtag, jlog);
}

void * nativeMQRoutine(void *arg)
{
    JNIEnv *env = NULL;
    int res = mJvm->AttachCurrentThread(&env, NULL);
    if (res < 0) return NULL;

    log(env, "--==--", "native thread run");
    jmethodID mid = env->GetStaticMethodID(cls_MainActivity, "startNewMessageQueue", "()V");
    env->CallStaticVoidMethod(cls_MainActivity, mid);
    log(env, "--==--", "native thread run below");

    mJvm->DetachCurrentThread();
    return NULL;
}

extern "C" JNIEXPORT void JNICALL
Java_com_czf_nativeapp_MainActivity_startNativeMQ(JNIEnv *env, jobject jobj)
{
    pthread_t pthread;
    pthread_create(&pthread, NULL, nativeMQRoutine, NULL);
}

void nativeRun(void)
{
    JNIEnv *env = NULL;
    mJvm->GetEnv((void **)&env, JNI_VERSION_1_6);
    log(env, "---------", "hehehe");
}

int setNativePtr2Java(JNIEnv *env) {
    jmethodID mid = env->GetStaticMethodID(cls_MainActivity, "setNativePtr", "(J)V");
    if (mid == NULL) return 1;

    env->CallStaticVoidMethod(cls_MainActivity, mid, (long)nativeRun);
    return 0;
}

extern "C" JNIEXPORT void JNICALL
Java_com_czf_nativeapp_MainActivity_sendMsg2Native(JNIEnv *env, jobject jobj, jobject handler, jstring msg)
{
    if (setNativePtr2Java(env) != 0) return;

    jclass clsHandler = env->GetObjectClass(handler);
    if (clsHandler == NULL) return;

    jmethodID mid = env->GetMethodID(clsHandler, "sendEmptyMessage", "(I)Z");
    env->DeleteLocalRef(clsHandler);
    if (mid == NULL) return;

    env->CallBooleanMethod(handler, mid, 7);
}

typedef void (*RUN_FN)(void);

extern "C" JNIEXPORT void JNICALL
Java_com_czf_nativeapp_MainActivity_nativeRun(JNIEnv *env, jclass jclz, jlong nativeFnPtr)
{
    ((RUN_FN)nativeFnPtr)();
}

extern "C" JNIEXPORT jobject JNICALL
Java_com_czf_nativeapp_MainActivity_getJavaObjFromNative(JNIEnv *env, jobject thiz)
{
    jclass cls = env->GetObjectClass(thiz);
    if (cls == NULL) return NULL;

    jmethodID mid = env->GetMethodID(cls, "<init>", "()V");
    if (mid == NULL) return NULL;

    jobject obj = env->NewObject(cls, mid);

    //return env->NewGlobalRef(obj); // memory leaks
    return obj;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_czf_nativeapp_MainActivity_handleDataFromNative(JNIEnv *env, jobject thiz, jintArray iArr)
{
    jint *jintArr;
    jboolean isCopy = JNI_FALSE;
    jintArr = env->GetIntArrayElements(iArr, &isCopy);
    jintArr[0] = 7;
    env->ReleaseIntArrayElements(iArr, jintArr, 0);
    return isCopy;
}

/**
 * When System.loadLibrary loads a native library, the virtual machine
 * searches for the method.
 * typical use: caching javaVM pointer, class references, field and
 *              method IDs.
 */
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved)
{
    mJvm = vm;
    cacheJClasses(vm);
    JNIEnv *env;
    vm->GetEnv((void**)&env, JNI_VERSION_1_6);
    log(env, "-=-=-=-=-", "lib is attached to jvm.");

    return JNI_VERSION_1_6;
}

/**
 * In summary, you should be careful when writing JNI_OnUnload
 * handlers. Avoid complex locking operations that may introduce
 * deadlocks. Keep in mind that classes have been unloaded when
 * the JNI_OnUnload handler is invoked.
 */
JNIEXPORT void JNICALL JNI_OnUnload(JavaVM* vm, void* reserved)
{
    JNIEnv *env;
    vm->GetEnv((void**)&env, JNI_VERSION_1_6);
    log(env, "-=-=-=-=-", "lib is detached from jvm.");
}
