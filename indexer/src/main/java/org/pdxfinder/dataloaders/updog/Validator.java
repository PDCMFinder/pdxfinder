package org.pdxfinder.dataloaders.updog;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        checkAllRequiredFilesPresent(tableSet, tableSetSpecification);
        checkAllRequiredColumnsPresent(tableSet, tableSetSpecification);
        checkAllRequiredValuesPresent(tableSet, tableSetSpecification);
        return validationErrors;
    }

    private void checkAllRequiredColumnsPresent(
        Map<String, Table> tableSet,
        TableSetSpecification tableSetSpecification
    ) {
        if (tableSetSpecification.hasRequiredColumns()) {
            createValidationErrorsForMissingRequiredColumns(tableSet, tableSetSpecification);
        }
    }

    private void createValidationErrorsForMissingRequiredColumns(
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

    private void checkAllRequiredFilesPresent(
        Map<String, Table> tableSet,
        TableSetSpecification tableSetSpecification
    ) {
        if (isMissingRequiredFiles(tableSet, tableSetSpecification)) {
            tableSetSpecification.getMissingFilesFrom(tableSet).forEach(
                f -> validationErrors.add(
                    TableValidationError.missingFile(f).setProvider(tableSetSpecification.getProvider())));
        }
    }

    public boolean passesValidation(Map<String, Table> tableSet, TableSetSpecification tableSetSpecification) {
        return validate(tableSet, tableSetSpecification).isEmpty();
    }

    private boolean isMissingRequiredFiles(Map<String, Table> tableSet, TableSetSpecification tableSetSpecification) {
        return !tableSetSpecification.getMissingFilesFrom(tableSet).isEmpty();
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

    public List<TableValidationError> getValidationErrors() {
        return validationErrors;
    }

}
