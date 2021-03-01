package com.zolad.ipceventnotify.util;

import android.os.Parcel;

import java.nio.ByteBuffer;

/**
 * Listener interface for IPC TransData
 */
public interface IPCTransDataListener {
    public void onTransData(ByteBuffer ipcTransDataParcelByteData);

}
