#ifndef BASEUTIL_H_
#define BASEUTIL_H_
#define __ANDROID__ 1
#include <jni.h>
#ifdef __ANDROID__
#include <android/log.h>
#endif
#include <unistd.h>
#include <libgen.h>
#include "base_define.h"

#pragma interface

//log switch control
#define LOG_SWITCH 1

#define		SAFE_FREE(p)				{ if (p) { free((p)); (p) = NULL; } }
#define		SAFE_DELETE(p)				{ if (p) { delete (p); (p) = NULL; } }
#define		SAFE_DELETE_ARRAY(p)		{ if (p) { delete [](p); (p) = NULL; } }
#define		NUM_ARRAY_ELEMENTS(p)		((int) sizeof(p) / sizeof(p[0]))

#if defined(__GNUC__)
// the macro for branch prediction optimaization for gcc(-O2/-O3 required)
#define		CONDITION(cond)				((__builtin_expect((cond)!=0, 0)))
#define		LIKELY(x)					((__builtin_expect(!!(x), 1)))	// x is likely true
#define		UNLIKELY(x)					((__builtin_expect(!!(x), 0)))	// x is likely false
#else
#define		CONDITION(cond)				((cond))
#define		LIKELY(x)					((x))
#define		UNLIKELY(x)					((x))
#endif


#include <assert.h>
#define CHECK(CONDITION) { bool RES = (CONDITION); assert(RES); }
#define CHECK_EQ(X, Y) { bool RES = (X == Y); assert(RES); }
#define CHECK_NE(X, Y) { bool RES = (X != Y); assert(RES); }
#define CHECK_GE(X, Y) { bool RES = (X >= Y); assert(RES); }
#define CHECK_GT(X, Y) { bool RES = (X > Y); assert(RES); }
#define CHECK_LE(X, Y) { bool RES = (X <= Y); assert(RES); }
#define CHECK_LT(X, Y) { bool RES = (X < Y); assert(RES); }

#if(LOG_SWITCH == 1)

	#define LOGV(format, ...)  __android_log_print(ANDROID_LOG_VERBOSE, LOG_TAG, format, ##__VA_ARGS__)
	#define LOGD(format, ...)  __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, format, ##__VA_ARGS__)
	#define LOGI(format, ...)  __android_log_print(ANDROID_LOG_INFO, LOG_TAG, format, ##__VA_ARGS__)
	#define LOGW(format, ...)  __android_log_print(ANDROID_LOG_WARN, LOG_TAG, format, ##__VA_ARGS__)
	#define LOGE(format, ...)  __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, format, ##__VA_ARGS__)

#else

	#define LOGV(...) NULL
	#define LOGD(...) NULL
	#define LOGI(...) NULL
	#define LOGW(...) NULL
	#define LOGE(...) NULL

#endif

#define		ENTER()				LOGD("begin")
#define		RETURN(code,type)	{LOGD("return"); type RESULT = code;  return RESULT;}
#define		RET(code)			{LOGD("end"); return code;}
#define		EXIT()				{LOGD("end"); return;}
#define		PRE_EXIT()			LOGD("end")


#define LITERAL_TO_STRING_INTERNAL(x)    #x
#define LITERAL_TO_STRING(x) LITERAL_TO_STRING_INTERNAL(x)

#define TRESPASS() \
		LOG_ALWAYS_FATAL(                                       \
			__FILE__ ":" LITERAL_TO_STRING(__LINE__)            \
			" Should not be here.");

void setVM(JavaVM *);
JavaVM *getVM();
JNIEnv *getEnv();

#endif /* BASEUTIL_H_ */
