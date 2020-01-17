package org.pdxfinder.dataloaders.updog;

import tech.tablesaw.api.Table;

import java.util.*;
import java.util.stream.Collectors;

public class MetadataValidator {

    private final List<String> requiredDataTables = Arrays.asList(
        "patient",
        "sample",
        "model",
        "model_validation",
        "sharing",
        "loader"
    );

    public MetadataValidator(
        Map<String, Table> pdxDataTables,
        Map<String, List<String>> columnSpecification) { }


    public boolean validate(Map<String, Table> pdxDataTables) {
        if (pdxDataTables.isEmpty()) return false;
        if (isMissingRequiredTables(pdxDataTables)) return false;

        return true;
    }

    private boolean isMissingRequiredTables(Map<String, Table> pdxDataTables) {
        List<String> requiredFiles = getRequiredFiles();
        return !pdxDataTables.keySet().containsAll(requiredFiles);
    }

    private ArrayList<String> getRequiredFiles() {
        return requiredDataTables
            .stream()
            .map(s -> String.format("metadata-%s.tsv", s))
            .collect(Collectors.toCollection(ArrayList::new));
    }

}
