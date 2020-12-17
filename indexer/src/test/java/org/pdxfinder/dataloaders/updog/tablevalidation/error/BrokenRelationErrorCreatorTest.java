package org.pdxfinder.dataloaders.updog.tablevalidation.error;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.pdxfinder.dataloaders.updog.tablevalidation.ColumnReference;
import org.pdxfinder.dataloaders.updog.tablevalidation.Relation;
import org.pdxfinder.dataloaders.updog.tablevalidation.Relation.ValidityType;
import org.pdxfinder.dataloaders.updog.tablevalidation.TableSetSpecification;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

public class BrokenRelationErrorCreatorTest {

    private BrokenRelationErrorCreator brokenInterTableRelationErrorCreator = new BrokenRelationErrorCreator();

    private final String LEFT_TABLE = "left_table.tsv";
    private final String RIGHT_TABLE = "right_table.tsv";
    private final Relation INTER_TABLE_RELATION = Relation.betweenTableKeys(
            ColumnReference.of(LEFT_TABLE, "id"),
            ColumnReference.of(RIGHT_TABLE, "table_1_id")
    );

    private final Relation INTRA_TABLE_ONE_TO_MANY = Relation.betweenTableColumns(
            ValidityType.ONE_TO_MANY,
            ColumnReference.of(LEFT_TABLE, "id"),
            ColumnReference.of(LEFT_TABLE, "table_1_id")
    );

    private final Relation INTRA_TABLE_ONE_TO_ONE = Relation.betweenTableColumns(
            ValidityType.ONE_TO_ONE,
            ColumnReference.of(LEFT_TABLE, "id"),
            ColumnReference.of(LEFT_TABLE, "table_1_id")
    );

    private final String PROVIDER = "PROVIDER-BC";

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
            .addRelations(INTER_TABLE_RELATION);

    private final TableSetSpecification ONE_TO_MANY_SPECIFICATION = TableSetSpecification.create().setProvider(PROVIDER)
            .addRelations(INTRA_TABLE_ONE_TO_MANY);

    private final TableSetSpecification ONE_TO_ONE_SPECIFICATION = TableSetSpecification.create().setProvider(PROVIDER)
            .addRelations(INTRA_TABLE_ONE_TO_ONE);

    @Test(expected = Test.None.class)
    public void checkRelationsValid_givenNoRightTable_noExceptionThrown() {
        Map<String, Table> tableSetWithSimpleJoin = makeTableSetWithSimpleJoin();
        tableSetWithSimpleJoin.put(RIGHT_TABLE, null);
        assertThat(brokenInterTableRelationErrorCreator.generateErrors(tableSetWithSimpleJoin, SIMPLE_JOIN_SPECIFICATION).isEmpty(),
                is(true));
    }

    @Test(expected = Test.None.class)
    public void checkRelationsValid_givenNoLeftTable_noExceptionThrown() {
        Map<String, Table> tableSetWithSimpleJoin = makeTableSetWithSimpleJoin();
        tableSetWithSimpleJoin.put(LEFT_TABLE, null);
        assertThat(brokenInterTableRelationErrorCreator.generateErrors(tableSetWithSimpleJoin, SIMPLE_JOIN_SPECIFICATION).isEmpty(),
                is(true));
    }

    @Test(expected = Test.None.class)
    public void oneToManyNoError_givenValidOneToManyJoin_emptyErrorList() {
        Table leftTable = Table.create(LEFT_TABLE).addColumns(
                StringColumn.create("id", Arrays.asList("1", "2", "3")),
                StringColumn.create("table_1_id", Arrays.asList("1","1","1"))
        );
        Map<String, Table> tableSetWithOneToMany = new HashMap<>();
        tableSetWithOneToMany.put(LEFT_TABLE, leftTable);
        assertThat(brokenInterTableRelationErrorCreator.generateErrors(tableSetWithOneToMany, ONE_TO_MANY_SPECIFICATION).isEmpty(),
                is(true));

    }

