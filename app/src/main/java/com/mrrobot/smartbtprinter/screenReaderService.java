package com.mrrobot.smartbtprinter;

import static androidx.core.view.ViewCompat.setBackground;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;
import org.greenrobot.eventbus.EventBus;
import android.accessibilityservice.AccessibilityService;
import android.graphics.PixelFormat;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.Toast;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.view.View;


public class screenReaderService extends AccessibilityService {

    int mDebugDepth = 0;

    private WindowManager windowManager;
    private boolean isOverlayVisible = false;
    private Handler handler;

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
        findTextWithStartingWords(mNodeInfo, "No Connection Found");
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
                    Log.d("XXX" , "Found the word");
                    showOverlay();
                }
                else if(foundText.startsWith("No Connection")) {
                    Log.d("XXX", "Connection Not Found");
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

    private void showOverlay() {
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

            final int duration = 5000; // 5 seconds

            ColorDrawable colorDrawable = new ColorDrawable(0x80FF0000);

            OverlayView overlayView = new OverlayView(getApplicationContext(), colorDrawable);
            windowManager.addView(overlayView, params);

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
}