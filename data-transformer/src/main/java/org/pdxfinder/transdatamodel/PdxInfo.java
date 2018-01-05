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


/*
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({
            "pdxInfo"
    })
    public class Example {

        @JsonProperty("pdxInfo")
        private List<PdxInfo> pdxInfo = null;

        @JsonProperty("pdxInfo")
        public List<PdxInfo> getPdxInfo() {
            return pdxInfo;
        }

        @JsonProperty("pdxInfo")
        public void setPdxInfo(List<PdxInfo> pdxInfo) {
            this.pdxInfo = pdxInfo;
        }

    }
 */