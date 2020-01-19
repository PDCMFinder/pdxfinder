package org.pdxfinder.dataloaders.updog;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import tech.tablesaw.api.Table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertEquals;

public class MetadataValidatorTest {

    public Map<String, Table> completeFileSet = new HashMap<>();
    public Map<String, Table> incompleteFileSet = new HashMap<>();

    @Before public void setUp() {
        MockitoAnnotations.initMocks(this);

        completeFileSet = makeCompleteFileSet();
        incompleteFileSet = makeIncompleteFileSet();
    }

    @InjectMocks private MetadataValidator metadataValidator;

    @Test public void validate_givenEmptyFileSet_failsValidation() {
        Map<String, Table> emptyHashMap = new HashMap<>();
        assertThat(metadataValidator.validate(emptyHashMap), is(false));
    }

    @Test public void validate_givenIncompleteFileSet_failsValidation() {
        Map<String, Table> incompleteFileSet = makeIncompleteFileSet();
        assertThat(metadataValidator.validate(incompleteFileSet), is(false));
    }

    @Test public void validate_givenCompleteFileSet_passesValidation() {
        assertThat(metadataValidator.validate(completeFileSet), is(true));
    }

    @Test public void validate_givenExtraFileInFileSet_passesValidation() {
        Map<String, Table> completeFileSetPlusOne = new HashMap<>();
        completeFileSetPlusOne.putAll(completeFileSet);
        completeFileSetPlusOne.put("extra-file.tsv", Table.create());
        assertThat(metadataValidator.validate(completeFileSetPlusOne), is(true));
    }

    @Test public void getValidationErrors_givenNoValidation_producesEmptyErrorList() {
        assertThat(metadataValidator.getValidationErrors().isEmpty(), is(true));
    }

    @Test public void validateAndGetErrors_givenIncompleteFileSet_addsAnErrorToErrorList() {
        Map<String, Table> incompleteFileSet = makeIncompleteFileSet();
        ArrayList<TableValidationError> expected = new ArrayList<>(
            Arrays.asList(
            TableValidationError.create("metadata-patient.tsv")));
        assertEquals(
            expected.get(0).toString(),
            metadataValidator.validateAndGetErrors(incompleteFileSet).get(0).toString()
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