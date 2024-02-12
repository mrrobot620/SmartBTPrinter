package com.mrrobot.smartbtprinter;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.appcompat.app.AppCompatActivity;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.tscdll.TSCActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;
import com.google.common.reflect.TypeToken;
import com.google.firebase.FirebaseApp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.opencsv.CSVReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.gson.Gson;


public class MainActivity extends AppCompatActivity {

    private static final int SYSTEM_ALERT_WINDOW_PERMISSION = 23;
    private static final int BLUETOOTH_PERMISSION = 24;
    private static final int FINE_LOCATION_PERMISSION = 25;
    private TSCActivity TscDll;
    private String hostAddress;
    String json;
    public Map<String, String> csvDataMap = new LinkedHashMap<>();

    public Map<String , String> varsMap = new LinkedHashMap<>();

    public String bagTemplateA;

    public String bagTemplateB;

    public String site;




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
        MaterialAutoCompleteTextView materialSpinner = findViewById(R.id.autoCompleteTextView);
        TextInputLayout layout1 = findViewById(R.id.autoCompleteTextView1);
        Button submitButton =  findViewById(R.id.submitButton);
        TextView selectedSite = findViewById(R.id.selectedSite);
        ImageView sucessTick = findViewById(R.id.greenTick);
        ImageView labelPrinter = findViewById(R.id.imageView2);
        ImageView connectedPrinter = findViewById(R.id.connectedPrinter);
        checkDrawOverlayPermission();
        checkBluetoothPermission();
        checkFineLocationPermission();
        FirebaseApp.initializeApp(this);
        csvDataMap.clear();
        varsMap.clear();
        downloadSites(materialSpinner);
        getVars();

        submitButton.setOnClickListener(v -> {
            String siteName = materialSpinner.getText().toString();
            site = retrieveSite(siteName);
            downloadCSV(siteName , materialSpinner , layout1 , selectedSite , sucessTick , labelPrinter , submitButton , connectedPrinter);
            this.hostAddress = this.getBluetoothDevice();
            Log.wtf("App", "started");
        });

        downloadFile("prn_templates/bagType1.txt", new FileDownloadCallback() {
            @Override
            public void onSuccess(String fileContents) {
                bagTemplateA = fileContents;
                Log.d("XXX" , "Bag Template A: " + bagTemplateA);
            }
            @Override
            public void onFailure(Exception exception) {
                Log.e("XXX"  , "Error Downloading File" + exception);
            }
        });

