package org.pdxfinder.services.dto;

import java.util.List;

/*
 * Created by abayomi on 19/06/2019.
 */
public class StatisticsDTO {

    private String category;
    private List<CountDTO> dataCounts;

    public StatisticsDTO(String category, List<CountDTO> dataCounts) {
        this.category = category;
        this.dataCounts = dataCounts;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<CountDTO> getDataCounts() {
        return dataCounts;
    }

    public void setDataCounts(List<CountDTO> dataCounts) {
        this.dataCounts = dataCounts;
    }
}