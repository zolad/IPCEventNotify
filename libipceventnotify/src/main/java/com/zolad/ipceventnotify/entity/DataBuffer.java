package com.zolad.ipceventnotify.entity;

public class DataBuffer implements Comparable<DataBuffer> {

    public int size = 0;
    public int offset = 0;
    public boolean isFree = true;
    public DataBuffer prev;
    public DataBuffer next;



    public DataBuffer(int size, int offset, boolean isFree) {
        this.size = size;
        this.offset = offset;
        this.isFree = isFree;
    }

    @Override
    public int compareTo(DataBuffer o) {
        if(o==null)
            return 1;
        if(size > o.size)
            return 1;
        else if(size < o.size)
            return -1;
        else {

            if(offset > o.offset)
                return 1;
            else if(offset < o.offset){
                return -1;
            }else
               return 0;
        }
    }
}
