package org.pdxfinder.dataloaders.updog;

import javafx.util.Pair;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
    private final String PROVIDER = "PROVIDER";

    @Before public void setUp() {
        MockitoAnnotations.initMocks(this);

        completeFileSet = makeCompleteFileSet();
        incompleteFileSet = makeIncompleteFileSet();
    }

    @InjectMocks private MetadataValidator metadataValidator;

    @Test public void passesValidation_givenEmptyFileSet_failsValidation() {
        Map<String, Table> emptyHashMap = new HashMap<>();
        assertThat(metadataValidator.passesValidation(emptyHashMap, basicFileSetSpecification, PROVIDER), is(false));
    }

    @Test public void passesValidation_givenEmptyFileSetAndEmptySpecificaiton_passesValidation() {
        Map<String, Table> emptyHashMap = new HashMap<>();
        FileSetSpecification emptyFileSetSpecification = FileSetSpecification.create();
        assertThat(metadataValidator.passesValidation(emptyHashMap, emptyFileSetSpecification, PROVIDER), is(true));
    }

    @Test public void passesValidation_givenIncompleteFileSet_failsValidation() {
        assertThat(metadataValidator.passesValidation(incompleteFileSet, basicFileSetSpecification, PROVIDER), is(false));
    }

    @Test public void passesValidation_givenCompleteFileSet_passesValidation() {
        assertThat(metadataValidator.passesValidation(completeFileSet, basicFileSetSpecification, PROVIDER), is(true));
    }

    @Test public void passesValidation_givenExtraFileInFileSet_passesValidation() {
        Map<String, Table> completeFileSetPlusOne = new HashMap<>(completeFileSet);
        completeFileSetPlusOne.put("extra-file.tsv", Table.create());
        assertThat(metadataValidator.passesValidation(completeFileSetPlusOne, basicFileSetSpecification, PROVIDER), is(true));
    }

    @Test public void validate_givenNoValidation_producesEmptyErrorList() {
        assertThat(metadataValidator.getValidationErrors().isEmpty(), is(true));
    }

    @Test public void validate_givenCompleteFileSet_producesEmptyErrorList() {
        assertThat(metadataValidator.validate(completeFileSet, basicFileSetSpecification, PROVIDER).isEmpty(), is(true));
    }

    @Test public void validate_givenIncompleteFileSet_addsErrorWithCorrectContextToErrorList() {
        ArrayList<TableValidationError> expected = new ArrayList<>(
            Collections.singletonList(TableValidationError.missingFile("metadata-patient.tsv").setProvider("PROVIDER")));
        assertEquals(
            expected.toString(),
            metadataValidator.validate(incompleteFileSet, basicFileSetSpecification, PROVIDER).toString()
        );
    }

    @Test public void checkAllRequiredColsPresent_givenMissingColumnDefinedInColSpec_addsMissingColErrorTotErrorList() {
        ArrayList<TableValidationError> expected = new ArrayList<>(
            Collections.singletonList(
                TableValidationError
                    .missingColumn("metadata-patient.tsv", "missing_column")
                    .setProvider(PROVIDER)));
        Map<String, ColumnSpecification> columnSpecifications = new HashMap<>();
        Collections.singletonList("metadata-patient.tsv").forEach(
            s -> columnSpecifications.put(s, new ColumnSpecification(
                Table.create().addColumns(StringColumn.create("missing_column"))
            )));
        FileSetSpecification fileSetSpecification = FileSetSpecification
            .create()
            .addRequiredColumnSets(columnSpecifications)
            .build();
        assertEquals(
            expected.toString(),
            metadataValidator.validate(completeFileSet, fileSetSpecification, PROVIDER).toString()
        );
    }

    @Test public void checkAllRequiredValuesPresent_givenMissingValue_addsMissingValueErrorToErrorList() {
        Map<String, Table> fileSetWithInvalidTable = new HashMap<>();
        Table tableWithMissingValue = completeFileSet.get("metadata-patient.tsv").addColumns(
            StringColumn.create("required_col", Collections.singletonList("")));
        fileSetWithInvalidTable.put("metadata-patient.tsv", tableWithMissingValue);

        Set<String> requiredFile = new HashSet<>();
        requiredFile.add("metadata-patient.tsv");

        FileSetSpecification fileSetSpecification =  FileSetSpecification
            .create()
            .addRequiredColumns(new Pair<>("metadata-patient.tsv", "required_col"));

        ArrayList<TableValidationError> expected = new ArrayList<>(
            Collections.singletonList(
                TableValidationError
                    .missingRequiredValue("metadata-patient.tsv", "required_col", tableWithMissingValue.row(0))
                    .setProvider(PROVIDER)));
        assertEquals(
            expected,
            metadataValidator.validate(fileSetWithInvalidTable, fileSetSpecification,PROVIDER)
        );
    }

    @Test public void checkAllRequiredValuesPresent_givenMissingValueInRow2_addsMissingValueErrorToErrorList() {
        Map<String, Table> fileSetWithInvalidTable = new HashMap<>();
        Table tableWithMissingValue = completeFileSet.get("metadata-patient.tsv").addColumns(
            StringColumn.create("required_col", Arrays.asList("value_1", "")));
        fileSetWithInvalidTable.put("metadata-patient.tsv", tableWithMissingValue);

        Set<String> requiredFile = new HashSet<>();
        requiredFile.add("metadata-patient.tsv");

        FileSetSpecification fileSetSpecification =  FileSetSpecification
            .create()
            .addRequiredColumns(new Pair<>("metadata-patient.tsv", "required_col"));

        ArrayList<TableValidationError> expected = new ArrayList<>(
            Collections.singletonList(
                TableValidationError
                    .missingRequiredValue("metadata-patient.tsv", "required_col", tableWithMissingValue.row(1))
                    .setProvider(PROVIDER)));
        assertEquals(
            expected,
            metadataValidator.validate(fileSetWithInvalidTable, fileSetSpecification,PROVIDER)
        );
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
        requiredFiles.forEach(s -> completeFileSet.put(s, Table.create()));
        return completeFileSet;
    }

    private Map<String, Table> makeIncompleteFileSet() {
        Map<String, Table> incompleteFileSet = new HashMap<>(completeFileSet);
        incompleteFileSet.remove("metadata-patient.tsv");
        return incompleteFileSet;
    }

}