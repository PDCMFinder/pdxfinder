package org.pdxfinder.services.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.pdxfinder.services.dto.pdxgun.Reference;

import java.util.List;
import java.util.Map;


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

    public List<Map<String, Object>> getMolecularDataCsv() {
        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, Object>> dataList = mapper.convertValue(molecularDataRows, new TypeReference<List<Map<String, Object>>>(){});
        dataList.forEach(dataMap -> dataMap.forEach((key, value)->{
            boolean notString = !(value instanceof String);
            if (notString){
                dataMap.put(key, mapper.convertValue(value, Reference.class).getLabel());
            }
        }));
        return dataList;
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
