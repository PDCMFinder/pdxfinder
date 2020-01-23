package org.pdxfinder.dataloaders.updog;

import org.springframework.stereotype.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.tablesaw.api.Table;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@Component
public class MetadataReader {

    private static final Logger log = LoggerFactory.getLogger(MetadataReader.class);
    private static final List<String> allowedOmicData = Arrays.asList("cna", "cyto", "mut");

    Map<String, Table> readAllTsvFilesIn(Path targetDirectory, PathMatcher filter) {
        HashMap<String, Table> tables = new HashMap<>();
        try (final Stream<Path> stream = Files.list(targetDirectory)) {
            stream
                .filter(filter::matches)
                .forEach(path -> tables.put(
                    path.getFileName().toString(),
                    TableUtilities.readTsvOrReturnEmpty(path.toFile()))
                );
        } catch (IOException e) {
            log.error("There was an error reading the metadata files", e);
        }
        return tables;
    }

    Map<String, Table> readAllOmicsFilesIn(Path targetDirectory, PathMatcher filter) {
        HashMap<String, Optional<Path>> potentialOmicsPaths = new HashMap<>();
        for (String s : allowedOmicData) { potentialOmicsPaths.put(s, getOmicsDirectory(targetDirectory, s)); }

        Map<String, Path> availableOmicsPaths = new HashMap<>();
        potentialOmicsPaths.forEach((k, v) -> v.ifPresent(t -> availableOmicsPaths.put(k, t)));

        Map<String, Table> omicsTables = new HashMap<>();
        // Only runs once
        availableOmicsPaths.forEach((k, v) -> omicsTables.putAll(readAllTsvFilesIn(v, filter)));
        return omicsTables;
    }

    Optional<Path> getOmicsDirectory(Path targetDirectory, String omicsDirectoryName) {
        Optional omicsDirectory;
        return targetDirectory.resolve(omicsDirectoryName).toFile().exists() ?
            Optional.of(targetDirectory.resolve(omicsDirectoryName)) :
            Optional.empty();
    }

}
