package org.pdxfinder.zooma;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "propertyType",
        "propertyValue"
})
public class Property {

    @JsonProperty("propertyType")
    private String propertyType;
    @JsonProperty("propertyValue")
    private String propertyValue;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("propertyType")
    public String getPropertyType() {
        return propertyType;
    }

    @JsonProperty("propertyType")
    public void setPropertyType(String propertyType) {
        this.propertyType = propertyType;
    }

    @JsonProperty("propertyValue")
    public String getPropertyValue() {
        return propertyValue;
    }

    @JsonProperty("propertyValue")
    public void setPropertyValue(String propertyValue) {
        this.propertyValue = propertyValue;
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