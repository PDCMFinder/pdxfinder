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
        checkAllRequiredValuesPresent(tableSet, tableSetSpecification);
        checkAllUniqueValuesForDuplicates(tableSet, tableSetSpecification);
        return validationErrors;
    }

    private void checkAllRequiredTablesPresent(
        Map<String, Table> tableSet,
        TableSetSpecification tableSetSpecification
    ) {
        if (isMissingRequiredTables(tableSet, tableSetSpecification)) {
            tableSetSpecification.getMissingFilesFrom(tableSet).forEach(
                f -> validationErrors.add(
                    TableValidationError.missingFile(f).setProvider(tableSetSpecification.getProvider())));
        }
    }

    private boolean isMissingRequiredTables(Map<String, Table> tableSet, TableSetSpecification tableSetSpecification) {
        return !tableSetSpecification.getMissingFilesFrom(tableSet).isEmpty();
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

    private void checkAllRequiredValuesPresent(
        Map<String, Table> tableSet,
        TableSetSpecification tableSetSpecification
    ) {
        List<Pair<String, String>> requiredTableColumns = tableSetSpecification.getRequiredColumns();
        for (Pair<String, String> tableColumn : requiredTableColumns) {
            String tableName = tableColumn.getKey();
            String columnName = tableColumn.getValue();
            Table table = tableSet.get(tableName);
            Table missing = table.where(
                table.stringColumn(columnName).isMissing());
            for (Row row : missing) {
                validationErrors.add(
                    TableValidationError
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
                validationErrors.add(
                    TableValidationError
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

    boolean passesValidation(Map<String, Table> tableSet, TableSetSpecification tableSetSpecification) {
        return validate(tableSet, tableSetSpecification).isEmpty();
    }

    List<TableValidationError> getValidationErrors() {
        return validationErrors;
    }

}
