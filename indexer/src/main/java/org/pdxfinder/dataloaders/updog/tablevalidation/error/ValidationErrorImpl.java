package org.pdxfinder.dataloaders.updog.tablevalidation.error;

import org.pdxfinder.dataloaders.updog.tablevalidation.Relation;
import tech.tablesaw.api.Table;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;

public class ValidationErrorImpl implements ValidationError {
    private String table;
    private String provider;
    private Type errorType;
    private String column;
    private Relation relation;
    private Table invalidRows;
    private String description;

    public String message() {
        return toString();
    }

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

    public Optional<Relation> getRelation() {
        return Optional.ofNullable(relation);
    }

    public Optional<Table> getInvalidRows() {
        return Optional.ofNullable(invalidRows);
    }

    private ValidationErrorImpl(String table) {
        this.table = table;
    }

    public static ValidationErrorImpl generic(String table) {
        return new ValidationErrorImpl(table).setType(Type.GENERIC);
    }

    public static ValidationErrorImpl missingFile(String tableName) {
        return new ValidationErrorImpl(tableName).setType(Type.MISSING_TABLE);
    }

    public static ValidationErrorImpl missingColumn(String tableName, String columnName) {
        return new ValidationErrorImpl(tableName).setType(Type.MISSING_COL).setColumn(columnName);
    }

    public static ValidationErrorImpl missingRequiredValue(
        String tableName,
        String columnName,
        Table invalidRows
    ) {
        return new ValidationErrorImpl(tableName)
            .setType(Type.MISSING_REQ_VALUE)
            .setColumn(columnName)
            .setInvalidRows(invalidRows);
    }

    public static ValidationErrorImpl duplicateValue(
        String tableName,
        String columnName,
        Set duplicateValues
    ) {
        return new ValidationErrorImpl(tableName)
            .setType(Type.DUPLICATE_VALUE)
            .setColumn(columnName)
            .setDescription(duplicateValues.toString());
    }

    public static ValidationErrorImpl brokenRelation(
        String tableName,
        Relation relation,
        Table invalidRows
    ) {
        return new ValidationErrorImpl(tableName)
            .setType(Type.BROKEN_RELATION)
            .setRelation(relation)
            .setInvalidRows(invalidRows);
    }

    public ValidationErrorImpl setDescription(String description) {
        this.description = description;
        return this;
    }

    private ValidationErrorImpl setInvalidRows(Table invalidRows) {
        this.invalidRows = invalidRows;
        return this;
    }

    public ValidationErrorImpl setProvider(String provider) {
        this.provider = provider;
        return this;
    }

    private ValidationErrorImpl setColumn(String columnName) {
        this.column = columnName;
        return this;
    }

    private ValidationErrorImpl setType(Type type) {
        this.errorType = type;
        return this;
    }

    public ValidationErrorImpl setRelation(
        Relation relation
    ) {
        this.relation = relation;
        return this;
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
                    "Duplicates found in column [%s]",
                    getColumn().orElse(notSpecified)));
                break;
            case BROKEN_RELATION:
                message.add(String.format("Broken relation [%s]", relation));
                break;
            case GENERIC:
                message.add("Generic error");
                break;
        }
        getDescription().ifPresent(s -> message.add(String.format(": %s", s)));
        getInvalidRows().ifPresent( t -> message.add(String.format(":%n%s", t.toString())));
        return message.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ValidationErrorImpl that = (ValidationErrorImpl) o;
        return Objects.equals(getTable(), that.getTable()) &&
            Objects.equals(getProvider(), that.getProvider()) &&
            Objects.equals(getErrorType(), that.getErrorType()) &&
            Objects.equals(getColumn(), that.getColumn()) &&
            Objects.equals(getRelation(), that.getRelation()) &&
            Objects.equals(getInvalidRows(), that.getInvalidRows()) &&
            Objects.equals(getDescription(), that.getDescription());
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            getTable(),
            getProvider(),
            getErrorType(),
            getColumn(),
            getRelation(),
            getInvalidRows(),
            getDescription());
    }

}
