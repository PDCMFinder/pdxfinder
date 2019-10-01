package org.pdxfinder.preload;

import java.util.*;
import java.util.stream.IntStream;

public class OmicHarmonizer {

    private String omicType = "undefined";
    private ArrayList<ArrayList<String>> omicSheet;


    void determineDataType() {

        int size = omicSheet.get(0).size();

        if (size == 23) omicType = "MUT";
        else if (size == 20) omicType = "CNA";
    }

    protected int getColumnByHeader(String header) {

        ArrayList<String> headers = getHeaders();
        Iterator<String> iterator = headers.iterator();
        boolean foundMatch = false;
        int index = 0;

        for(; (iterator.hasNext() && ! foundMatch);  index++){
            foundMatch = iterator.next().equals(header);

        }

        return index;

    }
    protected ArrayList<String> getHeaders(){
        return omicSheet.get(0);
    }

    String getOmicType() {
        return omicType;
    }

    public ArrayList<ArrayList<String>> getOmicSheet() {
        return omicSheet;
    }

    public void setOmicSheet(ArrayList<ArrayList<String>> omicSheet) {
        this.omicSheet = omicSheet;
    }

}
