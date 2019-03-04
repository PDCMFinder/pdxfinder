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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        MarkerLogEntity that = (MarkerLogEntity) o;

        if (!usedMarkerSymbol.equals(that.usedMarkerSymbol)) return false;
        if (!suggestedMarkerSymbol.equals(that.suggestedMarkerSymbol)) return false;
        return reasonForChange.equals(that.reasonForChange);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + usedMarkerSymbol.hashCode();
        result = 31 * result + suggestedMarkerSymbol.hashCode();
        result = 31 * result + reasonForChange.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "MarkerLogEntity{" +
                "type='" + super.getType() + '\'' +
                ", reporter='" + super.getReporter() + '\'' +
                ", dataSource='" + super.getDataSource() + '\'' +
                ", model='" + super.getModel() + '\'' +
                "usedMarkerSymbol='" + usedMarkerSymbol + '\'' +
                ", suggestedMarkerSymbol='" + suggestedMarkerSymbol + '\'' +
                ", reasonForChange='" + reasonForChange + '\'' +
                ", message='" + super.getMessage() + '\'' +
                '}';
    }
}
