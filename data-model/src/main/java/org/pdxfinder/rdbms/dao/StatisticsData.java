package org.pdxfinder.rdbms.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.Map;

@Entity
public class StatisticsData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Integer id;

    @ElementCollection
    @CollectionTable(name="statistics_data_point", joinColumns = @JoinColumn(name = "statistics_data_id"))
    @Column(name="statistics_data_point", columnDefinition="Text")
    private Map<String, String> statisticsDataPoint;

    @ManyToOne
    @JoinColumn(name = "statistics_id")
    private Statistics statistics;

    public StatisticsData() {
    }

    public StatisticsData(Map<String, String> statisticsDataPoint) {
        this.statisticsDataPoint = statisticsDataPoint;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Map<String, String> getStatisticsDataPoint() {
        return statisticsDataPoint;
    }

    public void setStatisticsDataPoint(Map<String, String> statisticsDataPoint) {
        this.statisticsDataPoint = statisticsDataPoint;
    }

    public void setStatistics(Statistics statistics) {
        this.statistics = statistics;
    }

}
