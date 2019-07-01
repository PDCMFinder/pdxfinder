package org.pdxfinder.services.highchart;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/*
 * Created by abayomi on 26/06/2019.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "valueSuffix",
        "shared"
})
public class ToolTip {

    private String valueSuffix;
    private Boolean shared;
    private String headerFormat;
    private String pointFormat;
    private String footerFormat;
    private Boolean useHTML;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public ToolTip() {
    }

    public ToolTip(String valueSuffix) {
        this.valueSuffix = valueSuffix;
    }

    public ToolTip(Boolean shared, String headerFormat, String pointFormat, String footerFormat, Boolean useHTML) {
        this.shared = shared;
        this.headerFormat = headerFormat;
        this.pointFormat = pointFormat;
        this.footerFormat = footerFormat;
        this.useHTML = useHTML;
    }

    public String getValueSuffix() {
        return valueSuffix;
    }

    public Boolean getShared() {
        return shared;
    }

    public void setShared(Boolean shared) {
        this.shared = shared;
    }

    public void setValueSuffix(String valueSuffix) {
        this.valueSuffix = valueSuffix;
    }

    public String getHeaderFormat() {
        return headerFormat;
    }

    public void setHeaderFormat(String headerFormat) {
        this.headerFormat = headerFormat;
    }

    public String getPointFormat() {
        return pointFormat;
    }

    public void setPointFormat(String pointFormat) {
        this.pointFormat = pointFormat;
    }

    public String getFooterFormat() {
        return footerFormat;
    }

    public void setFooterFormat(String footerFormat) {
        this.footerFormat = footerFormat;
    }

    public Boolean getUseHTML() {
        return useHTML;
    }

    public void setUseHTML(Boolean useHTML) {
        this.useHTML = useHTML;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}