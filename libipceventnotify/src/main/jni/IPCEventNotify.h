#ifndef IPCEVENTNOTIFY_H_
#define IPCEVENTNOTIFY_H_

#include <stdlib.h>
#include <linux/time.h>
#include <unistd.h>
#include <string>
#include <cstring>
#include <pthread.h>
#include <stdio.h>
#include <jni.h>
#include <base_util.h>

#pragma interface

#define	LOCAL_DEBUG 0

using namespace std;

class MutexLock  {
    private:
        pthread_mutex_t m_lock;
    public:
        MutexLock(pthread_mutex_t  cs) : m_lock(cs) {
            pthread_mutex_lock(&m_lock);
        }
        ~MutexLock() {
            pthread_mutex_unlock(&m_lock);
        }
};//Lock


class IPCEventNotify {

public:
    IPCEventNotify();
    ~IPCEventNotify();
    void setPipeFilePath(string pipefilePath);
    string getPipeFilePath();
    void setPipeFileName(string pipefileName);
    string getPipeFileName();
    static  IPCEventNotify*  getInstance();
    void setTransDataListener(JNIEnv *env,jobject transdata_listener_obj);
    jobject getTransDataListener();
    bool isinit;

private:
     static IPCEventNotify* instance;
     static pthread_mutex_t  mutex;
     string pipefilepath;
     string pipefilename;
     jobject transdata_listener;
     jmethodID transdata_listener_method;
};


#endif /* IPCEVENTNOTIFY_H_ */
