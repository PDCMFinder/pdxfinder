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
        "type",
        "zoomType"
})
public class Chart {

    private String type;
    private String zoomType;
    private Options3d options3d;

    private String plotBackgroundColor;
    private Integer plotBorderWidth;
    private Boolean plotShadow;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public Chart() {
    }

    public Chart(String type) {
        this.type = type;
    }

    public Chart(String type, String zoomType) {
        this.type = type;
        this.zoomType = zoomType;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getZoomType() {
        return zoomType;
    }

    public void setZoomType(String zoomType) {
        this.zoomType = zoomType;
    }

    public Options3d getOptions3d() {
        return options3d;
    }

    public void setOptions3d(Options3d options3d) {
        this.options3d = options3d;
    }

    public String getPlotBackgroundColor() {
        return plotBackgroundColor;
    }

    public void setPlotBackgroundColor(String plotBackgroundColor) {
        this.plotBackgroundColor = plotBackgroundColor;
    }

    public Integer getPlotBorderWidth() {
        return plotBorderWidth;
    }

    public void setPlotBorderWidth(Integer plotBorderWidth) {
        this.plotBorderWidth = plotBorderWidth;
    }

    public Boolean getPlotShadow() {
        return plotShadow;
    }

    public void setPlotShadow(Boolean plotShadow) {
        this.plotShadow = plotShadow;
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