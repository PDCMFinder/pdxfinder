package org.pdxfinder.dataloaders.updog.tablevalidation.error;

import org.pdxfinder.dataloaders.updog.tablevalidation.ColumnReference;
import org.pdxfinder.dataloaders.updog.tablevalidation.TableSetSpecification;
import tech.tablesaw.api.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EmptyValueErrorCreator extends ErrorCreator {
    private List<ValidationError> errors = new ArrayList<>();

    public List<ValidationError> create(
        Map<String, Table> tableSet,
        TableSetSpecification tableSetSpecification
    ) {
        for (ColumnReference tested : tableSetSpecification.getNonEmptyColumns()) {
            Table table = tableSet.get(tested.table());
            Table missing = table.where(
                table.stringColumn(tested.column()).isMissing());
            if (missing.rowCount() > 0) {
                errors.add(
                    new EmptyValueError(
                        tested.table(),
                        tested.column(),
                        missing,
                        tableSetSpecification.getProvider()));
            }
        }
        return errors;
    }

    public EmptyValueError emptyValueError(String tableName, String columnName, Table invalidRows, String provider) {
        return new EmptyValueError(tableName, columnName, invalidRows, provider);
    }

}
