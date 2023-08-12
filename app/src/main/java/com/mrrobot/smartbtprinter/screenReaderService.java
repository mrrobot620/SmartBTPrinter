package com.mrrobot.smartbtprinter;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import com.mrrobot.rscsmartbtprinter.SendGridDataEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class screenReaderService extends AccessibilityService {

    int mDebugDepth = 0;

    @Override
    public void onServiceConnected() {
        Log.wtf("this", "accessbility connected ");
        Toast.makeText(this, "Accessibility Connected", Toast.LENGTH_LONG).show();
    }


    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED) {
            String packageName = event.getPackageName().toString();
            if ("com.android.chrome".equals(packageName)) {
                AccessibilityNodeInfo clickedNode = event.getSource();
                if (clickedNode != null) {
                    traverseAccessibilityTree(clickedNode);
                    clickedNode.recycle();
                }
            }
        }
    }

    private void traverseAccessibilityTree(AccessibilityNodeInfo node) {
        if (node == null) {
            return;
        }

        // Process text within this node
        if (node.getText() != null) {
            Log.d("Accessibility", "Text: " + node.getText());
        }

        // Traverse child nodes
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo childNode = node.getChild(i);
            traverseAccessibilityTree(childNode);
            childNode.recycle();
        }
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

    private String regexFinder(String pattern, String line) {
        String value = null;
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
}
