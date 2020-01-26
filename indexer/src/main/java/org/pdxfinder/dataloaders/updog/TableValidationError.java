package org.pdxfinder.dataloaders.updog;

import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;

public class TableValidationError {
    private String provider;
    private String table;
    private Optional<String> column = Optional.empty();
    private Optional<String> errorType = Optional.empty();
    private Optional<String> description = Optional.empty();

    public enum Type {
        MISSING_COL("Missing column"),
        MISSING_FILE("Missing file"),
        MISSING_REQ_VALUE("Missing required value");

        private String name;
        Type(String name) {
            this.name = name;
        }

        @Override public String toString() {
            return name;
        }

    }

    public String getTable() {
        return table;
    }

    public Optional<String> getColumn() {
        return column;
    }

    public Optional<String> getDescription() {
        return description;
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

    public static TableValidationError missingColumn(String tableName, String columnName) {
        return new TableValidationError(tableName).setType(Type.MISSING_COL).setColumn(columnName);
    }

    public TableValidationError setDescription(String description) {
        this.description = Optional.of(description);
        return this;
    }

    public TableValidationError setProvider(String provider) {
        this.provider = provider;
        return this;
    }

    public TableValidationError setColumn(String columnName) {
        this.column = Optional.of(columnName);
        return this;
    }

    public TableValidationError setType(Type type) {
        this.errorType = Optional.of(type.toString());
        return this;
    }

    @Override
    public String toString() {
        StringJoiner message = new StringJoiner("");
        message.add(String.format("Error in %s: ", getTable()));
        String type = getErrorType().orElse("");
        if (type.equals(Type.MISSING_COL.toString())) {
            message.add(String.format("Missing column: %s", getColumn().orElse("(not specified)")));
        } else {
            message.add(type);
        }
        message.add(getDescription().orElse(""));
        return message.toString();
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
