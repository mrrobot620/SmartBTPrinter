package com.mrrobot.smartbtprinter;

public class GridData {
    private String id;
    private String value;

    // Default constructor (required for Firestore)
    public GridData() {
        // Default constructor required for Firestore
    }

    public GridData(String id, String value) {
        this.id = id;
        this.value = value;
    }

    // Getter and setter methods for "id"
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    // Getter and setter methods for "value"
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}