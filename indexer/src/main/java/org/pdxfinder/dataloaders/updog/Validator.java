package org.pdxfinder.dataloaders.updog;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class Validator {

    private List<ValidationError> validationErrors;

    public Validator() {
        this.validationErrors = new ArrayList<>();
    }

    public List<ValidationError> validate(
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
                ValidationError.missingFile(f).setProvider(tableSetSpecification.getProvider())));
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
                validationErrors.add(ValidationError
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
            if (missing.rowCount() > 0) {
                validationErrors.add(ValidationError
                    .missingRequiredValue(tableName, columnName, missing)
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
                validationErrors.add(ValidationError
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
        String provider = tableSetSpecification.getProvider();
        for (Pair<Pair<String, String>, Pair<String, String>> relation
            : tableSetSpecification.getOneToManyRelations()) {
            reportMissingColumnsInOneToManyRelation(tableSet, relation, provider);
            reportMissingValuesInOneToManyRelation(tableSet, relation, provider);

        }
    }

    private void reportMissingColumnsInOneToManyRelation(
        Map<String, Table> tableSet,
        Pair<Pair<String, String>, Pair<String, String>> relation,
        String provider
    ) {
        if (missingLeftColumn(tableSet, relation)) {
            String leftTableName = relation.getLeft().getLeft();
            String leftColName = relation.getLeft().getRight();
            validationErrors.add(ValidationError
                .brokenRelation(leftTableName, relation, tableSet.get(leftTableName).emptyCopy())
                .setDescription(String.format("because [%s] is missing column [%s]", leftTableName, leftColName))
                .setProvider(provider));
        }
        if (missingRightColumn(tableSet, relation)) {
            String rightTableName = relation.getRight().getLeft();
            String rightColName = relation.getRight().getRight();
            validationErrors.add(ValidationError
                .brokenRelation(rightTableName, relation, tableSet.get(rightTableName).emptyCopy())
                .setDescription(String.format("because [%s] is missing column [%s]", rightTableName, rightColName))
                .setProvider(provider));
        }
    }

    private void reportMissingValuesInOneToManyRelation(
        Map<String, Table> tableSet,
        Pair<Pair<String, String>, Pair<String, String>> relation,
        String provider
    ) {
        String leftTableName = relation.getLeft().getLeft();
        String leftColumnName = relation.getLeft().getRight();
        String rightTableName = relation.getRight().getLeft();
        String rightColumnName = relation.getRight().getRight();
        if (bothColumnsPresent(tableSet, relation)) {
            Set<String> uniqueLeftValues = tableSet.get(leftTableName).stringColumn(leftColumnName).asSet();
            Table rightTable = tableSet.get(rightTableName);
            StringColumn rightCol = rightTable.stringColumn(rightColumnName);
            Table orphanRightTableRows = rightTable.where(rightCol.isNotIn(uniqueLeftValues));
            if(!orphanRightTableRows.isEmpty()) {
                validationErrors.add(ValidationError
                    .brokenRelation(rightTableName, relation, orphanRightTableRows)
                    .setDescription(
                        String.format("%s orphan row(s) found in [%s]",
                            orphanRightTableRows.rowCount(),
                            rightTableName))
                    .setProvider(provider));
            }
        }
    }

    private boolean bothColumnsPresent(
        Map<String, Table> tableSet,
        Pair<Pair<String, String>, Pair<String, String>> relation
    ) {
        return (!missingLeftColumn(tableSet, relation) && !missingRightColumn(tableSet, relation));
    }

    private boolean missingLeftColumn(
        Map<String, Table> tableSet,
        Pair<Pair<String, String>, Pair<String, String>> relation
    ) {
        return tableMissingColumn(
            tableSet.get(relation.getLeft().getLeft()),
            relation.getLeft().getRight());
    }

    private boolean missingRightColumn(
        Map<String, Table> tableSet,
        Pair<Pair<String, String>, Pair<String, String>> relation
    ) {
        return tableMissingColumn(
            tableSet.get(relation.getRight().getLeft()),
            relation.getRight().getRight());
    }

    private boolean tableMissingColumn(Table table, String columnName) {
        return !table.columnNames().contains(columnName);
    }

    boolean passesValidation(Map<String, Table> tableSet, TableSetSpecification tableSetSpecification) {
        return validate(tableSet, tableSetSpecification).isEmpty();
    }

    List<ValidationError> getValidationErrors() {
        return validationErrors;
    }

}
