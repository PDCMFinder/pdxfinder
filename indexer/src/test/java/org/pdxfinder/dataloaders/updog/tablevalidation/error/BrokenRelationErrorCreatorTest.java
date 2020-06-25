package org.pdxfinder.dataloaders.updog.tablevalidation.error;

import org.junit.Test;
import org.pdxfinder.dataloaders.updog.tablevalidation.ColumnReference;
import org.pdxfinder.dataloaders.updog.tablevalidation.Relation;
import org.pdxfinder.dataloaders.updog.tablevalidation.TableSetSpecification;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class BrokenRelationErrorCreatorTest {

    private BrokenRelationErrorCreator brokenRelationErrorCreator = new BrokenRelationErrorCreator();

    private final String LEFT_TABLE = "left_table.tsv";
    private final String RIGHT_TABLE = "right_table.tsv";
    private final Relation RELATION = Relation.between(
        ColumnReference.of(LEFT_TABLE, "id"),
        ColumnReference.of(RIGHT_TABLE, "table_1_id")
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
        .addRelations(RELATION);

    @Test(expected = Test.None.class)
    public void checkRelationsValid_givenNoRightTable_noExceptionThrown() {
        Map<String, Table> tableSetWithSimpleJoin = makeTableSetWithSimpleJoin();
        tableSetWithSimpleJoin.put(RIGHT_TABLE, null);
        assertThat(brokenRelationErrorCreator.generateErrors(tableSetWithSimpleJoin, SIMPLE_JOIN_SPECIFICATION).isEmpty(),
            is(true));
    }

    @Test(expected = Test.None.class)
    public void checkRelationsValid_givenNoLeftTable_noExceptionThrown() {
        Map<String, Table> tableSetWithSimpleJoin = makeTableSetWithSimpleJoin();
        tableSetWithSimpleJoin.put(LEFT_TABLE, null);
        assertThat(brokenRelationErrorCreator.generateErrors(tableSetWithSimpleJoin, SIMPLE_JOIN_SPECIFICATION).isEmpty(),
            is(true));
    }

    @Test public void checkRelationsValid_givenValidOneToManyJoin_emptyErrorList() {
        Map<String, Table> tableSetWithSimpleJoin = makeTableSetWithSimpleJoin();

        assertThat(brokenRelationErrorCreator.generateErrors(tableSetWithSimpleJoin, SIMPLE_JOIN_SPECIFICATION).isEmpty(),
            is(true));
    }

    @Test public void checkRelationsValid_givenNoLeftTable_ErrorListWithMissingRequiredCol() {
        Map<String, Table> tableSetWithSimpleJoin = makeTableSetWithSimpleJoin();
        tableSetWithSimpleJoin.get(LEFT_TABLE).removeColumns("id");

        BrokenRelationError expected = brokenRelationErrorCreator.create(
            LEFT_TABLE, RELATION, tableSetWithSimpleJoin.get(LEFT_TABLE).emptyCopy(),
            String.format("because [%s] is missing column [%s]", LEFT_TABLE, "id"), PROVIDER);

        assertEquals(
            Collections.singletonList(expected).toString(),
            brokenRelationErrorCreator.generateErrors(tableSetWithSimpleJoin, SIMPLE_JOIN_SPECIFICATION).toString()
        );
    }

    @Test public void checkRelationsValid_givenNoRightTable_ErrorListWithMissingRequiredCol() {
        Map<String, Table> tableSetWithSimpleJoin = makeTableSetWithSimpleJoin();
        tableSetWithSimpleJoin.get(RIGHT_TABLE).removeColumns("table_1_id");

        BrokenRelationError expected = brokenRelationErrorCreator.create(
            RIGHT_TABLE, RELATION, tableSetWithSimpleJoin.get(RIGHT_TABLE).emptyCopy(),
            String.format("because [%s] is missing column [%s]", RIGHT_TABLE, "table_1_id"), PROVIDER);

        assertEquals(
            Collections.singletonList(expected).toString(),
            brokenRelationErrorCreator.generateErrors(tableSetWithSimpleJoin, SIMPLE_JOIN_SPECIFICATION).toString()
        );
    }

    @Test public void checkRelationsValid_givenMissingValueInRightColumn_ErrorListWithOrphanLeftRows() {
        Map<String, Table> tableSetWithSimpleJoin = makeTableSetWithSimpleJoin();
        tableSetWithSimpleJoin.get(RIGHT_TABLE).replaceColumn(
            StringColumn.create("table_1_id", Collections.EMPTY_LIST));
        BrokenRelationError expected = brokenRelationErrorCreator.create(
            RIGHT_TABLE, RELATION, tableSetWithSimpleJoin.get(LEFT_TABLE),
            String.format("1 orphan row(s) found in [%s]", LEFT_TABLE), PROVIDER);

        assertEquals(
            Collections.singletonList(expected).toString(),
            brokenRelationErrorCreator.generateErrors(tableSetWithSimpleJoin, SIMPLE_JOIN_SPECIFICATION).toString()
        );
    }

    @Test public void checkRelationsValid_givenMissingValuesInLeftColumn_ErrorListWithOrphanRightRows() {
        Map<String, Table> tableSetWithSimpleJoin = makeTableSetWithSimpleJoin();
        tableSetWithSimpleJoin.get(LEFT_TABLE).replaceColumn(
            StringColumn.create("id", Collections.EMPTY_LIST));
        BrokenRelationError expected = brokenRelationErrorCreator.create(
            LEFT_TABLE, RELATION, tableSetWithSimpleJoin.get(RIGHT_TABLE),
            String.format("1 orphan row(s) found in [%s]", RIGHT_TABLE), PROVIDER
        );

        assertEquals(
            Collections.singletonList(expected).toString(),
            brokenRelationErrorCreator.generateErrors(tableSetWithSimpleJoin, SIMPLE_JOIN_SPECIFICATION).toString()
        );
    }

    @Test public void checkRelationsValid_givenMissingValueInLeftAndRightColumn_ErrorListWithMissingValueRows() {
        Map<String, Table> tableSetWithSimpleJoin = makeTableSetWithSimpleJoin();
        tableSetWithSimpleJoin.get(RIGHT_TABLE).replaceColumn(
            StringColumn.create("table_1_id", Arrays.asList("not 1", "not 1"))
        );
        List<ValidationError> expected = Arrays.asList(
            brokenRelationErrorCreator.create(RIGHT_TABLE, RELATION, tableSetWithSimpleJoin.get(LEFT_TABLE),
                String.format("1 orphan row(s) found in [%s]", LEFT_TABLE), PROVIDER),
            brokenRelationErrorCreator.create(LEFT_TABLE, RELATION, tableSetWithSimpleJoin.get(RIGHT_TABLE),
                String.format("2 orphan row(s) found in [%s]", RIGHT_TABLE), PROVIDER)
        );

        assertEquals(
            expected.toString(),
            brokenRelationErrorCreator.generateErrors(tableSetWithSimpleJoin, SIMPLE_JOIN_SPECIFICATION).toString()
        );
    }


}