package org.pdxfinder.rdbms.dao;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Entity
public class Statistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String statType;
    private Date time;

    @JsonProperty("statistics")
    @OneToMany(mappedBy = "statistics", cascade = {CascadeType.PERSIST})
    private List<StatisticsData> statisticsDataList;


    public Statistics() {
    }

    public Statistics(String statType, Date time, List<StatisticsData> statisticsDataList) {
        this.statType = statType;
        this.time = time;
        this.statisticsDataList = statisticsDataList;
    }


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getStatType() {
        return statType;
    }

    public void setStatType(String statType) {
        this.statType = statType;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public List<StatisticsData> findStatisticDataList() {
        return statisticsDataList;
    }

    public List<Map> getStatisticsDataList() {

        List<Map> dataPoints = new ArrayList<>();
        statisticsDataList.forEach(statisticsData -> {
            dataPoints.add(statisticsData.getStatisticsDataPoint());
        });

        return dataPoints;
    }


    public void setStatisticsDataList(List<StatisticsData> statisticsDataList) {
        this.statisticsDataList = statisticsDataList;
    }
}
