package org.pdxfinder.dataloaders.updog.tablevalidation.error;

import tech.tablesaw.api.Table;

public class EmptyValueError implements ValidationError {
    private String tableName;
    private String columnName;
    private Table invalidRows;
    private String provider;

    EmptyValueError(String tableName, String columnName, Table invalidRows, String provider) {
        this.tableName = tableName;
        this.columnName = columnName;
        this.invalidRows = invalidRows;
        this.provider = provider;
    }

    @Override
    public String message() {
        return String.format(
            "Error in [%s] for provider [%s]: Missing value(s) in required column [%s]:" +
                "%n%s",
            tableName,
            provider,
            columnName,
            getInvalidRows()
        );
    }

    private Table getInvalidRows() {
        return invalidRows;
    }

    @Override
    public String toString() {
        return message();
    }
}
