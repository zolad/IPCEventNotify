package com.zolad.ipceventnotify;

import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.SharedMemory;
import android.text.TextUtils;
import android.util.Log;

import com.zolad.ipceventnotify.entity.DataBuffer;
import com.zolad.ipceventnotify.entity.IPCTransData;
import com.zolad.ipceventnotify.event.FreeDataIPCEvent;
import com.zolad.ipceventnotify.event.IPCEvent;
import com.zolad.ipceventnotify.sharememory.ShareMemoryManager;
import com.zolad.ipceventnotify.util.IPCEventListener;
import com.zolad.ipceventnotify.util.IPCTransDataListener;
import com.zolad.ipceventnotify.util.ParcelUtil;

import java.io.FileDescriptor;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.zolad.ipceventnotify.util.Constants.EVENTTYPE_ADDITION;
import static com.zolad.ipceventnotify.util.Constants.EVENTTYPE_FREEDATA;
import static com.zolad.ipceventnotify.util.Constants.EVENTTYPE_NORMAL;
import static com.zolad.ipceventnotify.util.Constants.EVENTTYPE_REGISTER;
import static com.zolad.ipceventnotify.util.Constants.EVENTTYPE_UNREGISTER;
import static com.zolad.ipceventnotify.util.Constants.FAIL;
import static com.zolad.ipceventnotify.util.Constants.LIMIT_DATASIZE;
import static com.zolad.ipceventnotify.util.Constants.SENDFAIL_EVENTSIZE_ERROR;
import static com.zolad.ipceventnotify.util.Constants.SENDFAIL_NOT_INIT;
import static com.zolad.ipceventnotify.util.Constants.SUCCESS;
import static com.zolad.ipceventnotify.util.Constants.INITFAIL_CONTEXT_NULL;


/**
 * IPCEventNotify
 * A util for IPC Communicate
 */
public class IPCEventNotify {

    private static final String TAG = IPCEventNotify.class.getSimpleName();
    private volatile static IPCEventNotify instance = null;
    private static IPCEventListener ipcEventListener;
    private long mNativePtr; // used by native code
    private Context context;
    //private byte[] cacheMsg = new byte[LIMIT_DATASIZE];
    private ConcurrentHashMap<String, Set<String>> eventObserverGroup = new ConcurrentHashMap<>();
    private static final int THREAD_MAX = 2;
    private ExecutorService mThreadExecutor;
    private static String mIndicationName;
    private ShareMemoryManager mShareMemoryManager;
    private static  final int INITAIL_SHAREMEMORY_SIZE = 1024*1024;
    private  static final int COREPoolSIZE = 2;
    private  static final int MAXINUMPOOLSIZE = 4;
    private  static final int KEEPALIVETIME = 2000;
    private  static final int MAXINUMTASKQUEUE = 15;


    private IPCEventNotify(Context context) {
        this.context = context;
        mIndicationName = context.getPackageName();
        mNativePtr = nativeCreate(mIPCTransDataListener);

    }

    /**
     * @param context
     * Get a single  instance  of IPCEventNotify Class
     */
    public static IPCEventNotify getInstance(Context context) {
        if (instance == null) {
            synchronized (IPCEventNotify.class) {
                if (instance == null) {
                    instance = new IPCEventNotify(context);
                }
            }
        }
        return instance;
    }

    /**
     * @param BridgeFilePath init and set a Bridge FilePath
     * it will create a pipe file using "app PackageName"
     * for example  BridgeFilePath/com.xx.xx
     */
    public int init(String BridgeFilePath) {
       return init(BridgeFilePath, context.getPackageName());

    }

    /**
     * @param BridgeFilePath init and set a Bridge FilePath
     * @param  indicationName  create a pipe file By given indicationName
     * it will create a pipe file using given indicationName
     * for example  BridgeFilePath/indicationName
     */
    public int init(String BridgeFilePath, String indicationName) {
        if (context != null) {
            int ret = nativeInit(mNativePtr, BridgeFilePath, indicationName);
            if(ret == SUCCESS)
                mIndicationName = indicationName;
            return  ret;
        } else
            return INITFAIL_CONTEXT_NULL;

    }

