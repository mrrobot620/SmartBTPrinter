package com.mrrobot.smartbtprinter;


import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import com.example.tscdll.TSCActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class PrintPrn implements Runnable {
    private static final String TAG = "PrintPrn";

    private TSCActivity TscDll;
    private final File download_path;
    private String filename;
    private final String watchFolder;

    private final String site;
    private LinkedHashMap<String ,String> gridMap;

    private LinkedHashMap<String ,String> varMap;
    private String bagTemplateA;
    private String bagTemplateB;

    private static final Object lock = new Object();


    PrintPrn(String site) {
        this.site = site;
        File file;
        this.download_path = file = Environment.getExternalStoragePublicDirectory((String)Environment.DIRECTORY_DOWNLOADS);
        this.watchFolder = file.getAbsolutePath();
    }

    PrintPrn(String site ,LinkedHashMap<String ,String> gridMap ,LinkedHashMap<String ,String> varMap , String string2,  TSCActivity tSCActivity , String bagTemplateA , String bagTemplateB) {
        this.site = site;
        this.gridMap = gridMap;
        this.varMap = varMap;
        File file;
        this.download_path = file = Environment.getExternalStoragePublicDirectory((String)Environment.DIRECTORY_DOWNLOADS);
        this.watchFolder = file.getAbsolutePath();
        this.filename = string2;
        this.TscDll = tSCActivity;
        this.bagTemplateA = bagTemplateA;
        this.bagTemplateB = bagTemplateB;

    }

    private void readfile(String string2) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(this.watchFolder);
        stringBuilder.append("/");
        stringBuilder.append(string2);
        File file = new File(stringBuilder.toString());
        Log.d((String)TAG, (String)"Waiting for file to Read");
        Log.d("XX" , "STUCK AT LINE 53");
        while (file.length() == 0L) {
            Log.d((String)TAG, (String)"Waiting for file to Read");
            Log.d("XX" , "STUCK AT LINE 56");
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append(this.watchFolder);
            stringBuilder2.append("/");
            stringBuilder2.append(string2);
            file = new File(stringBuilder2.toString());
        }
    }

    public void run() {
        try {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(this.watchFolder);
            stringBuilder.append("/");
            stringBuilder.append(this.filename);
            File file = new File(stringBuilder.toString());
            if (file.length() == 0L) {
                Log.d((String)TAG, (String)"File is Empty");
                this.readfile(this.filename);
            }
            this.TscDll.clearbuffer();
            if (this.filename.startsWith("wid")) {
                this.TscDll.setup(10, 22, 4, 15, 0, 3, 0);
                Log.d((String)TAG, (String)"wid Setup");
            } else if (this.filename.startsWith("wsn")) {
                this.TscDll.setup(10, 22, 4, 15, 0, 3, 0);
                Log.d((String)TAG, (String)"wsn Setup");
            } else if (this.filename.startsWith("ticket")) {
                this.TscDll.setup(10, 22, 4, 15, 0, 3, 0);
                this.TscDll.sendcommand("^XA\n^MCY^PMN\n^PW406\n^JZY\n^LH0,0^LRN\n^XZ");
                Log.d((String)TAG, (String)"ticket Setup");
            } else if (this.filename.contains("bag")) {
                this.TscDll.setup(55, 33, 4, 15, 0, 3, 0);
                Log.d((String)TAG, (String)"bagLabel Setup");
            } else if (this.filename.startsWith("shipment_display")) {
                Log.d((String)TAG, (String)"prepack Setup");
            } else {
                this.TscDll.setup(101, 152, 4, 15, 0, 3, 0);
                Log.d((String)TAG, (String)"IBL Setup");
            }
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append("File Working : ");
            stringBuilder2.append(file.getName());
            Log.d((String)TAG, (String)stringBuilder2.toString());
            BufferedReader bufferedReader = new BufferedReader((Reader)new FileReader(file));

            Log.d((String)TAG, (String)"File Content");
            do {
                String string2;
                if ((string2 = bufferedReader.readLine()) == null) {
                    bufferedReader.close();
                    this.TscDll.clearbuffer();
                    if (file.delete()) {
                        Log.d(TAG, "File deleted successfully: " + file.getName());
                    } else {
                        Log.d(TAG, "Failed to delete file: " + file.getName());
                    }
                    return;
                }

                Log.d((String)TAG, (String)string2);

                if (string2.length() == 0) continue;

                if (string2.endsWith("XZ")) {
                    String from = regexFinder(varMap.get("from"), string2);
                    String to = regexFinder(varMap.get("to"), string2);
                    String date = regexFinder(varMap.get("date"), string2);
                    String shipmentCount = regexFinder(varMap.get("shipmentCount"), string2);
                    String bagId1 = regexFinder(varMap.get("bagId1"), string2);
                    String bagId2 = regexFinder(varMap.get("bagId2"), string2);
                    String cDest = regexFinder(varMap.get("cDest"), string2);
                    String bagA = regexFinder(varMap.get("bagA"), string2);
                    String sealID = regexFinder(varMap.get("sealID"), string2);
                    String casperID = regexFinder(varMap.get("casperID"), string2);
                    String seller_info = regexFinder(varMap.get("seller_info"), string2);
                    String wildCard1 = regexFinder(varMap.get("wildCard1") , string2);
                    String wildCard2 = regexFinder(varMap.get("wildCard2") , string2);
                    String realGrid = gridMap.get(to);

                    if (!(bagId1.isEmpty())) {
                        Log.d("XXX" , "Template A");
                        generatePrintCommand(bagTemplateA , to , from , date , shipmentCount , bagId1 , bagId2 , cDest , bagA , sealID , casperID , seller_info , wildCard1 , wildCard2 , realGrid );
                        new ApiRequestAsyncTask().execute(bagId1 , sealID , realGrid);
                    } else {
                        Log.d("XXX" , "Template B");
                        generatePrintCommand(bagTemplateB , to , from , date , shipmentCount , bagId1 , bagId2 , cDest , bagA , sealID , casperID , seller_info , wildCard1 , wildCard2 , realGrid );
                        new ApiRequestAsyncTask().execute(bagId2, sealID , realGrid);
                    }
                }
            } while (true);
        }
        catch (Exception exception) {
            Log.d((String)TAG, (String)"There was a Exception " + exception);
            return;
        }
    }

    private String regexFinder(String pattern, String line) {
        String value = "";
        try {
            Pattern regexPattern = Pattern.compile(pattern);
            Matcher matcher = regexPattern.matcher(line);
            if (matcher.find()) {
                if (matcher.groupCount() > 0) {
                    value = matcher.group(1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }


    public void generatePrintCommand(String template , String from , String to ,  String date , String shipmentCount , String bagId1 , String bagId2 , String cDest , String bagA , String sealID , String casperID , String seller_info , String wildCard1 , String wildCard2 , String realGrid){
        Map<String , String> values = new HashMap<>();
        values.put("from" , from);
        values.put("to" , to);
        values.put("date" , date);
        values.put("shipmentCount" , shipmentCount);
        values.put("bagId1", bagId1);
        values.put("bagId2" , bagId2);
        values.put("cDest" , cDest);
        values.put("bagA", bagA);
        values.put("sealID" , sealID);
        values.put("casperID", casperID);
        values.put("seller_info", seller_info);
        values.put("wildCard1" ,wildCard1);
        values.put("wildCard2" , wildCard2);
        values.put("realGrid"  , realGrid);
        values.put("site" , site);

        for (Map.Entry<String, String> entry : values.entrySet()) {
            if (entry.getValue() != null) {
                template = template.replace("%{" + entry.getKey() + "}", entry.getValue());
            } else {
                Log.w("XXX", "Value for key " + entry.getKey() + " is null");
            }
        }
        this.TscDll.sendcommand(template);
        Log.d("DD" , site);
        Log.d("DD" ,  template);
    }

    private class ApiRequestAsyncTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            String bag = params[0];
            String seal = params[1];
            String grid = params[2];
            String apiUrl = gridMap.get("apiUrl");
            if (!(apiUrl == null)) {
                try {
                    Log.d("Url", apiUrl);
                    URL url = new URL(apiUrl);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setDoOutput(true);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    JSONObject jsonPayload = new JSONObject();
                    jsonPayload.put("bag_id", bag);
                    jsonPayload.put("seal_id", seal);
                    jsonPayload.put("grid_code", grid);
                    String payload = jsonPayload.toString();
                    OutputStream os = conn.getOutputStream();
                    os.write(payload.getBytes());
                    os.flush();
                    if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        Log.d(TAG, "API request successful");
                    } else {
                        Log.e(TAG, "API request failed");
                    }
                    conn.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            } else {
                Log.d("XXX" , "BagTrac Api not exists for this site");
            }
            return null;
        }
    }

}