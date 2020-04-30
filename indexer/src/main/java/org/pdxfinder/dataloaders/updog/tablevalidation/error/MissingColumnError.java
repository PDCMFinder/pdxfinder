package org.pdxfinder.dataloaders.updog.tablevalidation.error;

import org.pdxfinder.dataloaders.updog.tablevalidation.ColumnReference;

public class MissingColumnError implements ValidationError {
    private ColumnReference columnReference;
    private String provider;

    MissingColumnError(ColumnReference columnReference, String provider) {
        this.columnReference = columnReference;
        this.provider = provider;
    }

    @Override
    public String message() {
        return String.format(
            "Error in [%s] for provider [%s]: Missing column: [%s]",
            columnReference.table(),
            provider,
            columnReference.column());
    }

    @Override
    public String toString() {
        return message();
    }
}
