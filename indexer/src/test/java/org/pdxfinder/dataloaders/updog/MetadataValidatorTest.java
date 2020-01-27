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
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class MetadataValidatorTest {

    private Map<String, Table> completeFileSet = new HashMap<>();
    private Map<String, Table> incompleteFileSet = new HashMap<>();
    private String provider = "PROVIDER";

    @Before public void setUp() {
        MockitoAnnotations.initMocks(this);

        completeFileSet = makeCompleteFileSet();
        incompleteFileSet = makeIncompleteFileSet();
    }

    @InjectMocks private MetadataValidator metadataValidator;

    @Test public void passesValidation_givenEmptyFileSet_failsValidation() {
        Map<String, Table> emptyHashMap = new HashMap<>();
        assertThat(metadataValidator.passesValidation(emptyHashMap, fileSetSpecification, provider), is(false));
    }

    @Test public void passesValidation_givenEmptyFileSetAndEmptySpecificaiton_passesValidation() {
        Map<String, Table> emptyHashMap = new HashMap<>();
        FileSetSpecification emptyFileSetSpecification = FileSetSpecification.create();
        assertThat(metadataValidator.passesValidation(emptyHashMap, emptyFileSetSpecification, provider), is(true));
    }

    @Test public void passesValidation_givenIncompleteFileSet_failsValidation() {
        assertThat(metadataValidator.passesValidation(incompleteFileSet, fileSetSpecification, provider), is(false));
    }

    @Test public void passesValidation_givenCompleteFileSet_passesValidation() {
        assertThat(metadataValidator.passesValidation(completeFileSet, fileSetSpecification, provider), is(true));
    }

    @Test public void passesValidation_givenExtraFileInFileSet_passesValidation() {
        Map<String, Table> completeFileSetPlusOne = new HashMap<>();
        completeFileSetPlusOne.putAll(completeFileSet);
        completeFileSetPlusOne.put("extra-file.tsv", Table.create());
        assertThat(metadataValidator.passesValidation(completeFileSetPlusOne, fileSetSpecification, provider), is(true));
    }

    @Test public void validate_givenNoValidation_producesEmptyErrorList() {
        assertThat(metadataValidator.getValidationErrors().isEmpty(), is(true));
    }

    @Test public void validate_givenCompleteFileSet_producesEmptyErrorList() {
        assertThat(metadataValidator.validate(completeFileSet, fileSetSpecification, new HashMap<>(), provider).isEmpty(), is(true));
    }

    @Test public void validate_givenIncompleteFileSet_addsErrorWithCorrectContextToErrorList() {
        ArrayList<TableValidationError> expected = new ArrayList<>(
            Arrays.asList(TableValidationError
                .missingFile("metadata-patient.tsv")
                .setProvider("PROVIDER")));
        assertEquals(
            expected.toString(),
            metadataValidator.validate(incompleteFileSet, fileSetSpecification, new HashMap<>(), provider).toString()
        );
    }

    @Test public void validate_givenMissingColumnDefinedInColSpec_addsMissingColErrorTotErrorList() {
        ArrayList<TableValidationError> expected = new ArrayList<>(
            Arrays.asList(
                TableValidationError
                    .missingColumn("metadata-patient.tsv", "missing_field")
                    .setProvider(provider)));
        Map<String, ColumnSpecification> columnSpecifications = new HashMap<>();
        Arrays.asList("metadata-patient.tsv").stream().forEach(
            s -> columnSpecifications.put(s, new ColumnSpecification(
                Table.create().addColumns(StringColumn.create("missing_field"))
            )));
        assertEquals(
            expected.toString(),
            metadataValidator.validate(completeFileSet, fileSetSpecification, columnSpecifications, provider).toString()
        );
    }

    @Test public void checkAllRequiredValuesPresent_givenMissingValue_addsMissingValueErrorToErrorList() {

    }

    private List<String> requiredFiles = Arrays.asList(
        "metadata-loader.tsv",
        "metadata-sharing.tsv",
        "metadata-model_validation.tsv",
        "metadata-patient.tsv",
        "metadata-model.tsv",
        "metadata-sample.tsv"
    );

    private FileSetSpecification fileSetSpecification = FileSetSpecification
        .create()
        .addRequiredFileList(requiredFiles);

    private Map<String, Table> makeCompleteFileSet() {
        Map<String, Table> completeFileSet = new HashMap<>();
        requiredFiles.stream().forEach(s -> completeFileSet.put(s, Table.create()));
        return completeFileSet;
    }

    private Map<String, Table> makeIncompleteFileSet() {
        Map<String, Table> incompleteFileSet = new HashMap<>();
        incompleteFileSet.putAll(completeFileSet);
        incompleteFileSet.remove("metadata-patient.tsv");
        return incompleteFileSet;
    }

}