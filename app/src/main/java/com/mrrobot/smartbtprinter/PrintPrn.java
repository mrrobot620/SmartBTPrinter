package com.mrrobot.smartbtprinter;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import com.example.tscdll.TSCActivity;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.sql.Connection;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;



public class PrintPrn implements Runnable {
    private static final String TAG = "PrintPrn";
    private TSCActivity TscDll;
    private final File download_path;
    private String filename;
    private final String watchFolder;

    private final String site;
    private LinkedHashMap<String ,String> gridMap;
    private String realGrid1;

    private Connection sqlConnection;



    PrintPrn(String site) {
        this.site = site;
        File file;
        this.download_path = file = Environment.getExternalStoragePublicDirectory((String)Environment.DIRECTORY_DOWNLOADS);
        this.watchFolder = file.getAbsolutePath();
    }

    PrintPrn(String site ,LinkedHashMap<String ,String> gridMap , String string2,  TSCActivity tSCActivity , Connection sqlConnection) {
        this.site = site;
        this.gridMap = gridMap;
        File file;
        this.download_path = file = Environment.getExternalStoragePublicDirectory((String)Environment.DIRECTORY_DOWNLOADS);
        this.watchFolder = file.getAbsolutePath();
        this.filename = string2;
        this.TscDll = tSCActivity;
        this.sqlConnection = sqlConnection;
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
            } else if (this.filename.startsWith("bag")) {
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
                    file.delete();
                    return;
                }

                Log.d((String)TAG, (String)string2);

                if (string2.length() == 0) continue;

