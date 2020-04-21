package org.pdxfinder.dataloaders.updog.tablevalidation.error;

import org.pdxfinder.dataloaders.updog.tablevalidation.ColumnReference;

public class MissingColumnError implements ValidationError {
    private String tableName;
    private String columnName;
    private String provider;

    MissingColumnError(ColumnReference columnReference, String provider) {
        this.tableName = tableName;
        this.columnName = columnName;
        this.provider = provider;
    }

    @Override
    public String message() {
        return String.format(
            "Error in [%s] for provider [%s]: Missing column: [%s]",
            tableName,
            provider,
            columnName);
    }

    @Override
    public String toString() {
        return message();
    }
}
