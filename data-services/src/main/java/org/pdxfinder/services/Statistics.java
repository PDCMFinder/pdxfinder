package org.pdxfinder.services;

import org.pdxfinder.services.dto.CountDTO;
import org.pdxfinder.services.dto.StatisticsDTO;
import org.pdxfinder.services.highchart.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/*
 * Created by abayomi on 20/06/2019.
 */
@Service
public class Statistics {

    private ChartHelper chartHelper = new ChartHelper();
    private Logger log = LoggerFactory.getLogger(Statistics.class);
    private String NULLSTRING = null;
    private Boolean TRUE = true;


    /*************************************************************************************************************
     *         CUSTOM COLUMN AND BAR CHARTS           *
     **************************************************/



    public ChartData basicColumnChart(List<CountDTO> stats, String chartName, String subtitle, HexColors hexColors) {

        List<String> categories = new ArrayList<>();
        List<Object> categorySum = new ArrayList<>();

        // Get all the Existing Keys
        List<Object> dataList = new ArrayList<>();

        stats.forEach(CountDTO -> {

            // Populate the chartData category List
            categories.add(CountDTO.getKey());
            dataList.add(CountDTO.getValue());

        });

        // Create the Column Charts Data to create a cluster
        List<Series> seriesList = new ArrayList<>();
        seriesList.add(chartHelper.columnChart(dataList, chartName, hexColors.get()));


        Title title = new Title(chartName);
        XAxis xAxis = new XAxis(categories);

        ChartData chartData = new ChartData(title, xAxis, seriesList);

        // Set Subtitle, Chart Title and ToolTip
        chartData = chartHelper.subtitleYAxisNToolTip(chartData, subtitle);

        return chartData;

    }



    public ChartData basicBarChart(List<CountDTO> stats, String chartName, String subtitle, HexColors hexColors) {

        ChartData chartData = basicColumnChart(stats, chartName, subtitle, hexColors) ;

        chartData.getSeries().forEach(Series ->{
            Series.setType(SeriesType.BAR.get());
        });
        return chartData;
    }




    public ChartData stackedBarChart(List<StatisticsDTO> stats, String chartTitle, String subtitle) {

        ChartData chartData = stackedColumnChart( stats, chartTitle, subtitle);

        chartData.getSeries().forEach(Series ->{
            Series.setType(SeriesType.BAR.get());
        });

        return chartData;

    }


    public ChartData stackedColumnChart(List<StatisticsDTO> stats, String chartTitle, String subtitle) {

        ChartData chartData = clusteredColumnChart(stats, chartTitle, subtitle);

        Series series = new Series();
        series.setStacking("normal");
        PlotOptions plotOptions = new PlotOptions();
        plotOptions.setSeries(series);

        chartData.setPlotOptions(plotOptions);

        return chartData;

    }



    public ChartData clusteredBarChart(List<StatisticsDTO> stats, String chartTitle, String subtitle) {

        ChartData chartData = clusteredColumnChart( stats, chartTitle, subtitle);

        chartData.getSeries().forEach(Series ->{
            Series.setType(SeriesType.BAR.get());
        });

        return chartData;

    }



    public ChartData clusteredColumnChart(List<StatisticsDTO> stats, String chartTitle, String subtitle) {

        List<String> categories = new ArrayList<>();

        // Get all the Existing Keys
        Map<String, List<Object>> dataMap = getKeysFromStatDTO(stats.get(0));

        stats.forEach(StatisticsDTO -> {

            // Populate the chartData category List
            categories.add(StatisticsDTO.getCategory());

            StatisticsDTO.getDataCounts().forEach(CountDTO -> {
                dataMap.get(CountDTO.getKey()).add(CountDTO.getValue());
            });

        });

        // Create the Column Charts Data to create a cluster
        List<Series> seriesList = clusterColumnData(dataMap);

        Title title = new Title(chartTitle);
        XAxis xAxis = new XAxis(categories, true);

        ChartData chartData = new ChartData(title, xAxis, seriesList);

        // Set Subtitle, Chart Title and ToolTip
        chartData = chartHelper.subtitleYAxisNToolTip(chartData, subtitle);

        return chartData;

    }




