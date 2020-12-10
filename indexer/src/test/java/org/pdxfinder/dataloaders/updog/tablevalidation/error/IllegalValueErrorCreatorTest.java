package org.pdxfinder.dataloaders.updog.tablevalidation.error;

import org.junit.Assert;
import org.junit.Test;
import org.pdxfinder.dataloaders.updog.tablevalidation.ColumnReference;
import org.pdxfinder.dataloaders.updog.tablevalidation.TableSetSpecification;
import org.pdxfinder.dataloaders.updog.tablevalidation.ValueRestrictions;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

import java.util.*;

public class IllegalValueErrorCreatorTest {

    private IllegalValueErrorCreator illegalValueErrorCreator = new IllegalValueErrorCreator();
    private static final String PROVIDER = "PROVIDER-BC";
    private static final String TABLE_NAME = "TABLE_NAME";
    private static final String COLUMN_NAME = "ID";

    private StringColumn column;
    private Map<String, Table> tablset;
    private ColumnReference columnReference;
    private Set<ColumnReference> columns;
    private TableSetSpecification tableSetSpecification = TableSetSpecification.create()
            .setProvider(PROVIDER);

    private Map<String, Table> makeTableSetWithSingleColumn(List<String> columnValues) {
        column = StringColumn.create(COLUMN_NAME, columnValues);
        columnReference = ColumnReference.of(TABLE_NAME,COLUMN_NAME);
        columns = new HashSet<>();
        columns.add(columnReference);

        Table table = Table.create(TABLE_NAME).addColumns(column);
        tablset = new HashMap<>();
        tablset.put(TABLE_NAME, table);
        return tablset;
    }

    @Test public void startIllegalValueCreator_NoRestrictedValueSpecification_basicColumn_returnNothing(){
        Map<String, Table> tableSet = makeTableSetWithSingleColumn(Collections.singletonList("TEST"));
        Assert.assertTrue(illegalValueErrorCreator.generateErrors(tableSet, tableSetSpecification).isEmpty());
    }

    @Test public void UrlSafeCharTest_columnWithUrlSafeString_returnNoError(){
        Map<String, Table> tableSet = makeTableSetWithSingleColumn(Arrays.asList("TEST", "T.E ST-~_"));
        tableSetSpecification.addValueRestriction(columns, ValueRestrictions.URL_SAFE());
        Assert.assertTrue(illegalValueErrorCreator.generateErrors(tableSet, tableSetSpecification).isEmpty());
    }

    @Test public void UrlSafeError_columnWithNoneUrlSafeString_returnError(){
        Map<String, Table> tableSet = makeTableSetWithSingleColumn(Collections.singletonList("T#E/ST"));
        tableSetSpecification.addValueRestriction(columns, ValueRestrictions.URL_SAFE());
        Assert.assertFalse(illegalValueErrorCreator.generateErrors(tableSet, tableSetSpecification).isEmpty());
    }

    @Test public void UrlSafeError_columnWithMixOfNoneString_returnError(){
        Map<String, Table> tableSet = makeTableSetWithSingleColumn(Arrays.asList("T#E/ST", "TEST", "TEES", "TES23#"));
        tableSetSpecification.addValueRestriction(columns, ValueRestrictions.URL_SAFE());
        Assert.assertFalse(illegalValueErrorCreator.generateErrors(tableSet, tableSetSpecification).isEmpty());
    }

}
