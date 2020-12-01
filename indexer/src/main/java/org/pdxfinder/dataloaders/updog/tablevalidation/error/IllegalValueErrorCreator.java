package org.pdxfinder.dataloaders.updog.tablevalidation.error;

import org.pdxfinder.dataloaders.updog.tablevalidation.ColumnReference;
import org.pdxfinder.dataloaders.updog.tablevalidation.TableSetSpecification;
import org.pdxfinder.dataloaders.updog.tablevalidation.ValueRestrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class IllegalValueErrorCreator extends ErrorCreator {

    private static final Logger log = LoggerFactory.getLogger(IllegalValueError.class);

    public List<ValidationError> generateErrors(
            Map<String, Table> tableSet,
            TableSetSpecification tableSetSpecification
    ) {
        String provider = tableSetSpecification.getProvider();
        tableSetSpecification.getCharSetRestrictions().forEach(
                (columns, valueRestriction) -> reportIllegalValue(columns, valueRestriction, tableSet, provider)
        );
        return errors;
    }

    public IllegalValueError create(String tableName, String errorDescription,Table invalidRows, String provider) {
        return new IllegalValueError(tableName, errorDescription, invalidRows, provider);
    }
    private void reportIllegalValue(
            Set<ColumnReference> columns,
            ValueRestrictions valueRestrictions,
            Map<String, Table> tableSet,
            String provider)
    {
        for( ColumnReference columnReference : columns){
            if(!tableMissingColumn(tableSet.get(columnReference.table()),
                    columnReference.column(),
                    columnReference.table()))
            {
                validateColumn(columnReference,valueRestrictions, tableSet, provider);
            }
        }
    }

    private void validateColumn(
            ColumnReference columnReference,
            ValueRestrictions valueRestrictions,
            Map<String,Table> tableSet,
            String provider)
    {
       Table workingTable = tableSet.get(columnReference.table());
       StringColumn column = workingTable.column(columnReference.column()).asStringColumn();
       Predicate<String> charRestriction = Pattern.compile(valueRestrictions.getRegex()).asPredicate();
       List<String> invalidValues = column.asList().stream()
               .filter(charRestriction)
               .collect(Collectors.toCollection(LinkedList::new));
       int[] indexOfInvalids = invalidValues.stream()
               .map(column::indexOf)
               .flatMapToInt(x -> IntStream.of(x.intValue()))
               .toArray();

       if(!invalidValues.isEmpty()){
           String errorDescriptions = String.format(
                   "in column [%s] found %s values has characters not contained in [%s] : %s",
                   columnReference.column(),
                   invalidValues.size(),
                   valueRestrictions.getDescription(),
                   invalidValues.toString()
           );
           errors.add(create(columnReference.table(), errorDescriptions, workingTable.rows(indexOfInvalids), provider));
       }
}

    private boolean tableMissingColumn(Table table, String columnName, String tableName) {
        try {
            return !table.columnNames().contains(columnName);
        } catch (NullPointerException e) {
            log.error("Couldn't access table {} because of {}", tableName, e.toString());
            return true;
        }
    }
}
