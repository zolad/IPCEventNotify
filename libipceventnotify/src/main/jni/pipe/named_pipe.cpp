#include "named_pipe.h"

/***
* named_pipe operator
*/
pthread_mutex_t namedpipefile_lock = PTHREAD_MUTEX_INITIALIZER;
jclass clazz;
jmethodID methodId;

//isStop listen
static bool isStop = false;
static char *buffer_pool;//[BUFFER_POOLSIZE];
static int buffer_pool_datasize = 0;
static int buffer_pool_capacity = 0;

static const char * MSGHEAD = "IPCEVENT_DATAHEAD";
static const char * MSGTAIL = "IPCEVENT_DATATAIL";

static const int MSGHEAD_LEN = 17;
static const int MSGTAIL_LEN = 17;

static int MSGHEAD_NEXT[MSGHEAD_LEN + 1];
static int MSGTAIL_NEXT[MSGTAIL_LEN + 1];


/*
*check pipe FilePath Valid
*/
bool checkFilePathValid(const char *fileDir,const char *fileName){

    const char* pipeFileName = NULL;
    string temp = string(fileDir) +"/"+string(fileName);
    pipeFileName = temp.c_str();
    LOGD("mkfifo named pipe file %s",pipeFileName);

    int result = mkfifo(pipeFileName, S_IRWXU  | S_IRWXG | S_IRWXO);
    if((result<0)&&(errno!=EEXIST)){

        LOGE("mkfifo named pipe file fail,error=%s",strerror(errno));
        return false;
    }
    errno = SUCCESS;
    result = chmod(pipeFileName,S_IRWXU | S_IRGRP | S_IWGRP | S_IROTH |  S_IWOTH );
    if(errno != 0){
        LOGE("chmod named pipe file fail %s\n",strerror(errno)) ;
        return false;
    }
    errno = SUCCESS;
    int fd = open(pipeFileName, O_RDWR);
    if (fd == -1) {
         LOGE("open named pipe file failed");
         return false;
    }else{
         close(fd);
         return true;
    }

}

/*
*keep read name_pipe file ,parser byte data and combine into a msg package
*/
void startListen(IPCEventNotify *notify,JNIEnv *env,const string filePath){

   MutexLock lock(namedpipefile_lock);

   isStop = false;
   const char* pipeFileName = filePath.c_str();

   int fd = open(pipeFileName, O_RDWR);

    if (fd < 0) {
        LOGE("start listen open pipe file failed");
        return;
    }else{

        LOGD("start pipe file listen read") ;
        //kmp get  key next arrary
        kmp_next(MSGHEAD,MSGHEAD_NEXT,MSGHEAD_LEN);
        kmp_next(MSGTAIL,MSGTAIL_NEXT,MSGTAIL_LEN);


        char * buf =  (char *)malloc(BUFFER_SIZE * sizeof(char));
        buffer_pool =  (char *)malloc(BUFFER_SIZE * sizeof(char));
        buffer_pool_capacity = malloc_usable_size(buffer_pool);
        if(buf == NULL || buffer_pool == NULL ){
           LOGE("malloc buffer pool fail");
           return;
        }
        ssize_t len;
        memset(buffer_pool, 0, buffer_pool_capacity);
        buffer_pool_datasize = 0;
        char *insert_offset =  buffer_pool;
        int head_offset,tail_offset;
        //char *head; //pointer to msg head
        //char *tail;  //pointer to msg tail
        while ((len = read(fd, buf, BUFFER_SIZE)) > 0 && !isStop){
          //if oversize and can not find head ,clean the pool
          if(buffer_pool_datasize + len >= BUFFER_POOLSIZE){
              memset(buffer_pool, 0, buffer_pool_capacity);
              buffer_pool_datasize = 0;
              insert_offset =  buffer_pool;
          } else if(buffer_pool_datasize + len >= buffer_pool_capacity){
              //if the pool capacity is not enough,realloc a new pool
              int bufferpool_resize = buffer_pool_capacity + BUFFER_SIZE;
              buffer_pool =  (char *) realloc (buffer_pool,(bufferpool_resize) * sizeof(char));
              if(buffer_pool == NULL){
                  LOGE("realloc buffer pool fail,size:%d",(bufferpool_resize));
                  free(buf);
                  close(fd);
                  return;
              }
              buffer_pool_capacity = malloc_usable_size(buffer_pool);
              insert_offset = &buffer_pool[buffer_pool_datasize];

          }

          //copy buf to pool
          memcpy(insert_offset,buf,len);
          buffer_pool_datasize += len;
          insert_offset = &buffer_pool[buffer_pool_datasize];

          //find msg head and tail
          head_offset = kmp_search(buffer_pool,MSGHEAD,MSGHEAD_NEXT,buffer_pool_datasize,MSGHEAD_LEN);

          //if redundant data exist before msg head
          //we need to cut off it
          if(head_offset > 0){
             LOGD("piperead need moveBufferPoolLeft=%d",head_offset);
             moveBufferPoolLeft(head_offset);
             insert_offset = &buffer_pool[buffer_pool_datasize];
             //head = strstr(buffer_pool,MSGHEAD);
             head_offset = kmp_search(buffer_pool,MSGHEAD,MSGHEAD_NEXT,buffer_pool_datasize,MSGHEAD_LEN);
          }

          if(head_offset == 0){
            //tail = strstr(buffer_pool,MSGTAIL);
            tail_offset = kmp_search(buffer_pool,MSGTAIL,MSGTAIL_NEXT,buffer_pool_datasize,MSGTAIL_LEN);
            //callback java ontransdata
            if(tail_offset > 0){
                insert_offset = &buffer_pool[head_offset + strlen(MSGHEAD)];
                int datalen = tail_offset - head_offset - strlen(MSGHEAD);
                callBackIPCTransData(notify,env,insert_offset,datalen);

                moveBufferPoolLeft(tail_offset + strlen(MSGTAIL));
                insert_offset = &buffer_pool[buffer_pool_datasize];

            }

          }
          //LOGD("piperead %s",buffer_pool);
          //LOGD("piperead datasize %d",buffer_pool_datasize);
          //LOGD("piperead head %d",head_offset);
          //LOGD("piperead tail %d",tail_offset);


      }


      //write(fd, &buf, strlen(buf));
      //  printf("write done\n");
      free(buffer_pool);
      free(buf);
      close(fd);
    }

}