    /**
     * @param ipcEvent broadcast ipcevent
     * broadcast  a ipcEvent to observer who had register listen
     */
    public void notifyIPCEvent(IPCEvent ipcEvent) {

        if(!checkParam() || eventObserverGroup == null  || ipcEvent == null )
            return;

        String eventName = ipcEvent.getIPCEventName();

        if(!TextUtils.isEmpty(eventName) ) {
            Set<String> observerGroup = eventObserverGroup.get(eventName);
            if(observerGroup == null || observerGroup.size() == 0)
                return;

            String[] observerList = (String[])observerGroup.toArray();
            //get event data size
            int eventDataSize = getIPCEventDataSize(ipcEvent);
            //check the eventdata size
            //if eventdata size is small than LIMIT_DATASIZE,we send it directly
            if(eventDataSize<LIMIT_DATASIZE){
                IPCTransData ipcTransData = new IPCTransData(EVENTTYPE_NORMAL,-1,-1,-1,
                        eventName,mIndicationName,ipcEvent);

                Parcel sendParcel = Parcel.obtain();
                ipcTransData.writeToParcel(sendParcel, 0);
                int sendParcelDataSize = sendParcel.dataSize();
                byte[] data = ParcelUtil.marshall(sendParcel);
                nativeNotifyIPCEvent(mNativePtr,observerList,observerList.length,data,sendParcelDataSize);

            }else{
                //if eventData is too large, we need to save data into ShareMemory
                //unrealized
                return;

            }
        }

    }

    /**
     * send  a ipcEvent to observer
     * @param observer
     * @param ipcEvent
     */
    public int sendIPCEvent(String observer, IPCEvent ipcEvent) {
        if(!checkParam() || ipcEvent == null)
            return SENDFAIL_NOT_INIT;
        //get event data size
        int eventDataSize = getIPCEventDataSize(ipcEvent);
        //get event name
        String eventName = ipcEvent.getIPCEventName();
        Log.d(TAG,"sendIPCEvent,datasize="+eventDataSize);
        //check the eventdata size
        if(eventDataSize <= 0) {
            return SENDFAIL_EVENTSIZE_ERROR;
        }
        //if eventdata size is small than LIMIT_DATASIZE,we send it directly
        if(eventDataSize<LIMIT_DATASIZE){
            IPCTransData ipcTransData = new IPCTransData(EVENTTYPE_NORMAL,-1,-1,-1,
                    eventName,mIndicationName,ipcEvent);

            return sendIPCTransData(observer,eventName,ipcTransData);

        }else{
            //if eventData is too large, we need to save data into ShareMemory
            if(getShareMemoryManager()!=null){

                DataBuffer availBuffer = getShareMemoryManager().getAvailDataBuffer(eventDataSize);
                if(availBuffer!=null){
                    //write data into databuffer
                    Parcel sendParcel = Parcel.obtain();
                    ipcEvent.writeToParcel(sendParcel, 0);
                    byte[] eventData = ParcelUtil.marshall(sendParcel);
                    SharedMemory sharedMemory = getShareMemoryManager().writeData(eventData,availBuffer);


                    ParcelFileDescriptor shmfd = getShareMemoryManager().getFileDescriper();
                    if(sharedMemory != null && shmfd!=null){
                        IPCTransData ipcTransData = new IPCTransData(EVENTTYPE_ADDITION,
                                shmfd.getFd(),
                                availBuffer.offset,availBuffer.size,
                                eventName,mIndicationName,null);
                        return sendIPCTransData(observer,eventName,ipcTransData);

                    }
                }else
                    Log.e(TAG,"can not get avail databuffer,need size:"+eventDataSize);
            }

        }

        return FAIL;


    }

    private int sendIPCTransData(String observer,String eventName,IPCTransData ipcTransData){
        Parcel sendParcel = Parcel.obtain();

        ipcTransData.writeToParcel(sendParcel, 0);

        int sendParcelDataSize = sendParcel.dataSize();
        byte[] data = ParcelUtil.marshall(sendParcel);
        return nativeSendIPCEvent(mNativePtr,observer,
               data ,sendParcelDataSize);
    }



    /**
     * set a ipcevent listener
     * @param ipcEventListener
     */
    public void setOnIPCEventListener(IPCEventListener ipcEventListener) {
        this.ipcEventListener = ipcEventListener;
    }


