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
@JsonPropertyOrder({
        "labels",
        "title",
        "opposite"
})
public class YAxis {


    private Integer min;
    private Labels labels;
    private Title title;
    private Boolean opposite;
    private ToolTip toolTip;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();


    public YAxis() {
    }

    public YAxis(Title title, Boolean opposite) {
        this.title = title;
        this.opposite = opposite;
    }

    public YAxis(Labels labels, Title title, Boolean opposite) {
        this.labels = labels;
        this.title = title;
        this.opposite = opposite;
    }

    public Integer getMin() {
        return min;
    }

    public void setMin(Integer min) {
        this.min = min;
    }

    public Labels getLabels() {
        return labels;
    }

    public void setLabels(Labels labels) {
        this.labels = labels;
    }

    public Title getTitle() {
        return title;
    }

    public void setTitle(Title title) {
        this.title = title;
    }

    public Boolean getOpposite() {
        return opposite;
    }

    public void setOpposite(Boolean opposite) {
        this.opposite = opposite;
    }

    public ToolTip getToolTip() {
        return toolTip;
    }

    public void setToolTip(ToolTip toolTip) {
        this.toolTip = toolTip;
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