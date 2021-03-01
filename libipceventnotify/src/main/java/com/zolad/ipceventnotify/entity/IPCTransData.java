package com.zolad.ipceventnotify.entity;

import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.os.SharedMemory;

import com.zolad.ipceventnotify.event.IPCEvent;

/**
 * IPCTransData is a container , for  ipceventdata
 */
public class IPCTransData implements Parcelable {

    public int eventType; //事件类型,1为正常,2为需要共享内存读取事件,3为注册事件,4为反注册事件
    public int shmFd;  //如果command_type为2,则需要此字段,共享内存文件的fd
    public int shmDataOffset = -1; //数据在共享内存文件的offset
    public  int shmDataSize = -1;
    public String eventName;  //eventName
    public String from;
    public IPCEvent event;

    public IPCTransData(int eventType, int shmFd, int shmDataOffset, int shmDataSize, String eventName, String from, IPCEvent event) {

        this.eventType = eventType;
        this.shmFd = shmFd;
        this.shmDataOffset = shmDataOffset;
        this.shmDataSize =  shmDataSize;
        this.eventName = eventName;
        this.from = from;
        this.event = event;

    }
    public IPCTransData(Parcel in) {

        eventType = in.readInt();
        //shmFile = in.readParcelable(SharedMemory.class.getClassLoader());
        //shmFile = in.readFileDescriptor();
        shmFd = in.readInt();
        shmDataOffset = in.readInt();
        shmDataSize = in.readInt();
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

      //  dest.writeParcelable(shmFile,flags);
   //    dest.writeFileDescriptor(shmFile.getFileDescriptor());
        dest.writeInt(shmFd);
        //  shmFd = new ParcelFileDescriptor();
       // dest.writeFileDescriptor(shmFd == null? new FileDescriptor():shmFd.getFileDescriptor());
        dest.writeInt(shmDataOffset);
        dest.writeInt(shmDataSize);
        dest.writeString(eventName);
        dest.writeString(from);
        dest.writeParcelable(event,flags);

    }

    @Override
    public String toString() {
        return "IPCTransData{" +
                "eventType=" + eventType +
                ", shmFd=" + shmFd +
                ", shmDataOffset=" + shmDataOffset +
                ", shmDataSize=" + shmDataSize +
                ", eventName='" + eventName + '\'' +
                ", from='" + from + '\'' +
                ", event=" + event +
                '}';
    }
}
