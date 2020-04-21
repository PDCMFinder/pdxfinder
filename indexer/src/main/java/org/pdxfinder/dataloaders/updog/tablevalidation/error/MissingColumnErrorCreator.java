package org.pdxfinder.dataloaders.updog.tablevalidation.error;

import org.pdxfinder.dataloaders.updog.tablevalidation.ColumnReference;
import org.pdxfinder.dataloaders.updog.tablevalidation.TableSetSpecification;
import tech.tablesaw.api.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MissingColumnErrorCreator extends ErrorCreator {
    private List<ValidationError> errors = new ArrayList<>();

    public List<ValidationError> create(
        Map<String, Table> tableSet,
        TableSetSpecification tableSetSpecification
    ) {
        for (ColumnReference required : tableSetSpecification.getRequiredColumns()) {
            if (tableIsMissingColumn(tableSet, required)) {
                errors.add(
                    new MissingColumnError(required.table(), required.column(),
                        tableSetSpecification.getProvider())
                );
            }
        }

        return errors;
    }

    private boolean tableIsMissingColumn(Map<String, Table> tableSet, ColumnReference columnReference) {
        return !tableSet.get(columnReference.table()).columnNames().contains(columnReference.column());
    }

    public MissingColumnError missingColumn(
        String tableName,
        String columnName,
        String provider
    ) {
        return new MissingColumnError(tableName, columnName, provider);
    }

}
