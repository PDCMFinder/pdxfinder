package org.pdxfinder.dataloaders.updog;

import org.springframework.stereotype.Component;
import tech.tablesaw.api.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
@Component
public class MetadataValidator {

    private ArrayList<TableValidationError> validationErrors;

    public MetadataValidator() {
        this.validationErrors = new ArrayList<>();
    }

    public List<TableValidationError> validate(
        Map<String, Table> pdxDataTables,
        FileSetSpecification fileSetSpecification,
        String provider) {
        checkAllRequiredFilesPresent(pdxDataTables, fileSetSpecification, provider);
        checkAllRequiredColumnsPresent(pdxDataTables, fileSetSpecification, provider);
        return validationErrors;
    }

    private void checkAllRequiredColumnsPresent(
        Map<String, Table> pdxDataTables,
        FileSetSpecification fileSetSpecification,
        String provider
    ) {
        if (fileSetSpecification.hasRequiredColumns()) {
            createValidationErrorsForMissingRequiredColumns(pdxDataTables, fileSetSpecification, provider);
        }

    }

    private void createValidationErrorsForMissingRequiredColumns(
        Map<String, Table> pdxDataTables,
        FileSetSpecification fileSetSpecification,
        String provider) {

        String key;
        ColumnSpecification value;
        List<String> missingCols;
        Map<String, ColumnSpecification> columnSpecification = fileSetSpecification.getColumnSpecification();
        for (Map.Entry<String, ColumnSpecification> entry : columnSpecification.entrySet()) {
            key = entry.getKey();
            value = entry.getValue();
            missingCols = value.getMissingColumnsFrom(pdxDataTables.get(key));
            for (String missingCol : missingCols) {
                validationErrors.add(TableValidationError.missingColumn(key, missingCol).setProvider(provider));
            }
        }
    }

    private void checkAllRequiredFilesPresent(Map<String, Table> pdxDataTables, FileSetSpecification fileSetSpecification, String provider) {
        if (isMissingRequiredFiles(pdxDataTables, fileSetSpecification)) {
            fileSetSpecification.getMissingFilesFrom(pdxDataTables).forEach(
                f -> validationErrors.add(TableValidationError.missingFile(f).setProvider(provider)));
        }
    }

    public boolean passesValidation(Map<String, Table> pdxDataTables, FileSetSpecification fileSetSpecification, String provider) {
        return validate(pdxDataTables, fileSetSpecification, provider).isEmpty();
    }

    private boolean isMissingRequiredFiles(Map<String, Table> pdxDataTables, FileSetSpecification fileSetSpecification) {
        return !fileSetSpecification.getMissingFilesFrom(pdxDataTables).isEmpty();
    }

    public List<TableValidationError> getValidationErrors() {
        return validationErrors;
    }

}
