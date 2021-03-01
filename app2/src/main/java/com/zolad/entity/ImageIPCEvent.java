package com.zolad.entity;

import android.os.Parcel;

import com.zolad.ipceventnotify.event.IPCEvent;

public class ImageIPCEvent extends IPCEvent {

    public byte[] picture;

    public ImageIPCEvent(byte[] picture) {

        this.picture = picture;
    }

    protected ImageIPCEvent(Parcel in) {

        int length = in.readInt();
        //如果数组长度大于0，那么就读数组， 所有数组的操作都可以这样。
        if(length>0){
            picture = new byte[length];
            in.readByteArray(picture);
        }
    }

    public static final Creator<ImageIPCEvent> CREATOR = new Creator<ImageIPCEvent>() {
        @Override
        public ImageIPCEvent createFromParcel(Parcel in) {
            return new ImageIPCEvent(in);
        }

        @Override
        public ImageIPCEvent[] newArray(int size) {
            return new ImageIPCEvent[size];
        }
    };

    @Override
    public String getIPCEventName() {
        return ImageIPCEvent.class.getName();
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if(picture == null) {
            dest.writeInt(0);
        } else {
            dest.writeInt(picture.length);
            dest.writeByteArray(picture);
        }
        // dest.writeByteArray(picture);
    }

    @Override
    public String toString() {
        return getIPCEventName();
    }
}
