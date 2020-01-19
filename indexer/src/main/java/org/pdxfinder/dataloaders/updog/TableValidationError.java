package org.pdxfinder.dataloaders.updog;

import java.util.Objects;
import java.util.Optional;

public class TableValidationError {
    private String table;
    private Optional<String> column;

    private TableValidationError(String table) {
        this.table = table;
    }

    public static TableValidationError create(String table) {
        return new TableValidationError(table);
    }

    public TableValidationError setColumn(String columnName) {
        this.column = Optional.of(columnName);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TableValidationError that = (TableValidationError) o;
        return table.equals(that.table) &&
            column.equals(that.column);
    }

    @Override
    public int hashCode() {
        return Objects.hash(table, column);
    }
}
