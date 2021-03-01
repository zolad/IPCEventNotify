package com.zolad.app1;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import com.zolad.entity.ImageIPCEvent;
import com.zolad.entity.TestIPCEvent;
import com.zolad.ipceventnotify.IPCEventNotify;
import com.zolad.ipceventnotify.entity.IPCTransData;
import com.zolad.ipceventnotify.event.IPCEvent;
import com.zolad.ipceventnotify.util.IPCEventListener;


import android.os.MemoryFile;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toolbar;

import java.io.IOException;

import static com.zolad.ipceventnotify.util.Constants.EVENTTYPE_ADDITION;

public class MainActivity extends Activity {
    IPCEventNotify notify;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
      //  setSupportActionBar(toolbar);

        notify = IPCEventNotify.getInstance(this);
        notify.init("/data/bctc","Demo1");
        findViewById(R.id.button_first).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // NavHostFragment.findNavController(FirstFragment.this)
                //       .navigate(R.id.action_FirstFragment_to_SecondFragment);
                TestIPCEvent event =  new TestIPCEvent(123,"this message is from Demo1");
                notify.sendIPCEvent("Demo2",event);
            }
        });




        notify.setOnIPCEventListener(new IPCEventListener() {
            @Override
            public void onEvent(IPCEvent event, final IPCTransData ipcTransData) {
                if(ipcTransData.eventType == EVENTTYPE_ADDITION){

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((TextView)findViewById(R.id.textview_first)).setText("rece IPCEvent from="+
                                    ipcTransData.from+" rece IPCTransData"+ipcTransData.toString());

                        }
                    });


                    Parcel result = notify.getShmFileParcel(ipcTransData.shmFd,ipcTransData.shmDataOffset,ipcTransData.shmDataSize);


                    if(result!=null) {
                        ImageIPCEvent imageIPCEvent = new ImageIPCEvent(result);
                        final Bitmap bitmap = BitmapFactory.decodeByteArray(imageIPCEvent.picture, 0,
                                imageIPCEvent.picture.length);

                        Log.d("IPCEventListener",bitmap.getWidth()+" bitmap");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ((ImageView) findViewById(R.id.imageView)).setImageBitmap(bitmap);

                            }
                        });
                    }else{
                        Log.d("IPCEventListener","Parcel null");

                    }
                    Log.d("IPCEventListener",ipcTransData.from+" rece IPCTransData"+ipcTransData.toString());

                }
            }
        });
    }



}