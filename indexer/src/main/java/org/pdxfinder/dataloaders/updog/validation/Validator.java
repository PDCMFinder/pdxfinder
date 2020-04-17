package org.pdxfinder.dataloaders.updog.validation;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(Validator.class);
    private List<ValidationError> validationErrors;

    public Validator() {
        this.validationErrors = new ArrayList<>();
    }

    public List<ValidationError> validate(
        Map<String, Table> tableSet,
        TableSetSpecification tableSetSpecification
    ) {
        checkRequiredTablesPresent(tableSet, tableSetSpecification);
        if (CollectionUtils.isNotEmpty(validationErrors)) {
            log.error(
                "Not all required tables where present for {}. Aborting further validation",
                tableSetSpecification.getProvider());
            return validationErrors;
        }
        checkRequiredColumnsPresent(tableSet, tableSetSpecification);
        checkAllNonEmptyValuesPresent(tableSet, tableSetSpecification);
        checkAllUniqueValuesForDuplicates(tableSet, tableSetSpecification);
        checkRelationsValid(tableSet, tableSetSpecification);
        return validationErrors;
    }

    private void checkRequiredTablesPresent(
        Map<String, Table> tableSet,
        TableSetSpecification tableSetSpecification
    ) {
        tableSetSpecification.getMissingTablesFrom(tableSet).forEach(
            f -> validationErrors.add(
                ValidationError.missingFile(f).setProvider(tableSetSpecification.getProvider())));
    }

    private void checkRequiredColumnsPresent(
        Map<String, Table> tableSet,
        TableSetSpecification tableSetSpecification
    ) {
        if (tableSetSpecification.hasRequiredColumns()) {
            addErrorsForMissingColumns(tableSet, tableSetSpecification);
        }
    }

    private void addErrorsForMissingColumns(
        Map<String, Table> tableSet,
        TableSetSpecification tableSetSpecification
    ) {
        for (ColumnReference required : tableSetSpecification.getRequiredColumns()) {
            if (tableIsMissingColumn(tableSet, required.table(), required.column())) {
                validationErrors.add(ValidationError
                    .missingColumn(required.table(), required.column())
                    .setProvider(tableSetSpecification.getProvider()));
            }
        }
    }

    private boolean tableIsMissingColumn(Map<String, Table> tableSet, String tableName, String columnName) {
        return !tableSet.get(tableName).columnNames().contains(columnName);
    }

    private void checkAllNonEmptyValuesPresent(
        Map<String, Table> tableSet,
        TableSetSpecification tableSetSpecification
    ) {
        Set<ColumnReference> nonEmptyTableColumns = tableSetSpecification.getNonEmptyColumns();
        for (ColumnReference tested : nonEmptyTableColumns) {
            Table table = tableSet.get(tested.table());
            Table missing = table.where(
                table.stringColumn(tested.column()).isMissing());
            if (missing.rowCount() > 0) {
                validationErrors.add(ValidationError
                    .missingRequiredValue(tested.table(), tested.column(), missing)
                    .setProvider(tableSetSpecification.getProvider()));
            }
        }
    }

    private void checkAllUniqueValuesForDuplicates(
        Map<String, Table> tableSet,
        TableSetSpecification tableSetSpecification
    ) {
        Set<ColumnReference> uniqueTableColumns = tableSetSpecification.getUniqueColumns();
        for (ColumnReference tested : uniqueTableColumns) {
            Table table = tableSet.get(tested.table());
            Set<String> duplicates = findDuplicates(table.stringColumn(tested.column()).asList());
            if (!duplicates.isEmpty()) {
                validationErrors.add(ValidationError
                    .duplicateValue(tested.table(), tested.column(), duplicates)
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

    private void checkRelationsValid(
        Map<String, Table> tableSet,
        TableSetSpecification tableSetSpecification
    ) {
        String provider = tableSetSpecification.getProvider();
        for (Relation relation
            : tableSetSpecification.getHasRelations()) {
            reportMissingColumnsInRelation(tableSet, relation, provider);
            reportOrphanRowsWhenMissingValuesInRelation(tableSet, relation, provider);

        }
    }

    private void reportMissingColumnsInRelation(
        Map<String, Table> tableSet,
        Relation relation,
        String provider
    ) {
        if (missingLeftColumn(tableSet, relation)) {
            String leftTableName = relation.leftTable();
            String leftColName = relation.leftColumn();
            validationErrors.add(ValidationError
                .brokenRelation(relation.leftTable(), relation, tableSet.get(relation.leftTable()).emptyCopy())
                .setDescription(String.format("because [%s] is missing column [%s]", leftTableName, leftColName))
                .setProvider(provider));
        }
        if (missingRightColumn(tableSet, relation)) {
            String rightTableName = relation.rightTable();
            String rightColName = relation.rightColumn();
            validationErrors.add(ValidationError
                .brokenRelation(rightTableName, relation, tableSet.get(rightTableName).emptyCopy())
                .setDescription(String.format("because [%s] is missing column [%s]", rightTableName, rightColName))
                .setProvider(provider));
        }
    }

    private void reportOrphanRowsWhenMissingValuesInRelation(
        Map<String, Table> tableSet,
        Relation relation,
        String provider
    ) {
        if (bothColumnsPresent(tableSet, relation)) {
            reportOrphanRowsFor(tableSet, relation, provider, relation.leftTable(), relation.leftColumn());
            reportOrphanRowsFor(tableSet, relation, provider, relation.rightTable(), relation.rightColumn());
        }
    }

    private void reportOrphanRowsFor(
        Map<String, Table> tableSet,
        Relation relation,
        String provider,
        String childTableName,
        String childColName
    ) {
        ColumnReference otherColumn = getOtherColumn(childTableName, childColName, relation);
        Table orphanTable = getTableOfOrphanRows(
            tableSet.get(childTableName),
            tableSet.get(childTableName).stringColumn(childColName),
            tableSet.get(otherColumn.table()).stringColumn(otherColumn.column())
        );
        if (orphanTable.rowCount() > 0) {
            validationErrors.add(ValidationError
                .brokenRelation(otherColumn.table(), relation, orphanTable)
                .setDescription(
                    String.format("%s orphan row(s) found in [%s]",
                        orphanTable.rowCount(),
                        childTableName))
                .setProvider(provider));
        }
    }

    private ColumnReference getOtherColumn(String tableName, String columnName, Relation relation) {
        if (
            tableName.equals(relation.leftTable())
                && columnName.equals(relation.leftColumn())
        ) {
            return relation.rightColumnReference();
        } else {
            return relation.leftColumnReference();
        }
    }

    private Table getTableOfOrphanRows(Table childTable, StringColumn child, StringColumn parent) {
        Set<String> parentSet = parent.asSet();
        return childTable.where(child.isNotIn(parentSet));
    }

    private boolean bothColumnsPresent(
        Map<String, Table> tableSet,
        Relation relation
    ) {
        return (!missingLeftColumn(tableSet, relation) && !missingRightColumn(tableSet, relation));
    }

    private boolean missingLeftColumn(Map<String, Table> tableSet, Relation relation) {
        return tableMissingColumn(
            tableSet.get(relation.leftTable()),
            relation.leftColumn());
    }

    private boolean missingRightColumn(Map<String, Table> tableSet, Relation relation) {
        return tableMissingColumn(
            tableSet.get(relation.rightTable()),
            relation.rightColumn());
    }

    private boolean tableMissingColumn(Table table, String columnName) {
        try {
            return !table.columnNames().contains(columnName);
        } catch (NullPointerException e) {
            log.error("Couldn't access table {} {}", e, table);
            return true;
        }
    }

    boolean passesValidation(Map<String, Table> tableSet, TableSetSpecification tableSetSpecification) {
        return validate(tableSet, tableSetSpecification).isEmpty();
    }

    List<ValidationError> getValidationErrors() {
        return validationErrors;
    }

}
