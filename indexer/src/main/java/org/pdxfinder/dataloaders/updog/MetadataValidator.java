package org.pdxfinder.dataloaders.updog;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.tablesaw.api.Table;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class MetadataValidator implements TableCollectionValidator {

    private final List<String> requiredDataTables = Arrays.asList(
        "patient",
        "sample",
        "model",
        "model_validation",
        "sharing",
        "loader"
    );

    @Autowired
    public MetadataValidator() {
    }


    @Override
    public boolean passesValidation(Map<String, Table> pdxDataTables) {
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
