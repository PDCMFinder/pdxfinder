package org.pdxfinder.dataloaders.updog.tablevalidation;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pdxfinder.dataloaders.updog.tablevalidation.error.BrokenRelationErrorCreator;
import org.pdxfinder.dataloaders.updog.tablevalidation.error.DuplicateValueErrorCreator;
import org.pdxfinder.dataloaders.updog.tablevalidation.error.EmptyValueErrorCreator;
import org.pdxfinder.dataloaders.updog.tablevalidation.error.MissingColumnError;
import org.pdxfinder.dataloaders.updog.tablevalidation.error.MissingTableErrorCreator;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;

public class ValidatorTest {

    private final String TABLE_1 = "table_1.tsv";
    private Set<String> minimalRequiredTable = Stream.of(TABLE_1).collect(Collectors.toSet());
    private Map<String, Table> makeCompleteTableSet() {
        Map<String, Table> completeFileSet = new HashMap<>();
        minimalRequiredTable.forEach(s -> completeFileSet.put(s, Table.create(s, StringColumn.create("valid_col"))));
        return completeFileSet;
    }
    private Map<String, Table> tableSet = makeCompleteTableSet();
    TableSetSpecification tableSetSpecification = TableSetSpecification.create().setProvider("PROVIDER-BC")
        .addRequiredColumns(ColumnReference.of(TABLE_1, "valid_col"));

    @Mock private MissingTableErrorCreator missingTableErrorCreator;
    @InjectMocks private Validator validator;

    @Before public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test public void validate_givenNoValidation_producesEmptyErrorList() {
        assertThat(validator.getValidationErrors().isEmpty(), is(true));
    }

    @Test public void validate_givenNoMissingTables_checksForMissingTablesAndNoErrorsFound() {
        validator.validate(tableSet, tableSetSpecification);
        verify(missingTableErrorCreator, times(1)).generateErrors(tableSet, tableSetSpecification);
        verify(missingTableErrorCreator, times(0)).create(anyString(), anyString());
    }

}