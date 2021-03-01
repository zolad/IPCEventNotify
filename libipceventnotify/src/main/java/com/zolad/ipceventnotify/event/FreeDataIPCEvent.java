package com.zolad.ipceventnotify.event;

import android.os.Parcel;

public class FreeDataIPCEvent extends IPCEvent {

    public int size = -1;
    public int offset = -1;

    public FreeDataIPCEvent(Parcel in) {
        size = in.readInt();
        offset = in.readInt();
    }

    public FreeDataIPCEvent(int size, int offset) {
        this.size = size;
        this.offset = offset;
    }

    public static final Creator<FreeDataIPCEvent> CREATOR = new Creator<FreeDataIPCEvent>() {
        @Override
        public FreeDataIPCEvent createFromParcel(Parcel in) {
            return new FreeDataIPCEvent(in);
        }

        @Override
        public FreeDataIPCEvent[] newArray(int size) {
            return new FreeDataIPCEvent[size];
        }
    };


    @Override
    public String getIPCEventName() {
        return FreeDataIPCEvent.class.getName();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(size);
        dest.writeInt(offset);
    }
}
