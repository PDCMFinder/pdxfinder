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
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        assertThat(metadataValidator.passesValidation(emptyHashMap, basicFileSetSpecification, provider), is(false));
    }

    @Test public void passesValidation_givenEmptyFileSetAndEmptySpecificaiton_passesValidation() {
        Map<String, Table> emptyHashMap = new HashMap<>();
        FileSetSpecification emptyFileSetSpecification = FileSetSpecification.create();
        assertThat(metadataValidator.passesValidation(emptyHashMap, emptyFileSetSpecification, provider), is(true));
    }

    @Test public void passesValidation_givenIncompleteFileSet_failsValidation() {
        assertThat(metadataValidator.passesValidation(incompleteFileSet, basicFileSetSpecification, provider), is(false));
    }

    @Test public void passesValidation_givenCompleteFileSet_passesValidation() {
        assertThat(metadataValidator.passesValidation(completeFileSet, basicFileSetSpecification, provider), is(true));
    }

    @Test public void passesValidation_givenExtraFileInFileSet_passesValidation() {
        Map<String, Table> completeFileSetPlusOne = new HashMap<>();
        completeFileSetPlusOne.putAll(completeFileSet);
        completeFileSetPlusOne.put("extra-file.tsv", Table.create());
        assertThat(metadataValidator.passesValidation(completeFileSetPlusOne, basicFileSetSpecification, provider), is(true));
    }

    @Test public void validate_givenNoValidation_producesEmptyErrorList() {
        assertThat(metadataValidator.getValidationErrors().isEmpty(), is(true));
    }

    @Test public void validate_givenCompleteFileSet_producesEmptyErrorList() {
        assertThat(metadataValidator.validate(completeFileSet, basicFileSetSpecification, provider).isEmpty(), is(true));
    }

    @Test public void validate_givenIncompleteFileSet_addsErrorWithCorrectContextToErrorList() {
        ArrayList<TableValidationError> expected = new ArrayList<>(
            Arrays.asList(TableValidationError
                .missingFile("metadata-patient.tsv")
                .setProvider("PROVIDER")));
        assertEquals(
            expected.toString(),
            metadataValidator.validate(incompleteFileSet, basicFileSetSpecification, provider).toString()
        );
    }

    @Test public void checkAllRequiredColsPresent_givenMissingColumnDefinedInColSpec_addsMissingColErrorTotErrorList() {
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
        FileSetSpecification fileSetSpecification = FileSetSpecification
            .create()
            .addRequiredColumnSets(columnSpecifications)
            .build();
        assertEquals(
            expected.toString(),
            metadataValidator.validate(completeFileSet, fileSetSpecification, provider).toString()
        );
    }

    @Test public void checkAllRequiredValuesPresent_givenMissingValue_addsMissingValueErrorToErrorList() {
    }

    private Set<String> requiredFiles = Stream.of(
        "metadata-loader.tsv",
        "metadata-sharing.tsv",
        "metadata-model_validation.tsv",
        "metadata-patient.tsv",
        "metadata-model.tsv",
        "metadata-sample.tsv"
    ).collect(Collectors.toSet());

    private FileSetSpecification basicFileSetSpecification = FileSetSpecification
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