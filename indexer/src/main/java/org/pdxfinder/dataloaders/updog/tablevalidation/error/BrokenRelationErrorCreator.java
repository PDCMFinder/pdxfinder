package org.pdxfinder.dataloaders.updog.tablevalidation.error;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.apache.commons.lang3.tuple.Pair;
import org.pdxfinder.dataloaders.updog.tablevalidation.ColumnReference;
import org.pdxfinder.dataloaders.updog.tablevalidation.Relation;
import org.pdxfinder.dataloaders.updog.tablevalidation.TableSetSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

        Relation.validityType validity = relation.getValidity();
        if(validity.equals(Relation.validityType.table_key)) {
            reportOrphanRowsWhenMissingValuesInRelation(tableSet, relation, tableSetSpecification.getProvider());
        }
        else if(validity.equals(Relation.validityType.one_to_many)){
            reportBrokenOneToManyRelation(tableSet,relation, tableSetSpecification.getProvider());
        }
    }

    private void reportBrokenOneToManyRelation(
            Map<String, Table> tableSet,
            Relation relation,
            String provider
    ){
        if (bothColumnsPresent(tableSet, relation)) {
            ColumnReference leftColumn = relation.leftColumnReference();
            ColumnReference rightColumn = relation.getOtherColumn(leftColumn);
            StringColumn oneRestrictedColumn = tableSet.get(rightColumn.table()).stringColumn(leftColumn.column());
            StringColumn manyRestrictedColumn = tableSet.get(leftColumn.table()).stringColumn(rightColumn.column());
            MultiValuedMap<String, Pair<String, String>> columnPairs = new HashSetValuedHashMap<>();
            for (int i = 0; i < manyRestrictedColumn.size(); i++) {
                columnPairs.put(oneRestrictedColumn.get(i), Pair.of(manyRestrictedColumn.get(i), oneRestrictedColumn.get(i)));
            }
            List<Pair<String, String>> listOfBrokenPairs = oneRestrictedColumn.asList().stream()
                    .filter(x -> oneRestrictedColumn.countOccurrences(x) > 1)
                    .map(columnPairs::get)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
            Table workingTable = tableSet.get(leftColumn.table());
            int[] invalidRows = oneRestrictedColumn.asList().stream()
                    .filter(x -> oneRestrictedColumn.countOccurrences(x) > 1)
                    .map(oneRestrictedColumn::indexOf)
                    .mapToInt(x -> x)
                    .toArray();
            if (listOfBrokenPairs.size() > 0) {
                String description = String
                        .format("in [%s] one-to-many found %s relationships with conflicts %s",
                                leftColumn.table(), listOfBrokenPairs.size(), listOfBrokenPairs.toString());
                errors.add(create(leftColumn.table(), relation, workingTable.rows(invalidRows), description, provider));
            }
        }
    }


    private void reportMissingColumnsInRelation(Map<String, Table> tableSet, Relation relation, String provider) {
        if (tableSet.get(relation.leftTable()) == null || tableSet.get(relation.rightTable()) == null) return;
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
            String description = String.format("%s orphan row(s) found in [%s]", orphanTable.rowCount(), child.table());
            errors.add(create(parent.table(), relation, orphanTable, description, provider));
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
            relation.leftColumn(), relation.leftTable());
    }

    private boolean missingRightColumn(Map<String, Table> tableSet, Relation relation) {
        return tableMissingColumn(
            tableSet.get(relation.rightTable()),
            relation.rightColumn(), relation.rightTable());
    }

    private boolean tableMissingColumn(Table table, String columnName, String tableName) {
        try {
            return !table.columnNames().contains(columnName);
        } catch (NullPointerException e) {
            log.error("Couldn't access table {} because of {}",tableName, e.toString());
            return true;
        }
    }

}
