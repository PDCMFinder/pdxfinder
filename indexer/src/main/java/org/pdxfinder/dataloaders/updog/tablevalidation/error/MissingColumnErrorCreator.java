package org.pdxfinder.dataloaders.updog.tablevalidation.error;

import org.pdxfinder.dataloaders.updog.tablevalidation.ColumnReference;
import org.pdxfinder.dataloaders.updog.tablevalidation.TableSetSpecification;
import org.springframework.stereotype.Component;
import tech.tablesaw.api.Table;

import java.util.List;
import java.util.Map;

@Component
public class MissingColumnErrorCreator extends ErrorCreator {

    public List<ValidationError> generateErrors(
        Map<String, Table> tableSet,
        TableSetSpecification tableSetSpecification
    ) {
        for (ColumnReference required : tableSetSpecification.getRequiredColumns()) {
            if (tableIsMissingColumn(tableSet, required)) {
                errors.add(create(required, tableSetSpecification.getProvider()));
            }
        }

        return errors;
    }

    private boolean tableIsMissingColumn(Map<String, Table> tableSet, ColumnReference columnReference) {
        return !tableSet.get(columnReference.table()).columnNames().contains(columnReference.column());
    }

    public MissingColumnError create(ColumnReference columnReference, String provider) {
        return new MissingColumnError(columnReference, provider);
    }

}
