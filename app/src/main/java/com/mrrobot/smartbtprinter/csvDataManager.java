package com.mrrobot.smartbtprinter;

import java.util.HashMap;
import java.util.Map;

public class csvDataManager {
    private static csvDataManager instance;
    private Map<String, String> csvDataMap = new HashMap<>();

    private csvDataManager() {
        // Private constructor to prevent direct instantiation.
    }

    public static csvDataManager getInstance() {
        if (instance == null) {
            instance = new csvDataManager();
        }
        return instance;
    }

    public void loadCsvData(Map<String, String> data) {
        csvDataMap.clear();
        csvDataMap.putAll(data);
    }

    public String getValueForKey(String key) {
        return csvDataMap.get(key);
    }

}