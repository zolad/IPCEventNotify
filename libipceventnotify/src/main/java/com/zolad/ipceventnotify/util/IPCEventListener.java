package com.zolad.ipceventnotify.util;

import com.zolad.ipceventnotify.event.IPCEvent;
import com.zolad.ipceventnotify.entity.IPCTransData;

/**
 * Listener interface for IPC EVENT
 */
public interface IPCEventListener {
    public void onEvent(IPCEvent event, IPCTransData ipcTransData);
}
