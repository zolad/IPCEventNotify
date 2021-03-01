package com.zolad.app2;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;


import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.zolad.entity.ImageIPCEvent;
import com.zolad.ipceventnotify.IPCEventNotify;
import com.zolad.ipceventnotify.entity.IPCTransData;
import com.zolad.ipceventnotify.event.IPCEvent;
import com.zolad.ipceventnotify.util.IPCEventListener;

import java.io.ByteArrayOutputStream;

import static com.zolad.ipceventnotify.util.Constants.EVENTTYPE_NORMAL;

public class MainActivity extends Activity {
    IPCEventNotify notify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        notify = IPCEventNotify.getInstance(this);
        notify.init("/sdcard/ipcdir","Demo2");
        findViewById(R.id.button_first).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // NavHostFragment.findNavController(FirstFragment.this)
                //       .navigate(R.id.action_FirstFragment_to_SecondFragment);
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.raw.pineapple);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] data = baos.toByteArray();


                ImageIPCEvent event = new ImageIPCEvent(data);


                notify.sendIPCEvent("Demo1",event);
            }
        });



        notify.setOnIPCEventListener(new IPCEventListener() {
            @Override
            public void onEvent(final IPCEvent event, final IPCTransData ipcTransData) {
                if(ipcTransData.eventType == EVENTTYPE_NORMAL){

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((TextView)findViewById(R.id.textview_first)).setText(
                                    "rece IPCEvent from="+ipcTransData.from+" IPCEvent="+event.toString());

                        }
                    });

                    Log.d("IPCEventListener",ipcTransData.from+" rece IPCTransData"+ipcTransData.toString());

                }
            }
        });
    }


}
