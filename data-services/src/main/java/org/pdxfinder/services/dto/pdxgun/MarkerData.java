package org.pdxfinder.services.dto.pdxgun;

import java.util.Map;

public class MarkerData {

    private String symbol;
    private Map refData;

    public MarkerData() {
        // Empty Constructor
    }

    public MarkerData(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    public MarkerData setSymbol(String symbol) {
        this.symbol = symbol;
        return this;
    }

    public Map getRefData() {
        return refData;
    }

    public MarkerData setRefData(Map refData) {
        this.refData = refData;
        return this;
    }

    public MarkerData build() {
        return this;
    }

}
