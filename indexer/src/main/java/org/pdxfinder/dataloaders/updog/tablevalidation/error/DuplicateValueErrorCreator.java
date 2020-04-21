package org.pdxfinder.dataloaders.updog.tablevalidation.error;

import org.apache.commons.collections4.CollectionUtils;
import org.pdxfinder.dataloaders.updog.tablevalidation.ColumnReference;
import org.pdxfinder.dataloaders.updog.tablevalidation.TableSetSpecification;
import tech.tablesaw.api.Table;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DuplicateValueErrorCreator extends ErrorCreator {
    private List<ValidationError> errors = new ArrayList<>();

    public List<ValidationError> create(
        Map<String, Table> tableSet,
        TableSetSpecification tableSetSpecification
    ) {
        for (ColumnReference tested : tableSetSpecification.getUniqueColumns()) {
            Table table = tableSet.get(tested.table());
            Set<String> duplicates = findDuplicates(table.stringColumn(tested.column()).asList());
            if (CollectionUtils.isNotEmpty(duplicates)) {
                errors.add(new DuplicateValueError(
                    tested.table(), tested.column(), duplicates, tableSetSpecification.getProvider()));
            }
        }
        return errors;
    }

    private Set<String> findDuplicates(List<String> listContainingDuplicates) {
        final Set<String> duplicates = new HashSet<>();
        final Set<String> set1 = new HashSet<>();
        for (String string : listContainingDuplicates) {
            if (!set1.add(string))  duplicates.add(string);
        }
        return duplicates;
    }

    public DuplicateValueError duplicateValue(
        String tableName,
        String columnName,
        Set<String> duplicates,
        String provider
    ) {
        return new DuplicateValueError(tableName, columnName, duplicates, provider);
    }

}
