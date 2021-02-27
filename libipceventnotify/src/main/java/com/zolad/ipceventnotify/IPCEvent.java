package com.zolad.ipceventnotify;

import android.os.Parcel;
import android.os.Parcelable;
/**
 * IPCEvent is base IPCEvent Class
 */
public abstract class IPCEvent implements Parcelable {

    abstract String getIPCEventName();


}
