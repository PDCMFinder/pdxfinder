package org.pdxfinder.services.dto;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by abayomi on 18/09/2017.
 */
public class VariationDataDTO {

    private int draw;
    private int recordsTotal;
    private int recordsFiltered;

    List<List<String>> data = new ArrayList();

    public VariationDataDTO(){

        this.draw = 0;
        this.recordsTotal = 0;
        this.recordsFiltered = 0;

    }


    public int getDraw() {
        return draw;
    }

    public void setDraw(int draw) {
        this.draw = draw;
    }

    public int getRecordsTotal() {
        return recordsTotal;
    }

    public void setRecordsTotal(int recordsTotal) {
        this.recordsTotal = recordsTotal;
    }

    public int getRecordsFiltered() {
        return recordsFiltered;
    }

    public void setRecordsFiltered(int recordsFiltered) {
        this.recordsFiltered = recordsFiltered;
    }

    public List<List<String>> getData() {
        return data;
    }

    public void setData(List<List<String>> data) {
        this.data = data;
    }
}