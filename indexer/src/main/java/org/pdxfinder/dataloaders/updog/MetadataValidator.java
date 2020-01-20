package org.pdxfinder.dataloaders.updog;

import tech.tablesaw.api.Table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MetadataValidator {

    private ArrayList<TableValidationError> validationErrors;
    private final List<String> requiredDataTables = Arrays.asList(
        "patient",
        "sample",
        "model",
        "model_validation",
        "sharing",
        "loader"
    );

    public MetadataValidator() {
        this.validationErrors = new ArrayList<>();
    }

    public List<TableValidationError> validate(Map<String, Table> pdxDataTables, String provider) {
        checkAllRequiredFilesPresent(pdxDataTables, provider);
        return validationErrors;
    }

    private void checkAllRequiredFilesPresent(Map<String, Table> pdxDataTables, String provider) {
        if (isMissingRequiredFiles(pdxDataTables)) {
            getMissingFilesFrom(pdxDataTables).forEach(
                f -> validationErrors.add(
                    TableValidationError
                        .create(f)
                        .setProvider(provider)
                        .setType("Missing file")));
        }
    }

    public boolean passesValidation(Map<String, Table> pdxDataTables, String provider) {
        return validate(pdxDataTables, provider).isEmpty();
    }

    private boolean isMissingRequiredFiles(Map<String, Table> pdxDataTables) {
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

    public List<TableValidationError> getValidationErrors() {
        return validationErrors;
    }

}
