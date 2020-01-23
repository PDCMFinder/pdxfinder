package org.pdxfinder.dataloaders.updog;

import java.util.Objects;
import java.util.Optional;

public class TableValidationError {
    private String provider;
    private String table;
    private Optional<String> column = Optional.empty();
    private Optional<String> errorType = Optional.empty();

    public String getTable() {
        return table;
    }

    public Optional<String> getColumn() {
        return column;
    }

    public Optional<String> getErrorType() {
        return errorType;
    }

    public String getProvider() {
        return provider;
    }

    private TableValidationError(String table) {
        this.table = table;
    }

    public static TableValidationError create(String table) {
        return new TableValidationError(table);
    }

    public TableValidationError setProvider(String provider) {
        this.provider = provider;
        return this;
    }

    public TableValidationError setColumn(String columnName) {
        this.column = Optional.of(columnName);
        return this;
    }

    public TableValidationError setType(String errorType) {
        this.errorType = Optional.of(errorType);
        return this;
    }

    @Override
    public String toString() {
        return String.format("Error in %s: ", getTable());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TableValidationError that = (TableValidationError) o;
        return table.equals(that.table) &&
            column.equals(that.column);
    }

    @Override
    public int hashCode() {
        return Objects.hash(table, column);
    }
}
