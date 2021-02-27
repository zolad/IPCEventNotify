#include "libIPCEventNotify.h"
#include "IPCEventNotify.h"
#include "pipe/named_pipe.h"



/**
*create a singleton IPCEventNotify object
*/
jlong nativeCreate(JNIEnv *env, jobject thiz,jobject transdata_listener){

  IPCEventNotify *notify = IPCEventNotify::getInstance();
  if (LIKELY(notify)) {
      notify->setTransDataListener(env,env->NewGlobalRef(transdata_listener));
      return reinterpret_cast<jlong>(notify);
  }

  return FAIL;

}

/**
*init IPCEventNotify and check input,will create a named pipe file with dir_filepath and indication_name
*/
jint nativeInit(JNIEnv *env, jobject thiz,jlong nativePtr,jstring dirFilepath,jstring indicationName){
//	LOGD("nativeInit");

  IPCEventNotify *notify = reinterpret_cast<IPCEventNotify *>(nativePtr);
  // LOGD("init check isinit%d",notify->isinit);
  bool result = false;
  if (LIKELY(notify)) {
       if(notify->isinit)
          return INITFAIL_ALREADYINIT;
       const char *dir_fp = env->GetStringUTFChars(dirFilepath, JNI_FALSE);
       const char *id_name = env->GetStringUTFChars(indicationName, JNI_FALSE);

       result = checkFilePathValid(dir_fp,id_name);
       notify->isinit = result;

       if(result){
          notify->setPipeFilePath(string(dir_fp));
          notify->setPipeFileName(string(id_name));
          pthread_t pthread;
          pthread_create(&pthread, NULL, startPipeDataListen, (void *)notify);
          //pthread_join(pthread, 0);
       }

       env->ReleaseStringUTFChars(dirFilepath, dir_fp);
       env->ReleaseStringUTFChars(indicationName, id_name);
   }


   return result?SUCCESS:FAIL;

}

/**
*notify  those registered observer of a ipc event
*/
void nativeNotifyIPCEvent(JNIEnv *env, jobject thiz,jlong nativePtr,jobjectArray observerArray,int oberserNum,jbyteArray eventData, jint dataLen){

  IPCEventNotify *notify = reinterpret_cast<IPCEventNotify *>(nativePtr);

  if (LIKELY(notify)) {

    const int datalen = dataLen;
    jbyte * jbytedata = env->GetByteArrayElements(eventData, 0);
    if(jbytedata == NULL)
         return;
    char* data =  convertJByteaArrayToChars(env,jbytedata,datalen);
     if(data == NULL)
         return;
    for(int i=0;i<oberserNum;i++)
    {
       jstring obj = (jstring)env->GetObjectArrayElement(observerArray,i);
       if(obj == NULL)
            break;
       const char* observer_name  = env->GetStringUTFChars(obj, NULL);
       const string filePath = string(notify->getPipeFilePath()) +"/"+string(observer_name);
       env->ReleaseStringUTFChars(obj,observer_name);
       const char* pipeFilePath = filePath.c_str();
       writeEventData(env,pipeFilePath,data,datalen);
       env->DeleteLocalRef(obj);
    }
    env->ReleaseByteArrayElements(eventData, jbytedata, 0);


  }
}

/**
*send a ipc event to "observer"
*/
jint nativeSendIPCEvent(JNIEnv *env, jobject thiz,jlong nativePtr,jstring observer,jbyteArray eventData, jint dataLen){

    IPCEventNotify *notify = reinterpret_cast<IPCEventNotify *>(nativePtr);
      // LOGD("init check isinit%d",notify->isinit);
    int ret = SENDFAIL_NOT_INIT;
    if (LIKELY(notify)) {
       const char *observer_name = env->GetStringUTFChars(observer, JNI_FALSE);
       const string filePath = string(notify->getPipeFilePath()) +"/"+string(observer_name);
       const int datalen = dataLen;

       jbyte * jbytedata = env->GetByteArrayElements(eventData, 0);
       if(jbytedata == NULL)
         return SENDFAIL_JNI_MEMORYERROR;
       char* data =  convertJByteaArrayToChars(env,jbytedata,datalen);

       if(data == NULL)
          return SENDFAIL_JNI_MEMORYERROR;

       //write data into pipe file
       const char* pipeFilePath = filePath.c_str();
       ret = writeEventData(env,pipeFilePath,data,datalen);

       env->ReleaseStringUTFChars(observer, observer_name);
       env->ReleaseByteArrayElements(eventData, jbytedata, 0);

    }
    return ret;

}






