package org.pdxfinder.services.dto;

import java.util.ArrayList;
import java.util.List;

/*
 * Created by csaba on 13/03/2019.
 */
public class MolecularDataTableDTO {


    private List<String> tableHeaders;

    private List<List<String>> tableRows;

    public MolecularDataTableDTO() {
        tableHeaders = new ArrayList<>();
        tableRows = new ArrayList<>();
    }


    public List<String> getTableHeaders() {
        return tableHeaders;
    }

    public void setTableHeaders(List<String> tableHeaders) {
        this.tableHeaders = tableHeaders;
    }

    public List<List<String>> getTableRows() {
        return tableRows;
    }

    public void setTableRows(List<List<String>> tableRows) {
        this.tableRows = tableRows;
    }
}
