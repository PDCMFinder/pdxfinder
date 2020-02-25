package org.pdxfinder.dataloaders.updog;

import org.apache.commons.lang3.tuple.Pair;
import tech.tablesaw.api.Table;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

abstract class ValidationRuleCreator {

    abstract TableSetSpecification generate(String provider);

    protected Set<Pair<String, String>> matchingColumnsFromTable(
        Set<Pair<String, String>> columns,
        String tableName,
        String[] columnNamePatterns) {
        return columns
            .stream()
            .filter(p -> p.getKey().contains(tableName))
            .filter(p -> containsAny(p.getValue(), columnNamePatterns))
            .collect(Collectors.toSet());
    }

    protected Set<Pair<String, String>> matchingColumnsFromAnyTable(
        Set<Pair<String, String>> columns,
        String columnNamePattern
    ) {
        return columns.stream()
            .filter(p -> p.getValue().contains(columnNamePattern))
            .collect(Collectors.toSet());
    }

    protected TableSetSpecification renameTables(TableSetSpecification tableSetSpecification, String tableName) {
        TableSetSpecification renamedTableSetSpecification = TableSetSpecification.create();
        renamedTableSetSpecification.addRequiredTables(new HashSet<>(Collections.singletonList(tableName)));
        for (Pair<String, String> requiredColumns : tableSetSpecification.getRequiredColumns()) {
            renamedTableSetSpecification.addRequiredColumns(Pair.of(tableName, requiredColumns.getValue()));
        }
        for (Pair<String, String> nonEmptyColumns : tableSetSpecification.getNonEmptyColumns()) {
            renamedTableSetSpecification.addRequiredColumns(Pair.of(tableName, nonEmptyColumns.getValue()));
        }
        for (Pair<String, String> uniqueColumns : tableSetSpecification.getUniqueColumns()) {
            renamedTableSetSpecification.addRequiredColumns(Pair.of(tableName, uniqueColumns.getValue()));
        }
        for (Pair<Pair<String, String>, Pair<String, String>> relation : tableSetSpecification.getHasRelations()) {
            renamedTableSetSpecification.addHasRelations(
                Pair.of(
                    tableName,
                    relation.getLeft().getRight()),
                Pair.of(
                    relation.getRight().getLeft(),
                    relation.getRight().getRight())
            );
        }
        renamedTableSetSpecification.setProvider(tableSetSpecification.getProvider());
        return renamedTableSetSpecification;
    }

    protected Map<String, Table> matchingColumnsFromAnyMatchingTable(
        Map<String, Table> tableSet,
        String tablePattern,
        String columnPattern
    ) {
        return tableSet.entrySet().stream()
            .filter(i -> i.getKey().contains(tablePattern))
            .filter(i -> i.getValue().columnNames().contains(columnPattern))
            .collect(Collectors.toMap(
                map -> map.getKey(),
                map -> map.getValue()
            ));
    }

    protected Set<Pair<String, String>> tableSetToTableColumns(Map<String, Table> tableSet) {
        Set<Pair<String, String>> tableColumns = new HashSet<>();
        for (Map.Entry<String, Table> entry : tableSet.entrySet()) {
            tableColumns.addAll(tableToTableColumn(entry.getValue()));
        }
        return tableColumns;
    }

    protected Set<Pair<String, String>> tableToTableColumn(Table table) {
        Set<Pair<String, String>> tableColumns = new HashSet<>();
        for (String column : table.columnNames()) {
            tableColumns.add(Pair.of(table.name(), column));
        }
        return tableColumns;
    }

    protected Pair<Pair<String, String>, Pair<String, String>> relation(
        String from, String to, String columnName
    ) {
        return Pair.of(Pair.of(from, columnName), Pair.of(to, columnName));
    }

    private static boolean containsAny(String inputStr, String[] patterns) {
        return Arrays.stream(patterns).parallel().anyMatch(inputStr::contains);
    }

}
