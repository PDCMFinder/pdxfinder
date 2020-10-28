package org.pdxfinder.dataloaders.updog;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.io.csv.CsvReadOptions;
import tech.tablesaw.selection.Selection;

import java.io.File;
import java.io.IOException;

public final class TableUtilities {

    private static final Logger log = LoggerFactory.getLogger(TableUtilities.class);

    private TableUtilities() {
        throw new IllegalStateException("Utility class");
    }


    public static Table readTsvOrReturnEmpty(File file) {
        Table dataTable = Table.create();
        log.trace("Reading tsv file {}", file);
        System.out.print(String.format("Reading tsv file %s\r", file));
        try { dataTable = readTsv(file); }
        catch (IOException e) { log.error("There was an error reading the tsv file" , e); }
        return dataTable;
    }

    public static Table readTsv(File file) throws IOException {
        CsvReadOptions.Builder builder = CsvReadOptions
            .builder(file)
            .sample(false)
            .separator('\t');
        CsvReadOptions options = builder.build();
        return Table.read().usingOptions(options);
    }

    public static Table removeHeaderRows(Table table, int numberOfRows) {
        return doesNotHaveEnoughRows(table, numberOfRows)
            ? table.emptyCopy()
            : table.dropRange(numberOfRows);
    }

    private static boolean doesNotHaveEnoughRows(Table table, int numberOfRows) {
        return table.rowCount() <= numberOfRows;
    }

    public static Table removeRowsMissingRequiredColumnValue(Table table, String requiredColumn) {
        Selection missing = table.column(requiredColumn).isMissing();
        return table.dropWhere(missing);
    }

    public static Table removeRowsMissingRequiredColumnValue(Table table, StringColumn requiredColumn) {
        return removeRowsMissingRequiredColumnValue(table, requiredColumn.name());
    }

    public static Table fromString(String tableName, String ... lines) {
        Table table = Table.create();
        String string = String.join("\n", lines);
        try {
            table = Table.read().csv(IOUtils.toInputStream(string));
            table.setName(tableName);
        } catch (Exception e) {
            log.error("There was an error parsing string to Table", e);
        }
        return table;
    }

}
