package org.pdxfinder.dataloaders.updog;

import org.apache.commons.lang3.StringUtils;
import tech.tablesaw.api.Table;

import java.util.Map;
import java.util.stream.Collectors;

class TableSetUtilities {

    private TableSetUtilities() { throw new IllegalStateException("Utility class"); }

    static Map<String, Table> cleanPdxTableSet(Map<String, Table> pdxTableSet) {
        pdxTableSet = removeProviderNameFromFilename(pdxTableSet);
        pdxTableSet.remove("metadata-checklist.tsv");
        removeDescriptionColumn(pdxTableSet);
        pdxTableSet = removeHeaderRows(pdxTableSet);
        pdxTableSet = removeBlankRows(pdxTableSet);
        return pdxTableSet;
    }

    static Map<String, Table> cleanOmicsTableSet(Map<String, Table> omicsTableSet) {
        omicsTableSet = removeProviderNameFromFilename(omicsTableSet);
        omicsTableSet = removeHeaderRowsIfPresent(omicsTableSet);
        omicsTableSet = removeBlankRows(omicsTableSet);
        return omicsTableSet;
    }

    static Map<String, Table> removeHeaderRows(Map<String, Table> tableSet) {
        return tableSet.entrySet().stream().collect(
            Collectors.toMap(
                Map.Entry::getKey,
                e -> TableUtilities.removeHeaderRows(e.getValue(), 4)
            ));
    }

    @Deprecated
    static Map<String, Table> removeBlankRows(Map<String, Table> tableSet) {
        return tableSet.entrySet().stream().collect(
            Collectors.toMap(
                Map.Entry::getKey,
                e -> TableUtilities.removeRowsMissingRequiredColumnValue(
                    e.getValue(),
                    e.getValue().column(0).asStringColumn())
            ));
    }

    static Map<String, Table> removeHeaderRowsIfPresent(Map<String, Table> tableSet) {
        return tableSet.entrySet().stream().collect(
            Collectors.toMap(
                Map.Entry::getKey,
                e -> removeHeaderRowsIfPresent(e.getValue())));
    }

    static Table removeHeaderRowsIfPresent(Table table) {
        return table.columnNames().contains("Field")
            ? TableUtilities.removeHeaderRows(table, 4)
            : table;
    }

    static void removeDescriptionColumn(Map<String, Table> tableSet) {
        tableSet.values().forEach(t -> t.removeColumns("Field"));
    }

    static Map<String, Table> removeProviderNameFromFilename(Map<String, Table> tableSet) {
        return tableSet.entrySet().stream().collect(
            Collectors.toMap(
                e -> StringUtils.substringAfter(e.getKey(), "_"),
                e -> e.getValue().setName(StringUtils.substringAfter(e.getKey(), "_"))
            ));
    }

}
