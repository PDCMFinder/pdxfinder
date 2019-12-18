package org.pdxfinder.dataloaders.updog;

//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;

import tech.tablesaw.api.Table;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

//@Component
public class TemplateReader {

    public TemplateReader(String provider) {
        this.provider = provider;
    }

//    @Value("${pdxfinder.root.dir}")
    private String dataDirectory = "data/";
    private String provider;
    private Path providerDirectory;

//    public List<File> getTemplatePaths(Path providerDirectory) { }


    public Map<String, Table> read(){
        HashMap<String, Table> dataTableHashMap = new HashMap<>();
        providerDirectory = Paths.get(dataDirectory, "data", "UPDOG", provider);

        if (providerDirectory.toFile().exists()) {
            collectDataTables();
        }
        return dataTableHashMap;
    }

    private HashMap<String, Table> collectDataTables() {
        HashMap<String, Table> dataTableHashMap = new HashMap<>();
        final PathMatcher onlyTsv = providerDirectory
            .getFileSystem()
            .getPathMatcher("glob:*.tsv");
        try (final Stream<Path> stream = Files.list(providerDirectory)) {
            stream
                .filter(onlyTsv::matches)
                .forEach(f -> dataTableHashMap.put(
                    f.toString(),
                    PdxDataTable.readTsvFile(f.toFile()))
                );
        } catch (IOException e) {
            System.out.println(e);
        }
        return dataTableHashMap;
    }

    public void setProvider(String provider) { this.provider = provider; }

}
