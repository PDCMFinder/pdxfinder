package org.pdxfinder.dataloaders.updog;

import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.HashMap;
import java.util.stream.Stream;

@Component
public class MetadataReader {

    private static final Logger log = LoggerFactory.getLogger(MetadataReader.class);

    @Autowired public MetadataReader() {}

    public HashMap<String, Table> readMetadataTsvs(Path targetDirectory) {
        HashMap<String, Table> metaDataTables = new HashMap<>();
        final PathMatcher onlyMetadataTsv = targetDirectory
            .getFileSystem()
            .getPathMatcher("glob:**metadata-*.tsv");
        try (final Stream<Path> stream = Files.list(targetDirectory)) {
            stream
                .filter(onlyMetadataTsv::matches)
                .forEach(path -> metaDataTables.put(
                    path.getFileName().toString(),
                    readTsvOrReturnEmpty(path.toFile()))
                );
        } catch (IOException e) {
            log.error("There was an error reading the metadata files", e);
        }
        return metaDataTables;
    }

    private static Table readTsvOrReturnEmpty(File file) {
        Table dataTable = Table.create();
        log.trace("Reading tsv file {}", file);
        try { dataTable = readTsv(file); }
        catch (IOException e) { log.error("There was an error reading the tsv file" , e); }
        return dataTable;
    }

    private static Table readTsv(File file) throws IOException {
        CsvReadOptions.Builder builder = CsvReadOptions
            .builder(file)
            .separator('\t');
        CsvReadOptions options = builder.build();
        return Table.read().usingOptions(options);
    }

}
