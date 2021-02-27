#ifndef LIBIPCEVENTNOTIFY_H_
#define LIBIPCEVENTNOTIFY_H_

#include <jni.h>
#include "base_util.h"
#include "base_define.h"

#include <string>
#include <cstring>
#include "pthread.h"


#pragma interface

void *startPipeDataListen(void *ptr);

#endif /* LIBIPCEVENTNOTIFY_H_ */
