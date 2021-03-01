#include "IPCEventNotify.h"

pthread_mutex_t  IPCEventNotify::mutex = PTHREAD_MUTEX_INITIALIZER;
IPCEventNotify* IPCEventNotify::instance = 0;


IPCEventNotify::IPCEventNotify() {

   isinit = false;
}

IPCEventNotify::~IPCEventNotify() {
    //isinit = -1;
    //instance = NULL;
    //mutex = NULL;
}

IPCEventNotify* IPCEventNotify::getInstance()
{
   if(instance == NULL)   {
           //double check
           MutexLock lock(mutex);
           if(instance == NULL)   {
               instance = new IPCEventNotify();
           }
       }
   return instance;
}


void IPCEventNotify::setPipeFilePath(string filePath){
     pipefilepath = filePath;
}


string IPCEventNotify::getPipeFilePath(){
    return pipefilepath;
}

void IPCEventNotify::setPipeFileName(string fileName){
     pipefilename = fileName;
}


string IPCEventNotify::getPipeFileName(){
    return pipefilename;
}

void IPCEventNotify::setTransDataListener(JNIEnv *env,jobject transdata_listener_obj){
   	transdata_listener = transdata_listener_obj;
    if (transdata_listener_obj) {
  		 // get method IDs of Java object for callback
        jclass clazz = env->GetObjectClass(transdata_listener_obj);
        if (LIKELY(clazz)) {
            //transdata_listener_method = env->GetMethodID(clazz,
            //    "onTransData",	"(Ljava/nio/ByteBuffer;)V");
            env->DeleteLocalRef(clazz);

        } else {
            LOGW("failed to get object class");
        }

        env->ExceptionClear();
        /*
        *
        if (!transdata_listener_method) {
            LOGE("Can't find IPCTransDataListener#onTransData");
            env->DeleteGlobalRef(transdata_listener_obj);
            transdata_listener = transdata_listener_obj = NULL;
        }else{
             transdata_listener_method = NULL;
             LOGD("setTransDataListener success");
        }*/
  	}
}

jobject IPCEventNotify::getTransDataListener(){
   return transdata_listener;
}