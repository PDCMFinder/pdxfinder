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


    public Object clusteredChart(List<StatisticsDTO> stats){

        List<String> categories = new ArrayList<>();
        Map<String, List<Object>> dataMap = new HashMap<>();
        List<Object> categorySum = new ArrayList<>();

        // Get all the Existing Keys
        stats.get(0).getDataCounts().forEach(CountDTO->{
            dataMap.put(CountDTO.getKey(), new ArrayList<>());
        });


        stats.forEach(StatisticsDTO ->{

            // Populate the chart category List
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


        // Create a Spline Chart of Total Dataset counts
        seriesList.add( chartHelper.splineChart(categorySum, "Aggregate") );


        // Get Most Recent Dataset from the Clustered Data to create Pie-Chart.
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

        Axis xAxis = new Axis(categories);

        Labels labels = chartHelper.simpleLabel(lablelString , labelLeft, labelTop, labelColor);

        Chart chart = new Chart(title,xAxis,labels,seriesList);


        return chart;

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
