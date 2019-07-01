package org.pdxfinder.services.highchart;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/*
 * Created by abayomi on 19/06/2019.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "enabled"
})
public class DataLabels {


    private Boolean enabled;
    private String format;
    private Integer connectorWidth;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public DataLabels(Boolean enabled) {
        this.enabled = enabled;
    }

    public DataLabels(Boolean enabled, String format, Integer connectorWidth) {
        this.enabled = enabled;
        this.format = format;
        this.connectorWidth = connectorWidth;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public Integer getConnectorWidth() {
        return connectorWidth;
    }

    public void setConnectorWidth(Integer connectorWidth) {
        this.connectorWidth = connectorWidth;
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
