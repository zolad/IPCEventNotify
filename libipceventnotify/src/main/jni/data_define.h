#ifndef DATADEFINE_H_
#define DATADEFINE_H_

#pragma interface

//#define
/***  unused
enum COMMAND_TYPE {NORMAL=1, ADDITION,REGISTER,UNREGISTER};

typedef struct transaction_data_info
{
    uint32_t datalen;  //parcel数据大小
    uint16_t eventname_len; //事件名称字符串长度
    uint8_t command_type; //事件类型,枚举为正常,需要共享内存读取事件,注册事件,反注册事件
    int shm_fd;  //如果command_type为2,则需要此字段,共享内存文件的fd
    int data_offset; //数据在共享内存文件的offset
}trans_data_info;
***/



#endif /* DATADEFINE_H_ */
