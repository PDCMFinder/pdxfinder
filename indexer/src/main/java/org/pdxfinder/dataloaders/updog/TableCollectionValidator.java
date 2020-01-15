package org.pdxfinder.dataloaders.updog;

import tech.tablesaw.api.Table;

import java.util.Map;

public interface TableCollectionValidator {
    boolean passesValidation(Map<String, Table> pdxDataTables);
}
