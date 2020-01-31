package org.pdxfinder.dataloaders.updog;

import org.apache.commons.lang3.tuple.Pair;
import tech.tablesaw.api.Table;

import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;

public class ValidationError {
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

    private ValidationError(String table) {
        this.table = table;
    }

    public static ValidationError generic(String table) {
        return new ValidationError(table).setType(Type.GENERIC);
    }

    public static ValidationError missingFile(String tableName) {
        return new ValidationError(tableName).setType(Type.MISSING_TABLE);
    }

    public static ValidationError missingColumn(String tableName, String columnName) {
        return new ValidationError(tableName).setType(Type.MISSING_COL).setColumn(columnName);
    }

    public static ValidationError missingRequiredValue(
        String tableName,
        String columnName,
        Table invalidRows
    ) {
        return new ValidationError(tableName)
            .setType(Type.MISSING_REQ_VALUE)
            .setColumn(columnName)
            .setInvalidRows(invalidRows);
    }

    public static ValidationError duplicateValue(
        String tableName,
        String columnName,
        Set duplicateValues
    ) {
        return new ValidationError(tableName)
            .setType(Type.DUPLICATE_VALUE)
            .setColumn(columnName)
            .setDescription(duplicateValues.toString());
    }

    public static ValidationError brokenRelation(
        String tableName,
        Pair<Pair<String, String>, Pair<String, String>> relation,
        Table invalidRows
    ) {
        return new ValidationError(tableName)
            .setType(Type.BROKEN_RELATION)
            .setRelation(relation)
            .setInvalidRows(invalidRows);
    }

    public ValidationError setDescription(String description) {
        this.description = description;
        return this;
    }

    private ValidationError setInvalidRows(Table invalidRows) {
        this.invalidRows = invalidRows;
        return this;
    }

    public ValidationError setProvider(String provider) {
        this.provider = provider;
        return this;
    }

    private ValidationError setColumn(String columnName) {
        this.column = columnName;
        return this;
    }

    private ValidationError setType(Type type) {
        this.errorType = type;
        return this;
    }

    public ValidationError setRelation(
        Pair<Pair<String, String>, Pair<String, String>> relation
    ) {
        this.relation = relation;
        return this;
    }

    private String prettyPrintRelation(
        Pair<Pair<String, String>, Pair<String, String>> relation
    ) {
        return String.format(
            "(%s) %s -> %s (%s)",
            relation.getLeft().getKey(),
            relation.getLeft().getValue(),
            relation.getRight().getValue(),
            relation.getRight().getKey());
    }

    @Override
    public String toString() {
        String notSpecified = "not specified";
        StringJoiner message = new StringJoiner("");
        message.add(String.format("Error in [%s]", getTable()));
        getProvider().ifPresent(s -> message.add(String.format(" for provider [%s]", s)));
        message.add(": ");
        Type type = getErrorType().orElse(Type.GENERIC);
        switch(type) {
            case MISSING_TABLE:
                message.add("Missing required table");
                break;
            case MISSING_COL:
                message.add(String.format("Missing column: [%s]", getColumn().orElse(notSpecified)));
                break;
            case MISSING_REQ_VALUE:
                message.add(String.format(
                    "Missing value(s) in required column [%s]",
                    getColumn().orElse(notSpecified)));
                break;
            case DUPLICATE_VALUE:
                message.add(String.format(
                    "Duplicate value(s) in required column [%s]",
                    getColumn().orElse(notSpecified)));
                break;
            case BROKEN_RELATION:
                message.add(String.format(
                    "Broken relation [%s]", prettyPrintRelation(relation)));
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
