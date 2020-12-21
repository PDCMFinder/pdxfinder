package org.pdxfinder.dataloaders.updog.tablevalidation.error;

import java.util.List;
import java.util.Map;
import org.pdxfinder.dataloaders.updog.tablevalidation.ColumnReference;
import org.pdxfinder.dataloaders.updog.tablevalidation.TableSetSpecification;
import org.springframework.stereotype.Component;
import tech.tablesaw.api.Table;

@Component
public class EmptyValueErrorCreator extends ErrorCreator {

    public List<ValidationError> generateErrors(
        Map<String, Table> tableSet,
        TableSetSpecification tableSetSpecification
    ) {
        for (ColumnReference tested : tableSetSpecification.getNonEmptyColumns()) {
            Table table = tableSet.get(tested.table());
            Table missing = table.where(
                table.column(tested.column()).isMissing());
            Table blankColumns = table.where(
                table.column(tested.column()).asStringColumn().isEmptyString()
            );


            if (missing.rowCount() > 0) {
                errors.add(create(tested, missing, tableSetSpecification.getProvider()));
            }
        }
        return errors;
    }

    public EmptyValueError create(ColumnReference columnReference, Table invalidRows, String provider) {
        return new EmptyValueError(columnReference, invalidRows, provider);
    }

}
