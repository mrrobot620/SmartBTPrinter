package com.mrrobot.smartbtprinter;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.tscdll.TSCActivity;
import com.mrrobot.smartbtprinter.WorkerService;

import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private static final int SYSTEM_ALERT_WINDOW_PERMISSION = 23;
    private static final int BLUETOOTH_PERMISSION = 24;
    private static final int FINE_LOCATION_PERMISSION = 25;
    private TSCActivity TscDll;

    private String hostAddress;

    private Button btn;

    private EditText text;

    private String getBluetoothDevice() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        String string2 = "";
        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            for (BluetoothDevice bluetoothDevice : bluetoothAdapter.getBondedDevices()) {
                String string3 = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Bluetooth Device Address : ");
                stringBuilder.append(bluetoothDevice.getAddress());
                Log.d((String) string3, (String) stringBuilder.toString());
                string2 = bluetoothDevice.getAddress();
            }
        }
        return string2;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check and request permissions
        checkDrawOverlayPermission();
        checkBluetoothPermission();
        checkFineLocationPermission();

        this.hostAddress = this.getBluetoothDevice();
        Log.wtf("App", "started");
        this.startService();
    }

    protected void onDestroy() {
        Log.wtf("App", "Closed");
        this.stopService();
        super.onDestroy();
    }

    public void startService() {
        Intent intent = new Intent(MainActivity.this, WorkerService.class);
        intent.putExtra("hostAddress", this.hostAddress);
        intent.putExtra("inputExtra", "");
        ContextCompat.startForegroundService(MainActivity.this, intent);
        Log.d("Created", "Worker Service Created");
    }

    public void stopService() {
        this.stopService(new Intent((Context) this, WorkerService.class));
    }

    @TargetApi(23)
    private void checkDrawOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            askPermission(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, SYSTEM_ALERT_WINDOW_PERMISSION);
        }
    }

    private void checkBluetoothPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.BLUETOOTH}, BLUETOOTH_PERMISSION);
        }
    }

    private void checkFineLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == BLUETOOTH_PERMISSION || requestCode == FINE_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, you can perform actions that require the permission
            } else {
                // Permission denied, handle accordingly
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SYSTEM_ALERT_WINDOW_PERMISSION) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this)) {
                // Permission granted, you can show the system alert window
                showSystemAlertWindow();
            } else {
                // Permission not granted, handle accordingly
                Toast.makeText(this, "SYSTEM_ALERT_WINDOW permission not granted.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void askPermission(String action, int permissionCode) {
        Intent intent = new Intent(action,
                Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, permissionCode);
    }

    private void showSystemAlertWindow() {
        // Make sure you are using the correct context for displaying the window
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        if (windowManager != null) {
            // Add your code to display the system alert window here
        }
    }
}
