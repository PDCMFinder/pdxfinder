package org.pdxfinder.dataloaders.updog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.tablesaw.api.Table;
import tech.tablesaw.io.csv.CsvReadOptions;

import java.io.File;
import java.io.IOException;

public class PdxDataTable {

    private static String provider;
    private static File dataDirectory;


    private Table patient;

    private final static Logger log = LoggerFactory.getLogger(PdxDataTable.class);

    public PdxDataTable(String provider) {
        this.provider = provider;
    }

    public void readData() {
         readPatient(dataDirectory);
    }

    void readPatient(File updogDirectory) {
        try {
            File file;
            file = new File(updogDirectory + provider );
            Table dataTable = readTsv(file);
            setPatient(dataTable);
        }
        catch (IOException e) {
            log.error("There was an error reading the file" , e);
        }
    }

    Table readTsv(File file) throws IOException {
        CsvReadOptions.Builder builder = CsvReadOptions
            .builder(file)
            .separator('\t');
        CsvReadOptions options = builder.build();
        Table dataTable = Table.read().usingOptions(options);
        return dataTable;
    }

    public Table getPatient() {
        return patient;
    }

    public void setPatient(Table patient) {
        this.patient = patient;
    }

}
