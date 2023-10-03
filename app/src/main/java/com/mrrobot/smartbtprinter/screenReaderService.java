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
        AccessibilityNodeInfo mNodeInfo = event.getSource();
        findTextWithStartingWords(mNodeInfo, "Keep this bag to");
        findTextWithStartingWords(mNodeInfo, "Connection not");
        findTextWithStartingWords(mNodeInfo, "Item already staged");
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

                    // This is the code here I want to do the following instead of the line below , alos I think using regex would be better than substring

//                    String gridCode = foundText.replaceAll("[^0-9]", "");

                    String gridCode = regexFinder(":\\s*(\\w+)" , foundText);

                    EventBus.getDefault().post(new SendGridDataEvent(gridCode));

                }
                else if(foundText.startsWith("Connection not")) {
                    Log.d("HH", "Event Bus In ScreenReader: No Connection Found ");
                    EventBus.getDefault().post(new com.mrrobot.rscsmartbtprinter.SendGridDataEvent("404"));
                }
                else if(foundText.startsWith("Item already staged")){
                    Log.d("HH" , "Event Bus in Screen Reader: Item Already Staged");
                    EventBus.getDefault().post(new SendGridDataEvent("405"));
                }
            }
        }
        return null; // Moved return statement outside of the loop
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
