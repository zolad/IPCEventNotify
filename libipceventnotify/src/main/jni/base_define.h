#ifndef BASEDEFINES_H_
#define BASEDEFINES_H_

#pragma interface

#include <jni.h>

#ifndef LOG_TAG
#define LOG_TAG "libIPCEventNotify"
#endif

// write back array that got by getXXXArrayElements into original Java object and release its array
#define	ARRAYELEMENTS_COPYBACK_AND_RELEASE 0
// write back array that got by getXXXArrayElements into origianl Java object but do not release its array
#define	ARRAYELEMENTS_COPYBACK_ONLY JNI_COMMIT
// never write back array that got by getXXXArrayElements but release its array
#define ARRAYELEMENTS_ABORT_AND_RELEASE JNI_ABORT

#define THREAD_PRIORITY_DEFAULT			0
#define THREAD_PRIORITY_LOWEST			19
#define THREAD_PRIORITY_BACKGROUND		10
#define THREAD_PRIORITY_FOREGROUND		-2
#define THREAD_PRIORITY_DISPLAY			-4
#define THREAD_PRIORITY_URGENT_DISPLAY	-8
#define THREAD_PRIORITY_AUDIO			-16
#define THREAD_PRIORITY_URGENT_AUDIO	-19

#define LIMIT_DATASIZE  2048



//#define LOG_NDEBUG


// Absolute class name of Java object
// if you change the package name of UVCCamera library, you must fix these
#define		JTYPE_SYSTEM				"Ljava/lang/System;"
#define		JTYPE_IPCEVENTNOTIFY		"Lcom/zolad/ipceventnotify/IPCEventNotify;"
//
typedef		jlong						ID_TYPE;




#define	 SUCCESS  0
#define	 FAIL -1
#define	 INITFAIL_CONTEXT_NULL -2
#define	 INITFAIL_FILEPATH_NOT_AVAILABLE  -3
#define	 INITFAIL_NOT_PERMISSION  -4
#define	 INITFAIL_ALREADYINIT -5

#define	 SENDFAIL_NOT_INIT  -6;
#define	 SENDFAIL_WRITEFAIL  -7;
#define	 SENDFAIL_OBSERVER_NOT_EXIST  -8;
#define	 SENDFAIL_JNI_MEMORYERROR -9;

#endif /* BASEDEFINES_H_ */
