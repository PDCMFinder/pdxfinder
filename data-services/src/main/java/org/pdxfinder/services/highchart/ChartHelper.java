package org.pdxfinder.services.highchart;

import java.util.Arrays;
import java.util.List;

/*
 * Created by abayomi on 19/06/2019.
 */
public class ChartHelper {

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




}