    @Test(expected = Test.None.class)
    public void oneToManyError_givenInvalidValidOneToManyJoin_hasErrorEntry() {
        Table leftTable = Table.create(LEFT_TABLE).addColumns(
                StringColumn.create("id", Arrays.asList("1","1","1")),
                StringColumn.create("table_1_id", Arrays.asList("1", "2", "3"))
        );
        Map<String, Table> tableSetWithOneToManyWithErrors = new HashMap<>();
        tableSetWithOneToManyWithErrors.put(LEFT_TABLE, leftTable);
        assertThat(brokenInterTableRelationErrorCreator.generateErrors(tableSetWithOneToManyWithErrors, ONE_TO_MANY_SPECIFICATION).isEmpty(),
                is(false));
    }

    @Test(expected = Test.None.class)
    public void oneToOne_givenValidPairOfColumns_hasNoErrorEntry() {
        Table leftTable = Table.create(LEFT_TABLE).addColumns(
                StringColumn.create("id", Arrays.asList("1","2","3")),
                StringColumn.create("table_1_id", Arrays.asList("3","2","1"))
        );
        Map<String, Table> tableSetWithOneToOne = new HashMap<>();
        tableSetWithOneToOne.put(LEFT_TABLE,leftTable);
        assertThat(brokenInterTableRelationErrorCreator.generateErrors(tableSetWithOneToOne, ONE_TO_MANY_SPECIFICATION).isEmpty(),
                is(true));
    }

    @Test(expected = Test.None.class)
    public void oneToOneErro_givenInvalidPairOfColumns_hasErrorEntry() {
        Table leftTable = Table.create(LEFT_TABLE).addColumns(
                StringColumn.create("id", Arrays.asList("1","1","2","2","5","6","7","8")),
                StringColumn.create("table_1_id", Arrays.asList("1","2","3","4","5","5","7","8"))
        );
        Map<String, Table> tableSetWithOneToOne = new HashMap<>();
        tableSetWithOneToOne.put(LEFT_TABLE,leftTable);
        assertThat(brokenInterTableRelationErrorCreator.generateErrors(tableSetWithOneToOne, ONE_TO_ONE_SPECIFICATION).isEmpty(),
                is(false));
    }

    @Test
    public void checkRelationsValid_givenNoLeftTable_ErrorListWithMissingRequiredCol() {
        Map<String, Table> tableSetWithSimpleJoin = makeTableSetWithSimpleJoin();
        tableSetWithSimpleJoin.get(LEFT_TABLE).removeColumns("id");

        BrokenRelationError expected = brokenInterTableRelationErrorCreator.create(
                LEFT_TABLE, INTER_TABLE_RELATION, tableSetWithSimpleJoin.get(LEFT_TABLE).emptyCopy(),
                String.format("because [%s] is missing column [%s]", LEFT_TABLE, "id"), PROVIDER);

        assertEquals(
                Collections.singletonList(expected).toString(),
                brokenInterTableRelationErrorCreator.generateErrors(tableSetWithSimpleJoin, SIMPLE_JOIN_SPECIFICATION).toString()
        );
    }

    @Test
    public void checkRelationsValid_givenNoRightTable_ErrorListWithMissingRequiredCol() {
        Map<String, Table> tableSetWithSimpleJoin = makeTableSetWithSimpleJoin();
        tableSetWithSimpleJoin.get(RIGHT_TABLE).removeColumns("table_1_id");

        BrokenRelationError expected = brokenInterTableRelationErrorCreator.create(
                RIGHT_TABLE, INTER_TABLE_RELATION, tableSetWithSimpleJoin.get(RIGHT_TABLE).emptyCopy(),
                String.format("because [%s] is missing column [%s]", RIGHT_TABLE, "table_1_id"), PROVIDER);

        assertEquals(
                Collections.singletonList(expected).toString(),
                brokenInterTableRelationErrorCreator.generateErrors(tableSetWithSimpleJoin, SIMPLE_JOIN_SPECIFICATION).toString()
        );
    }

