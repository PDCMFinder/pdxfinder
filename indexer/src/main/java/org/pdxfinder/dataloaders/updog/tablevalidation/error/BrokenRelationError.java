package org.pdxfinder.dataloaders.updog.tablevalidation.error;

import org.pdxfinder.dataloaders.updog.tablevalidation.Relation;
import tech.tablesaw.api.Table;

public class BrokenRelationError implements ValidationError {
    private String tableName;
    private Relation relation;
    private Table invalidRows;
    private String description;
    private String provider;

    public BrokenRelationError(
        String tableName,
        Relation relation,
        Table invalidRows,
        String description,
        String provider
    ) {
        this.tableName = tableName;
        this.relation = relation;
        this.invalidRows = invalidRows;
        this.description = description;
        this.provider = provider;
    }

    private Table getInvalidRows() {
        return this.invalidRows;
    }

    @Override
    public String verboseMessage() {
        return String.format("%s:%n%s", message(), getInvalidRows());
    }

    @Override
    public String message() {
        return String.format(
            "Error in [%s] for provider [%s]: Broken relation [%s]: %s",
            tableName,
            provider,
            relation,
            description
        );
    }

    @Override
    public String toString() {
        return verboseMessage();
    }
}
