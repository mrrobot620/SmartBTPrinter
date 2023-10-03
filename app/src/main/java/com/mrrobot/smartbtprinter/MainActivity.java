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
        import android.bluetooth.BluetoothAdapter;
        import android.bluetooth.BluetoothDevice;
        import android.content.Intent;
        import android.provider.Settings;
        import android.util.Log;
        import android.view.WindowManager;
        import android.widget.Toast;
        import com.example.tscdll.TSCActivity;
        import com.google.firebase.storage.FirebaseStorage;
        import com.google.firebase.storage.StorageReference;
        import com.opencsv.CSVReader;
        import java.io.File;
        import java.io.FileReader;
        import java.util.Arrays;
        import java.util.LinkedHashMap;
        import java.util.Map;
        import com.google.gson.Gson;


        public class MainActivity extends AppCompatActivity {

            private static final int SYSTEM_ALERT_WINDOW_PERMISSION = 23;
            private static final int BLUETOOTH_PERMISSION = 24;
            private static final int FINE_LOCATION_PERMISSION = 25;
            private TSCActivity TscDll;

            private String hostAddress;

            String json;


            public Map<String, String> csvDataMap = new LinkedHashMap<>();

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

                checkDrawOverlayPermission();
                checkBluetoothPermission();
                checkFineLocationPermission();
                csvDataMap.clear();
                downloadCSV();
                this.hostAddress = this.getBluetoothDevice();
                Log.wtf("App", "started");

            }

            protected void onDestroy() {
                Log.wtf("App", "Closed");
                csvDataMap.clear();
                this.stopService();
                super.onDestroy();
            }

            public void startService() {
                Intent intent = new Intent(MainActivity.this, WorkerService.class);
                intent.putExtra("hostAddress", this.hostAddress);
                intent.putExtra("inputExtra", "");
                Gson gson = new Gson();
                String json = gson.toJson(csvDataMap);
                intent.putExtra("grid" , json);
                Log.d("MMM" , json);
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
                        showSystemAlertWindow();
                    } else {
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
                WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
                if (windowManager != null) {
                }
            }


            public void downloadCSV() {
                StorageReference csvRef = FirebaseStorage.getInstance().getReference().child("grid.csv");
                File localFile = new File(getExternalFilesDir(null), "grid800.csv");

                csvRef.getFile(localFile)
                        .addOnSuccessListener(taskSnapshot -> {
                            Log.d("XXX", "File Downloaded Successfully");
                            csvDataMap = readCSVFile(localFile);
                            csvDataManager.getInstance().loadCsvData(csvDataMap);
                            this.startService();
                            Log.d("CSV1" , "csv written sucessfully");
                        })
                        .addOnFailureListener(exception -> {
                            Log.e("XXX", "File Download Failed: " + exception.getMessage());

                        });
            }

            private Map<String, String> readCSVFile(File csvFile) {
                    Map<String, String> csvDataMap = new LinkedHashMap<>();

                try {
                    CSVReader csvReader = new CSVReader(new FileReader(csvFile));

                    String[] nextRecord;
                    while ((nextRecord = csvReader.readNext()) != null) {
                        if (nextRecord.length == 2) {
                            csvDataMap.put(nextRecord[0], nextRecord[1]);
                        } else {
                            Log.e("CSV", "Invalid CSV line: " + Arrays.toString(nextRecord));
                        }
                    }
                } catch (Exception e) {

                }
                return csvDataMap;
            }


        }