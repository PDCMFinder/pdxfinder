package org.pdxfinder.services.highchart;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "type",
        "name",
        "data"
})
public class Series {

    private String type;
    private String name;
    private Object data;
    private String color;
    private Marker marker;

    private List<Integer> center;
    private Integer size;
    private Boolean showInLegend;
    private DataLabels dataLabels;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();


    public Series() {
    }

    public Series(String type, String name, Object data) {
        this.type = type;
        this.name = name;
        this.data = data;
    }

    public Series(String type, String name, Object data, String color) {
        this.type = type;
        this.name = name;
        this.data = data;
        this.color = color;
    }

    public Series(String type, String name, Object data, List<Integer> center, Integer size, Boolean showInLegend) {
        this.type = type;
        this.name = name;
        this.data = data;
        this.center = center;
        this.size = size;
        this.showInLegend = showInLegend;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public List<Integer> getCenter() {
        return center;
    }

    public void setCenter(List<Integer> center) {
        this.center = center;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Boolean getShowInLegend() {
        return showInLegend;
    }

    public void setShowInLegend(Boolean showInLegend) {
        this.showInLegend = showInLegend;
    }

    public DataLabels getDataLabels() {
        return dataLabels;
    }

    public void setDataLabels(DataLabels dataLabels) {
        this.dataLabels = dataLabels;
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
