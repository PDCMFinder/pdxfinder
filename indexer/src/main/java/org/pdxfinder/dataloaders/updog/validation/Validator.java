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
        for (String table : tableSetSpecification.getMissingTablesFrom(tableSet)) {
            validationErrors.add(ValidationError
                .missingFile(table)
                .setProvider(tableSetSpecification.getProvider()));
        }
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
            if (tableIsMissingColumn(tableSet, required)) {
                validationErrors.add(ValidationError
                    .missingColumn(required.table(), required.column())
                    .setProvider(tableSetSpecification.getProvider()));
            }
        }
    }

    private boolean tableIsMissingColumn(Map<String, Table> tableSet, ColumnReference columnReference) {
        return !tableSet.get(columnReference.table()).columnNames().contains(columnReference.column());
    }

    private void checkAllNonEmptyValuesPresent(
        Map<String, Table> tableSet,
        TableSetSpecification tableSetSpecification
    ) {
        for (ColumnReference tested : tableSetSpecification.getNonEmptyColumns()) {
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
        for (ColumnReference tested : tableSetSpecification.getUniqueColumns()) {
            Table table = tableSet.get(tested.table());
            Set<String> duplicates = findDuplicates(table.stringColumn(tested.column()).asList());
            if (CollectionUtils.isNotEmpty(duplicates)) {
                validationErrors.add(ValidationError
                    .duplicateValue(tested.table(), tested.column(), duplicates)
                    .setProvider(tableSetSpecification.getProvider()));
            }
        }
    }

    private Set<String> findDuplicates(List<String> listContainingDuplicates) {
        final Set<String> duplicates = new HashSet<>();
        final Set<String> set1 = new HashSet<>();
        for (String string : listContainingDuplicates) {
            if (!set1.add(string))  duplicates.add(string);
        }
        return duplicates;
    }

    private void checkRelationsValid(Map<String, Table> tableSet, TableSetSpecification tableSetSpecification) {
        for (Relation relation : tableSetSpecification.getRelations()) {
            reportMissingColumnsInRelation(tableSet, relation, tableSetSpecification.getProvider());
            reportOrphanRowsWhenMissingValuesInRelation(tableSet, relation, tableSetSpecification.getProvider());
        }
    }

    private void reportMissingColumnsInRelation(Map<String, Table> tableSet, Relation relation, String provider) {
        if (missingLeftColumn(tableSet, relation)) {
            validationErrors.add(ValidationError
                .brokenRelation(relation.leftTable(), relation, tableSet.get(relation.leftTable()).emptyCopy())
                .setDescription(String.format("because [%s] is missing column [%s]",
                    relation.leftTable(),
                    relation.leftColumn()))
                .setProvider(provider));
        }
        if (missingRightColumn(tableSet, relation)) {
            validationErrors.add(ValidationError
                .brokenRelation(relation.rightTable(), relation, tableSet.get(relation.rightTable()).emptyCopy())
                .setDescription(String.format("because [%s] is missing column [%s]",
                    relation.rightTable(),
                    relation.rightColumn()))
                .setProvider(provider));
        }
    }

    private void reportOrphanRowsWhenMissingValuesInRelation(
        Map<String, Table> tableSet,
        Relation relation,
        String provider
    ) {
        if (bothColumnsPresent(tableSet, relation)) {
            reportOrphanRowsFor(tableSet, relation, relation.leftColumnReference(), provider);
            reportOrphanRowsFor(tableSet, relation, relation.rightColumnReference(), provider);
        }
    }

    private void reportOrphanRowsFor(
        Map<String, Table> tableSet,
        Relation relation,
        ColumnReference child,
        String provider
    ) {
        ColumnReference parent = relation.getOtherColumn(child);
        Table orphanTable = getTableOfOrphanRows(
            tableSet.get(child.table()),
            tableSet.get(child.table()).stringColumn(child.column()),
            tableSet.get(parent.table()).stringColumn(parent.column()));
        if (orphanTable.rowCount() > 0) {
            validationErrors.add(ValidationError
                .brokenRelation(parent.table(), relation, orphanTable)
                .setDescription(String.format("%s orphan row(s) found in [%s]",
                    orphanTable.rowCount(),
                    child.table()))
                .setProvider(provider));
        }
    }

    private Table getTableOfOrphanRows(Table childTable, StringColumn child, StringColumn parent) {
        Set<String> parentSet = parent.asSet();
        return childTable.where(child.isNotIn(parentSet));
    }

    private boolean bothColumnsPresent(Map<String, Table> tableSet, Relation relation) {
        return (
            !missingLeftColumn(tableSet, relation)
                && !missingRightColumn(tableSet, relation));
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
