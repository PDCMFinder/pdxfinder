package org.pdxfinder.services.zooma;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({
        "propertyType",
        "propertyValue"
})
public class Property {

    private String propertyType;
    private String propertyValue;

    public Property() {
    }

    public Property(String propertyType, String propertyValue) {
        this.propertyType = propertyType;
        this.propertyValue = propertyValue;
    }

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

}