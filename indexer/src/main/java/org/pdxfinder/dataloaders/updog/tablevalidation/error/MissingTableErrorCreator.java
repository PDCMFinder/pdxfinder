package org.pdxfinder.dataloaders.updog.tablevalidation.error;

import org.pdxfinder.dataloaders.updog.tablevalidation.TableSetSpecification;
import tech.tablesaw.api.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MissingTableErrorCreator extends ErrorCreator {
    private List<ValidationError> errors = new ArrayList<>();

    public List<ValidationError> create(
        Map<String, Table> tableSet,
        TableSetSpecification tableSetSpecification
    ) {
        for (String table : tableSetSpecification.getMissingTablesFrom(tableSet)) {
            errors.add(new MissingTableError(table, tableSetSpecification.getProvider()));
        }

        return errors;
    }

    public MissingTableError missingTable(String tableName, String provider) {
        return new MissingTableError(tableName, provider);
    }

}
