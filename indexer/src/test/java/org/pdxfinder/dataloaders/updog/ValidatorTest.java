package org.pdxfinder.dataloaders.updog;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
    private final String TABLE_1 = "table_1.tsv";
    private final String TABLE_2 = "table_2.tsv";
    private final String PROVIDER = "PROVIDER-BC";

    @Before public void setUp() {
        MockitoAnnotations.initMocks(this);

        completeTableSet = makeCompleteTableSet();
        incompleteTableSet = makeIncompleteTableSet();
    }

    @InjectMocks private Validator validator;

    @Test public void passesValidation_givenEmptyFileSet_failsValidation() {
        Map<String, Table> emptyHashMap = new HashMap<>();
        assertThat(validator.passesValidation(emptyHashMap, requireTable), is(false));
    }

    @Test public void passesValidation_givenEmptyFileSetAndEmptySpecification_passesValidation() {
        Map<String, Table> emptyHashMap = new HashMap<>();
        TableSetSpecification emptyTableSetSpecification = TableSetSpecification.create();
        assertThat(validator.passesValidation(emptyHashMap, emptyTableSetSpecification), is(true));
    }

    @Test public void passesValidation_givenIncompleteFileSet_failsValidation() {
        assertThat(validator.passesValidation(incompleteTableSet, requireTable), is(false));
    }

    @Test public void passesValidation_givenCompleteFileSet_passesValidation() {
        assertThat(validator.passesValidation(completeTableSet, requireTable), is(true));
    }

    @Test public void passesValidation_givenExtraFileInFileSet_passesValidation() {
        Map<String, Table> completeFileSetPlusOne = new HashMap<>(completeTableSet);
        completeFileSetPlusOne.put("extra-table.tsv", Table.create());
        assertThat(validator.passesValidation(completeFileSetPlusOne, requireTable), is(true));
    }

    @Test public void validate_givenNoValidation_producesEmptyErrorList() {
        assertThat(validator.getValidationErrors().isEmpty(), is(true));
    }

    @Test public void validate_givenCompleteFileSet_producesEmptyErrorList() {
        assertThat(validator.validate(completeTableSet, requireTable).isEmpty(), is(true));
    }

    @Test public void validate_givenIncompleteFileSet_addsErrorWithCorrectContextToErrorList() {
        List<TableValidationError> expected = Collections.singletonList(
            TableValidationError.missingFile(TABLE_1).setProvider(PROVIDER));
        assertEquals(
            expected.toString(),
            validator.validate(incompleteTableSet, requireTable).toString()
        );
    }

    @Test public void checkAllRequiredColsPresent_givenMissingColumnDefinedInColSpec_addsMissingColErrorTotErrorList() {
        List<TableValidationError> expected = Collections.singletonList(
                TableValidationError.missingColumn(TABLE_1, "missing_column").setProvider(PROVIDER));
        Map<String, ColumnSpecification> columnSpecifications = new HashMap<>();
        Collections.singletonList(TABLE_1).forEach(
            s -> columnSpecifications.put(s, new ColumnSpecification(
                Table.create().addColumns(StringColumn.create("missing_column"))
            )));
        TableSetSpecification tableSetSpecification = TableSetSpecification.create().setProvider(PROVIDER)
            .addRequiredColumnSets(columnSpecifications);
        assertEquals(
            expected.toString(),
            validator.validate(completeTableSet, tableSetSpecification).toString()
        );
    }

    @Test public void checkAllRequiredValuesPresent_givenMissingValue_addsMissingValueErrorToErrorList() {
        Map<String, Table> fileSetWithInvalidTable = new HashMap<>();
        Table tableWithMissingValue = completeTableSet.get(TABLE_1).addColumns(
            StringColumn.create("required_col", Collections.singletonList("")));
        fileSetWithInvalidTable.put(TABLE_1, tableWithMissingValue);

        List<TableValidationError> expected = Collections.singletonList(
                TableValidationError
                    .missingRequiredValue(TABLE_1, "required_col", tableWithMissingValue.row(0))
                    .setProvider(PROVIDER));
        assertEquals(
            expected,
            validator.validate(fileSetWithInvalidTable, requireColumn)
        );
    }

    @Test public void checkAllRequiredValuesPresent_givenMissingValueInRow2_addsMissingValueErrorToErrorList() {
        Map<String, Table> fileSetWithInvalidTable = new HashMap<>();
        Table tableWithMissingValue = completeTableSet.get(TABLE_1).addColumns(
            StringColumn.create("required_col", Arrays.asList("value_1", "")));
        fileSetWithInvalidTable.put(TABLE_1, tableWithMissingValue);

        List<TableValidationError> expected = Collections.singletonList(
                TableValidationError
                    .missingRequiredValue(TABLE_1, "required_col", tableWithMissingValue.row(1))
                    .setProvider(PROVIDER));
        assertEquals(
            expected,
            validator.validate(fileSetWithInvalidTable, requireColumn)
        );
    }

    @Test public void checkUniqueValues_givenUniqueValues_emptyErrorList() {
        Map<String, Table> tableSetWithUniqueValues = new HashMap<>();
        Table tableWithUniqueValues = completeTableSet.get(TABLE_1).addColumns(
            StringColumn.create("unique_col", Arrays.asList("1", "2")));
        tableSetWithUniqueValues.put(TABLE_1, tableWithUniqueValues);

        TableSetSpecification tableSetSpecification = TableSetSpecification.create().setProvider(PROVIDER)
            .addUniqueColumns(Pair.of(TABLE_1, "unique_col"));

        assertThat(validator.validate(tableSetWithUniqueValues, tableSetSpecification).isEmpty(), is(true));
    }

    @Test public void checkUniqueValues_givenDuplicateValues_addsDuplicateValueErrorToErrorList() {
        Table tableWithUniqueValues = completeTableSet.get(TABLE_1).addColumns(
            StringColumn.create("unique_col", Arrays.asList("1", "1", "2")));
        Map<String, Table> tableSetWithDuplicateValues = new HashMap<>();
        tableSetWithDuplicateValues.put(TABLE_1, tableWithUniqueValues);

        TableSetSpecification tableSetSpecification = TableSetSpecification.create().setProvider(PROVIDER)
            .addUniqueColumns(Pair.of(TABLE_1, "unique_col"));

        Set<String> duplicateValue = Stream.of("1").collect(Collectors.toSet());
        List<TableValidationError> expected = Arrays.asList(
            TableValidationError.duplicateValue(TABLE_1, "unique_col", duplicateValue)
        );

        assertEquals(
            expected,
            validator.validate(tableSetWithDuplicateValues, tableSetSpecification)
        );
    }

    private Set<String> minimalRequiredTable = Stream.of(TABLE_1).collect(Collectors.toSet());

    private TableSetSpecification requireTable = TableSetSpecification.create().setProvider(PROVIDER)
        .addRequiredFileList(minimalRequiredTable);

    private TableSetSpecification requireColumn = TableSetSpecification.create().setProvider(PROVIDER)
        .addRequiredColumns(Pair.of(TABLE_1, "required_col"));

    private Map<String, Table> makeCompleteTableSet() {
        Map<String, Table> completeFileSet = new HashMap<>();
        minimalRequiredTable.forEach(s -> completeFileSet.put(s, Table.create()));
        return completeFileSet;
    }

    private Map<String, Table> makeIncompleteTableSet() {
        Map<String, Table> incompleteFileSet = new HashMap<>(completeTableSet);
        incompleteFileSet.remove(TABLE_1);
        return incompleteFileSet;
    }

}