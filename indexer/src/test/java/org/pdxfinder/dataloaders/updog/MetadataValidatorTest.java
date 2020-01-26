package org.pdxfinder.dataloaders.updog;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class MetadataValidatorTest {

    public Map<String, Table> completeFileSet = new HashMap<>();
    public Map<String, Table> incompleteFileSet = new HashMap<>();
    public String provider = "PROVIDER";

    @Before public void setUp() {
        MockitoAnnotations.initMocks(this);

        completeFileSet = makeCompleteFileSet();
        incompleteFileSet = makeIncompleteFileSet();
    }

    @InjectMocks private MetadataValidator metadataValidator;

    @Test public void passesValidation_givenEmptyFileSet_failsValidation() {
        Map<String, Table> emptyHashMap = new HashMap<>();
        assertThat(metadataValidator.passesValidation(emptyHashMap, provider), is(false));
    }

    @Test public void passesValidation_givenIncompleteFileSet_failsValidation() {
        assertThat(metadataValidator.passesValidation(incompleteFileSet, provider), is(false));
    }

    @Test public void passesValidation_givenCompleteFileSet_passesValidation() {
        assertThat(metadataValidator.passesValidation(completeFileSet, provider), is(true));
    }

    @Test public void passesValidation_givenExtraFileInFileSet_passesValidation() {
        Map<String, Table> completeFileSetPlusOne = new HashMap<>();
        completeFileSetPlusOne.putAll(completeFileSet);
        completeFileSetPlusOne.put("extra-file.tsv", Table.create());
        assertThat(metadataValidator.passesValidation(completeFileSetPlusOne, provider), is(true));
    }

    @Test public void validate_givenNoValidation_producesEmptyErrorList() {
        assertThat(metadataValidator.getValidationErrors().isEmpty(), is(true));
    }

    @Test public void validate_givenCompleteFileSet_producesEmptyErrorList() {
        assertThat(metadataValidator.validate(completeFileSet, new HashMap<>(), provider).isEmpty(), is(true));
    }

    @Test public void validate_givenIncompleteFileSet_addsErrorWithCorrectContextToErrorList() {
        ArrayList<TableValidationError> expected = new ArrayList<>(
            Arrays.asList(TableValidationError
                .create("metadata-patient.tsv")
                .setProvider("PROVIDER")
                .setType(TableValidationError.Type.MISSING_FILE)));
        assertEquals(
            expected.get(0).getErrorType(),
            metadataValidator.validate(incompleteFileSet, new HashMap<>(), provider).get(0).getErrorType()
        );
        assertEquals(
            expected.get(0).getProvider(),
            metadataValidator.validate(incompleteFileSet, new HashMap<>(), provider).get(0).getProvider()
        );
    }

    @Test public void validate_givenMissingColumnDefinedInColSpec_addsMissingColErrorTotErrorList() {
        ArrayList<TableValidationError> expected = new ArrayList<>(
            Arrays.asList(TableValidationError.create("metadata-patient.tsv")
                .setProvider(provider)
                .setType(TableValidationError.Type.MISSING_COL)
                .setColumn("missing_field")));
        Map<String, ColumnSpecification> columnSpecifications = new HashMap<>();
        Arrays.asList("metadata-patient.tsv").stream().forEach(
            s -> columnSpecifications.put(s, new ColumnSpecification(
                Table.create().addColumns(StringColumn.create("missing_field"))
            )));
        assertEquals(
            expected.get(0).getColumn(),
            metadataValidator.validate(completeFileSet, columnSpecifications, provider).get(0).getColumn()
        );
    }

    private Map<String, Table> makeCompleteFileSet() {
        Map<String, Table> completeFileSet = new HashMap<>();
        Arrays.asList(
            "metadata-loader.tsv",
            "metadata-checklist.tsv",
            "metadata-sharing.tsv",
            "metadata-model_validation.tsv",
            "metadata-patient.tsv",
            "metadata-model.tsv",
            "metadata-sample.tsv"
        ).stream().forEach(s -> completeFileSet.put(s, Table.create()));
        return completeFileSet;
    }

    public Map<String, Table> makeIncompleteFileSet() {
        Map<String, Table> incompleteFileSet = new HashMap<>();
        incompleteFileSet.putAll(completeFileSet);
        incompleteFileSet.remove("metadata-patient.tsv");
        return incompleteFileSet;
    }

}