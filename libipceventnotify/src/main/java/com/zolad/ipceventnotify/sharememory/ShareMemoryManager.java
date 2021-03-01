package com.zolad.ipceventnotify.sharememory;

import android.content.Context;
import android.os.Build;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.SharedMemory;
import android.system.ErrnoException;
import android.system.OsConstants;
import android.util.Log;

import com.zolad.ipceventnotify.entity.DataBuffer;
import com.zolad.ipceventnotify.entity.RBTree;
import com.zolad.ipceventnotify.entity.RBTreeNode;
import com.zolad.ipceventnotify.util.InvokeUtil;

import java.io.FileDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;

/**
 * ShareMemory File Manager
 * */
public class ShareMemoryManager {

    private static  final  String TAG = ShareMemoryManager.class.getSimpleName();
    private volatile static ShareMemoryManager instance = null;
    private Context context;
    private int memoryFileSize = 0;
    private int memoryUsed = 0;
    //private MemoryFile mmfile;
    private RBTree<DataBuffer> bufferFreeRBTree; // a rbtree which save free buffer
    private RBTree<DataBuffer> bufferUsedRBTree; // a rbtree which save used buffer
    private SharedMemory mSharedMemory;
    private static  final int ROOTNODE_SIZE = 1024;
    private DataBuffer  rootBuffer;


    private ShareMemoryManager(Context context, int memoryFileSize) {
        this.context = context;
        this.memoryFileSize = memoryFileSize;
        //可用 databuffer 红黑树
        bufferFreeRBTree = new RBTree<DataBuffer>();
        //已被使用 databuffer 红黑树
        bufferUsedRBTree = new RBTree<DataBuffer>();

        //创建一个根节点buffer
        rootBuffer = new DataBuffer(ROOTNODE_SIZE,0,true);
        bufferFreeRBTree.addNode(rootBuffer);

        memoryUsed = ROOTNODE_SIZE;

        try {
            //mmfile = new MemoryFile(context.getPackageName(),memoryFileSize);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                mSharedMemory = SharedMemory.create(context.getPackageName(),memoryFileSize);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param context
     * Get a single  instance  of IPCEventNotify Class
     */
    public static ShareMemoryManager getInstance(Context context, int memoryFileSize) {
        if (instance == null) {
            synchronized (ShareMemoryManager.class) {
                if (instance == null) {
                    instance = new ShareMemoryManager(context,memoryFileSize);
                }
            }
        }
        return instance;
    }


    public synchronized  DataBuffer getAvailDataBuffer(int request_size){

        if(mSharedMemory == null)
            return null;

        if(bufferFreeRBTree.getSize() == 0){
            //has enough space left
             return getNewDataBuffer(request_size);

        }else{

            RBTreeNode<DataBuffer> dataRoot = bufferFreeRBTree.getRoot();

            RBTreeNode<DataBuffer> bestFit = null;
            //寻找尺寸最接近的可用databuffer
            while (dataRoot != null) {
                if (dataRoot.getValue().size > request_size) {
                    bestFit = dataRoot;
                    dataRoot = dataRoot.getLeft();
                } else if(dataRoot.getValue().size < request_size) {
                    dataRoot = dataRoot.getRight();
                } else{
                    bestFit = dataRoot;
                    break;
                }
            }

             if(bestFit == null){
                 return getNewDataBuffer(request_size);
             }else {
                 bestFit.getValue().isFree = true;

                 //如果bestfit大小大于request_size,则拆分成两个,一个塞到free树,一个塞到used树
                 int bestFitBufferSize = bestFit.getValue().size;
                 int bestFitBufferOffset = bestFit.getValue().offset;
                 if(bestFitBufferSize > request_size){
                     //拆分成两个
                     DataBuffer newBuffer =  new DataBuffer(request_size,bestFit.getValue().offset,false);
                     DataBuffer restBuffer = new DataBuffer(bestFitBufferSize - request_size ,bestFitBufferOffset + request_size,true);

                     //重新修改next prev的引用
                     newBuffer.prev = bestFit.getValue().prev;
                     newBuffer.next = restBuffer;
                     restBuffer.prev = newBuffer;
                     restBuffer.next = bestFit.getValue().next;

                     //红黑树移除和添加
                     bufferFreeRBTree.remove(bestFit.getValue());
                     bufferFreeRBTree.addNode(restBuffer);
                     bufferUsedRBTree.addNode(newBuffer);

                     return newBuffer;
                 }

                 return bestFit.getValue();
             }


        }

    }

    private DataBuffer  getNewDataBuffer(int request_size){
        if(memoryFileSize - memoryUsed > request_size){
            DataBuffer newBuffer = new DataBuffer(request_size,memoryUsed,false);
            bufferUsedRBTree.addNode(newBuffer);
            memoryUsed += request_size;

            DataBuffer last = rootBuffer;

            findlast:while (last != null){

                if(last.next!= null)
                  last =  last.next;
                else
                    break findlast;

            }

            last.next = newBuffer;
            newBuffer.prev = last;


            return newBuffer;
        }else{
            //not enough space left
            return null;
        }
    }


    public SharedMemory writeData(byte[] data, DataBuffer dataBuffer){

        if(mSharedMemory == null || data.length != dataBuffer.size)
            return null;

        ByteBuffer byteBuffer;
        try {
             Log.d(TAG,"dataBuffer offset="+dataBuffer.offset +",size="+dataBuffer.size);
           //  mSharedMemory.setProtect(OsConstants.PROT_READ| OsConstants.PROT_WRITE);
          //   dataBuffer.size = 8192;
          //   dataBuffer.offset = 0;
          //   byteBuffer = mSharedMemory.map(OsConstants.PROT_READ| OsConstants.PROT_WRITE,dataBuffer.offset,dataBuffer.size);

            byteBuffer = mSharedMemory.mapReadWrite();

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG,""+e.getMessage());
            return null;
        }
        //byteBuffer.put(data);
        byteBuffer.position(dataBuffer.offset);
        byteBuffer.put(data,0,dataBuffer.size);

        //mSharedMemory.unmap(byteBuffer);


        return mSharedMemory;


    }




    public synchronized void freeDataBuffer(int offset,int size){

        if(mSharedMemory == null)
            return;


        if(bufferUsedRBTree.getSize() > 0){

            DataBuffer dataBuffer = new DataBuffer(size,offset,true);
            DataBuffer matchDataBuffer = bufferFreeRBTree.find(dataBuffer);

            if(matchDataBuffer !=null){
                bufferUsedRBTree.remove(matchDataBuffer);
            }

            matchDataBuffer.isFree = true;

            //bufferFreeRBTree.addNode(matchDataBuffer);

            DataBuffer current = matchDataBuffer;

            //向后寻找是否有可以合并的
            DataBuffer forward = current.next;

            if (forward!=null && forward.isFree){

                current = combineBuffer(current,forward);
                if(current!=null) {
                    //forward = current.next;
                    bufferFreeRBTree.remove(forward);
                }

            }

            //向前寻找是否有可以合并的
            DataBuffer backward = current.prev;

            if (backward!=null && backward.isFree){

                current = combineBuffer(backward,current);
                if(current!=null) {
                    //forward = current.prev;
                    bufferFreeRBTree.remove(backward);

                }

            }

            bufferFreeRBTree.addNode(current);


        }

    }

    //合并buffer,减少碎片化
    private  DataBuffer combineBuffer(DataBuffer left,DataBuffer right){

        if(left == null || right == null)
            return null;

        DataBuffer combine = new DataBuffer(left.size + right.size,left.offset,true);
        //重新修改next prev的引用
        if(left.prev!=null)
            left.prev.next = combine;
        combine.prev = left.prev;
        combine.next = right.next;

        //bufferFreeRBTree.remove(left);
        //bufferFreeRBTree.remove(right);
        //bufferFreeRBTree.addNode(combine);

        return combine;

    }

    public ParcelFileDescriptor getFileDescriper(){
        if(mSharedMemory == null){
            throw new IllegalArgumentException("memoryFile is null");
        }
        FileDescriptor fd = null;
        ParcelFileDescriptor pfd = null;
        try {
            fd = (FileDescriptor) InvokeUtil.invokeMethod(mSharedMemory, "getFileDescriptor");
            pfd = (ParcelFileDescriptor) InvokeUtil.newInstanceOrThrow(ParcelFileDescriptor.class, fd);

            pfd = pfd.dup();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG,""+e.getMessage());
        }
        return pfd;

    }

