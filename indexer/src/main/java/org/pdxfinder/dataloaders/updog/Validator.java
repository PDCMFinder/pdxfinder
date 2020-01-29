package org.pdxfinder.dataloaders.updog;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.Table;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class Validator {

    private ArrayList<TableValidationError> validationErrors;

    public Validator() {
        this.validationErrors = new ArrayList<>();
    }

    public List<TableValidationError> validate(
        Map<String, Table> tableSet,
        TableSetSpecification tableSetSpecification
    ) {
        checkAllRequiredTablesPresent(tableSet, tableSetSpecification);
        checkAllRequiredColumnsPresent(tableSet, tableSetSpecification);
        checkAllNonEmptyValuesPresent(tableSet, tableSetSpecification);
        checkAllUniqueValuesForDuplicates(tableSet, tableSetSpecification);
        checkOneToManyRelationsValid(tableSet, tableSetSpecification);
        return validationErrors;
    }

    private void checkAllRequiredTablesPresent(
        Map<String, Table> tableSet,
        TableSetSpecification tableSetSpecification
    ) {
        tableSetSpecification.getMissingTablesFrom(tableSet).forEach(
            f -> validationErrors.add(
                TableValidationError.missingFile(f).setProvider(tableSetSpecification.getProvider())));
    }

    private void checkAllRequiredColumnsPresent(
        Map<String, Table> tableSet,
        TableSetSpecification tableSetSpecification
    ) {
        if (tableSetSpecification.hasRequiredColumns()) {
            createValidationErrorsForMissingColumns(tableSet, tableSetSpecification);
        }
    }

    private void createValidationErrorsForMissingColumns(
        Map<String, Table> tableSet,
        TableSetSpecification tableSetSpecification
    ) {
        String key;
        ColumnSpecification value;
        List<String> missingCols;
        Map<String, ColumnSpecification> columnSpecification = tableSetSpecification.getColumnSpecification();
        for (Map.Entry<String, ColumnSpecification> entry : columnSpecification.entrySet()) {
            key = entry.getKey();
            value = entry.getValue();
            missingCols = value.getMissingColumnsFrom(tableSet.get(key));
            for (String missingCol : missingCols) {
                validationErrors.add(TableValidationError
                    .missingColumn(key, missingCol)
                    .setProvider(tableSetSpecification.getProvider()));
            }
        }
    }

    private void checkAllNonEmptyValuesPresent(
        Map<String, Table> tableSet,
        TableSetSpecification tableSetSpecification
    ) {
        List<Pair<String, String>> nonEmptyTableColumns = tableSetSpecification.getNonEmptyColumns();
        for (Pair<String, String> tableColumn : nonEmptyTableColumns) {
            String tableName = tableColumn.getKey();
            String columnName = tableColumn.getValue();
            Table table = tableSet.get(tableName);
            Table missing = table.where(
                table.stringColumn(columnName).isMissing());
            for (Row row : missing) {
                validationErrors.add(TableValidationError
                    .missingRequiredValue(tableName, columnName, row)
                    .setProvider(tableSetSpecification.getProvider()));
            }
        }
    }

    private void checkAllUniqueValuesForDuplicates(
        Map<String, Table> tableSet,
        TableSetSpecification tableSetSpecification
    ) {
        List<Pair<String, String>> uniqueTableColumns = tableSetSpecification.getUniqueColumns();
        for (Pair<String, String> tableColumn : uniqueTableColumns) {
            String tableName = tableColumn.getKey();
            String columnName = tableColumn.getValue();
            Table table = tableSet.get(tableName);
            Set<String> duplicates = findDuplicates(table.stringColumn(columnName).asList());
            if (!duplicates.isEmpty()) {
                validationErrors.add(TableValidationError
                    .duplicateValue(tableName, columnName, duplicates)
                    .setProvider(tableSetSpecification.getProvider()));
            }
        }
    }

    private Set<String> findDuplicates(List<String> listContainingDuplicates) {
        final Set<String> setToReturn = new HashSet<>();
        final Set<String> set1 = new HashSet<>();
        for (String string : listContainingDuplicates) {
            if (!set1.add(string))  setToReturn.add(string);
        }
        return setToReturn;
    }

    private void checkOneToManyRelationsValid(
        Map<String, Table> tableSet,
        TableSetSpecification tableSetSpecification
    ) {
        for (Pair<Pair<String, String>, Pair<String, String>> relation
            : tableSetSpecification.getOneToManyRelations()) {
            String leftTableName = relation.getLeft().getLeft();
            String leftColumnName = relation.getLeft().getRight();
            String rightTableName = relation.getRight().getLeft();
            String rightColumnName = relation.getRight().getRight();
            if (tableMissingColumn(tableSet.get(leftTableName), leftColumnName)) {
                validationErrors.add(TableValidationError
                    .brokenRelation(relation, "Missing column in the left table")
                    .setProvider(tableSetSpecification.getProvider()));
            }
            if (tableMissingColumn(tableSet.get(rightTableName), rightColumnName)) {
                validationErrors.add(TableValidationError
                    .brokenRelation(relation, "Missing column in the right table")
                    .setProvider(tableSetSpecification.getProvider()));
            }
        }
    }

    private boolean tableMissingColumn(Table table, String columnName) {
        return !table.columnNames().contains(columnName);
    }

    boolean passesValidation(Map<String, Table> tableSet, TableSetSpecification tableSetSpecification) {
        return validate(tableSet, tableSetSpecification).isEmpty();
    }

    List<TableValidationError> getValidationErrors() {
        return validationErrors;
    }

}