/**
*register a event listen on "subject"

void nativeRegisterIPCEventListen(JNIEnv *env, jobject thiz,jlong nativePtr,jstring subject, jstring eventName){

}
*/
/**
*unregister a event listen on "subject"

void nativeUnRegisterIPCEventListen(JNIEnv *env, jobject thiz,jlong nativePtr,jstring subject, jstring eventName){

}
*/






/**
*destory the singleton IPCEventNotify object
*/
void nativeDestroy(JNIEnv *env, jobject thiz,jlong nativePtr){
    IPCEventNotify *notify = reinterpret_cast<IPCEventNotify *>(nativePtr);
	if (LIKELY(notify)) {
		//SAFE_DELETE(notify);
		stopListen(env);
        notify->isinit = FAIL;

        if(!notify->getTransDataListener())
           env->DeleteGlobalRef(notify->getTransDataListener());
	}

}






//**********************************************************************
//
//**********************************************************************

void *startPipeDataListen(void *ptr){

   IPCEventNotify *notify = reinterpret_cast<IPCEventNotify *>(ptr);
   if (LIKELY(notify)) {
		JavaVM *vm = getVM();
		JNIEnv *env;
		// attach to JavaVM
		vm->AttachCurrentThread(&env, NULL);
		//IPCEventNotify *notifyglb = env->NewGlobalRef(notify);
		int prio = getpriority(PRIO_PROCESS, 0);
        nice(-18);
        if (UNLIKELY(getpriority(PRIO_PROCESS, 0) >= prio)) {
            LOGW("could not change thread priority");
        }
        startListen(notify,env,notify->getPipeFilePath()+"/"+notify->getPipeFileName());
		// detach from JavaVM
		//env->DeleteGlobalRef(notifyglb);
		vm->DetachCurrentThread();
	}
	pthread_exit(NULL);

}


//**********************************************************************
//jni func init
//**********************************************************************
jint registerNativeMethods(JNIEnv* env, const char *class_name, JNINativeMethod *methods, int num_methods) {
	int result = 0;

	jclass clazz = env->FindClass(class_name);
	if (LIKELY(clazz)) {
		int result = env->RegisterNatives(clazz, methods, num_methods);
		if (UNLIKELY(result < 0)) {
			LOGE("registerNativeMethods failed(class=%s)", class_name);
		}
	} else {
		LOGE("registerNativeMethods: class'%s' not found", class_name);
	}
	return result;
}

static JNINativeMethod methods[] = {
	{ "nativeCreate",					"(Lcom/zolad/ipceventnotify/util/IPCTransDataListener;)J", (void *) nativeCreate },
	{ "nativeInit",					"(JLjava/lang/String;Ljava/lang/String;)I", (void *) nativeInit },
	{ "nativeDestroy",					"(J)V", (void *) nativeDestroy  },
	{ "nativeNotifyIPCEvent",					"(J[Ljava/lang/String;I[BI)V", (void *) nativeNotifyIPCEvent  },
	{ "nativeSendIPCEvent",					"(JLjava/lang/String;[BI)I", (void *) nativeSendIPCEvent  },
	//{ "nativeRegisterIPCEventListen",					"(JLjava/lang/String;Ljava/lang/String;)V", (void *) nativeRegisterIPCEventListen  },
	///{ "nativeUnRegisterIPCEventListen",					"(JLjava/lang/String;Ljava/lang/String;)V", (void *) nativeUnRegisterIPCEventListen},


};

int register_ipcevnetnotify(JNIEnv *env) {
	LOGD("register_libipceventnotify");
	if (registerNativeMethods(env,
		"com/zolad/ipceventnotify/IPCEventNotify",
		methods, NUM_ARRAY_ELEMENTS(methods)) < 0) {
		return -1;
	}
    return 0;
}
