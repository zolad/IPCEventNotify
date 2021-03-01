package com.zolad.entity;

import android.os.Parcel;

import com.zolad.ipceventnotify.event.IPCEvent;

public class TestIPCEvent extends IPCEvent {

    public int code = 999;
    public String text  = "This is TestIPCEvent!";

    public TestIPCEvent(int code, String text) {

        this.code = code;
        this.text = text;
    }

    protected TestIPCEvent(Parcel in) {


        code = in.readInt();
        text = in.readString();
    }

    public static final Creator<TestIPCEvent> CREATOR = new Creator<TestIPCEvent>() {
        @Override
        public TestIPCEvent createFromParcel(Parcel in) {
            return new TestIPCEvent(in);
        }

        @Override
        public TestIPCEvent[] newArray(int size) {
            return new TestIPCEvent[size];
        }
    };

    @Override
    public String getIPCEventName() {
        return TestIPCEvent.class.getName();
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(code);
        dest.writeString(text);
    }

    @Override
    public String toString() {
        return getIPCEventName() + "{"+
                "code=" + code +
                ", text='" + text + '\'' +
                '}';
    }
}
