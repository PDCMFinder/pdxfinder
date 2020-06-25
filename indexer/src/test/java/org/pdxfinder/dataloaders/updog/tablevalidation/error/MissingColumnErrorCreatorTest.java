package org.pdxfinder.dataloaders.updog.tablevalidation.error;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.pdxfinder.dataloaders.updog.tablevalidation.ColumnReference;
import org.pdxfinder.dataloaders.updog.tablevalidation.TableSetSpecification;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class MissingColumnErrorCreatorTest {

    private Map<String, Table> completeTableSet = new HashMap<>();
    private final String TABLE_1 = "table_1.tsv";
    private final String PROVIDER = "PROVIDER-BC";

    private Set<String> minimalRequiredTable = Stream.of(TABLE_1).collect(Collectors.toSet());
    private Map<String, Table> makeCompleteTableSet() {
        Map<String, Table> completeFileSet = new HashMap<>();
        minimalRequiredTable.forEach(s -> completeFileSet.put(s, Table.create(s, StringColumn.create("valid_col"))));
        return completeFileSet;
    }

    private MissingColumnErrorCreator missingColumnErrorCreator = new MissingColumnErrorCreator();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        completeTableSet = makeCompleteTableSet();
    }

    @Test public void checkAllRequiredColsPresent_givenNoMissingCols_emptyErrorList() {
        TableSetSpecification tableSetSpecification = TableSetSpecification.create().setProvider(PROVIDER)
            .addRequiredColumns(ColumnReference.of(TABLE_1, "valid_col"));

        assertThat(missingColumnErrorCreator.generateErrors(completeTableSet, tableSetSpecification)
            .isEmpty(), is(true));
    }

    @Test public void checkAllRequiredColsPresent_givenMissingColumnDefinedInColSpec_addsMissingColErrorTotErrorList() {
        ColumnReference missing = ColumnReference.of(TABLE_1, "missing_column");
        List<ValidationError> expected = Collections.singletonList(
            missingColumnErrorCreator.create(missing, PROVIDER));
        TableSetSpecification tableSetSpecification = TableSetSpecification.create().setProvider(PROVIDER)
            .addRequiredColumns(missing);

        assertEquals(
            expected.toString(),
            missingColumnErrorCreator.generateErrors(completeTableSet, tableSetSpecification).toString()
        );
    }

}