    public ChartData combinedColumnLineAndPieChart(List<StatisticsDTO> stats) {

        List<String> categories = new ArrayList<>();
        List<Object> categorySum = new ArrayList<>();

        // Get all the Existing Keys
        Map<String, List<Object>> dataMap = getKeysFromStatDTO(stats.get(0));

        stats.forEach(StatisticsDTO -> {

            // Populate the chartData category List
            categories.add(StatisticsDTO.getCategory());

            //
            StatisticsDTO.getDataCounts().forEach(CountDTO -> {
                dataMap.get(CountDTO.getKey()).add(CountDTO.getValue());
            });

            // Get Sum of Datasets in each Data Release category
            Integer sum = StatisticsDTO.getDataCounts().stream()
                    .map(x -> x.getValue())
                    .reduce(0, Integer::sum);
            categorySum.add(sum);

        });


        // Create the Column Charts Data to create a cluster
        List<Series> seriesList = clusterColumnData(dataMap);

        // Create a Spline ChartData of Total Dataset counts
        seriesList.add(chartHelper.splineChart(categorySum, "Total Data Per Release"));


        // Get Most Recent Dataset from the Clustered Data to create Pie-ChartData.
        List<PieData> pieDatas = new ArrayList<>();
        AtomicInteger counter = new AtomicInteger(0);
        stats.get(stats.size() - 1).getDataCounts().forEach(CountDTO -> {
            pieDatas.add(
                    new PieData(CountDTO.getKey(), CountDTO.getValue(), chartHelper.colors(counter.getAndIncrement()))
            );
        });

        seriesList.add(chartHelper.pieChart(pieDatas, "Molecular Data"));

        String chartTitle = "";
        String lablelString = "Data Distribution Today";
        String labelLeft = "50px";
        String labelTop = "18px";
        String labelColor = "#000000";

        Title title = new Title(chartTitle);

        XAxis xAxis = new XAxis(categories);

        Labels labels = chartHelper.simpleLabel(lablelString, labelLeft, labelTop, labelColor);

        ChartData chartData = new ChartData(title, xAxis, labels, seriesList);


        return chartData;

    }


    public ChartData fixedPlacementColumnChart(Map<String, List<StatisticsDTO>> data, String chartTitle) {


        Map colors = new HashMap();
        Map<String, Double> positions = new HashMap();
        List<YAxis> yAxisList = new ArrayList();


        List<Series> seriesList = new ArrayList<>();
        XAxis xAxis = new XAxis();
        int count = 0;

        for (Map.Entry<String, List<StatisticsDTO>> dData : data.entrySet()) {

            List<StatisticsDTO> stats = dData.getValue();

            List<String> categories = new ArrayList<>();
            Map<String, List<Object>> dataMap = new HashMap<>();

            // Get all the Existing Keys
            int pos = 0;
            Double position = -0.4;
            for (CountDTO countDTO : stats.get(0).getDataCounts()) {

                dataMap.put(countDTO.getKey() + "-" + dData.getKey(), new ArrayList<>());

                colors.put(countDTO.getKey() + "-" + dData.getKey(), chartHelper.colors(pos));
                positions.put(countDTO.getKey() + "-" + dData.getKey(), position);
                pos++;
                position = position + 0.2;
            }


            stats.forEach(StatisticsDTO -> {

                // Populate the chartData category List
                categories.add(StatisticsDTO.getCategory());

                //
                StatisticsDTO.getDataCounts().forEach(CountDTO -> {
                    dataMap.get(CountDTO.getKey() + "-" + dData.getKey()).add(CountDTO.getValue());
                });

            });

            xAxis = new XAxis(categories);
            Double opacity = 0.6;
            Double pointPadding = 0.4;
            Integer yAxis = 0;

            if (count == 0) {
                yAxisList.add(chartHelper.simpleYAxis(dData.getKey(), false));
            } else {
                opacity = 0.9;
                pointPadding = 0.35;
                yAxis = 1;
                yAxisList.add(chartHelper.simpleYAxis(dData.getKey(), true));
            }

            // Create the Column Charts Data to create a cluster
            for (Map.Entry<String, List<Object>> map : dataMap.entrySet()) {
                seriesList.add(chartHelper.columnChart(map.getValue(), map.getKey(), colors.get(map.getKey()), opacity, pointPadding, positions.get(map.getKey()), yAxis));
            }


            count++;
        }


        String chartType = SeriesType.COLUMN.get();

        Chart chart = new Chart(chartType);
        Title title = new Title(chartTitle);

        PlotOptions plotOptions = chartHelper.simplePlotOptions();

        ChartData chartData = new ChartData(chart, title, xAxis, seriesList, yAxisList, plotOptions);

        return chartData;
    }



    /*************************************************************************************************************
     *         CUSTOM BUILT PIE CHARTS           *
     **************************************************/

