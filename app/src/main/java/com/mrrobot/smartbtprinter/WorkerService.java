package com.mrrobot.smartbtprinter;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Environment;
import android.os.FileObserver;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.WindowManager;

import androidx.core.app.NotificationCompat;

import com.example.tscdll.TSCActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.Objects;


public class WorkerService
        extends Service {
    private static final String TAG = "WorkerService";
    private TSCActivity TscDll;
    private final File download_path;
    private String hostAddress;
    private FileObserver observer;
    private final String watchFolder;

    private String inWorkerGrid;

    private String site;



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
        EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGridCodeEvent(com.mrrobot.rscsmartbtprinter.SendGridDataEvent event){
        inWorkerGrid = event.getGridData();
    }



    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        this.observer.stopWatching();
        this.TscDll.closeport();
        super.onDestroy();
    }


    public int onStartCommand(Intent intent, int n, int n2) {
        String string2;
//        intent.getStringExtra("inputExtra");
        this.hostAddress = string2 = intent.getStringExtra("hostAddress");
        if (!string2.isEmpty()) {
            TSCActivity tSCActivity;
            this.TscDll = tSCActivity = new TSCActivity();
            tSCActivity.openport(this.hostAddress);
        }
        this.observer = new FileObserver(this.watchFolder, FileObserver.CLOSE_WRITE) {

            public void onEvent(int n, String string2) {
                if (n == 8) {
                    String string3 = TAG;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("onEvent: File Found : ");
                    stringBuilder.append(string2);

                    Log.d((String) string3, (String) stringBuilder.toString());
                    if (string2.endsWith(".prn")) {
                        // Here I am adding a System Overlay with Pop Up , With a Editvalue which will be passed to the PrintPrn
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        switch (which) {
                                            case DialogInterface.BUTTON_POSITIVE:
                                                if (Objects.equals(inWorkerGrid , null)) {
                                                    Log.d("HH" , "ScreenReader Unable to Share Grid Code");
                                                    Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                        v.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE));
                                                    } else {
                                                        v.vibrate(1000);
                                                    }
                                                    AlertDialog mismatchDialog = new AlertDialog.Builder(getApplicationContext())
                                                            .setTitle("Error")
                                                            .setMessage("Unable to get Data from HMS \nPlease Contact Local IT Team")
                                                            .setPositiveButton("Try Again", new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialog, int which) {
                                                                    dialog.dismiss();
                                                                }
                                                            })
                                                            .create();
                                                    mismatchDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                                                    mismatchDialog.show();
                                                }
                                                else if (inWorkerGrid.equals("405")) {
                                                    Log.d("HH" , "ScreenReader Unable to Share Screen");
                                                    Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                        v.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE));
                                                    } else {
                                                        v.vibrate(1000);
                                                    }
                                                    AlertDialog mismatchDialog = new AlertDialog.Builder(getApplicationContext())
                                                            .setTitle("Error")
                                                            .setMessage("Bag Already Staged.")
                                                            .setPositiveButton("Try Again", new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialog, int which) {
                                                                    dialog.dismiss();
                                                                }
                                                            })
                                                            .create();
                                                    mismatchDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                                                    mismatchDialog.show();
                                                }
                                                else if (inWorkerGrid.equals("404")) {
                                                    Log.d("HH" , "ScreenReader Unable to Share Screen");
                                                    Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                        v.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE));
                                                    } else {
                                                        v.vibrate(1000);
                                                    }
                                                    AlertDialog mismatchDialog = new AlertDialog.Builder(getApplicationContext())
                                                            .setTitle("Error")
                                                            .setMessage("No Connection Found")
                                                            .setPositiveButton("Print Without Grid", new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialog, int which) {
                                                                    new Thread((Runnable) new PrintPrn("NA", "YKB" , string2, WorkerService.this.TscDll)).start();
                                                                    dialog.dismiss();
                                                                }
                                                            })
                                                            .create();
                                                    mismatchDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                                                    mismatchDialog.show();
                                                }
                                                else {
                                                    Log.d("HH", "Input and Grid Matched");
                                                    new Thread((Runnable) new PrintPrn(inWorkerGrid,"YKB" ,string2, WorkerService.this.TscDll )).start();
                                                }
                                            case DialogInterface.BUTTON_NEGATIVE:
//                                                inWorkerGrid = "NA";
//                                                new Thread((Runnable) new PrintPrn(inWorkerGrid, string2, WorkerService.this.TscDll)).start();
                                                break;
                                        }
                                    }
                                };
                                AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
                                builder.setMessage("Print Label");
                                builder.setPositiveButton("Print", dialogClickListener);
//                                builder.setNegativeButton("Simple Print", dialogClickListener);
                                AlertDialog alertDialog = builder.create();
                                alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                                alertDialog.show();
                            }
                        }, 2000);
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