/*
*stop name_pipe file listen
*/
void stopListen(JNIEnv *env){
  isStop = true;
  if (LIKELY(clazz))
  env->DeleteLocalRef(clazz);
  //if (LIKELY(methodId))
  //env->DeleteLocalRef(methodId);
}



/*
 * move buffer pool char array Left
*/
void moveBufferPoolLeft(int offset)
{
    //    LOGD("capacitysize-- %d",malloc_usable_size(buffer_pool));

    if(offset<0 || offset >= buffer_pool_capacity)
        return;

    if(offset == BUFFER_POOLSIZE){
       memset(buffer_pool, 0, buffer_pool_capacity);
       buffer_pool_datasize = 0;
    }else{
        for (int i = 0; i < buffer_pool_datasize; i++)
        {
            if(i+offset > buffer_pool_datasize-1){
              buffer_pool[i] = 0;
            }else
              buffer_pool[i] = buffer_pool[i+offset];
        }
        buffer_pool_datasize = buffer_pool_datasize - offset;

    }
}

/*
*callback java data listen function
*/
void callBackIPCTransData(IPCEventNotify *notify,JNIEnv *env,char * data,int datalen){

    if (!LIKELY(notify)){
         LOGW("callBackIPCTransData notify null");
         return;
    }
    if (!notify->getTransDataListener()){
         LOGW("callBackIPCTransData getTransDataListener null");
         return;
    }
    if(!LIKELY(clazz))
       clazz = env->GetObjectClass(notify->getTransDataListener());

    if (LIKELY(clazz)) {
        if(!LIKELY(methodId))
           methodId = env->GetMethodID(clazz,"onTransData","(Ljava/nio/ByteBuffer;)V");
    } else {
        LOGW("callBackIPCTransData failed to get object class");
        return;
    }
   // env->ExceptionClear();

    if (LIKELY(methodId)) {
       jobject buf = env->NewDirectByteBuffer(data, datalen);
       env->CallVoidMethod(notify->getTransDataListener(), methodId, buf);
       env->ExceptionClear();
       //free(buf);
       env->DeleteLocalRef(buf);

    }else
       LOGW("callBackIPCTransData methodId null");


}



int writeEventData(JNIEnv *env,const char* filePath,char* data,int datalen){


   int fd = open(filePath, O_RDWR  | O_NONBLOCK);
   int result = 0;
    if (fd < 0) {
        LOGE("writeEventData open pipe file failed");
        return SENDFAIL_OBSERVER_NOT_EXIST;
    }else{
      //char* data =  convertJByteaArrayToChars(env,bytearray,datalen);
      if(data == NULL)
        return SENDFAIL_WRITEFAIL;
      //LOGD("pipewrite %s",data);
      errno = 0;
      //result = write(fd,MSGHEAD,MSGHEAD_LEN);
      //result = write(fd,bytearray,datalen);
      //result = write(fd,MSGTAIL,MSGTAIL_LEN);

      result = write(fd, data, datalen + MSGHEAD_LEN + MSGTAIL_LEN);
      free(data);
      if(result<0 || errno != 0){
         LOGE("writeEventData result%d\n", result);
         LOGE("writeEventData error%s\n", strerror(errno));
      }
      close(fd);
    }

    return result;

}




char* convertJByteaArrayToChars(JNIEnv *env, jbyte* bytearray,int len)
{
    char *chars = NULL;
    chars =  (char *)malloc((len + MSGHEAD_LEN + MSGTAIL_LEN) * sizeof(char));
    if(chars == NULL)
       return NULL;

    //memset(chars, 0, malloc_usable_size(chars));
    memcpy(chars, MSGHEAD, MSGHEAD_LEN);
    for (int i = 0; i < len; i++){
       chars[i + MSGHEAD_LEN] = bytearray[i];
    }
    for (int j = 0; j < MSGTAIL_LEN; j++){
           chars[j + MSGHEAD_LEN + len] = MSGTAIL[j];
    }

    return chars;
}
