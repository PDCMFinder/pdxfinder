package org.pdxfinder.dataloaders.updog.tablevalidation.error;

import org.pdxfinder.dataloaders.updog.tablevalidation.TableSetSpecification;
import org.springframework.stereotype.Component;
import tech.tablesaw.api.Table;

import java.util.List;
import java.util.Map;

@Component
public class MissingTableErrorCreator extends ErrorCreator {

    public List<ValidationError> generateErrors(
        Map<String, Table> tableSet,
        TableSetSpecification tableSetSpecification
    ) {
        for (String table : tableSetSpecification.getMissingTablesFrom(tableSet)) {
            errors.add(new MissingTableError(table, tableSetSpecification.getProvider()));
        }

        return errors;
    }

    public MissingTableError create(String tableName, String provider) {
        return new MissingTableError(tableName, provider);
    }

}
