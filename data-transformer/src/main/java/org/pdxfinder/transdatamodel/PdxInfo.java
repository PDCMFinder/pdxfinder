package org.pdxfinder.transdatamodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PdxInfo {

    @JsonProperty("pdxInfo")
    private List<PdmrPdxInfo> pdxInfo = null;

    public List<PdmrPdxInfo> getPdxInfo() {
        return pdxInfo;
    }

    public void setPdxInfo(List<PdmrPdxInfo> pdxInfo) {
        this.pdxInfo = pdxInfo;
    }

}