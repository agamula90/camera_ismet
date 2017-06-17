package com.ismet.usbcamera.table;

public class RowItem {
    public final int row;
    private final String columnValues[];

    public RowItem(int row, String columnValues[]) {
        this.columnValues = columnValues;
        this.row = row;
    }

    String[] getColumnValues() {
        return columnValues;
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj instanceof RowItem && ((RowItem) obj).row == row;
    }
}
