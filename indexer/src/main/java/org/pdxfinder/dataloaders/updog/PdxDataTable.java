package org.pdxfinder.dataloaders.updog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.tablesaw.api.Table;
import tech.tablesaw.io.csv.CsvReadOptions;

import java.io.File;
import java.io.IOException;

public class PdxDataTable {

    private String provider;
    private final String[] ignoredFilenames = new String[]{"checklist"};

    private final static Logger log = LoggerFactory.getLogger(PdxDataTable.class);

    public PdxDataTable(String provider) {
        this.provider = provider;
    }

    public static Table readTsvFile(File file) {
        Table dataTable = Table.create();
        try { dataTable = readTsv(file); }
        catch (IOException e) { log.error("There was an error reading the file" , e); }
        return dataTable;
    }

    public static Table readTsv(File file) throws IOException {
        CsvReadOptions.Builder builder = CsvReadOptions
            .builder(file)
            .separator('\t');
        CsvReadOptions options = builder.build();
        return Table.read().usingOptions(options);
    }

}