    public ChartData threeDPieChart(List<String> labels, List values, String title, String subtitle, String sliced) {

        ChartData chartData = pieChart(labels, values, title,subtitle, sliced);

        // SET 3D OPTION FOR CHART
        chartData.getChart().setOptions3d(new Options3d(true, 45));

        return chartData;
    }


    public ChartData pieWithLegend(List<String> labels, List values, String title, String subtitle, String sliced) {

        ChartData chartData = pieChart(labels, values, title,subtitle, sliced);
        chartData.getPlotOptions().getPie().setShowInLegend(true);

        return chartData;
    }


    public ChartData pieChart(List<String> labels, List values, String title, String subtitle, String sliced) {

        AtomicInteger count = new AtomicInteger(0);
        List<String> colors = new ArrayList<>();

        // CREATE CHART TITLE
        Title charTitle = new Title(title);
        String description = "Million user";

        // BUILD SERIES DATA
        List data = new ArrayList();
        labels.forEach(label -> {

            data.add( (label.equals(sliced) && label != null) ?
                    new PieData(label, values.get(count.getAndIncrement()), TRUE, TRUE) : Arrays.asList(label, values.get(count.getAndIncrement()))
            );
            colors.add(chartHelper.colors(count.get()));
        });

        Series series = new Series(null, description, data);

        // CREATE CHART DATA
        ChartData chartData = new ChartData(charTitle, null, Arrays.asList(series));

        // SET 3D OPTION FOR CHART
        Chart chart = new Chart(SeriesType.PIE.get());
        chartData.setChart(chart);

        // CREATE CHART SUBTITLE
        Subtitle chartSubtitle = new Subtitle(subtitle);
        chartData.setSubtitle(chartSubtitle);

        // CREATE PLOT OPTIONS
        PlotOptions plotOptions = new PlotOptions(chartHelper.plotPie());
        chartData.setPlotOptions(plotOptions);

        // USE TOOL TIP
        ToolTip toolTip = chartHelper.pieHTMLToolTip();
        chartData.setTooltip(toolTip);

        chartData.setColors(colors);
        return chartData;
    }


    public ChartData doughNutPie(List<String> labels, List values, String title, String subtitle, String sliced) {

        ChartData chartData = pieChart(labels, values, title,subtitle, sliced);

        //SET DOUGHNUT OPTION
        chartData.getPlotOptions().getPie().setInnerSize(100);

        return chartData;
    }

    public ChartData threeDdoughNutPie(List<String> labels, List values, String title, String subtitle, String sliced) {

        ChartData chartData = threeDPieChart(labels, values, title,subtitle, sliced);

        //SET DOUGHNUT OPTION
        chartData.getPlotOptions().getPie().setInnerSize(100);

        return chartData;
    }







    private List<Series> clusterColumnData(Map<String, List<Object>> dataMap) {

        List<Series> seriesList = new ArrayList<>();

        int count = 0;
        for (Map.Entry<String, List<Object>> map : dataMap.entrySet()) {
            seriesList.add(chartHelper.columnChart(map.getValue(), map.getKey(), chartHelper.colors(count++)));
        }

        return seriesList;
    }


    private Map<String, List<Object>> getKeysFromStatDTO(StatisticsDTO stats) {

        Map<String, List<Object>> dataMap = new LinkedHashMap<>();
        stats.getDataCounts().forEach(CountDTO -> {
            dataMap.put(CountDTO.getKey(), new ArrayList<>());
        });

        return dataMap;
    }


    public Map<String, List<StatisticsDTO>> groupedData() {

        Map<String, List<StatisticsDTO>> data = new HashMap<>();

        data.put("Patients", pdxCountHavingTreatmentDataPerDataSource());
        data.put("Drugs", treatmentsCountPerDataSource());

        return data;
    }



    public List<StatisticsDTO> pdxCountHavingDrugDataPerDataSource() {

        List<CountDTO> count1 = Arrays.asList(
                new CountDTO("IRCC-CRC", 275),
                new CountDTO("JAX", 81),
                new CountDTO("PDXNet-HCI-BCM", 0)
        );

        List<CountDTO> count2 = Arrays.asList(
                new CountDTO("IRCC-CRC", 275),
                new CountDTO("JAX", 90),
                new CountDTO("PDXNet-HCI-BCM", 6)
        );

        List<CountDTO> count3 = Arrays.asList(
                new CountDTO("IRCC-CRC", 275),
                new CountDTO("JAX", 89),
                new CountDTO("PDXNet-HCI-BCM", 6)
        );

        List<StatisticsDTO> stats = Arrays.asList(
                new StatisticsDTO("APR 2018", count1),
                new StatisticsDTO("APR 2019", count2),
                new StatisticsDTO("OCT 2019", count3)
        );

        return stats;
    }