                if (string2.endsWith("XZ")) {
                    String from = regexFinder("From : ([A-Za-z0-9_]+)", string2);
                    Log.d("AA", "Value of From: " + from);

                    String to = regexFinder("To : ([A-Za-z0-9_]+)", string2);
                    Log.d("AA", "Value of to: " + to);

                    String date = regexFinder("\\^FT\\d+,\\d+\\^A0N,\\d+,\\d+\\^CI\\d+\\^FD(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d+)\\^FS", string2);
                    Log.d("AA", "Value of Date: " + date);

                    String shipmentCount = regexFinder("#(\\d{1,3})", string2);
                    Log.d("AA", "Value of the Shipment Count: " + shipmentCount);

                    String bagId1 = regexFinder("\\^BY2,3,53\\^FT32,165\\^BCN,,Y,N,N,A\\^FD(.*?)\\^FS", string2);
                    Log.d("AA", "Value of the BagID1: " + bagId1);

                    String bagId2 = regexFinder("\\^BY1,3,53\\^FT32,165\\^BCN,,Y,N,N,A\\^A0N,21,21\\^FD(.*?)\\^FS", string2);
                    Log.d("AA", "Value of the BagID2: " + bagId2);

                    String cDest = regexFinder("\\^FT24,222\\^A0N,34,33\\^CI28\\^FD(.*?)\\^FS", string2);
                    Log.wtf("AA", "Value of the Complete Destination: " + cDest);

                    String bagA = regexFinder("\\^FT362,220\\^A0N,20,20\\^CI28\\^FD(.*?)\\^FS", string2);
                    Log.d("AA", "Value of the Bag Type: " + bagA);

                    String sealId = regexFinder("\\^FT24,252\\^A0N,20,20\\^CI28\\^FD(.*?)\\^FS", string2);
                    Log.d("AA", "Value of the Seal ID: " + sealId);

                    String casperID = regexFinder("\\^FT240,252\\^A0N,20,20\\^CI28\\^FDCreated by :([^\\^]+)\\^FS", string2);
                    Log.d("AA", "Value of the Casper ID: " + casperID);

                    String seller_info = regexFinder("\\^FT260,252\\^AON,20,20\\^CI28\\^FD(.*?)\\^FS", string2);
                    Log.d("AA", "Value of the Seller-Info: " + seller_info);

                    String realGrid = gridMap.get(to);
                    Log.wtf("AA", "Value of the Complete Destination: " + cDest);


                    if (!(bagId1.isEmpty())) {
                        String PrintString = ("\u0010CT~~CD,~CC^~CT~^XA^MMT^PW446^LL264^LS0^FT200,44^A0N,20,20^CI28^FDFrom : " + from + "^FS^CI27^FT200,69^A0N,20,20^CI28^FDTo : " + to + "^FS^CI27^FT200,94^A0N,20,20^CI28^FD" + date + "^FS^CI27^FT72,100^A0N,20,20^CI28^FD#" + shipmentCount + "^FS^CI27^FT375,222^A0N,20,20^CI28^FD" + bagA + "^FS^CI27^FT270,252^A0N,17,17^CI28^FDCreated by :" + casperID + "^FS^CI27^FPH,1^FT110,222^A0N,27,27^CI28^FD" + cDest + "^FS^CI27^FT230,222^A0N,19,19^CI28^FD" + seller_info + "^FS^CI27^FT270,220^A0N,20,20^CI28^FD^FS^CI27^FT110,252^A0N,18,18^CI28^FD" + sealId + "^FS^CI27^BY2,3,53^FT32,165^BCN,,Y,N,N,A^FD" + bagId1 + "^FS^FO16,8\n" +
                                "^GFA,645,1440,20,:Z64:eJzVkz9Lw0AYxi+WQ1E0dQjtYMG5Djp2awS7t5Cji/kOFezepRB06FcIuJSb/AaJg+BooW49CLoUBbvGVnK+l17SS8Q/o77Lvffyy3PPc0kQ+ge10UvbzaTFZjpLWz1IZ2mrcMa3nL/S87/jfnvuD3ruF/62OI/0QH/mnE897nEOz2DGGPYrN7Dc26zCGAjqAAyC+hUsIZec0SZtbBoaaZMWacPOTDjeU7nK2LL7vo0si4zYiWGPzdhQfRBE4KDwNkfo0F0aMvDxOXTa2bn0p/eAe3mPuTDegp6PKvh2LLjRROaNOR5muFiPNAXXsmR0fepwwR1yvtJ7eGCY3SObsZWeyAEcl1zsr9vtCn+wnHUSf+L+HqOUi/UYG2P/VGMnDaG3LfWGehAV5qiwUPyBKaJZS3/rMi+8N68XZvMi8T4m2bzA1Z8Szk31jOMO3F/q785xBoE+jZxLJS8h2McjRkg2b1BYqDkMApyptWBZ3d+SC1VOfC99X5uo97dGKR24iB7wuTuUXFLabhHlC3A6pPRCxUCWjUiWm3uR97yfGWnNUqtk5vRms5dXSh31WNRoNpr5c6u16lF+VoTKz8q1ci0/2yvufdLbud4J8jNsKn+drDV3083P/kB9AJctGes=:8B5B^FT18,235^A0N,17,17^FH\\^CI28^FD" + site + "^FS^CI27\n" +
                                "^FT50,242^A0N,40,40^FH\\^CI28^FD" + realGrid + "^FS^CI27^FO9,194^GB91,65,3^FS^FO9,225^FT245,225^A0N,17,18^FH\\^CI28^FD ^FS^CI27^PQ1,0,1,Y^XZ");
                        this.TscDll.sendcommand(PrintString);

                    } else {
                        String PrintString = ("\u0010CT~~CD,~CC^~CT~^XA^MMT^PW446^LL264^LS0^FT200,44^A0N,20,20^CI28^FDFrom : " + from + "^FS^CI27^FT200,69^A0N,20,20^CI28^FDTo : " + to + "^FS^CI27^FT200,94^A0N,20,20^CI28^FD" + date + "^FS^CI27^FT72,100^A0N,20,20^CI28^FD#" + shipmentCount + "^FS^CI27^FT375,222^A0N,20,20^CI28^FD" + bagA + "^FS^CI27^FT270,252^A0N,17,17^CI28^FDCreated by :" + casperID + "^FS^CI27^FPH,1^FT110,222^A0N,27,27^CI28^FD" + cDest + "^FS^CI27^FT230,222^A0N,19,19^CI28^FD" + seller_info + "^FS^CI27^FT270,220^A0N,20,20^CI28^FD^FS^CI27^FT110,252^A0N,18,18^CI28^FD" + sealId + "^FS^CI27^BY1,3,53^FT32,165^BCN,,Y,N,N,A,A^A0N,21,21^FD" + bagId2 + "^FS^FO16,8\n" +
                                "^GFA,645,1440,20,:Z64:eJzVkz9Lw0AYxi+WQ1E0dQjtYMG5Djp2awS7t5Cji/kOFezepRB06FcIuJSb/AaJg+BooW49CLoUBbvGVnK+l17SS8Q/o77Lvffyy3PPc0kQ+ge10UvbzaTFZjpLWz1IZ2mrcMa3nL/S87/jfnvuD3ruF/62OI/0QH/mnE897nEOz2DGGPYrN7Dc26zCGAjqAAyC+hUsIZec0SZtbBoaaZMWacPOTDjeU7nK2LL7vo0si4zYiWGPzdhQfRBE4KDwNkfo0F0aMvDxOXTa2bn0p/eAe3mPuTDegp6PKvh2LLjRROaNOR5muFiPNAXXsmR0fepwwR1yvtJ7eGCY3SObsZWeyAEcl1zsr9vtCn+wnHUSf+L+HqOUi/UYG2P/VGMnDaG3LfWGehAV5qiwUPyBKaJZS3/rMi+8N68XZvMi8T4m2bzA1Z8Szk31jOMO3F/q785xBoE+jZxLJS8h2McjRkg2b1BYqDkMApyptWBZ3d+SC1VOfC99X5uo97dGKR24iB7wuTuUXFLabhHlC3A6pPRCxUCWjUiWm3uR97yfGWnNUqtk5vRms5dXSh31WNRoNpr5c6u16lF+VoTKz8q1ci0/2yvufdLbud4J8jNsKn+drDV3083P/kB9AJctGes=:8B5B^FT18,235^A0N,17,17^FH\\^CI28^FD" + site + "^FS^CI27\n" +
                                "^FT50,242^A0N,40,40^FH\\^CI28^FD" + realGrid + "^FS^CI27^FO9,194^GB91,65,3^FS^FO9,225^FT245,225^A0N,17,18^FH\\^CI28^FD ^FS^CI27^PQ1,0,1,Y^XZ");
                        this.TscDll.sendcommand(PrintString);


                    }
                }
            } while (true);
        }
        catch (Exception exception) {
            Log.d((String)TAG, (String)"There was a Exception");
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

    private class ApiRequestAsyncTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            String bag = params[0];
            String seal = params[1];
            String grid = params[2];
            String apiUrl = gridMap.get("apiUrl");
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
                    // Handle success response
                    Log.d(TAG, "API request successful");
                } else {
                    // Handle failure response
                    Log.e(TAG, "API request failed");
                }
                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
//This cor commit
}