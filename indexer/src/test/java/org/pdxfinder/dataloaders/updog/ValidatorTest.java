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
    private final String LEFT_TABLE = "left_table.tsv";
    private final String RIGHT_TABLE = "right_table.tsv";
    private final Pair<Pair<String, String>, Pair<String, String>> RELATION =
        Pair.of(Pair.of(LEFT_TABLE, "id"), Pair.of(RIGHT_TABLE, "table_1_id"));
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
        List<ValidationError> expected = Collections.singletonList(
            ValidationError.missingFile(TABLE_1).setProvider(PROVIDER));
        assertEquals(
            expected.toString(),
            validator.validate(incompleteTableSet, requireTable).toString()
        );
    }

    @Test public void checkAllRequiredColsPresent_givenMissingColumnDefinedInColSpec_addsMissingColErrorTotErrorList() {
        List<ValidationError> expected = Collections.singletonList(
                ValidationError.missingColumn(TABLE_1, "missing_column").setProvider(PROVIDER));
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

    @Test public void checkAllNonEmptyValuesPresent_givenNoMissingValues_emptyErrorList() {
        Map<String, Table> fileSetWithValidTable = new HashMap<>();
        Table tableWithNoMissingValues = completeTableSet.get(TABLE_1).addColumns(
            StringColumn.create("required_col", Collections.singletonList("required_value")));
        fileSetWithValidTable.put(TABLE_1, tableWithNoMissingValues);
        assertThat(validator.validate(fileSetWithValidTable, requireColumn).isEmpty(), is(true));
    }

    @Test public void checkAllNonEmptyValuesPresent_givenMissingValue_addsMissingValueErrorToErrorList() {
        Map<String, Table> fileSetWithInvalidTable = new HashMap<>();
        Table tableWithMissingValue = completeTableSet.get(TABLE_1).addColumns(
            StringColumn.create("required_col", Collections.singletonList("")));
        fileSetWithInvalidTable.put(TABLE_1, tableWithMissingValue);

        List<ValidationError> expected = Collections.singletonList(
                ValidationError
                    .missingRequiredValue(
                        TABLE_1,
                        "required_col",
                        tableWithMissingValue)
                    .setProvider(PROVIDER));
        assertEquals(
            expected.toString(),
            validator.validate(fileSetWithInvalidTable, requireColumn).toString()
        );
    }

    @Test public void checkAllNonEmptyValuesPresent_givenMissingValueInRow2_addsMissingValueErrorToErrorList() {
        Map<String, Table> fileSetWithInvalidTable = new HashMap<>();
        Table tableWithMissingValue = completeTableSet.get(TABLE_1).addColumns(
            StringColumn.create("required_col", Arrays.asList("value_1", "")),
            StringColumn.create("other_col", Arrays.asList("", "This is the invalid row"))
        );
        fileSetWithInvalidTable.put(TABLE_1, tableWithMissingValue);

        List<ValidationError> expected = Collections.singletonList(
            ValidationError
                .missingRequiredValue(
                    TABLE_1,
                    "required_col",
                    tableWithMissingValue.where(tableWithMissingValue.stringColumn("required_col").isEqualTo("")))
                .setProvider(PROVIDER));
        assertEquals(
            expected.toString(),
            validator.validate(fileSetWithInvalidTable, requireColumn).toString()
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
        List<ValidationError> expected = Arrays.asList(
            ValidationError.duplicateValue(TABLE_1, "unique_col", duplicateValue).setProvider(PROVIDER)
        );

        assertEquals(
            expected.toString(),
            validator.validate(tableSetWithDuplicateValues, tableSetSpecification).toString()
        );
    }

    @Test public void checkRelationsValid_givenValidOneToManyJoin_emptyErrorList() {
        Map<String, Table> tableSetWithSimpleJoin = makeTableSetWithSimpleJoin();
        assertThat(validator.validate(tableSetWithSimpleJoin, SIMPLE_JOIN_SPECIFICATION).isEmpty(), is(true));
    }

    @Test public void checkRelationsValid_givenNoLeftTable_ErrorListWithMissingRequiredCol() {
        Map<String, Table> tableSetWithSimpleJoin = makeTableSetWithSimpleJoin();
        tableSetWithSimpleJoin.get(LEFT_TABLE).removeColumns("id");

        ValidationError expected = ValidationError
            .brokenRelation(LEFT_TABLE, RELATION, tableSetWithSimpleJoin.get(LEFT_TABLE).emptyCopy())
            .setDescription(String.format("because [%s] is missing column [%s]", LEFT_TABLE, "id"))
            .setProvider(PROVIDER);

        assertEquals(
            Collections.singletonList(expected).toString(),
            validator.validate(tableSetWithSimpleJoin, SIMPLE_JOIN_SPECIFICATION).toString()
        );
    }

    @Test public void checkRelationsValid_givenNoRightTable_ErrorListWithMissingRequiredCol() {
        Map<String, Table> tableSetWithSimpleJoin = makeTableSetWithSimpleJoin();
        tableSetWithSimpleJoin.get(RIGHT_TABLE).removeColumns("table_1_id");

        ValidationError expected = ValidationError
            .brokenRelation(RIGHT_TABLE, RELATION, tableSetWithSimpleJoin.get(RIGHT_TABLE).emptyCopy())
            .setDescription(String.format("because [%s] is missing column [%s]", RIGHT_TABLE, "table_1_id"))
            .setProvider(PROVIDER);

        assertEquals(
            Collections.singletonList(expected).toString(),
            validator.validate(tableSetWithSimpleJoin, SIMPLE_JOIN_SPECIFICATION).toString()
        );
    }

    @Test public void checkRelationsValid_givenMissingValueInRightColumn_ErrorListWithOrphanLeftRows() {
        Map<String, Table> tableSetWithSimpleJoin = makeTableSetWithSimpleJoin();
        tableSetWithSimpleJoin.get(RIGHT_TABLE).replaceColumn(
            StringColumn.create("table_1_id", Collections.EMPTY_LIST)
        );
        ValidationError expected = ValidationError
            .brokenRelation(RIGHT_TABLE, RELATION, tableSetWithSimpleJoin.get(LEFT_TABLE))
            .setDescription(String.format("1 orphan row(s) found in [%s]", LEFT_TABLE))
            .setProvider(PROVIDER);
        assertEquals(
            Collections.singletonList(expected).toString(),
            validator.validate(tableSetWithSimpleJoin, SIMPLE_JOIN_SPECIFICATION).toString()
        );
    }

    @Test public void checkRelationsValid_givenMissingValuesInLeftColumn_ErrorListWithOrphanRightRows() {
        Map<String, Table> tableSetWithSimpleJoin = makeTableSetWithSimpleJoin();
        tableSetWithSimpleJoin.get(LEFT_TABLE).replaceColumn(
            StringColumn.create("id", Collections.EMPTY_LIST)
        );
        ValidationError expected = ValidationError
            .brokenRelation(LEFT_TABLE, RELATION, tableSetWithSimpleJoin.get(RIGHT_TABLE))
            .setDescription(String.format("1 orphan row(s) found in [%s]", RIGHT_TABLE))
            .setProvider(PROVIDER);
        assertEquals(
            Collections.singletonList(expected).toString(),
            validator.validate(tableSetWithSimpleJoin, SIMPLE_JOIN_SPECIFICATION).toString()
        );
    }

    @Test public void checkRelationsValid_givenMissingValueInLeftAndRightColumn_ErrorListWithMissingValueRows() {
        Map<String, Table> tableSetWithSimpleJoin = makeTableSetWithSimpleJoin();
        tableSetWithSimpleJoin.get(RIGHT_TABLE).replaceColumn(
            StringColumn.create("table_1_id", Arrays.asList("not 1", "not 1"))
        );
        List<ValidationError> expected = Arrays.asList(
            ValidationError
                .brokenRelation(RIGHT_TABLE, RELATION, tableSetWithSimpleJoin.get(LEFT_TABLE))
                .setDescription(String.format("1 orphan row(s) found in [%s]", LEFT_TABLE))
                .setProvider(PROVIDER),
            ValidationError
                .brokenRelation(LEFT_TABLE, RELATION, tableSetWithSimpleJoin.get(RIGHT_TABLE))
                .setDescription(String.format("2 orphan row(s) found in [%s]", RIGHT_TABLE))
                .setProvider(PROVIDER)
        );
        assertEquals(
            expected.toString(),
            validator.validate(tableSetWithSimpleJoin, SIMPLE_JOIN_SPECIFICATION).toString()
        );
    }


    private Set<String> minimalRequiredTable = Stream.of(TABLE_1).collect(Collectors.toSet());

    private TableSetSpecification requireTable = TableSetSpecification.create().setProvider(PROVIDER)
        .addRequiredTables(minimalRequiredTable);

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

    private Map<String, Table> makeTableSetWithSimpleJoin() {
        Table leftTable = Table.create(LEFT_TABLE).addColumns(
            StringColumn.create("id", Collections.singletonList("1")));
        Table rightTable = Table.create(RIGHT_TABLE).addColumns(
            StringColumn.create("table_1_id", Collections.singletonList("1")));
        Map<String, Table> tableSetWithSimpleJoin = new HashMap<>();
        tableSetWithSimpleJoin.put(LEFT_TABLE, leftTable);
        tableSetWithSimpleJoin.put(RIGHT_TABLE, rightTable);
        return tableSetWithSimpleJoin;
    }

    private final TableSetSpecification SIMPLE_JOIN_SPECIFICATION = TableSetSpecification.create().setProvider(PROVIDER)
        .addHasRelations(RELATION.getKey(), RELATION.getValue());
}