    public List<StatisticsDTO> drugCountPerDataSource() {

        List<CountDTO> count1 = Arrays.asList(
                new CountDTO("IRCC-CRC", 1),
                new CountDTO("JAX", 20),
                new CountDTO("PDXNet-HCI-BCM", 0)
        );

        List<CountDTO> count2 = Arrays.asList(
                new CountDTO("IRCC-CRC", 1),
                new CountDTO("JAX", 20),
                new CountDTO("PDXNet-HCI-BCM", 5)
        );

        List<CountDTO> count3 = Arrays.asList(
                new CountDTO("IRCC-CRC", 2),
                new CountDTO("JAX", 24),
                new CountDTO("PDXNet-HCI-BCM", 5)
        );

        List<StatisticsDTO> stats = Arrays.asList(
                new StatisticsDTO("APR 2018", count1),
                new StatisticsDTO("APR 2019", count2),
                new StatisticsDTO("OCT 2019", count3)
        );

        return stats;
    }



    public List<StatisticsDTO> pdxCountHavingTreatmentDataPerDataSource() {


        List<CountDTO> count1 = Arrays.asList(
                new CountDTO("IRCC-GC", 26),
                new CountDTO("PDMR", 529),
                new CountDTO("CRL", 142),
                new CountDTO("TRACE", 75),
                new CountDTO("UOM-BC", 0)
        );

        List<CountDTO> count2 = Arrays.asList(
                new CountDTO("IRCC-GC", 36),
                new CountDTO("PDMR", 633),
                new CountDTO("CRL", 145),
                new CountDTO("TRACE", 75),
                new CountDTO("UOM-BC", 47)
        );

        List<StatisticsDTO> stats = Arrays.asList(
                new StatisticsDTO("APR 2019", count1),
                new StatisticsDTO("OCT 2019", count2)
        );

        return stats;
    }


    public List<StatisticsDTO> treatmentsCountPerDataSource() {

        List<CountDTO> count1 = Arrays.asList(
                new CountDTO("IRCC-GC", 8),
                new CountDTO("PDMR", 84),
                new CountDTO("CRL", 41),
                new CountDTO("TRACE", 24)
        );

        List<CountDTO> count2 = Arrays.asList(
                new CountDTO("IRCC-GC", 11),
                new CountDTO("PDMR", 88),
                new CountDTO("CRL", 41),
                new CountDTO("TRACE", 24),
                new CountDTO("UOM-BC", 27)
        );

        List<StatisticsDTO> stats = Arrays.asList(
                new StatisticsDTO("APR 2019", count1),
                new StatisticsDTO("OCT 2019", count2)
        );

        return stats;
    }



    public List<StatisticsDTO> mockRepository() {

        List<CountDTO> count1 = Arrays.asList(
                new CountDTO("Mutation", 808),
                new CountDTO("Cytogenetics", 0),
                new CountDTO("Dosing Studies", 356),
                new CountDTO("CNA", 0),
                new CountDTO("Patient Treatment", 0)
        );

        List<CountDTO> count2 = Arrays.asList(
                new CountDTO("Mutation", 1472),
                new CountDTO("Cytogenetics", 81),
                new CountDTO("Dosing Studies", 371),
                new CountDTO("CNA", 729),
                new CountDTO("Patient Treatment", 216)
        );

        List<CountDTO> count3 = Arrays.asList(
                new CountDTO("Mutation", 1546),
                new CountDTO("Cytogenetics", 128),
                new CountDTO("Dosing Studies", 370),
                new CountDTO("CNA", 1082),
                new CountDTO("Patient Treatment", 244)
        );


        List<StatisticsDTO> stats = Arrays.asList(
                new StatisticsDTO("APR 2018", count1),
                new StatisticsDTO("APR 2019", count2),
                new StatisticsDTO("OCT 2019", count3)
        );

        return stats;
    }


    public List<CountDTO> modelCount() {

        List<CountDTO> count = Arrays.asList(
                new CountDTO("APR 2018", 1910),
                new CountDTO("APR 2019", 2629),
                new CountDTO("OCT 2019", 2888)
        );

        return count;
    }


    public List<CountDTO> providersCount() {

        List<CountDTO> count = Arrays.asList(
                new CountDTO("APR 2018", 7),
                new CountDTO("APR 2019", 12),
                new CountDTO("OCT 2019", 18)
        );

        return count;
    }


}
