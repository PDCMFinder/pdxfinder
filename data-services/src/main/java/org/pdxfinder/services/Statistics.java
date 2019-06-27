package org.pdxfinder.services;

import org.pdxfinder.services.dto.CountDTO;
import org.pdxfinder.services.dto.StatisticsDTO;
import org.pdxfinder.services.highchart.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/*
 * Created by abayomi on 20/06/2019.
 */
@Service
public class Statistics {

    private ChartHelper chartHelper = new ChartHelper();
    private Logger log = LoggerFactory.getLogger(Statistics.class);


    public Object combinedColumnLineAndPieChart(List<StatisticsDTO> stats){

        List<String> categories = new ArrayList<>();
        Map<String, List<Object>> dataMap = new HashMap<>();
        List<Object> categorySum = new ArrayList<>();

        // Get all the Existing Keys
        stats.get(0).getDataCounts().forEach(CountDTO->{
            dataMap.put(CountDTO.getKey(), new ArrayList<>());
        });


        stats.forEach(StatisticsDTO ->{

            // Populate the chartData category List
            categories.add(StatisticsDTO.getCategory());

            //
            StatisticsDTO.getDataCounts().forEach(CountDTO->{
                dataMap.get(CountDTO.getKey()).add(CountDTO.getValue());
            });

            // Get Sum of Datasets in each Data Release category
            Integer sum = StatisticsDTO.getDataCounts().stream()
                    .map(x -> x.getValue())
                    .reduce(0, Integer::sum);
            categorySum.add(sum);

        });


        // Create the Column Charts Data to create a cluster
        List<Series> seriesList = new ArrayList<>();
        for (Map.Entry<String, List<Object>> map : dataMap.entrySet()){
            seriesList.add( chartHelper.columnChart(map.getValue(), map.getKey(), "#03458E") );
        }


        // Create a Spline ChartData of Total Dataset counts
        seriesList.add( chartHelper.splineChart(categorySum, "Aggregate") );


        // Get Most Recent Dataset from the Clustered Data to create Pie-ChartData.
        List<PieData> pieDatas = new ArrayList<>();
        stats.get(stats.size()-1).getDataCounts().forEach(CountDTO->{
            pieDatas.add(
                    new PieData(CountDTO.getKey(),CountDTO.getValue(),"#03458E")
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

        Labels labels = chartHelper.simpleLabel(lablelString , labelLeft, labelTop, labelColor);

        ChartData chartData = new ChartData(title,xAxis,labels,seriesList);


        return chartData;

    }



    public Object fixedPlacementColumnChart(Map<String, List<StatisticsDTO>> data,
                                            String chartTitle){


        Map colors = new HashMap();
        Map<String, Double> positions = new HashMap();
        List<YAxis> yAxisList = new ArrayList();


        List<Series> seriesList = new ArrayList<>();
        XAxis xAxis = new XAxis();
        int count = 0;

        for (Map.Entry<String, List<StatisticsDTO>> dData : data.entrySet()){

            List<StatisticsDTO> stats = dData.getValue();

            List<String> categories = new ArrayList<>();
            Map<String, List<Object>> dataMap = new HashMap<>();

            // Get all the Existing Keys
            int pos = 0; Double position = -0.4;
            for (CountDTO countDTO : stats.get(0).getDataCounts()){

                dataMap.put(countDTO.getKey()+"-"+dData.getKey(), new ArrayList<>());

                colors.put(countDTO.getKey()+"-"+dData.getKey(), chartHelper.colors(pos));
                positions.put(countDTO.getKey()+"-"+dData.getKey(), position);
                pos++;
                position = position + 0.2;
            }


            stats.forEach(StatisticsDTO ->{

                // Populate the chartData category List
                categories.add(StatisticsDTO.getCategory());

                //
                StatisticsDTO.getDataCounts().forEach(CountDTO->{
                    dataMap.get(CountDTO.getKey()+"-"+dData.getKey()).add(CountDTO.getValue());
                });

            });

            xAxis = new XAxis(categories);
            Double opacity = 0.6;
            Double pointPadding = 0.4;
            Integer yAxis = 0;

            if (count == 0){
                yAxisList.add(chartHelper.simpleYAxis(dData.getKey(), false));
            }else {
                opacity = 0.9;
                pointPadding = 0.35;
                yAxis = 1;
                yAxisList.add(chartHelper.simpleYAxis(dData.getKey(), true));
            }

            // Create the Column Charts Data to create a cluster
            for (Map.Entry<String, List<Object>> map : dataMap.entrySet()){
                seriesList.add( chartHelper.columnChart(map.getValue(), map.getKey(), colors.get(map.getKey()), opacity, pointPadding, positions.get(map.getKey()), yAxis) );
            }


            count++;
        }

/*
        String yAxisText1 = "Drugs";
        String yAxisText2 = "Patients";

        YAxis yAxis1 = chartHelper.simpleYAxis(yAxisText1, false);
        yAxis1.setMin(0);
        YAxis yAxis2 = chartHelper.simpleYAxis(yAxisText2, true);*/


        String chartType = SeriesType.COLUMN.get();

        Chart chart = new Chart(chartType);
        Title title = new Title(chartTitle);

        PlotOptions plotOptions = chartHelper.simplePlotOptions();


        ChartData chartData = new ChartData(chart, title, xAxis, seriesList, yAxisList, plotOptions);



        return chartData;
    }





    public Map<String, List<StatisticsDTO>> groupedData(){

        Map<String, List<StatisticsDTO>> data = new HashMap<>();

        data.put("Patients", mockDataTreatmentPatients());
        data.put("Drugs", mockDataTreatmentDrugs());

        return data;
    }


    public List<StatisticsDTO> mockDataTreatmentPatients(){

        // Mock Data Generation Starts

        List<CountDTO> count1 = Arrays.asList(
                new CountDTO("IRCC-CRC",275),
                new CountDTO("JAX",90),
                new CountDTO("PDXNet-HCI-BCM",6)
        );

        List<CountDTO> count2 = Arrays.asList(
                new CountDTO("IRCC-CRC",375),
                new CountDTO("JAX",190),
                new CountDTO("PDXNet-HCI-BCM",106)
        );

        List<CountDTO> count3 = Arrays.asList(
                new CountDTO("IRCC-CRC",475),
                new CountDTO("JAX",290),
                new CountDTO("PDXNet-HCI-BCM",206)
        );

        List<CountDTO> count4 = Arrays.asList(
                new CountDTO("IRCC-CRC",575),
                new CountDTO("JAX",390),
                new CountDTO("PDXNet-HCI-BCM",306)
        );

        List<StatisticsDTO> stats = Arrays.asList(
                new StatisticsDTO("JAN 2019",count1),
                new StatisticsDTO("MAR 2019",count2),
                new StatisticsDTO("JUN 2019",count3),
                new StatisticsDTO("NOV 2019",count4)
        );

        // Mock data Generation Ends

        return stats;
    }



    public List<StatisticsDTO> mockDataTreatmentDrugs(){

        // Mock Data Generation Starts

        List<CountDTO> count1 = Arrays.asList(
                new CountDTO("IRCC-CRC",1),
                new CountDTO("JAX",20),
                new CountDTO("PDXNet-HCI-BCM",5)
        );

        List<CountDTO> count2 = Arrays.asList(
                new CountDTO("IRCC-CRC",101),
                new CountDTO("JAX",120),
                new CountDTO("PDXNet-HCI-BCM",105)
        );

        List<CountDTO> count3 = Arrays.asList(
                new CountDTO("IRCC-CRC",201),
                new CountDTO("JAX",220),
                new CountDTO("PDXNet-HCI-BCM",205)
        );

        List<CountDTO> count4 = Arrays.asList(
                new CountDTO("IRCC-CRC",301),
                new CountDTO("JAX",320),
                new CountDTO("PDXNet-HCI-BCM",305)
        );

        List<StatisticsDTO> stats = Arrays.asList(
                new StatisticsDTO("JAN 2019",count1),
                new StatisticsDTO("MAR 2019",count2),
                new StatisticsDTO("JUN 2019",count3),
                new StatisticsDTO("NOV 2019",count4)
        );

        // Mock data Generation Ends

        return stats;
    }


    public List<StatisticsDTO> mockRepository(){

        // Mock Data Generation Starts

        List<CountDTO> count1 = Arrays.asList(
                new CountDTO("mutation",1000),
                new CountDTO("cytogenetics",500),
                new CountDTO("cna",700)
        );

        List<CountDTO> count2 = Arrays.asList(
                new CountDTO("mutation",3000),
                new CountDTO("cytogenetics",4000),
                new CountDTO("cna",1000)
        );

        List<CountDTO> count3 = Arrays.asList(
                new CountDTO("mutation",5000),
                new CountDTO("cytogenetics",4500),
                new CountDTO("cna",4000)
        );

        List<CountDTO> count4 = Arrays.asList(
                new CountDTO("mutation",8000),
                new CountDTO("cytogenetics",5900),
                new CountDTO("cna",5000)
        );

        List<StatisticsDTO> stats = Arrays.asList(
                new StatisticsDTO("JAN 2019",count1),
                new StatisticsDTO("MAR 2019",count2),
                new StatisticsDTO("JUN 2019",count3),
                new StatisticsDTO("NOV 2019",count4)
        );

        // Mock data Generation Ends

        return stats;
    }




}
