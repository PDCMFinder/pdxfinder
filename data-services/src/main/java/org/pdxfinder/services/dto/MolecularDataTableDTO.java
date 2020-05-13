package org.pdxfinder.services.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;


@JsonPropertyOrder({
        "molecularDataRows",
        "visible",
        "reports",
})
public class MolecularDataTableDTO {

    private List<MolecularDataRowDTO> molecularDataRows;
    private boolean visible;
    private List<String> reports;

    public MolecularDataTableDTO() {
        visible = true;
    }

    public List<MolecularDataRowDTO> getMolecularDataRows() {
        return molecularDataRows;
    }

    public void setMolecularDataRows(List<MolecularDataRowDTO> molecularDataRows) {
        this.molecularDataRows = molecularDataRows;
    }

    public boolean getVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public List<String> getReports() {
        return reports;
    }

    public void setReports(List<String> reports) {
        this.reports = reports;
    }
}
