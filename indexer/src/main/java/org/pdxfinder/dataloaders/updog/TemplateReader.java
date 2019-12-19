package org.pdxfinder.dataloaders.updog;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.tablesaw.api.Table;
import tech.tablesaw.io.csv.CsvReadOptions;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

@Component
public class TemplateReader {

    public TemplateReader(String provider) {
        setProvider(provider);
    }

    @Value("${pdxfinder.root.dir}")
    private String dataDirectory = "data/";
    private String provider;
    private Path providerDirectory;
    private static final Logger log = LoggerFactory.getLogger(TemplateReader.class);


    public Map<String, Table> read(){
        HashMap<String, Table> emptyHashMap = new HashMap<>();
        providerDirectory = Paths.get(dataDirectory, "data", "UPDOG", provider);
        return directoryExists() ? collectDataTables() : emptyHashMap;
    }

    private boolean directoryExists() {
        return providerDirectory.toFile().exists();
    }

    private HashMap<String, Table> collectDataTables() {
        HashMap<String, Table> dataTableHashMap = new HashMap<>();
        final PathMatcher onlyMetadataTsv = providerDirectory
            .getFileSystem()
            .getPathMatcher("glob:**metadata-*.tsv");
        try (final Stream<Path> stream = Files.list(providerDirectory)) {
            stream
                .filter(onlyMetadataTsv::matches)
                .forEach(path -> dataTableHashMap.put(
                    path.getFileName().toString(),
                    readTsvFile(path.toFile()))
                );
        } catch (IOException e) {
            log.error("There was an error reading the templates: " , e);
        }
        return dataTableHashMap;
    }

    private static Table readTsvFile(File file) {
        Table dataTable = Table.create();
        try { dataTable = readTsv(file); }
        catch (IOException e) { log.error("There was an error reading the file:" , e); }
        return dataTable;
    }

    private static Table readTsv(File file) throws IOException {
        CsvReadOptions.Builder builder = CsvReadOptions
            .builder(file)
            .separator('\t');
        CsvReadOptions options = builder.build();
        return Table.read().usingOptions(options);
    }

    public void setProvider(String provider) { this.provider = provider; }

}
