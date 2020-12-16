package org.pdxfinder.dataloaders.updog.tablevalidation.error;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
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
        if (bothColumnsPresent(tableSet, relation)) {
            runAppropriateValidation(tableSet, relation, tableSetSpecification);
        }
    }

    private void runAppropriateValidation(Map<String, Table> tableSet, Relation relation, TableSetSpecification tableSetSpecification) {
        Relation.validityType validity = relation.getValidity();
        if (validity.equals(Relation.validityType.TABLE_KEY)) {
            reportOrphanRowsWhenMissingValuesInRelation(tableSet, relation, tableSetSpecification.getProvider());
        } else if (validity.equals(Relation.validityType.ONE_TO_ONE)) {
            reportBrokenOneToOneRelation(tableSet, relation, tableSetSpecification.getProvider());
        } else if (validity.equals(Relation.validityType.ONE_TO_MANY)) {
            reportBrokenOneToManyRelation(tableSet, relation, tableSetSpecification.getProvider());
        }
    }

    private void reportBrokenOneToOneRelation(
            Map<String, Table> tableSet,
            Relation relation,
            String provider) {
        ColumnReference leftRefColumn = relation.leftColumnReference();
        ColumnReference rightRefColumn = relation.getOtherColumn(leftRefColumn);
        StringColumn leftRestrictedColumn = tableSet.get(rightRefColumn.table()).stringColumn(leftRefColumn.column());
        StringColumn rightRestrictedColumn = tableSet.get(leftRefColumn.table()).stringColumn(rightRefColumn.column());
        Table workingTable = tableSet.get(leftRefColumn.table());
        int[] indexOfDuplicates = getIndexOfDuplicatedForPair(leftRestrictedColumn,rightRestrictedColumn);
        if(indexOfDuplicates.length > 0){
            List<Pair<String,String>> brokenPairs = IntStream.of(indexOfDuplicates)
                    .mapToObj(x -> Pair.of
                            (leftRestrictedColumn.get(x), rightRestrictedColumn.get(x)))
                    .collect(Collectors.toList());
            String description = String
                    .format("in [%s] one-to-one found %s relationships with conflicts %s",
                            leftRefColumn.table(), brokenPairs.size(), brokenPairs.toString());
            errors.add(create(leftRefColumn.table(), relation, workingTable.rows(indexOfDuplicates), description, provider));
        }
    }

    private int[] getIndexOfDuplicatedForPair(StringColumn leftRestrictedColumn, StringColumn rightRestrictedColumn) {
        Set<Integer> leftIndexOfDuplicates = getIndexOfDuplicatedColumnValues(leftRestrictedColumn);
        Set<Integer> rightIndexOfDuplicates = getIndexOfDuplicatedColumnValues(rightRestrictedColumn);
        Set<Integer> allDuplicates = new HashSet<>();
        allDuplicates.addAll(leftIndexOfDuplicates);
        allDuplicates.addAll(rightIndexOfDuplicates);
        return unboxSet(allDuplicates);

    }

    private Set<Integer> getIndexOfDuplicatedColumnValues(StringColumn column) {
        return column.asList().stream()
                .filter(x -> column.countOccurrences(x) > 1)
                .map(x -> indicesOf(column, x))
                .flatMapToInt(Arrays::stream)
                .mapToObj(Integer::valueOf)
                .collect(Collectors.toSet());
    }

    private int[] indicesOf(StringColumn column, String search) {
        return IntStream.range(0, column.size())
                .filter(i -> column.get(i).equals(search))
                .toArray();
    }

    private int[] unboxSet(Set<Integer> box){
        return box.stream()
                .mapToInt(x -> x)
                .toArray();
    }

    private void reportBrokenOneToManyRelation(
            Map<String, Table> tableSet,
            Relation relation,
            String provider
    ){
        ColumnReference leftColumn = relation.leftColumnReference();
        ColumnReference rightColumn = relation.getOtherColumn(leftColumn);
        StringColumn oneRestrictedColumn = tableSet.get(rightColumn.table()).stringColumn(leftColumn.column());
        StringColumn manyRestrictedColumn = tableSet.get(leftColumn.table()).stringColumn(rightColumn.column());
        Table workingTable = tableSet.get(leftColumn.table());
        MultiValuedMap<String, Pair<String, String>> columnPairs = new HashSetValuedHashMap<>();
        for (int i = 0; i < manyRestrictedColumn.size(); i++) {
            columnPairs.put(oneRestrictedColumn.get(i), Pair.of(manyRestrictedColumn.get(i), oneRestrictedColumn.get(i)));
        }
        List<Pair<String, String>> listOfBrokenPairs = oneRestrictedColumn.asList().stream()
                .filter(x -> oneRestrictedColumn.countOccurrences(x) > 1)
                .map(columnPairs::get)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        if (!listOfBrokenPairs.isEmpty()) {
            int[] invalidRows = unboxSet(
                    getIndexOfDuplicatedColumnValues(oneRestrictedColumn)
        );
            String description = String
                    .format("in [%s] one-to-many found %s relationships with conflicts %s",
                            leftColumn.table(), listOfBrokenPairs.size(), listOfBrokenPairs.toString());
            errors.add(create(leftColumn.table(), relation, workingTable.rows(invalidRows), description, provider));
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
        reportOrphanRowsFor(tableSet, relation, relation.leftColumnReference(), provider);
        reportOrphanRowsFor(tableSet, relation, relation.rightColumnReference(), provider);

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
