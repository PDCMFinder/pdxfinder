package org.pdxfinder.dataloaders.updog;

import org.apache.commons.lang3.tuple.Pair;
import tech.tablesaw.api.Row;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;

public class TableValidationError {
    private String table;
    private Optional<String> provider = Optional.empty();
    private Optional<String> errorType = Optional.empty();
    private Optional<String> column = Optional.empty();
    private Optional<Row> row = Optional.empty();
    private Optional<String> description = Optional.empty();

    public enum Type {
        MISSING_COL("Missing column"),
        MISSING_TABLE("Missing table"),
        MISSING_REQ_VALUE("Missing required value(s) found"),
        DUPLICATE_VALUE("Duplicate value(s) found"),
        BROKEN_RELATION("Broken relation found");

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

    public Optional<String> getProvider() {
        return provider;
    }

    public Optional<Row> getRow() {
        return row;
    }

    private TableValidationError(String table) {
        this.table = table;
    }

    public static TableValidationError create(String table) {
        return new TableValidationError(table);
    }

    public static TableValidationError missingFile(String tableName) {
        return new TableValidationError(tableName).setType(Type.MISSING_TABLE);
    }

    public static TableValidationError missingColumn(String tableName, String columnName) {
        return new TableValidationError(tableName).setType(Type.MISSING_COL).setColumn(columnName);
    }

    public static TableValidationError missingRequiredValue(String tableName, String columnName, Row row) {
        return new TableValidationError(tableName)
            .setType(Type.MISSING_REQ_VALUE)
            .setColumn(columnName)
            .setRow(row)
            .setDescription(row.toString());
    }

    public static TableValidationError duplicateValue(String tableName, String columnName, Set duplicateValues) {
        return new TableValidationError(tableName)
            .setType(Type.DUPLICATE_VALUE)
            .setColumn(columnName)
            .setDescription(duplicateValues.toString());
    }

    public static TableValidationError brokenRelation(
        Pair<Pair<String, String>, Pair<String, String>> relation,
        String description
    ) {
        return new TableValidationError(relation.toString())
            .setType(Type.BROKEN_RELATION)
            .setDescription(description);
    }

    public TableValidationError setDescription(String description) {
        this.description = Optional.of(description);
        return this;
    }

    public TableValidationError setProvider(String provider) {
        this.provider = Optional.of(provider);
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

    private TableValidationError setRow(Row row) {
        this.row = Optional.of(row);
        return this;
    }

    @Override
    public String toString() {
        StringJoiner message = new StringJoiner("");
        message.add(String.format("Error in [%s]", getTable()));
        if (getProvider().isPresent()) {
            message.add(String.format(" for provider [%s]", getProvider().get()));
        }
        message.add(": ");
        if (getErrorType().isPresent()) {
            if (getErrorType().get().equals(Type.MISSING_COL.toString())) {
                message.add(String.format("Missing column: [%s]", getColumn().orElse("not specified")));
            } else if (getErrorType().get().equals(Type.MISSING_REQ_VALUE.toString())) {
                message.add(String.format("Missing value in required column: [%s]", getColumn().get()));
            } else {
                message.add(getErrorType().get());
            }
        }
        message.add(formatDescription());
        return message.toString();
    }

    private int toOneBasedIndex(int number) {
        return number + 1;
    }

    private String formatDescription() {
        if (getDescription().isPresent()) {
            return String.format("%n%s", getDescription().get());
        } else {
            return"";
        }
    }

}
