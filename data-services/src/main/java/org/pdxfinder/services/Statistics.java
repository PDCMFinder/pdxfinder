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


    public ChartData barChart(List<CountDTO> stats, String chartName, String subtitle, HexColors hexColors) {

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


    public ChartData clusteredBarChart(List<StatisticsDTO> stats, String chartTitle, String subtitle) {

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


    public Object threeDPieChart(List<String> labels, List values, String title, String subtitle, String sliced) {

        AtomicInteger count = new AtomicInteger(0);

        // CREATE CHART TITLE
        Title charTitle = new Title(title);
        String description = "Million user";

        // BUILD SERIES DATA
        List data = new ArrayList();
        labels.forEach(label -> {

            data.add( (label.equals(sliced) && label != null) ?
                    new PieData(label, values.get(count.getAndIncrement()), TRUE, TRUE) : Arrays.asList(label, values.get(count.getAndIncrement()))
            );
        });

        Series series = new Series(null, description, data);

        // CREATE CHART DATA
        ChartData chartData = new ChartData(charTitle, null, Arrays.asList(series));


        // SET 3D OPTION FOR CHART
        Chart chart = new Chart(SeriesType.PIE.get());
        chart.setOptions3d(new Options3d(true, 45));
        chartData.setChart(chart);

        // CREATE CHART SUBTITLE
        Subtitle chartSubtitle = new Subtitle(subtitle);
        chartData.setSubtitle(chartSubtitle);

        // CREATE PLOT OPTIONS
        PlotOptions plotOptions = new PlotOptions(chartHelper.doughNutPie());
        chartData.setPlotOptions(plotOptions);

        // USE TOOL TIP
        ToolTip toolTip = chartHelper.pieHTMLToolTip();
        chartData.setTooltip(toolTip);

        return chartData;
    }


    // {name:'LinkedIn',  y:450, sliced: true,selected: true}

        /*

                ['Facebook', 1300],
                        ['Google+', 375],
    {name:'LinkedIn',  y:450, sliced: true,selected: true},
            ['Twitter', 313],
            ['YouTube', 1000]

threeDPieChart(List labels, List values, String title, String subtitle)

     */


    // list of colors

    /*

    Highcharts.chart('pie', {
    chart: {
        type: 'pie',
        options3d: {
            enabled: true,
            alpha: 45
        }
    },
    title: {
        text: 'Global Social Media User Statistics'
    },
    subtitle: {
        text: 'As per statistics data 2016'
    },
    plotOptions: {
        pie: {
            innerSize: 100,
            depth: 65,
			allowPointSelect: true,
            cursor: 'pointer',
            dataLabels: {
                enabled: true,
                format: '{point.name} ({point.percentage:.1f}%)',
				connectorWidth: 2,
            }
        }
    },
	colors:['#3b5998', '#d34836', '#8a3ab9', '#0077B5', '#0084b4', '#bb0000'],
    series: [{
        name: 'Million user',
        data: [
            ['Facebook', 1300],
			['Google+', 375],
			['Instagram', 500],
			{name:'LinkedIn',  y:450, sliced: true,selected: true},
			['Twitter', 313],
			['YouTube', 1000]
        ]
    }],
	tooltip:{
		useHTML: true,
		 headerFormat: '<h1>{point.key}</h1>',
		 pointFormat: '<h4>{point.percentage:.1f} {series.name} </h4>',
	}
});


     */


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

        data.put("Patients", mockDataTreatmentPatients());
        data.put("Drugs", mockDataDrugDosing());

        return data;
    }


    public List<StatisticsDTO> mockDataTreatmentPatients() {

        // Mock Data Generation Starts

        List<CountDTO> count1 = Arrays.asList(
                new CountDTO("IRCC-CRC", 275),
                new CountDTO("JAX", 90),
                new CountDTO("PDXNet-HCI-BCM", 6)
        );

        List<CountDTO> count2 = Arrays.asList(
                new CountDTO("IRCC-CRC", 375),
                new CountDTO("JAX", 190),
                new CountDTO("PDXNet-HCI-BCM", 106)
        );

        List<CountDTO> count3 = Arrays.asList(
                new CountDTO("IRCC-CRC", 475),
                new CountDTO("JAX", 290),
                new CountDTO("PDXNet-HCI-BCM", 206)
        );

        List<CountDTO> count4 = Arrays.asList(
                new CountDTO("IRCC-CRC", 575),
                new CountDTO("JAX", 390),
                new CountDTO("PDXNet-HCI-BCM", 306)
        );

        List<StatisticsDTO> stats = Arrays.asList(
                new StatisticsDTO("JAN 2019", count1),
                new StatisticsDTO("MAR 2019", count2),
                new StatisticsDTO("JUN 2019", count3),
                new StatisticsDTO("NOV 2019", count4)
        );

        // Mock data Generation Ends

        return stats;
    }


    public List<StatisticsDTO> mockDataDrugDosing() {

        // Mock Data Generation Starts

        List<CountDTO> count1 = Arrays.asList(
                new CountDTO("IRCC-CRC", 1),
                new CountDTO("JAX", 20),
                new CountDTO("PDXNet-HCI-BCM", 5)
        );

        List<CountDTO> count2 = Arrays.asList(
                new CountDTO("IRCC-CRC", 101),
                new CountDTO("JAX", 120),
                new CountDTO("PDXNet-HCI-BCM", 105)
        );

        List<CountDTO> count3 = Arrays.asList(
                new CountDTO("IRCC-CRC", 201),
                new CountDTO("JAX", 220),
                new CountDTO("PDXNet-HCI-BCM", 205)
        );

        List<CountDTO> count4 = Arrays.asList(
                new CountDTO("IRCC-CRC", 301),
                new CountDTO("JAX", 320),
                new CountDTO("PDXNet-HCI-BCM", 305)
        );

        List<StatisticsDTO> stats = Arrays.asList(
                new StatisticsDTO("JAN 2019", count1),
                new StatisticsDTO("MAR 2019", count2),
                new StatisticsDTO("JUN 2019", count3),
                new StatisticsDTO("NOV 2019", count4)
        );

        // Mock data Generation Ends

        return stats;
    }


    public List<StatisticsDTO> mockRepository() {

        // Mock Data Generation Starts

        List<CountDTO> count1 = Arrays.asList(
                new CountDTO("mutation", 1000),
                new CountDTO("cytogenetics", 500),
                new CountDTO("cna", 700)
        );

        List<CountDTO> count2 = Arrays.asList(
                new CountDTO("mutation", 3000),
                new CountDTO("cytogenetics", 4000),
                new CountDTO("cna", 1000)
        );

        List<CountDTO> count3 = Arrays.asList(
                new CountDTO("mutation", 5000),
                new CountDTO("cytogenetics", 4500),
                new CountDTO("cna", 4000)
        );

        List<CountDTO> count4 = Arrays.asList(
                new CountDTO("mutation", 8000),
                new CountDTO("cytogenetics", 5900),
                new CountDTO("cna", 5000)
        );

        List<StatisticsDTO> stats = Arrays.asList(
                new StatisticsDTO("JAN 2019", count1),
                new StatisticsDTO("MAR 2019", count2),
                new StatisticsDTO("JUN 2019", count3),
                new StatisticsDTO("NOV 2019", count4)
        );

        // Mock data Generation Ends

        return stats;
    }


    public List<CountDTO> modelCountData() {

        List<CountDTO> count = Arrays.asList(
                new CountDTO("JAN 2019", 900),
                new CountDTO("MAR 2019", 1350),
                new CountDTO("JUN 2019", 1915),
                new CountDTO("NOV 2019", 2500)
        );

        return count;
    }


    public List<CountDTO> providersCountData() {

        List<CountDTO> count = Arrays.asList(
                new CountDTO("JAN 2019", 3),
                new CountDTO("MAR 2019", 5),
                new CountDTO("JUN 2019", 10),
                new CountDTO("NOV 2019", 20)
        );

        return count;
    }


}
