package org.pdxfinder.dataloaders.updog.tablevalidation.error;

import org.pdxfinder.dataloaders.updog.tablevalidation.Relation;
import org.pdxfinder.dataloaders.updog.tablevalidation.TableSetSpecification;
import tech.tablesaw.api.Table;

import java.util.List;
import java.util.Map;

public abstract class ErrorCreator {
    abstract List<ValidationError> create(
        Map<String, Table> tableSet,
        TableSetSpecification tableSetSpecification
    );

    BrokenRelationError brokenRelation(
        String tableName,
        Relation relation,
        Table invalidRows,
        String description,
        String provider
    ) {
        return new BrokenRelationError(
            tableName,
            relation,
            invalidRows,
            description,
            provider);
    }
}
