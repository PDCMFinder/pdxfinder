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
public class Reader {

    private static final Logger log = LoggerFactory.getLogger(Reader.class);
    private static final List<String> allowedOmicData = Arrays.asList("cna", "cyto", "mut");
    private static final List<String> allowedTreatmentData = Arrays.asList("treatment", "drug");

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
            log.error("There was an error reading the files", e);
        }
        return tables;
    }

    Map<String, Table> readAllOmicsFilesIn(Path targetDirectory, PathMatcher filter) {
        HashMap<String, Optional<Path>> potentialOmicsPaths = new HashMap<>();
        for (String s : allowedOmicData) { potentialOmicsPaths.put(s, getSubDirectory(targetDirectory, s)); }

        Map<String, Path> availableOmicsPaths = new HashMap<>();
        potentialOmicsPaths.forEach((k, v) -> v.ifPresent(t -> availableOmicsPaths.put(k, t)));

        Map<String, Table> omicsTables = new HashMap<>();
        // Only runs once
        availableOmicsPaths.forEach((k, v) -> omicsTables.putAll(readAllTsvFilesIn(v, filter)));
        return omicsTables;
    }

    Map<String, Table> readAllTreatmentFilesIn(Path targetDirectory, PathMatcher filter){

        HashMap<String, Optional<Path>> potentialTreatmentPaths = new HashMap<>();
        for (String s : allowedTreatmentData) { potentialTreatmentPaths.put(s, getSubDirectory(targetDirectory, s)); }

        Map<String, Path> availableTreatmentPaths = new HashMap<>();
        potentialTreatmentPaths.forEach((k, v) -> v.ifPresent(t -> availableTreatmentPaths.put(k, t)));

        Map<String, Table> treatmentTables = new HashMap<>();
        // Only runs once
        availableTreatmentPaths.forEach((k, v) -> treatmentTables.putAll(readAllTsvFilesIn(v, filter)));
        return treatmentTables;

    }

    Optional<Path> getSubDirectory(Path targetDirectory, String subDirectoryName) {
        Optional omicsDirectory;
        return targetDirectory.resolve(subDirectoryName).toFile().exists() ?
            Optional.of(targetDirectory.resolve(subDirectoryName)) :
            Optional.empty();
    }

}
