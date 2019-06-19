package org.pdxfinder.services.dto;

/*
 * Created by abayomi on 19/06/2019.
 */
public class StatisticsDTO {

    private String category;
    private CountDTO countDTO;

    public StatisticsDTO(String category, CountDTO countDTO) {
        this.category = category;
        this.countDTO = countDTO;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public CountDTO getCountDTO() {
        return countDTO;
    }

    public void setCountDTO(CountDTO countDTO) {
        this.countDTO = countDTO;
    }
}