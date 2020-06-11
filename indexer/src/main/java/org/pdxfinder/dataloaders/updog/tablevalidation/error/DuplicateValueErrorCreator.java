package org.pdxfinder.dataloaders.updog.tablevalidation.error;

import org.apache.commons.collections4.CollectionUtils;
import org.pdxfinder.dataloaders.updog.tablevalidation.ColumnReference;
import org.pdxfinder.dataloaders.updog.tablevalidation.TableSetSpecification;
import org.springframework.stereotype.Component;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class DuplicateValueErrorCreator extends ErrorCreator {

    public List<ValidationError> generateErrors(
        Map<String, Table> tableSet,
        TableSetSpecification tableSetSpecification
    ) {
        for (ColumnReference tested : tableSetSpecification.getUniqueColumns()) {
            Set<String> duplicates = findDuplicatesInColumn(columnFromTableSet(tableSet, tested));

            if (CollectionUtils.isNotEmpty(duplicates)) {
                errors.add(create(tested, duplicates, tableSetSpecification.getProvider()));
            }
        }
        return errors;
    }

    private StringColumn columnFromTableSet(Map<String, Table> tableSet, ColumnReference columnReference) {
        return tableSet.get(columnReference.table()).stringColumn(columnReference.column());
    }

    private Set<String> findDuplicatesInColumn(StringColumn column) {
        return findDuplicates(column.asList());
    }

    private Set<String> findDuplicates(List<String> listContainingDuplicates) {
        final Set<String> duplicates = new HashSet<>();
        final Set<String> set1 = new HashSet<>();
        for (String string : listContainingDuplicates) {
            if (!set1.add(string))  duplicates.add(string);
        }
        return duplicates;
    }

    public DuplicateValueError create(ColumnReference uniqueColumn, Set<String> duplicates, String provider) {
        return new DuplicateValueError(uniqueColumn, duplicates, provider);
    }

}
