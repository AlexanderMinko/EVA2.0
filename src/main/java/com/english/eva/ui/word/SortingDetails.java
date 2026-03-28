package com.english.eva.ui.word;

public class SortingDetails {

    private String columnName;
    private String direction;

    public SortingDetails() {}

    public SortingDetails(String columnName, String direction) {
        this.columnName = columnName;
        this.direction = direction;
    }

    public String getColumnName() { return columnName; }
    public void setColumnName(String columnName) { this.columnName = columnName; }

    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }
}
