package org.pdxfinder.services.highchart;

import java.util.Arrays;
import java.util.List;

/*
 * Created by abayomi on 19/06/2019.
 */
public class ChartHelper {

    private String NULLSTRING = null;
    private Boolean NULLBOOLEAN = null;

    public ChartHelper() {
    }

    public Series splineChart(List<Object> data, String chartName){

        String type = SeriesType.SPLINE.get();
        int lineWidth = 2;
        String lineColor = "#007BFF";
        String fillColor = "white";

        Marker marker = new Marker(lineWidth,lineColor,fillColor);

        Series splineSeries = new Series(type,chartName,data);
        splineSeries.setMarker(marker);

        return splineSeries;
    }




    public Series columnChart(List<Object> data, String chartName, String color){

        String type = SeriesType.COLUMN.get();

        Series columnSeries = new Series(type,chartName,data,color);

        return columnSeries;
    }


    public Series barChart(List<Object> data, String chartName, String color){

        String type = SeriesType.BAR.get();

        Series columnSeries = new Series(type,chartName,data,color);

        return columnSeries;
    }


    public Series columnChart(List<Object> data, String chartName, Object color, Double opacity, Double pointPadding, Double pointPlacement, Integer yAxis){

        String type = SeriesType.COLUMN.get();

        Series columnSeries = new Series(type, chartName, data, color, opacity, pointPadding, pointPlacement, yAxis);

        return columnSeries;
    }


    public Series pieChart(List<PieData> data, String chartName){

        String type = SeriesType.PIE.get();
        List<Integer> center = Arrays.asList(100,80);
        Integer size = 100;
        Boolean showInLegend = false;

        Series pieSeries = new Series(type, chartName, data, center, size, showInLegend);
        DataLabels dataLabels = new DataLabels(false);
        pieSeries.setDataLabels(dataLabels);

        return pieSeries;
    }


    public Labels simpleLabel(String html, String left, String top, String color){

        Style style = new Style(left,top,color);

        Item item = new Item(html, style);

        Labels labels = new Labels(Arrays.asList(item));

        return labels;
    }


    public YAxis simpleYAxis(String labelText, String titleText, String labelColor, String titleColor, Boolean isOpposite){

        Style labelStyle = new Style(labelColor);
        String format = "{value}"+labelText;
        Labels labels = new Labels(format,labelStyle);

        Style titleStyle = new Style(titleColor);
        Title title = new Title(titleText, titleStyle);

        YAxis yAxis = new YAxis(labels,title,isOpposite);

        return yAxis;
    }

    public YAxis simpleYAxis(String titleText, Boolean isOpposite){

        Title title = new Title(titleText);
        YAxis yAxis = new YAxis(title,isOpposite);
        return yAxis;
    }

    public PlotOptions buildPlotOptions(Boolean grouping, Boolean shadow, Integer borderWidth){

        Column column = new Column(grouping, shadow, borderWidth);
        PlotOptions plotOptions = new PlotOptions(column);

        return plotOptions;
    }

    public PlotOptions simplePlotOptions(){
        return buildPlotOptions(false, true, 1);
    }

    public ToolTip customSharedHTMLToolTip(){

        Boolean shared = true;
        String headerFormat = ChartStrings.HTML_HEAD_FORMAT.get();
        String pointFormat = ChartStrings.HTML_POINT_FORMAT.get();
        String footerFormat= ChartStrings.HTML_FOOTER_FORMAT.get();
        Boolean useHTML = true;

        return new ToolTip(shared, headerFormat, pointFormat, footerFormat, useHTML);
    }


    public ToolTip pieHTMLToolTip(){

        String headerFormat = "<h1>{point.key}</h1>";
        String pointFormat = "<h4>{point.percentage:.1f} {series.name} </h4>";
        Boolean useHTML = true;

        return new ToolTip(NULLBOOLEAN, headerFormat, pointFormat,NULLSTRING, useHTML);
    }



    public Pie plotPie(){

        String pointFormat = "{point.name} ({point.percentage:.1f}%)";
        Integer connectorWidth = 2;
        Boolean enabled = true;

        Integer innerSize = 0;
        Integer depth = 65;
        Boolean allowPointSelect = true;
        String cursor = "pointer";

        DataLabels dataLabels = new DataLabels(enabled,pointFormat,connectorWidth);
        Pie pie = new Pie(innerSize,depth,allowPointSelect,cursor, dataLabels);

        return pie;
    }

    public ChartData subtitleYAxisNToolTip(ChartData chartData, String subtitle){

        // Set Subtitle
        chartData.setSubtitle(new Subtitle(subtitle));

        // Set Chart Title
        chartData.setyAxis(Arrays.asList(new YAxis(new Title(subtitle))));

        // Set ToolTip
        chartData.setTooltip(customSharedHTMLToolTip());

        return chartData;
    }



    public String colors(int pos){
        String colors[] = {"#3b5998","#d34836","#8a3ab9","#0077B5","#0084b4","#800080", "#000000", "#FF0000", "#6F42C1", "#03458E", "#40424B", "#BADAFC", "#FF0F00", "#CD0D74", "#2E92FE", "#2A0CD0", "#0D52D1"};
        return colors[pos];
    }

}



