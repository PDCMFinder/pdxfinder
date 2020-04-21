package org.pdxfinder.dataloaders.updog.tablevalidation.error;

import org.pdxfinder.dataloaders.updog.tablevalidation.ColumnReference;
import org.pdxfinder.dataloaders.updog.tablevalidation.Relation;
import org.pdxfinder.dataloaders.updog.tablevalidation.TableSetSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class BrokenRelationErrorCreator extends ErrorCreator {

    private static final Logger log = LoggerFactory.getLogger(BrokenRelationErrorCreator.class);

    public List<ValidationError> generateErrors(
        Map<String, Table> tableSet,
        TableSetSpecification tableSetSpecification
    ) {
        for (Relation relation : tableSetSpecification.getRelations()) {
            reportRelationErrors(tableSet, relation, tableSetSpecification);
        }
        return errors;
    }

    public BrokenRelationError create(
        String tableName,
        Relation relation,
        Table invalidRows,
        String description,
        String provider
        ) {
        return new BrokenRelationError(tableName, relation, invalidRows, description, provider);
    }

    private void reportRelationErrors(
        Map<String, Table> tableSet,
        Relation relation,
        TableSetSpecification tableSetSpecification
    ) {
        reportMissingColumnsInRelation(tableSet, relation, tableSetSpecification.getProvider());
        reportOrphanRowsWhenMissingValuesInRelation(tableSet, relation, tableSetSpecification.getProvider());
    }

    private void reportMissingColumnsInRelation(Map<String, Table> tableSet, Relation relation, String provider) {
        if (missingLeftColumn(tableSet, relation)) {
            errors.add(
                create(
                    relation.leftTable(),
                    relation,
                    tableSet.get(relation.leftTable()).emptyCopy(),
                    String.format("because [%s] is missing column [%s]", relation.leftTable(), relation.leftColumn()),
                    provider
                    ));
        }
        if (missingRightColumn(tableSet, relation)) {
            errors.add(
                create(
                    relation.rightTable(),
                    relation,
                    tableSet.get(relation.rightTable()).emptyCopy(),
                    String.format("because [%s] is missing column [%s]", relation.rightTable(), relation.rightColumn()),
                    provider
                ));
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
            errors.add(ValidationErrorImpl
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

}
