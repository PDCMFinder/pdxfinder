package org.pdxfinder.dataloaders.updog.tablevalidation.error;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.pdxfinder.dataloaders.updog.tablevalidation.ColumnReference;
import org.pdxfinder.dataloaders.updog.tablevalidation.Relation;
import org.pdxfinder.dataloaders.updog.tablevalidation.TableSetSpecification;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class DuplicateValueErrorCreatorTest {

    private Map<String, Table> completeTableSet = new HashMap<>();
    private final String TABLE_1 = "table_1.tsv";
    private final String LEFT_TABLE = "left_table.tsv";
    private final String RIGHT_TABLE = "right_table.tsv";
    private final Relation RELATION = Relation.between(
        ColumnReference.of(LEFT_TABLE, "id"),
        ColumnReference.of(RIGHT_TABLE, "table_1_id")
    );
    private final String PROVIDER = "PROVIDER-BC";

    private DuplicateValueErrorCreator duplicateValueErrorCreator = new DuplicateValueErrorCreator();

    private Set<String> minimalRequiredTable = Stream.of(TABLE_1).collect(Collectors.toSet());

    private Map<String, Table> makeCompleteTableSet() {
        Map<String, Table> completeFileSet = new HashMap<>();
        minimalRequiredTable.forEach(s -> completeFileSet.put(s, Table.create()));
        return completeFileSet;
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        completeTableSet = makeCompleteTableSet();
    }

    @Test public void checkUniqueValues_givenUniqueValues_emptyErrorList() {
        Map<String, Table> tableSetWithUniqueValues = new HashMap<>();
        Table tableWithUniqueValues = completeTableSet.get(TABLE_1).addColumns(
            StringColumn.create("unique_col", Arrays.asList("1", "2")));
        tableSetWithUniqueValues.put(TABLE_1, tableWithUniqueValues);

        TableSetSpecification tableSetSpecification = TableSetSpecification.create().setProvider(PROVIDER)
            .addUniqueColumns(ColumnReference.of(TABLE_1, "unique_col"));

        assertThat(duplicateValueErrorCreator.generateErrors(tableSetWithUniqueValues, tableSetSpecification).isEmpty(),
            is(true));
    }

    @Test public void checkUniqueValues_givenDuplicateValues_addsDuplicateValueErrorToErrorList() {
        Table tableWithUniqueValues = completeTableSet.get(TABLE_1).addColumns(
            StringColumn.create("unique_col", Arrays.asList("1", "1", "2")));
        Map<String, Table> tableSetWithDuplicateValues = new HashMap<>();
        tableSetWithDuplicateValues.put(TABLE_1, tableWithUniqueValues);

        ColumnReference uniqueCol = ColumnReference.of(TABLE_1, "unique_col");
        TableSetSpecification tableSetSpecification = TableSetSpecification.create().setProvider(PROVIDER)
            .addUniqueColumns(uniqueCol);

        Set<String> duplicateValue = Stream.of("1").collect(Collectors.toSet());
        List<ValidationError> expected = Arrays.asList(
            duplicateValueErrorCreator.create(uniqueCol, duplicateValue, PROVIDER)
        );

        assertEquals(
            expected.toString(),
            duplicateValueErrorCreator.generateErrors(tableSetWithDuplicateValues, tableSetSpecification).toString()
        );
    }

}