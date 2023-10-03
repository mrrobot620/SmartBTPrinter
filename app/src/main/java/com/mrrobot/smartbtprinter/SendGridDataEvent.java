package com.mrrobot.rscsmartbtprinter;

import android.util.Log;

public class SendGridDataEvent {
    private String gridData;

    public SendGridDataEvent(String gridData) {
        this.gridData = gridData;
    }

    public String getGridData() {
        Log.d("HH" ,"In SendGridDataEvent: " +gridData);
        return gridData;
    }
}
