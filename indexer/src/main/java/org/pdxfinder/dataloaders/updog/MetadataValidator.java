package org.pdxfinder.dataloaders.updog;

import org.springframework.stereotype.Component;
import tech.tablesaw.api.Table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
@Component
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

    public List<TableValidationError> validate(
        Map<String, Table> pdxDataTables,
        Map<String, ColumnSpecification> columnSpecification,
        String provider
    ) {
        checkAllRequiredFilesPresent(pdxDataTables, provider);
        checkAllRequiredColumnsPresent(pdxDataTables, columnSpecification, provider);
        return validationErrors;
    }

    private void checkAllRequiredColumnsPresent(
        Map<String, Table> pdxDataTables,
        Map<String, ColumnSpecification> columnSpecification,
        String provider
    ) {
        String key;
        ColumnSpecification value;
        List<String> missingCols;
        for (Map.Entry<String, ColumnSpecification> entry : columnSpecification.entrySet()) {
            key = entry.getKey();
            value = entry.getValue();
            missingCols = value.getMissingColumnsFrom(pdxDataTables.get(key));
            for (String missingCol : missingCols) {
                validationErrors.add(TableValidationError
                    .create(key)
                    .setType(TableValidationError.Type.MISSING_COL)
                    .setColumn(missingCol)
                    .setProvider(provider)
                );
            }
        }

    }

    private void checkAllRequiredFilesPresent(Map<String, Table> pdxDataTables, String provider) {
        if (isMissingRequiredFiles(pdxDataTables)) {
            getMissingFilesFrom(pdxDataTables).forEach(
                f -> validationErrors.add(
                    TableValidationError
                        .create(f)
                        .setProvider(provider)
                        .setType(TableValidationError.Type.MISSING_FILE)));
        }
    }

    public boolean passesValidation(Map<String, Table> pdxDataTables, String provider) {
        return validate(pdxDataTables, new HashMap<>(), provider).isEmpty();
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