    /**
     * register ipcevent listen to subject
     * @param subject  indicationName of  subject
     * @param eventName name of ipcevent
     */
    public void registerIPCEventListen(String subject, String eventName) {

        if(!checkParam() || eventName == null || subject == null)
            return;

        IPCTransData ipcTransData = new IPCTransData(EVENTTYPE_REGISTER,-1,-1,-1,
                eventName,mIndicationName,null);

        sendIPCTransData(subject,eventName,ipcTransData);

        //nativeRegisterIPCEventListen(mNativePtr,subject,eventName);
    }

    /**
     * unregister ipcevent listen to subject
     * @param subject  indicationName of  subject
     * @param eventName name of ipcevent
     */
    public void unRegisterIPCEventListen(String subject, String eventName) {

        if(!checkParam() || eventName == null || subject == null)
            return;

        IPCTransData ipcTransData = new IPCTransData(EVENTTYPE_UNREGISTER,-1,-1,-1,
                eventName,mIndicationName,null);

        sendIPCTransData(subject,eventName,ipcTransData);

        //nativeUnRegisterIPCEventListen(mNativePtr,subject,eventName);
    }


    /**
     * call subject to free IPCEventData
     * @param ipcTransData ipcTransData need free
     */
    public void freeIPCEventData(IPCTransData ipcTransData) {

        if(!checkParam() || ipcTransData == null)
            return;

        if(ipcTransData.eventType == EVENTTYPE_ADDITION && ipcTransData.shmDataSize >0 && ipcTransData.shmDataOffset >=0
                && ipcTransData.shmFd>0) {

            FreeDataIPCEvent freeDataIPCEvent = new FreeDataIPCEvent(ipcTransData.shmDataSize,ipcTransData.shmDataOffset);

            IPCTransData freeDataIPCTransData = new IPCTransData(EVENTTYPE_FREEDATA, -1, -1,-1,
                    freeDataIPCEvent.getIPCEventName(), mIndicationName, freeDataIPCEvent);

            sendIPCTransData(ipcTransData.from, freeDataIPCEvent.getIPCEventName(), ipcTransData);
        }

    }

    private int getIPCEventDataSize(IPCEvent ipcEvent){
        //get event data size
        Parcel sendParcel = Parcel.obtain();
        ipcEvent.writeToParcel(sendParcel, 0);
        int size = sendParcel.dataSize();
        sendParcel.recycle();
        return size;
    }

    private Parcel getIPCEventParcel(IPCEvent ipcEvent){
        //get event data size
        Parcel sendParcel = Parcel.obtain();
        ipcEvent.writeToParcel(sendParcel, 0);
        return sendParcel;
    }

    public SharedMemory getSharememory(){
        if(mShareMemoryManager!=null)
        return mShareMemoryManager.getShareMemory();
        else
            return null;

    }

    public Parcel getShmFileParcel(SharedMemory sharedMemory,int shmDataOffset,int shmDataSize){

        if(sharedMemory!=null) {
            byte[] data = ShareMemoryManager.readShareMemory(sharedMemory, shmDataOffset, shmDataSize);
            if(data!=null) {
                Parcel obj = ParcelUtil.unmarshall(data);
                if (obj != null)
                    return obj;
            }
        }

        return null;
    }

