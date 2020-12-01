package org.pdxfinder.dataloaders.updog.tablevalidation.error;

import tech.tablesaw.api.Table;

public class IllegalValueError implements ValidationError{

    private String tableName;
    private String description;
    private String provider;
    private Table invalidRows;

    public IllegalValueError(
            String tableName,
            String description,
            Table invalidRows,
            String provider
            ) {
            this.tableName = tableName;
            this.description = description;
            this.invalidRows = invalidRows;
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
            "Error in [%s] for provider %s: %s",
            tableName,
            provider,
            description
            );
            }

    @Override
    public String toString() {
            return verboseMessage();
            }
}

