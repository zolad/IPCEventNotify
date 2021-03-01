package com.zolad.ipceventnotify.util;

public class Constants {

    public static final int EVENTTYPE_NORMAL = 1;
    public static final int EVENTTYPE_ADDITION = 2;
    public static final int EVENTTYPE_REGISTER = 3;
    public static final int EVENTTYPE_UNREGISTER = 4;
    public static final int EVENTTYPE_FREEDATA = 5;
    public static final int LIMIT_DATASIZE = 4096 - 2048;



    public static final  int SUCCESS = 0;
    public static final  int FAIL = -1;
    public static final  int INITFAIL_CONTEXT_NULL = -2;
    public static final  int INITFAIL_FILEPATH_NOT_AVAILABLE = -3;
    public static final  int INITFAIL_NOT_PERMISSION = -4;
    public static final  int INITFAIL_ALREADYINIT= -5;

    public static final  int SENDFAIL_NOT_INIT = -6;
    public static final  int SENDFAIL_WRITEFAIL = -7;
    public static final  int SENDFAIL_OBSERVER_NOT_EXIST = -8;
    public static final  int SENDFAIL_EVENTSIZE_ERROR= -9;



}
