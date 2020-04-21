package org.pdxfinder.dataloaders.updog.tablevalidation;

import org.apache.commons.lang3.tuple.Pair;
import tech.tablesaw.api.Table;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

abstract class ValidationRuleCreator {

    abstract TableSetSpecification generate(String provider);

    protected Set<ColumnReference> matchingColumnsFromTable(
        Set<ColumnReference> columns,
        String tableName,
        String[] columnNamePatterns) {
        return columns
            .stream()
            .filter(c -> c.table().contains(tableName))
            .filter(c -> containsAny(c.column(), columnNamePatterns))
            .collect(Collectors.toSet());
    }

    protected Set<ColumnReference> matchingColumnsFromAnyTable(
        Set<ColumnReference> columns,
        String columnNamePattern
    ) {
        return columns.stream()
            .filter(c -> c.column().contains(columnNamePattern))
            .collect(Collectors.toSet());
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
                Map.Entry::getKey,
                Map.Entry::getValue
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
