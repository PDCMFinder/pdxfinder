package org.pdxfinder.dataloaders.updog;

import tech.tablesaw.api.Table;

import java.util.Map;
import java.util.stream.Collectors;

class TableSetUtilities {

    private TableSetUtilities() {
        throw new IllegalStateException("Utility class");
    }

    static Map<String, Table> cleanPdxTableSet(Map<String, Table> pdxTableSet) {
        pdxTableSet.remove("metadata-checklist.tsv");
        removeDescriptionColumn(pdxTableSet);
        pdxTableSet = removeHeaderRows(pdxTableSet);
        pdxTableSet = removeBlankRows(pdxTableSet);
        return pdxTableSet;
    }

    static Map<String, Table> removeHeaderRows(Map<String, Table> tableSet) {
        return tableSet.entrySet().stream().collect(
            Collectors.toMap(
                e -> e.getKey(),
                e -> TableUtilities.removeHeaderRows(e.getValue(), 4)
            ));
    }

    static Map<String, Table> removeBlankRows(Map<String, Table> tableSet) {
        return tableSet.entrySet().stream().collect(
            Collectors.toMap(
                e -> e.getKey(),
                e -> TableUtilities.removeRowsMissingRequiredColumnValue(
                    e.getValue(),
                    e.getValue().column(0).asStringColumn())
            ));
    }

    static void removeDescriptionColumn(Map<String, Table> tableSet) {
        tableSet.values().forEach(t -> t.removeColumns("Field"));
    }

}