    @Test
    public void checkRelationsValid_givenMissingValueInRightColumn_ErrorListWithOrphanLeftRows() {
        Map<String, Table> tableSetWithSimpleJoin = makeTableSetWithSimpleJoin();
        tableSetWithSimpleJoin.get(RIGHT_TABLE).replaceColumn(
                StringColumn.create("table_1_id", Collections.EMPTY_LIST));
        BrokenRelationError expected = brokenInterTableRelationErrorCreator.create(
                RIGHT_TABLE, INTER_TABLE_RELATION, tableSetWithSimpleJoin.get(LEFT_TABLE),
                String.format("1 orphan row(s) found in [%s]", LEFT_TABLE), PROVIDER);

        assertEquals(
                Collections.singletonList(expected).toString(),
                brokenInterTableRelationErrorCreator.generateErrors(tableSetWithSimpleJoin, SIMPLE_JOIN_SPECIFICATION).toString()
        );
    }

    @Test
    public void checkRelationsValid_givenMissingValuesInLeftColumn_ErrorListWithOrphanRightRows() {
        Map<String, Table> tableSetWithSimpleJoin = makeTableSetWithSimpleJoin();
        tableSetWithSimpleJoin.get(LEFT_TABLE).replaceColumn(
                StringColumn.create("id", Collections.EMPTY_LIST));
        BrokenRelationError expected = brokenInterTableRelationErrorCreator.create(
                LEFT_TABLE, INTER_TABLE_RELATION, tableSetWithSimpleJoin.get(RIGHT_TABLE),
                String.format("1 orphan row(s) found in [%s]", RIGHT_TABLE), PROVIDER
        );

        assertEquals(
                Collections.singletonList(expected).toString(),
                brokenInterTableRelationErrorCreator.generateErrors(tableSetWithSimpleJoin, SIMPLE_JOIN_SPECIFICATION).toString()
        );
    }

    @Test
    public void checkRelationsValid_givenMissingValueInLeftAndRightColumn_ErrorListWithMissingValueRows() {
        Map<String, Table> tableSetWithSimpleJoin = makeTableSetWithSimpleJoin();
        tableSetWithSimpleJoin.get(RIGHT_TABLE).replaceColumn(
                StringColumn.create("table_1_id", Arrays.asList("not 1", "not 1"))
        );
        List<ValidationError> expected = Arrays.asList(
                brokenInterTableRelationErrorCreator.create(RIGHT_TABLE, INTER_TABLE_RELATION, tableSetWithSimpleJoin.get(LEFT_TABLE),
                        String.format("1 orphan row(s) found in [%s]", LEFT_TABLE), PROVIDER),
                brokenInterTableRelationErrorCreator.create(LEFT_TABLE, INTER_TABLE_RELATION, tableSetWithSimpleJoin.get(RIGHT_TABLE),
                        String.format("2 orphan row(s) found in [%s]", RIGHT_TABLE), PROVIDER)
        );

        assertEquals(
                expected.toString(),
                brokenInterTableRelationErrorCreator.generateErrors(tableSetWithSimpleJoin, SIMPLE_JOIN_SPECIFICATION).toString()
        );
    }

    @Test
    public void checkToString_GivenIdenticalPairs_hashesMatch() {
        List<String> columnValuesLeft = Arrays.asList("value1", "value2", "value3");
        List<String> columnValuesRight = Arrays.asList("value4", "value5", "value6");
        StringColumn column1 = StringColumn.create("column1", columnValuesLeft);
        StringColumn column2 = StringColumn.create("column2", columnValuesRight);
        Pair<StringColumn, StringColumn> pair1 = Pair.of(column1, column2);
        Pair<StringColumn, StringColumn> pair2 = Pair.of(column1, column2);
        assertEquals(pair1, pair2);
        assertEquals(pair1.toString(), pair2.toString());
    }
}