        downloadFile("prn_templates/bagType2.txt", new FileDownloadCallback() {
            @Override
            public void onSuccess(String fileContents) {
                bagTemplateB = fileContents;
                Log.d("XXX" , "Bag Template B: " + bagTemplateB);
            }
            @Override
            public void onFailure(Exception exception) {
                Log.e("XXX"  , "Error Downloading File template B: " + exception);
            }
        });
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
        String vars = gson.toJson(varsMap);
        intent.putExtra("grid", json);
        intent.putExtra("vars" , vars);
        intent.putExtra("bagTemplateA" , bagTemplateA);
        intent.putExtra("bagTemplateB" , bagTemplateB);
        intent.putExtra("site" , site);
        Log.d("MMM", json);
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
            } else {
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


    public void downloadCSV(String siteName , AutoCompleteTextView materialSpinner , TextInputLayout layout1 , TextView selectedSite , ImageView tick , ImageView printerImg , Button btn , ImageView connectedPrinter) {
        StorageReference csvRef = FirebaseStorage.getInstance().getReference().child(siteName + ".csv");
        File localFile = new File(getExternalFilesDir(null), "grid800.csv");
        csvRef.getFile(localFile)
                .addOnSuccessListener(taskSnapshot -> {
                    Log.d("XXX", "File Downloaded Successfully");
                    csvDataMap = readCSVFile(localFile);
                    csvDataManager.getInstance().loadCsvData(csvDataMap);
                    this.startService();
                    materialSpinner.setVisibility(View.GONE);
                    layout1.setVisibility(View.GONE);
                    printerImg.setVisibility(View.GONE);
                    btn.setText("Disconnect");
                    btn.setOnClickListener(v ->{
                        finish();
                    });
                    selectedSite.setText(siteName);
                    tick.setVisibility(View.VISIBLE);
                    connectedPrinter.setVisibility(View.VISIBLE);
                    selectedSite.setVisibility(View.VISIBLE);
                    Log.d("CSV1", "csv written sucessfully");
                })
                .addOnFailureListener(exception -> {
                    showAlertDialog("Site Not Found \nConnect with Abhishek(182542)");
                    Log.e("XXX", "File Download Failed: " + exception.getMessage());
                });
    }


    public void downloadSites(AutoCompleteTextView materialSpinner) {
        StorageReference ref = FirebaseStorage.getInstance().getReference().child("sites.csv");
        File localFile = new File(getExternalFilesDir(null), "sites.csv");
        ref.getFile(localFile)
                .addOnSuccessListener(taskSnapshot -> {
                    Log.d("XXX", "Sites File Downloaded Successfully");
                    List<String> sites = readSitesFromCSV(localFile);
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_dropdown_item_1line, sites);
                    Log.d("XXX" , adapter.toString());
                    materialSpinner.setAdapter(adapter);
                })
                .addOnFailureListener(exception -> {
                    Log.e("XXX", "Sites File Download Failed: " + exception.getMessage());
                });
    }


    public void getVars(){
        StorageReference ref = FirebaseStorage.getInstance().getReference().child("vars.json");
        File localFile = new File(getExternalFilesDir(null) , "vars.json");
        ref.getFile(localFile)
                .addOnSuccessListener(taskSnapshot -> {
                    Log.d("XXX" , "Vars Downloaded Successfully");
                    try {
                        readVarsFromFile(localFile);
                    }
                    catch (IOException e){
                        e.printStackTrace();
                    }
                })
                .addOnFailureListener(exception -> {
                    Log.e("XXX" , "Vars File Download Failed: " + exception.getMessage());
                });
    }
    private void readVarsFromFile(File file) throws IOException {
        Gson gson = new Gson();
        Type type = new TypeToken<List<Variable>>(){}.getType();

        try (FileReader reader = new FileReader(file)) {
            List<Variable> variableList = gson.fromJson(reader, type);
            if (variableList != null) {
                varsMap = new LinkedHashMap<>();
                for (Variable variable : variableList) {
                    varsMap.put(variable.getName(), variable.getPattern());
                }
                Log.d("XXX", "Vars Map: " + varsMap);
            } else {
                Log.d("XXX", "Failed to parse vars.json");
            }
        }
    }

    private static class Variable {
        private String name;
        private String pattern;

        public String getName() {
            return name;
        }

        public String getPattern() {
            return pattern;
        }
    }

    private List<String> readSitesFromCSV(File csvFile) {
        List<String> sites = new ArrayList<>();

        try {
            CSVReader csvReader = new CSVReader(new FileReader(csvFile));

            String[] nextRecord;
            while ((nextRecord = csvReader.readNext()) != null) {
                // Assuming each row contains only one site name
                if (nextRecord.length >= 1) {
                    sites.add(nextRecord[0]);
                }
            }
        } catch (Exception e) {
            Log.e("XXX", "Error reading sites from CSV: " + e.getMessage());
        }

        return sites;
    }

    private void showAlertDialog(String message) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
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

    public interface FileDownloadCallback{
        void onSuccess(String fileContents);
        void onFailure(Exception exception);
    }


    private void downloadFile(String path  , FileDownloadCallback callback){
        StorageReference ref  = FirebaseStorage.getInstance().getReference().child(path);
        String randomFileName = UUID.randomUUID().toString();
        File localFile = new File(getExternalFilesDir(null) , randomFileName);

        ref.getFile(localFile)
                .addOnSuccessListener(taskSnapshot -> {
                    Log.d("XXX" , randomFileName + "Download SuccessFully");
                    try{
                        String fileContents  = readFile(localFile);
                        callback.onSuccess(fileContents);
                        deleteLocalFile(localFile);
                    } catch (IOException e){
                        callback.onFailure(e);
                    }
                })
                .addOnFailureListener(exception -> {
                    Log.e("XXX", randomFileName + " Download Failed" + exception.getMessage());
                    callback.onFailure(exception);
                });
    }

    private void deleteLocalFile(File file) {
        if (file.exists()) {
            file.delete();
        }
    }

    private String readFile(File file) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
            stringBuilder.append("\n");
        }
        reader.close();
        return stringBuilder.toString();
    }

    public String retrieveSite(String siteName) {
        String[] parts = siteName.split("_");

        if (parts.length >= 2) {
            String lastPart = parts[parts.length - 1].trim();
            String[] words = lastPart.split("\\s+");

            if (words.length >= 3) {
                return words[words.length - 3] + " " + words[words.length - 2] + " " + words[words.length - 1];
            } else {
                return lastPart;
            }
        } else {
            return siteName;
        }
    }

}