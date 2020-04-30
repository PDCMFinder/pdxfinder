package org.pdxfinder.dataloaders.updog.tablevalidation.error;

import org.pdxfinder.dataloaders.updog.tablevalidation.ColumnReference;

import java.util.Set;

public class DuplicateValueError implements ValidationError {
    private ColumnReference uniqueColumn;
    private Set<String> duplicateValues;
    private String provider;

    DuplicateValueError(ColumnReference uniqueColumn, Set<String> duplicateValues, String provider) {
        this.uniqueColumn = uniqueColumn;
        this.duplicateValues = duplicateValues;
        this.provider = provider;
    }

    private Set<String> getDuplicateValues() {
        return this.duplicateValues;
    }

    @Override
    public String message() {
        return String.format(
            "Error in [%s] for provider [%s]: Duplicates found in column [%s]: %s",
            uniqueColumn.table(),
            provider,
            uniqueColumn.column(),
            getDuplicateValues()
        );
    }

    @Override
    public String toString() {
        return message();
    }
}
