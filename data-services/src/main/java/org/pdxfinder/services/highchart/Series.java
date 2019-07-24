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

/*
 * Created by abayomi on 19/06/2019.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Series {

    private String type;
    private String name;
    private Object data;
    private Object color;
    private Marker marker;
    private String dashStyle;
    private Double opacity;
    private Double pointPadding;
    private Double pointPlacement;
    private Integer yAxis;
    private ToolTip toolTip;

    private List<Integer> center;
    private Integer size;
    private Boolean showInLegend;
    private DataLabels dataLabels;

    private String stacking;
    private Boolean colorByPoint;

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

    public Series(String type, String name, Object data, Object color, Double opacity, Double pointPadding, Double pointPlacement, Integer yAxis) {
        this.type = type;
        this.name = name;
        this.data = data;
        this.color = color;
        this.opacity = opacity;
        this.pointPadding = pointPadding;
        this.pointPlacement = pointPlacement;
        this.yAxis = yAxis;
    }

    public Series(String type, String name, Object data, List<Integer> center, Integer size, Boolean showInLegend) {
        this.type = type;
        this.name = name;
        this.data = data;
        this.center = center;
        this.size = size;
        this.showInLegend = showInLegend;
    }

    public Series(Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
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

    public Object getColor() {
        return color;
    }

    public void setColor(Object color) {
        this.color = color;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public String getDashStyle() {
        return dashStyle;
    }

    public void setDashStyle(String dashStyle) {
        this.dashStyle = dashStyle;
    }

    public Double getOpacity() {
        return opacity;
    }

    public void setOpacity(Double opacity) {
        this.opacity = opacity;
    }


    public Double getPointPadding() {
        return pointPadding;
    }

    public void setPointPadding(Double pointPadding) {
        this.pointPadding = pointPadding;
    }

    public Double getPointPlacement() {
        return pointPlacement;
    }

    public void setPointPlacement(Double pointPlacement) {
        this.pointPlacement = pointPlacement;
    }

    public Integer getyAxis() {
        return yAxis;
    }

    public void setyAxis(Integer yAxis) {
        this.yAxis = yAxis;
    }

    public ToolTip getToolTip() {
        return toolTip;
    }

    public void setToolTip(ToolTip toolTip) {
        this.toolTip = toolTip;
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

    public String getStacking() {
        return stacking;
    }

    public void setStacking(String stacking) {
        this.stacking = stacking;
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
