package com.zolad.ipceventnotify.event;

import android.os.Parcel;
import android.os.Parcelable;
/**
 * IPCEvent is base IPCEvent Class
 */
public abstract class IPCEvent implements Parcelable {

    public abstract String getIPCEventName();
}
