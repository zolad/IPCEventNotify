
######################################################################
######################################################################
LOCAL_PATH	:= $(call my-dir)
include $(CLEAR_VARS)

######################################################################
######################################################################
CFLAGS := -Werror

LOCAL_C_INCLUDES := \
		$(LOCAL_PATH)/ \

#LOCAL_CFLAGS := $(LOCAL_C_INCLUDES:%=-I%)
LOCAL_CFLAGS += -DANDROID_NDK
LOCAL_CFLAGS += -DLOG_NDEBUG
LOCAL_CFLAGS += -DACCESS_RAW_DESCRIPTORS
LOCAL_CFLAGS += -O3 -fstrict-aliasing -fprefetch-loop-arrays
LOCAL_LDLIBS := -L$(SYSROOT)/usr/lib -ldl
LOCAL_LDLIBS += -llog
LOCAL_LDLIBS += -landroid
#LOCAL_ARM_MODE := arm

LOCAL_SRC_FILES := \
		onload.cpp \
		base_util.cpp \
		libIPCEventNotify.cpp \
		IPCEventNotify.cpp \
		pipe/named_pipe.cpp \
		pipe/algorithm.cpp \





LOCAL_MODULE    := IPCEventNotify
include $(BUILD_SHARED_LIBRARY)
