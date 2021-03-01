#ifndef NAMEDPIPE_H_
#define NAMEDPIPE_H_

#pragma interface

#include <jni.h>
#include <string>
#include <stdio.h>
#include <stdlib.h>
#include <string>
#include <cstring>
#include <sys/msg.h>
#include <sys/ipc.h>
#include <android/log.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>
#include <sys/time.h>
#include <sys/resource.h>
#include <errno.h>
#include <pthread.h>
#include <sched.h>
#include <assert.h>
#include "base_util.h"
#include "IPCEventNotify.h"
#include "algorithm.h"


using namespace std;

#define BUFFER_POOLSIZE  4096
#define BUFFER_SIZE  1024

bool checkFilePathValid(const char *fileDir,const char *fileName);

void startListen(IPCEventNotify *notify,JNIEnv *env,const string filePath);
void stopListen(JNIEnv *env);

int writeEventData(JNIEnv *env,const char* filePath,char* data,int datalen);

/*
 * move buffer pool char array Left
*/
void moveBufferPoolLeft(int offset);
void callBackIPCTransData(IPCEventNotify *notify,JNIEnv *env,char * data,int datalen);
char* convertJByteaArrayToChars(JNIEnv *env, jbyte * bytearray,int len);

#endif /* NAMEDPIPE_H_ */
