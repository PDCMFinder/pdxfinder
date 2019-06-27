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
 * Created by abayomi on 19/06/2019.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "chart",
        "title",
        "xAxis",
        "yAxis",
        "plotOptions",
        "series",
        "labels"
})
public class ChartData {

    private Chart chart;
    private Title title;
    private XAxis xAxis;
    private List<YAxis> yAxis;
    private PlotOptions plotOptions;
    private List<Series> series;
    private Labels labels;


    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();


    public ChartData(Title title, XAxis xAxis, Labels labels, List<Series> series) {
        this.title = title;
        this.xAxis = xAxis;
        this.labels = labels;
        this.series = series;
    }

    public ChartData(Chart chart, Title title, XAxis xAxis, List<Series> series, List<YAxis> yAxis, PlotOptions plotOptions) {
        this.chart = chart;
        this.title = title;
        this.xAxis = xAxis;
        this.series = series;
        this.yAxis = yAxis;
        this.plotOptions = plotOptions;
    }

    public Chart getChart() {
        return chart;
    }

    public void setChart(Chart chart) {
        this.chart = chart;
    }

    public Title getTitle() {
        return title;
    }

    public void setTitle(Title title) {
        this.title = title;
    }

    public XAxis getxAxis() {
        return xAxis;
    }

    public void setxAxis(XAxis xAxis) {
        this.xAxis = xAxis;
    }


    public List<YAxis> getyAxis() {
        return yAxis;
    }

    public void setyAxis(List<YAxis> yAxis) {
        this.yAxis = yAxis;
    }

    public Labels getLabels() {
        return labels;
    }

    public void setLabels(Labels labels) {
        this.labels = labels;
    }

    public List<Series> getSeries() {
        return series;
    }

    public void setSeries(List<Series> series) {
        this.series = series;
    }

    public PlotOptions getPlotOptions() {
        return plotOptions;
    }

    public void setPlotOptions(PlotOptions plotOptions) {
        this.plotOptions = plotOptions;
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