    private Parcel getShmFileParcel(int shmFd,int shmDataOffset,int shmDataSize){
        ParcelFileDescriptor fileDescriptor = null;
        try {
            fileDescriptor = ParcelFileDescriptor.adoptFd(shmFd);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG,"getShmFileParcel,"+e.getMessage());

        }
        if(fileDescriptor != null) {
            SharedMemory sharedMemory = ShareMemoryManager.openMemoryFile(fileDescriptor);
            if(sharedMemory!=null) {
                byte[] data = ShareMemoryManager.readShareMemory(sharedMemory, shmDataOffset, shmDataSize);
                if(data!=null) {
                    Parcel obj = ParcelUtil.unmarshall(data);
                    if (obj != null)
                        return obj;
                }
            }
        }else{
            Log.e(TAG,"getShmFileParcel,getfd null");

        }
        return null;
    }

    /**
     * destory instance
     */
    public void destory() {

        if (mNativePtr != 0) {
            nativeDestroy(mNativePtr);

            mNativePtr = 0;
        }

    }

    private IPCTransDataListener mIPCTransDataListener = new IPCTransDataListener() {
        @Override
        public void onTransData(ByteBuffer ipcTransDataParcelbytedata) {

            if(ipcTransDataParcelbytedata == null)
                return;
            int remaining = ipcTransDataParcelbytedata.remaining();
            if(remaining == 0)
                return;
            byte [] msgdata = new byte[remaining];
            ipcTransDataParcelbytedata.get(msgdata);

            //Log.d(TAG,"onTransData:"+new String(msgdata));

            if(msgdata!=null) {

                handleIPCTransData(msgdata);

            }


        }
    };

    /**
     * handleIPCTransData
     * */
    private void handleIPCTransData(final byte [] msgdata){

        if( mThreadExecutor == null)
            mThreadExecutor = new ThreadPoolExecutor(COREPoolSIZE,MAXINUMPOOLSIZE,KEEPALIVETIME,
                    TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(MAXINUMTASKQUEUE),
                    new ThreadPoolExecutor.DiscardPolicy());


        mThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                Parcel ipcTransDataParcel = ParcelUtil.unmarshall(msgdata);
                IPCTransData ipcTransData = new IPCTransData(ipcTransDataParcel);
                if (ipcTransData != null && ipcTransData.eventType!=0) {
                    switch (ipcTransData.eventType) {
                        case EVENTTYPE_NORMAL:
                        case EVENTTYPE_ADDITION:
                            if (ipcEventListener != null)
                                ipcEventListener.onEvent(ipcTransData.event, ipcTransData);
                            break;
                        case EVENTTYPE_REGISTER:
                            handleRegisterEvent(ipcTransData);
                            break;
                        case EVENTTYPE_UNREGISTER:
                            handleUnRegisterEvent(ipcTransData);
                            break;
                    }

                }else
                    Log.e(TAG,"onTransData:parser ipctransdata fail");
            }
        });

    }

    /**
     * handleRegisterEvent
     * */
    private synchronized void handleRegisterEvent(IPCTransData ipcTransData){
        String eventName = ipcTransData.eventName;
        String observer = ipcTransData.from;
        //Log.d(TAG,"handleRegisterEvent:"+eventName + ","+observer);

        if(!TextUtils.isEmpty(eventName) && !TextUtils.isEmpty(observer)){
            Set<String > observerGroup = eventObserverGroup.get(eventName);
            boolean isNeedSetToMainMap = false;
            if(observerGroup == null){
                isNeedSetToMainMap = true;
                observerGroup = Collections.synchronizedSet(new HashSet<String>());
            }

            if (!observerGroup.contains(observer))
                    observerGroup.add(observer);

            if(isNeedSetToMainMap)
            eventObserverGroup.put(eventName,observerGroup);
        }


    }

    /**
     * handleUnRegisterEvent
     * */
    private synchronized void handleUnRegisterEvent(IPCTransData ipcTransData){
        String eventName = ipcTransData.eventName;
        String observer = ipcTransData.from;

        if(!TextUtils.isEmpty(eventName) && !TextUtils.isEmpty(observer)){
            Set<String > observerGroup = eventObserverGroup.get(eventName);
            if(observerGroup != null){
                if(observerGroup.contains(observer))
                    observerGroup.remove(observer);
            }
        }
    }



    private void freeIPCTransData(IPCTransData ipcTransData){

        if(ipcTransData == null)
            return;

        getShareMemoryManager().freeDataBuffer(ipcTransData.shmDataOffset,ipcTransData.shmDataSize);

    }



    private boolean checkParam(){
        if (mNativePtr == 0)
            return false;

        return true;
    }

    private  ShareMemoryManager getShareMemoryManager(){

        if(context == null)
            return null;

        if(mShareMemoryManager == null)
            mShareMemoryManager = ShareMemoryManager.getInstance(context,INITAIL_SHAREMEMORY_SIZE);

        return mShareMemoryManager;
    }

    static {
        System.loadLibrary("IPCEventNotify");
    }


    private native long nativeCreate(IPCTransDataListener ipcTransDataListener);

    private native int nativeInit(long mNativePtr, String bridgeFilePath, String indicationName);

    private native void nativeDestroy(long mNativePtr);

    private native void nativeNotifyIPCEvent(long mNativePtr,String[] observer, int oberserNum,byte[] ipcEventData, int dataLen);

    private native int nativeSendIPCEvent(long mNativePtr,String observer,  byte[] ipcEventData, int dataLen);

    //private native void nativeRegisterIPCEventListen(long mNativePtr,String Subject, String eventName);

    //private native void nativeUnRegisterIPCEventListen(long mNativePtr,String Subject, String eventName);


}