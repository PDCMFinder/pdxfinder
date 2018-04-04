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

    List<String[]> data = new ArrayList();

    List<String[]> moreData = new ArrayList();

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

    public List<String[]> getData() {
        return data;
    }

    public void setData(List<String[]> data) {
        this.data = data;
    }

    public void setMoreData(List<String[]> moreData) {
        this.moreData = moreData;
    }

    public List<String[]> moreData() {

        List<String[]> adjustedData = new LinkedList<>();

        for (String[] dataArr: data){

            String[] dataArr2 = new String[15];

            dataArr2[0] = dataArr[0];
            dataArr2[1] = dataArr[12];
            dataArr2[2] = "";
            dataArr2[3] = "";

            for (int i=1; i<12; i++){
                dataArr2[i+3] = dataArr[i];
            }

            adjustedData.add(dataArr2);
        }
        return adjustedData;
    }
}