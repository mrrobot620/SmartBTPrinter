package com.mrrobot.smartbtprinter;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.FileObserver;
import android.os.IBinder;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import com.example.tscdll.TSCActivity;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.File;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class WorkerService
        extends Service {
    private static final String TAG = "WorkerService";
    private TSCActivity TscDll;
    private final File download_path;
    private String hostAddress;
    private FileObserver observer;
    private final String watchFolder;
    private LinkedHashMap<String , String> gridMap;

    private Connection  sqlConnection;

    Connection conn = null;

//    private String serverIp , port , databaseName , userName , password;



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
        this.TscDll.closeport();
        super.onDestroy();
    }


    public int onStartCommand(Intent intent, int n, int n2) {
        String string2;
        intent.getStringExtra("inputExtra");
        this.hostAddress = string2 = intent.getStringExtra("hostAddress");
        String json = intent.getStringExtra("grid");
        if (!string2.isEmpty()) {
            TSCActivity tSCActivity;
            this.TscDll = tSCActivity = new TSCActivity();
            tSCActivity.openport(this.hostAddress);
            Log.d("XXX" , "Bluetooth Connected");
        }
        Gson gson = new Gson();
        Type type = new TypeToken<LinkedHashMap<String, String>>() {}.getType();
        gridMap = gson.fromJson(json, type);
        Log.d("GridMap" , String.valueOf(gridMap));


        this.observer = new FileObserver(this.watchFolder, FileObserver.CLOSE_WRITE) {

            public void onEvent(int n, String string2) {
                if (n == 8) {
                    String string3 = TAG;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("onEvent: File Found : ");
                    stringBuilder.append(string2);
                    Log.d((String) string3, (String) stringBuilder.toString());
                    if (string2.endsWith(".prn")) {
                        new Thread((Runnable) new PrintPrn("YKB" ,gridMap , string2, WorkerService.this.TscDll , sqlConnection)).start();
                        Log.d("XXX" , "Runnable Sent");
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