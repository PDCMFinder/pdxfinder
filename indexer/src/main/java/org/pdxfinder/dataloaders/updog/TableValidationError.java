package org.pdxfinder.dataloaders.updog;

import org.apache.commons.lang3.tuple.Pair;
import tech.tablesaw.api.Table;

import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;

public class TableValidationError {
    private String table;
    private String provider;
    private Type errorType;
    private String column;
    private Pair<Pair<String, String>, Pair<String, String>> relation;
    private Table invalidRows;
    private String description;

    public enum Type {
        GENERIC("Generic error"),
        MISSING_COL("Missing column"),
        MISSING_TABLE("Missing table"),
        MISSING_REQ_VALUE("Missing required value(s)"),
        DUPLICATE_VALUE("Duplicate value(s)"),
        BROKEN_RELATION("Broken relation");

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
        return Optional.ofNullable(column);
    }

    public Optional<String> getDescription() {
        return Optional.ofNullable(description);
    }

    public Optional<Type> getErrorType() {
        return Optional.ofNullable(errorType);
    }

    public Optional<String> getProvider() {
        return Optional.ofNullable(provider);
    }

    public Optional<Pair<Pair<String, String>, Pair<String, String>>> getRelation() {
        return Optional.ofNullable(relation);
    }

    public Optional<Table> getInvalidRows() {
        return Optional.ofNullable(invalidRows);
    }

    private TableValidationError(String table) {
        this.table = table;
    }

    public static TableValidationError generic(String table) {
        return new TableValidationError(table).setType(Type.GENERIC);
    }

    public static TableValidationError missingFile(String tableName) {
        return new TableValidationError(tableName).setType(Type.MISSING_TABLE);
    }

    public static TableValidationError missingColumn(String tableName, String columnName) {
        return new TableValidationError(tableName).setType(Type.MISSING_COL).setColumn(columnName);
    }

    public static TableValidationError missingRequiredValue(
        String tableName,
        String columnName,
        Table invalidRows
    ) {
        return new TableValidationError(tableName)
            .setType(Type.MISSING_REQ_VALUE)
            .setColumn(columnName)
            .setInvalidRows(invalidRows);
    }

    public static TableValidationError duplicateValue(
        String tableName,
        String columnName,
        Set duplicateValues
    ) {
        return new TableValidationError(tableName)
            .setType(Type.DUPLICATE_VALUE)
            .setColumn(columnName)
            .setDescription(duplicateValues.toString());
    }

    public static TableValidationError brokenRelation(
        String tableName,
        Pair<Pair<String, String>, Pair<String, String>> relation,
        Table invalidRows
    ) {
        return new TableValidationError(tableName)
            .setType(Type.BROKEN_RELATION)
            .setRelation(relation)
            .setInvalidRows(invalidRows);
    }

    public TableValidationError setDescription(String description) {
        this.description = description;
        return this;
    }

    private TableValidationError setInvalidRows(Table invalidRows) {
        this.invalidRows = invalidRows;
        return this;
    }

    public TableValidationError setProvider(String provider) {
        this.provider = provider;
        return this;
    }

    private TableValidationError setColumn(String columnName) {
        this.column = columnName;
        return this;
    }

    private TableValidationError setType(Type type) {
        this.errorType = type;
        return this;
    }

    public TableValidationError setRelation(
        Pair<Pair<String, String>, Pair<String, String>> relation
    ) {
        this.relation = relation;
        return this;
    }

    @Override
    public String toString() {
        StringJoiner message = new StringJoiner("");
        message.add(String.format("Error in [%s]", getTable()));
        getProvider().ifPresent(s -> message.add(String.format(" for provider [%s]", s)));
        message.add(": ");
        Type type = getErrorType().orElse(Type.GENERIC);
        switch(type) {
            case MISSING_COL:
                message.add(String.format("Missing column: [%s]", getColumn().orElse("not specified")));
                break;
            case MISSING_REQ_VALUE:
                message.add(String.format(
                    "Missing value(s) in required column [%s]",
                    getColumn().orElse("not specified")));
                break;
            case BROKEN_RELATION:
                message.add(String.format(
                    "Broken relation [%s]", relation.toString()));
                break;
            case GENERIC:
                message.add("Generic error");
                break;
        }
        getDescription().ifPresent(s -> message.add(String.format(": %s", s)));
        getInvalidRows().ifPresent( t -> message.add(String.format(":%n%s", t.toString())));
        return message.toString();
    }

    private int toOneBasedIndex(int number) {
        return number + 1;
    }

}
