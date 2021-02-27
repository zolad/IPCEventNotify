package com.zolad.ipceventnotify.util;

import android.os.Parcel;
import android.os.Parcelable;

public class ParcelUtil {


    public static byte[] marshall(Parcel parcel) {

        byte[] bytes = parcel.marshall();

        parcel.recycle();

        return bytes;

    }

    public static byte[] marshall(Parcelable parceable) {

        Parcel parcel = Parcel.obtain();

        parceable.writeToParcel(parcel, 0);

        byte[] bytes = parcel.marshall();

        parcel.recycle();

        return bytes;

    }



    public static Parcel unmarshall(byte[] bytes) {

        Parcel parcel = Parcel.obtain();

        parcel.unmarshall(bytes, 0, bytes.length);

        parcel.setDataPosition(0); // This is extremely important!

        return parcel;

    }

    public static Parcel unmarshall(byte[] bytes,int offset,int length) {

        Parcel parcel = Parcel.obtain();

        parcel.unmarshall(bytes, offset, length);

        parcel.setDataPosition(0); // This is extremely important!

        return parcel;

    }

    public static <T> T unmarshall(Parcel parcel, Parcelable.Creator<T> creator) {

        T result = creator.createFromParcel(parcel);

        parcel.recycle();

        return result;

    }

    public static <T> T unmarshall(byte[] bytes, Parcelable.Creator<T> creator) {

        Parcel parcel = unmarshall(bytes);

        T result = creator.createFromParcel(parcel);

        parcel.recycle();

        return result;

    }
}
