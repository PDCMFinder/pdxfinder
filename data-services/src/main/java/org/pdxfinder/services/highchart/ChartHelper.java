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


/*
    {
        name: 'IRCC-MODEL',
                color: '#000',
            opacity: 0.4,
            data: [150, 73, 20],

        pointPadding: 0.35,
                pointPlacement: -0.2,
            yAxis: 0
    }
*/

    public Series columnChart(List<Object> data, String chartName, String color){

        String type = SeriesType.COLUMN.get();

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


    public String colors(int pos){
        String colors[] = {"#800080", "#000", "#FF0000", "#6F42C1", "#03458E", "#40424B", "#BADAFC", "#FF0F00", "#CD0D74", "#2E92FE", "#2A0CD0", "#0D52D1"};
        return colors[pos];
    }

}



    /*


    Highcharts.chart('container', {
  chart: {
    type: 'column'
  },
  title: {
    text: 'Efficiency Optimization by Branch'
  },
  xAxis: {
    categories: ['January','February','March']
  },

  yAxis: [{
    min: 0,
    title: {
      text: 'Employees'
    }
  }, {
    title: {
      text: 'Profit (millions)'
    },
    opposite: true
  }],


  plotOptions: {
    column: {
      grouping: false,
      shadow: true,
      borderWidth: 1
    }
  },
  series: [{
    name: 'IRCC-MODEL',
    color: '#000',
    opacity: 0.4,
    data: [150, 73, 20],
    pointPadding: 0.35,
    pointPlacement: -0.2,
    yAxis: 0
  }, {
    name: 'IRCC-DRUG',
    color: '#000',
    opacity: 0.9,
    data: [120, 90, 40],
    pointPadding: 0.4,
    pointPlacement: -0.2,
    yAxis: 1
  }, {
    name: 'JAX-MODEL',
    color: '#FF0000',
    opacity: 0.4,
    data: [183.6, 178.8, 198.5],
    tooltip: {
      valuePrefix: '$',
      valueSuffix: ' M'
    },
    pointPadding: 0.35,
    pointPlacement: 0.2,
    yAxis: 0
  }, {
    name: 'JAX-DRUG',
    color: '#FF0000',
    opacity: 0.9,
    data: [203.6, 198.8, 208.5],
    tooltip: {
      valuePrefix: '$',
      valueSuffix: ' M'
    },
    pointPadding: 0.4,
    pointPlacement: 0.2,
    yAxis: 1
  }]
});


     */


