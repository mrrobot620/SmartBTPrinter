package com.mrrobot.smartbtprinter;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.os.FileObserver;
import android.os.IBinder;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import com.example.tscdll.TSCActivity;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;


import java.io.File;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.sql.Connection;
import java.util.Objects;


public class WorkerService
        extends Service {
    private static final String TAG = "WorkerService";
    private TSCActivity TscDll;
    private final File download_path;
    private String hostAddress;
    private FileObserver observer;
    private final String watchFolder;
    private LinkedHashMap<String , String> gridMap;

    private LinkedHashMap<String , String> varMap;

    private String site;

    private boolean printingInProgress = false;

    public synchronized boolean isPrintingInProgress() {
        return printingInProgress;
    }

    public synchronized void setPrintingInProgress(boolean printingInProgress) {
        this.printingInProgress = printingInProgress;
    }



    public WorkerService() {
        File file;
        this.download_path = file = Environment.getExternalStoragePublicDirectory((String) Environment.DIRECTORY_DOWNLOADS);
        this.watchFolder = file.getAbsolutePath();
    }

    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void onCreate() {
        super.onCreate();
        Log.wtf("HI", "i am in");
        this.hostAddress = "";
    }


    public void onDestroy() {
        this.observer.stopWatching();
//        this.TscDll.closeport();
        super.onDestroy();
    }


    public int onStartCommand(Intent intent, int n, int n2) {
        String string2;
        intent.getStringExtra("inputExtra");
        this.hostAddress = string2 = intent.getStringExtra("hostAddress");
        String json = intent.getStringExtra("grid");
        String vars = intent.getStringExtra("vars");
        if (!string2.isEmpty()) {
            TSCActivity tSCActivity;
            this.TscDll = tSCActivity = new TSCActivity();
            tSCActivity.openport(this.hostAddress);
            Log.d("XXX" , "Bluetooth Connected");
        }
        Gson gson = new Gson();
        Type type = new TypeToken<LinkedHashMap<String, String>>() {}.getType();
        gridMap = gson.fromJson(json, type);
        Log.d("GridMap in Service" , String.valueOf(gridMap));
        varMap = gson.fromJson(vars , type);
        Log.d("VarMap in Service" , String.valueOf(varMap));


        String bagTemplateA = intent.getStringExtra("bagTemplateA");
        String bagTemplateB = intent.getStringExtra("bagTemplateB");
        site = intent.getStringExtra("site");
        Log.d("XXX" , "A in Service: " + bagTemplateA);
        Log.d("XXX" , "B in Service: " + bagTemplateB);
        Log.d("XXX" , "SiteName in Service: " + site);

        this.observer = new FileObserver(this.watchFolder, FileObserver.CLOSE_WRITE) {

            public void onEvent(int n, String string2) {
                if (n == 8) {
                    String string3 = TAG;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("onEvent: File Found : ");
                    stringBuilder.append(string2);
                    Log.d((String) string3, (String) stringBuilder.toString());

                    if (string2.endsWith(".prn")) {
                        synchronized (WorkerService.this) {
                            if (!WorkerService.this.isPrintingInProgress()) {
                                WorkerService.this.setPrintingInProgress(true);
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        new PrintPrn(site, gridMap, varMap, string2, WorkerService.this.TscDll, bagTemplateA , bagTemplateB ).run();
                                        WorkerService.this.setPrintingInProgress(false);
                                    }
                                }).start();
                            } else {
                                Log.d("XXX", "Printing in progress, skipping file: " + string2);
                            }
                        }
                    }

                    if (string2.endsWith(".zip")) {
                        Log.d("Zip", "Zip not supported");
                    }
                }
            }
        };
            PendingIntent pendingIntent = PendingIntent.getActivity((Context) this, (int) 0, (Intent) new Intent((Context) this, MainActivity.class), (int) 0 | PendingIntent.FLAG_IMMUTABLE);
        Notification notification = new NotificationCompat.Builder((Context) this, "NOTIFICATION_CHANNEL_ID").setContentText((CharSequence) this.hostAddress).setTicker((CharSequence) "Connected").setContentIntent(pendingIntent).build();
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel notificationChannel = new NotificationChannel("NOTIFICATION_CHANNEL_ID", (CharSequence) "NOTIFICATION_CHANNEL_NAME", NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription("NOTIFICATION_CHANNEL_DESC");
            ((NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(notificationChannel);
        }
        this.startForeground(1, notification);
        this.observer.startWatching();
        return Service.START_STICKY;
    }

    public boolean stopService(Intent intent) {
        this.observer.stopWatching();
        this.TscDll.closeport();
        return super.stopService(intent);
    }


}