    public static SharedMemory openMemoryFile(ParcelFileDescriptor pfd) {
        if(pfd == null){
            throw new IllegalArgumentException("ParcelFileDescriptor is null");
        }
        FileDescriptor fd = pfd.getFileDescriptor();
        Object sharedMemory = null;
        try {

           //Class<?> c = Class.forName("android.os.SharedMemory");
            //sharedMemory = InvokeUtil.newInstanceOrThrow(c, fd);

            sharedMemory = SharedMemory.create("com.zolad.app2",1024*1024);
        }catch (Exception e){
            Log.e(TAG,"openMemoryFile="+pfd.getFd()+","+e.getMessage());
        }

        return (SharedMemory) sharedMemory;
    }

    public static byte[] readShareMemory(SharedMemory sharedMemory,int offset,int size) {
        if(sharedMemory == null){
            throw new IllegalArgumentException("sharedMemory is null");
        }
        ByteBuffer byteBuffer;
        try {

            byteBuffer = sharedMemory.mapReadWrite();
        } catch (ErrnoException e) {
            e.printStackTrace();
            Log.e(TAG,"readShareMemory:"+e.getMessage());
            return null;
        }
        byte[] data = new byte[size];
        byteBuffer.position(offset);
        byteBuffer.get(data,0,size);

        return data;
    }

    public void destroy(){

        if(mSharedMemory != null) {
            mSharedMemory.close();
        }

    }
}
