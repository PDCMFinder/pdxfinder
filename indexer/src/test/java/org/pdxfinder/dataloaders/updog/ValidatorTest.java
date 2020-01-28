package org.pdxfinder.dataloaders.updog;

import org.apache.commons.lang3.tuple.Pair;
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

public class ValidatorTest {

    private Map<String, Table> completeTableSet = new HashMap<>();
    private Map<String, Table> incompleteTableSet = new HashMap<>();
    private final String PROVIDER = "PROVIDER";

    @Before public void setUp() {
        MockitoAnnotations.initMocks(this);

        completeTableSet = makeCompleteTableSet();
        incompleteTableSet = makeIncompleteTableSet();
    }

    @InjectMocks private Validator validator;

    @Test public void passesValidation_givenEmptyFileSet_failsValidation() {
        Map<String, Table> emptyHashMap = new HashMap<>();
        assertThat(validator.passesValidation(emptyHashMap, basicTableSetSpecification), is(false));
    }

    @Test public void passesValidation_givenEmptyFileSetAndEmptySpecificaiton_passesValidation() {
        Map<String, Table> emptyHashMap = new HashMap<>();
        TableSetSpecification emptyTableSetSpecification = TableSetSpecification.create();
        assertThat(validator.passesValidation(emptyHashMap, emptyTableSetSpecification), is(true));
    }

    @Test public void passesValidation_givenIncompleteFileSet_failsValidation() {
        assertThat(validator.passesValidation(incompleteTableSet, basicTableSetSpecification), is(false));
    }

    @Test public void passesValidation_givenCompleteFileSet_passesValidation() {
        assertThat(validator.passesValidation(completeTableSet, basicTableSetSpecification), is(true));
    }

    @Test public void passesValidation_givenExtraFileInFileSet_passesValidation() {
        Map<String, Table> completeFileSetPlusOne = new HashMap<>(completeTableSet);
        completeFileSetPlusOne.put("extra-file.tsv", Table.create());
        assertThat(validator.passesValidation(completeFileSetPlusOne, basicTableSetSpecification), is(true));
    }

    @Test public void validate_givenNoValidation_producesEmptyErrorList() {
        assertThat(validator.getValidationErrors().isEmpty(), is(true));
    }

    @Test public void validate_givenCompleteFileSet_producesEmptyErrorList() {
        assertThat(validator.validate(completeTableSet, basicTableSetSpecification).isEmpty(), is(true));
    }

    @Test public void validate_givenIncompleteFileSet_addsErrorWithCorrectContextToErrorList() {
        ArrayList<TableValidationError> expected = new ArrayList<>(
            Collections.singletonList(TableValidationError.missingFile("metadata-patient.tsv").setProvider(PROVIDER)));
        assertEquals(
            expected.toString(),
            validator.validate(incompleteTableSet, basicTableSetSpecification).toString()
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
        TableSetSpecification tableSetSpecification = TableSetSpecification
            .create()
            .addRequiredColumnSets(columnSpecifications)
            .setProvider(PROVIDER);
        assertEquals(
            expected.toString(),
            validator.validate(completeTableSet, tableSetSpecification).toString()
        );
    }

    @Test public void checkAllRequiredValuesPresent_givenMissingValue_addsMissingValueErrorToErrorList() {
        Map<String, Table> fileSetWithInvalidTable = new HashMap<>();
        Table tableWithMissingValue = completeTableSet.get("metadata-patient.tsv").addColumns(
            StringColumn.create("required_col", Collections.singletonList("")));
        fileSetWithInvalidTable.put("metadata-patient.tsv", tableWithMissingValue);

        Set<String> requiredFile = new HashSet<>();
        requiredFile.add("metadata-patient.tsv");

        TableSetSpecification tableSetSpecification =  TableSetSpecification
            .create()
            .addRequiredColumns(Pair.of("metadata-patient.tsv", "required_col"))
            .setProvider(PROVIDER);
        ArrayList<TableValidationError> expected = new ArrayList<>(
            Collections.singletonList(
                TableValidationError
                    .missingRequiredValue("metadata-patient.tsv", "required_col", tableWithMissingValue.row(0))
                    .setProvider(PROVIDER)));
        assertEquals(
            expected,
            validator.validate(fileSetWithInvalidTable, tableSetSpecification)
        );
    }

    @Test public void checkAllRequiredValuesPresent_givenMissingValueInRow2_addsMissingValueErrorToErrorList() {
        Map<String, Table> fileSetWithInvalidTable = new HashMap<>();
        Table tableWithMissingValue = completeTableSet.get("metadata-patient.tsv").addColumns(
            StringColumn.create("required_col", Arrays.asList("value_1", "")));
        fileSetWithInvalidTable.put("metadata-patient.tsv", tableWithMissingValue);

        Set<String> requiredFile = new HashSet<>();
        requiredFile.add("metadata-patient.tsv");

        TableSetSpecification tableSetSpecification =  TableSetSpecification
            .create()
            .addRequiredColumns(Pair.of("metadata-patient.tsv", "required_col"))
            .setProvider(PROVIDER);

        ArrayList<TableValidationError> expected = new ArrayList<>(
            Collections.singletonList(
                TableValidationError
                    .missingRequiredValue("metadata-patient.tsv", "required_col", tableWithMissingValue.row(1))
                    .setProvider(PROVIDER)));
        assertEquals(
            expected,
            validator.validate(fileSetWithInvalidTable, tableSetSpecification)
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

    private TableSetSpecification basicTableSetSpecification = TableSetSpecification
        .create()
        .addRequiredFileList(requiredFiles)
        .setProvider(PROVIDER);

    private Map<String, Table> makeCompleteTableSet() {
        Map<String, Table> completeFileSet = new HashMap<>();
        requiredFiles.forEach(s -> completeFileSet.put(s, Table.create()));
        return completeFileSet;
    }

    private Map<String, Table> makeIncompleteTableSet() {
        Map<String, Table> incompleteFileSet = new HashMap<>(completeTableSet);
        incompleteFileSet.remove("metadata-patient.tsv");
        return incompleteFileSet;
    }

}