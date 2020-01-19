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
    private ArrayList<TableValidationError> validationErrors;

    public MetadataValidator(
        Map<String, Table> pdxDataTables,
        Map<String, List<String>> columnSpecification) {
        this.validationErrors = new ArrayList<>();
    }


    public boolean validate(Map<String, Table> pdxDataTables) {
        if (isMissingRequiredTables(pdxDataTables)) return false;

        return true;
    }

    public ArrayList<TableValidationError> validateAndGetErrors(Map<String, Table> pdxDataTables) {
        if (isMissingRequiredTables(pdxDataTables)) {
            getMissingFilesFrom(pdxDataTables).forEach(
                f -> validationErrors.add(TableValidationError.create(f))
            );
        }
        return validationErrors;
    }

    private boolean isMissingRequiredTables(Map<String, Table> pdxDataTables) {
        return !getMissingFilesFrom(pdxDataTables).isEmpty();
    }

    private List<String> getMissingFilesFrom(Map<String, Table> pdxDataTablesToBeValidated) {
        List<String> missingFiles = getRequiredFiles();
        missingFiles.removeAll(pdxDataTablesToBeValidated.keySet());
        return missingFiles;
    }

    private ArrayList<String> getRequiredFiles() {
        return requiredDataTables
            .stream()
            .map(s -> String.format("metadata-%s.tsv", s))
            .collect(Collectors.toCollection(ArrayList::new));
    }

    public ArrayList<TableValidationError> getValidationErrors() {
        return validationErrors;
    }

}
