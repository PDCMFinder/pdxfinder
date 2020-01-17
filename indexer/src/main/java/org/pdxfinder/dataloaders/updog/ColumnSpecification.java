package org.pdxfinder.dataloaders.updog;

import tech.tablesaw.api.Table;

import java.util.List;

public class ColumnSpecification {

    private final Table tableContainingRequiredColumns;

    public ColumnSpecification(Table table) {
        this.tableContainingRequiredColumns = table;
    }

    public boolean containsRequiredColumns(Table tableTobeValidated) {
        return getMissingColumnsFrom(tableTobeValidated).isEmpty();
    }

    public List<String> getMissingColumnsFrom(Table tableToBeValidated) {
        List<String> missingColumns = tableContainingRequiredColumns.columnNames();
        missingColumns.removeAll(tableToBeValidated.columnNames());

        return missingColumns;
    }

}
