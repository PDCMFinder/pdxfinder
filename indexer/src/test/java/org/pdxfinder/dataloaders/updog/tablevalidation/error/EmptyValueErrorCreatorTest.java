package org.pdxfinder.dataloaders.updog.tablevalidation.error;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.pdxfinder.dataloaders.updog.TableUtilities;
import org.pdxfinder.dataloaders.updog.tablevalidation.ColumnReference;
import org.pdxfinder.dataloaders.updog.tablevalidation.Relation;
import org.pdxfinder.dataloaders.updog.tablevalidation.TableSetSpecification;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EmptyValueErrorCreatorTest {

    private Map<String, Table> completeTableSet = new HashMap<>();
    private final String TABLE_1 = "table_1.tsv";
    private final String LEFT_TABLE = "left_table.tsv";
    private final String RIGHT_TABLE = "right_table.tsv";
    private final Relation RELATION = Relation.between(
        ColumnReference.of(LEFT_TABLE, "id"),
        ColumnReference.of(RIGHT_TABLE, "table_1_id")
    );
    private final String PROVIDER = "PROVIDER-BC";
    private EmptyValueErrorCreator emptyValueErrorCreator = new EmptyValueErrorCreator();

    private Set<String> minimalRequiredTable = Stream.of(TABLE_1).collect(Collectors.toSet());
    private Map<String, Table> makeCompleteTableSet() {
        Map<String, Table> completeFileSet = new HashMap<>();
        minimalRequiredTable.forEach(s -> completeFileSet.put(s, Table.create()));
        return completeFileSet;
    }
    private TableSetSpecification requireColumn = TableSetSpecification.create().setProvider(PROVIDER)
        .addNonEmptyColumns(ColumnReference.of(TABLE_1, "required_col"));


    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        completeTableSet = makeCompleteTableSet();
    }

    @Test public void checkAllNonEmptyValuesPresent_givenNoMissingValues_emptyErrorList() {
        Map<String, Table> fileSetWithValidTable = new HashMap<>();
        Table tableWithNoMissingValues = TableUtilities.fromString(
            TABLE_1,
            "required_col",
            "required_value");
        fileSetWithValidTable.put(TABLE_1, tableWithNoMissingValues);

        assertTrue(emptyValueErrorCreator.generateErrors(fileSetWithValidTable, requireColumn).isEmpty());
    }

    @Test public void checkAllNonEmptyValuesPresent_givenMissingValue_addsMissingValueErrorToErrorList() {
        Map<String, Table> fileSetWithInvalidTable = new HashMap<>();
        Table tableWithMissingValue = completeTableSet.get(TABLE_1).addColumns(
            StringColumn.create("required_col", Collections.singletonList("")));
        ColumnReference requiredCol =  ColumnReference.of(TABLE_1, "required_col");

        fileSetWithInvalidTable.put(TABLE_1, tableWithMissingValue);
        List<ValidationError> expected = Collections.singletonList(
            emptyValueErrorCreator.create(requiredCol, tableWithMissingValue, PROVIDER)
        );

        assertEquals(
            expected.toString(),
            emptyValueErrorCreator.generateErrors(fileSetWithInvalidTable, requireColumn).toString()
        );
    }

    @Test public void checkAllNonEmptyValuesPresent_givenMissingValueInRow2_addsMissingValueErrorToErrorList() {
        Map<String, Table> fileSetWithInvalidTable = new HashMap<>();
        Table tableWithMissingValue = completeTableSet.get(TABLE_1).addColumns(
            StringColumn.create("required_col", Arrays.asList("value_1", "")),
            StringColumn.create("other_col", Arrays.asList("", "This is the invalid row"))
        );
        fileSetWithInvalidTable.put(TABLE_1, tableWithMissingValue);
        ColumnReference requiredCol =  ColumnReference.of(TABLE_1, "required_col");

        List<ValidationError> expected = Collections.singletonList(
            emptyValueErrorCreator.create(
                requiredCol,
                tableWithMissingValue.where(tableWithMissingValue.stringColumn("required_col").isEqualTo("")),
                PROVIDER));
        assertEquals(
            expected.toString(),
            emptyValueErrorCreator.generateErrors(fileSetWithInvalidTable, requireColumn).toString()
        );
    }

}