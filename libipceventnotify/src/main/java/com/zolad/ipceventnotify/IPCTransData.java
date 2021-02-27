package com.zolad.ipceventnotify;

import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;

import java.io.FileDescriptor;
import java.io.IOException;

/**
 * IPCTransData is a container , for  ipceventdata
 */
public class IPCTransData implements Parcelable {

    int eventType; //事件类型,1为正常,2为需要共享内存读取事件,3为注册事件,4为反注册事件
    int shmFd;  //如果command_type为2,则需要此字段,共享内存文件的fd
    int shmDataOffset; //数据在共享内存文件的offset
    String eventName;  //eventName
    String from;
    IPCEvent event;

    public IPCTransData(int eventType,int shmFd,int shmDataOffset,String eventName,String from,IPCEvent event) {

        this.eventType = eventType;
        this.shmFd = shmFd;
        this.shmDataOffset = shmDataOffset;
        this.eventName = eventName;
        this.from = from;
        this.event = event;

    }
    protected IPCTransData(Parcel in) {

        eventType = in.readInt();
        shmFd = in.readInt();
        shmDataOffset = in.readInt();
        eventName = in.readString();
        from = in.readString();
        event = in.readParcelable(IPCEvent.class.getClassLoader());


    }

    public static final Creator<IPCTransData> CREATOR = new Creator<IPCTransData>() {
        @Override
        public IPCTransData createFromParcel(Parcel in) {
            return new IPCTransData(in);
        }

        @Override
        public IPCTransData[] newArray(int size) {
            return new IPCTransData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeInt(eventType);

        dest.writeInt(shmFd);
        //  shmFd = new ParcelFileDescriptor();
       // dest.writeFileDescriptor(shmFd == null? new FileDescriptor():shmFd.getFileDescriptor());
        dest.writeInt(shmDataOffset);
        dest.writeString(eventName);
        dest.writeString(from);
        dest.writeParcelable(event,flags);

    }
}
