package com.mrrobot.smartbtprinter;

import static androidx.core.view.ViewCompat.setBackground;

import android.accessibilityservice.AccessibilityService;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.PixelFormat;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.View;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.LinkedList;
import java.util.Queue;


public class screenReaderService extends AccessibilityService {

    int mDebugDepth = 0;

    private WindowManager windowManager;
    private boolean isOverlayVisible = false;
    private Handler handler;
    private String lastEnteredShipmentID = null;


    private OverlayView overlayView;



    @Override
    public void onServiceConnected() {
        Log.wtf("XXX", "Accessbility connected ");
        Toast.makeText(this, "Accessibility Connected", Toast.LENGTH_LONG).show();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        AccessibilityNodeInfo mNodeInfo = event.getSource();
        findTextWithStartingWords(mNodeInfo, "Keep this bag to");
        findTextWithStartingWords(mNodeInfo, "Rescan shipment to remove");
        findTextWithStartingWords(mNodeInfo , "successfully");
    }


    private String findTextWithStartingWords(AccessibilityNodeInfo mNodeInfo, String targetStartingWords) {
        if (mNodeInfo == null) return null;
        CharSequence nodeText = mNodeInfo.getText();
        if (nodeText != null && nodeText.toString().startsWith(targetStartingWords)) {
            return nodeText.toString();
        }

        if (mNodeInfo.getChildCount() < 1) return null;

        for (int i = 0; i < mNodeInfo.getChildCount(); i++) {
            String foundText = findTextWithStartingWords(mNodeInfo.getChild(i), targetStartingWords);
            if (foundText != null) {
                if (foundText.startsWith("Keep this bag to")) {
                    Log.d("XXX" , "Found the word:  " + foundText);
                    showOverlay(0x80FF0000 , 10000);
                }
                else if (foundText.contains("successfully")){
                    showOverlay(0x8000FF00 , 1000);
                }
                else if(foundText.startsWith("Rescan shipment to remove")) {
                    Log.d("XXX" , "Found the word:  " + foundText);
                    String shipmentId= findShipment(mNodeInfo , "FM");
//                    Log.d("XXX", "Found next line: " + nextLine);
//                    String shipmentId = extractShipmentId(nextLine);
                    Log.d("XXX" , "Shipment ID:  " + shipmentId);
                    if (!(shipmentId.equals(lastEnteredShipmentID))){
                    shipmentIdChecker(shipmentId);}
                    else {
                        Log.d("XXX", "Dialog not shown for repeated shipment ID: " + shipmentId);
                    }
                } else if (foundText.startsWith("Remove Shipment")) {
                    Log.d("XXX" , "Found Text: " + foundText);
                    showOverlay(0x80FF0000 , 10000);
                }

            }
        }
        return null;
    }


    @Override
    public void onInterrupt() {
        return;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "Stopped", Toast.LENGTH_LONG).show();
    }

    private void showOverlay(int color , int time) {
        if (!isOverlayVisible) {
            final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                            | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                            | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                    PixelFormat.TRANSLUCENT);
            params.gravity = Gravity.START | Gravity.TOP;

            ColorDrawable colorDrawable = new ColorDrawable(color);

            OverlayView overlayView = new OverlayView(getApplicationContext(), colorDrawable);
            windowManager.addView(overlayView, params);

            final int duration = time;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    windowManager.removeViewImmediate(overlayView);
                    isOverlayVisible = false;
                }
            }, duration);
            isOverlayVisible = true;
        }
    }

    private class OverlayView extends View {
        private Paint paint;

        public OverlayView(Context context, Drawable drawable) {
            super(context);
            setWillNotDraw(false);
            paint = new Paint();
            if (drawable != null) {
                setBackground(drawable);
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (getBackground() instanceof ColorDrawable) {
                ColorDrawable colorDrawable = (ColorDrawable) getBackground();
                paint.setColor(colorDrawable.getColor());
                canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
            }
        }
    }

  private void shipmentIdChecker(String shipmentID) {
        showOverlay(0x80FF0000 , 15000);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Scan QR Code");
        alertDialogBuilder.setMessage("Check");

      final EditText input = new EditText(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        input.setLayoutParams(layoutParams);
        alertDialogBuilder.setView(input);


      input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);// Disable multiline input
      input.setSingleLine(true);
      input.setMaxLines(1);

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY);
        alertDialog.setCancelable(false);
      alertDialog.show();


      input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
          @Override
          public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
              if (actionId == EditorInfo.IME_ACTION_DONE) {
                  String userInput = input.getText().toString().trim();
                  if (userInput.isEmpty()) {
                      Log.d("XXX" , "user Input:  " + userInput);
                      Toast.makeText(getApplicationContext(), "Please enter the shipment ID.", Toast.LENGTH_SHORT).show();
                      input.requestFocus();
                  } else if (userInput.equalsIgnoreCase(shipmentID)) {
                      Log.d("XXX" , "user Input:  " + userInput);

                      lastEnteredShipmentID = userInput;
                      alertDialog.dismiss();
                  } else {
                      Log.d("XXX" , "user Input:  " + userInput);

                      Toast.makeText(getApplicationContext(), "Incorrect shipment ID, please try again.", Toast.LENGTH_SHORT).show();
                      input.requestFocus();
                  }
              }
              return false;
          }
      });

  }

    private String findShipment(AccessibilityNodeInfo mNodeInfo, String targetStartingWords) {
        if (mNodeInfo == null) return null;
        CharSequence nodeText = mNodeInfo.getText();
        Log.d("XXX" ,"hahahahah22   " + nodeText);
        if (nodeText != null && nodeText.toString().startsWith(targetStartingWords)) {
            Log.d("XXX" ,"hahahahah   " + nodeText);
            return nodeText.toString();
        }
        for (int i = 0; i < mNodeInfo.getChildCount(); i++) {
            String foundText = findShipment(mNodeInfo.getChild(i), targetStartingWords);
            if (foundText != null) {
                Log.d("XXX" , "HHAH" + foundText);
                return foundText;

            }
        }
        return null; // Return null if targetStartingWords are not found in the subtree
    }


    private String extractShipmentId(String line) {
        String[] parts = line.split("\\s+"); // Split by whitespace
        if (parts.length >= 3) {
            return parts[2].trim(); // Third word
        }
        return null;
    }

}