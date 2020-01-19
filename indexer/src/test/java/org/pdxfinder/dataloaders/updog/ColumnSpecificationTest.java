package org.pdxfinder.dataloaders.updog;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;

public class ColumnSpecificationTest {

    List<String> emptyList = Arrays.asList();
    String fieldName1 = "field_1";
    String fieldName2 = "field_2";

    ColumnSpecification twoColumnSpecification= new ColumnSpecification(
        Table.create("table").addColumns(
            StringColumn.create(fieldName1),
            StringColumn.create(fieldName2)));
    Table twoColumnTable = Table.create("table")
        .addColumns(
            StringColumn.create(fieldName1),
            StringColumn.create(fieldName2));

    @Before public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test public void containsRequiredColumns_givenColumnSpecificationAndValidCols_passesValidation() {
        assertThat(twoColumnSpecification.containsRequiredColumns(twoColumnTable), is(true));
    }

    @Test public void containsRequiredColumns_givenColumnSpecificationAndMissingColInTable_failsValidation() {
        Table tableMissingColumn = twoColumnTable;
        tableMissingColumn.removeColumns(fieldName1);
        assertThat(twoColumnSpecification.containsRequiredColumns(tableMissingColumn), is(false));

    }

    @Test public void getMissingColumnsFrom_givenColumnSpecificationAndMissingColInTable_returnsMissingCol(){
        Table tableMissingColumn = twoColumnTable;
        tableMissingColumn.removeColumns(fieldName1);
        assertEquals(
            Arrays.asList(fieldName1),
            twoColumnSpecification.getMissingColumnsFrom(tableMissingColumn)
        );
    }

    @Test public void getMissingColumnsFrom_givenColumnsSpecificationAndCompleteTable_returnsNoMissingCols() {
        Table completeTable = twoColumnTable;
        assertEquals(
            emptyList,
            twoColumnSpecification.getMissingColumnsFrom(completeTable)
        );
    }

    @Test public void getMissingColumnsFrom_givenColumnSpecificationAndExtraColInTable_returnsNoMissingCols() {
        Table completeTableWithExtraColumn = twoColumnTable;
        completeTableWithExtraColumn.addColumns(StringColumn.create("field_3"));
        assertEquals(
            emptyList,
            twoColumnSpecification.getMissingColumnsFrom(completeTableWithExtraColumn)
        );
    }

}