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
        "innerSize",
        "depth",
        "allowPointSelect",
        "cursor",
        "dataLabels"
})
public class Pie {


    private Integer innerSize;
    private Integer depth;
    private Boolean allowPointSelect;
    private String cursor;
    private DataLabels dataLabels;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public Pie() {
    }

    public Pie(Integer innerSize, Integer depth, Boolean allowPointSelect, String cursor, DataLabels dataLabels) {
        this.innerSize = innerSize;
        this.depth = depth;
        this.allowPointSelect = allowPointSelect;
        this.cursor = cursor;
        this.dataLabels = dataLabels;
    }

    public Integer getInnerSize() {
        return innerSize;
    }

    public void setInnerSize(Integer innerSize) {
        this.innerSize = innerSize;
    }

    public Integer getDepth() {
        return depth;
    }

    public void setDepth(Integer depth) {
        this.depth = depth;
    }

    public Boolean getAllowPointSelect() {
        return allowPointSelect;
    }

    public void setAllowPointSelect(Boolean allowPointSelect) {
        this.allowPointSelect = allowPointSelect;
    }

    public String getCursor() {
        return cursor;
    }

    public void setCursor(String cursor) {
        this.cursor = cursor;
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