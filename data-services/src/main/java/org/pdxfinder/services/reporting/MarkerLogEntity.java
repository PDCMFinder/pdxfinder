package org.pdxfinder.services.reporting;

/*
 * Created by csaba on 28/02/2019.
 */
public class MarkerLogEntity extends LogEntity{

    private String usedMarkerSymbol;
    private String suggestedMarkerSymbol;
    private String reasonForChange;

    public MarkerLogEntity(String reporter, String dataSource, String model, String usedMarkerSymbol, String suggestedMarkerSymbol, String reasonForChange) {
        super(reporter, dataSource, model);
        this.usedMarkerSymbol = usedMarkerSymbol;
        this.suggestedMarkerSymbol = suggestedMarkerSymbol;
        this.reasonForChange = reasonForChange;
    }
}
