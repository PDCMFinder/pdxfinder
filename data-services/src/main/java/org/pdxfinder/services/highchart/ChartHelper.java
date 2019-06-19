package org.pdxfinder.services.highchart;

import java.util.Arrays;
import java.util.List;

public class ChartHelper {


    public Series splineChart(List<Double> data, String chartName){

        String type = "spline";
        int lineWidth = 2;
        String lineColor = "#007BFF";
        String fillColor = "white";

        Marker marker = new Marker(lineWidth,lineColor,fillColor);

        Series splineSeries = new Series(type,chartName,data);
        splineSeries.setMarker(marker);

        return splineSeries;
    }


    public Series columnChart(List<Double> data, String chartName){

        String type = "column";
        String color = "#007BFF";

        Series columnSeries = new Series(type,chartName,data,color);

        return columnSeries;
    }


    public Series pieChart(List<PieData> data, String chartName){

        String type = "pie";
        List<Integer> center = Arrays.asList(100,80);
        Integer size = 100;
        Boolean showInLegend = false;

        Series pieSeries = new Series(type, chartName, data, center, size, showInLegend);
        DataLabels dataLabels = new DataLabels(false);
        pieSeries.setDataLabels(dataLabels);

        return pieSeries;
    }




}
