# IPCEventNotify

A IPC Event Notify Library for Android app,base on linux named pipe,use shared memory 一个安卓APP跨进程通信库,基于linux管道，可发送自定义消息对象，实现共享内存管理(已实现)，和共享内存IPC传输（待实现）

Usage
==============

```java
//Get single instance of IPCEventNotify
IPCEventNotify instance = IPCEventNotify.getInstance(context);

//Init base
/**
 * @param BridgeFilePath init and set a Bridge FilePath
 * @param  indicationName  create a pipe file By given indicationName
 * it will create a pipe file using given indicationName
 * for example  BridgeFilePath/indicationName
 */
instance.init("/sdcard/ipcdir","PackageName or Some IndicationName You want");

or

 /**
 * @param BridgeFilePath init and set a Bridge FilePath
 * it will create a pipe file using "app PackageName"
 * for example  BridgeFilePath/com.xx.xx
 */
instance.init("/sdcard/ipcdir");

//Set a IPCEvent Listen
instance.setOnIPCEventListener(new IPCEventListener());

//New a custom IPCEvent you define(implement IPCEvent)
TestIPCEvent event =  new TestIPCEvent(123,"this message is from Demo1");

//Send it
 /**
 * send  a ipcEvent to observer
 * @param observer
 * @param ipcEvent
 */
instance.sendIPCEvent("Demo2",event);

or

/**
 * @param ipcEvent broadcast ipcevent
 * broadcast  a ipcEvent to observer who had register listen
 */
instance.notifyIPCEvent(IPCEvent ipcEvent);

        
```


License
==============

    Copyright 2021 Zolad

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
