package com.zolad.ipceventnotify.util;

import android.os.Parcel;

import com.zolad.ipceventnotify.IPCEvent;
import com.zolad.ipceventnotify.IPCTransData;

/**
 * Listener interface for IPC EVENT
 */
public interface IPCEventListener {
    public void onEvent(IPCEvent event, IPCTransData ipcTransData);
}
