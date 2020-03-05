package org.pdxfinder.services.reporting;

/*
 * Created by csaba on 28/02/2019.
 */
public class MarkerLogEntity extends LogEntity{

    private String characterizationType;
    private String platform;
    private String usedMarkerSymbol;
    private String suggestedMarkerSymbol;
    private String reasonForChange;


    public MarkerLogEntity(String reporter, String dataSource, String model, String characterizationType, String platform, String usedMarkerSymbol, String suggestedMarkerSymbol, String reasonForChange) {
        super(reporter, dataSource, model);
        this.characterizationType = characterizationType;
        this.platform = platform;
        this.usedMarkerSymbol = usedMarkerSymbol;
        this.suggestedMarkerSymbol = suggestedMarkerSymbol;
        this.reasonForChange = reasonForChange;
    }

    public static MarkerLogEntity logUpdateFromPreviousSymbol(
        String reporter,
        String dataSource,
        String model,
        String characterizationType,
        String platform,
        String usedMarkerSymbol,
        String suggestedMarkerSymbol,
        String reasonForChange
    ) {
        return new MarkerLogEntity(
            reporter,
            dataSource,
            model,
            characterizationType,
            platform,
            usedMarkerSymbol,
            suggestedMarkerSymbol,
            reasonForChange
        );
    }

    public static MarkerLogEntity logNoSingleValidSymbol(
        String reporter,
        String dataSource,
        String model,
        String characterizationType,
        String platform,
        String usedMarkerSymbol,
        String suggestedMarkerSymbol,
        String reasonForChange
    ) {
        return new MarkerLogEntity(
            reporter,
            dataSource,
            model,
            characterizationType,
            platform,
            usedMarkerSymbol,
            suggestedMarkerSymbol,
            reasonForChange
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        MarkerLogEntity that = (MarkerLogEntity) o;

        if (!characterizationType.equals(that.characterizationType)) return false;
        if (!platform.equals(that.platform)) return false;
        if (!usedMarkerSymbol.equals(that.usedMarkerSymbol)) return false;
        if (!suggestedMarkerSymbol.equals(that.suggestedMarkerSymbol)) return false;
        return reasonForChange.equals(that.reasonForChange);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + characterizationType.hashCode();
        result = 31 * result + platform.hashCode();
        result = 31 * result + usedMarkerSymbol.hashCode();
        result = 31 * result + suggestedMarkerSymbol.hashCode();
        result = 31 * result + reasonForChange.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return
            super.getType() +
            ", " + super.getReporter() +
            ", " + super.getDataSource() +
            ", " + super.getModel() +
            ", " + characterizationType +
            ", " + platform +
            ", " + usedMarkerSymbol +
            ", " + suggestedMarkerSymbol +
            ", " + reasonForChange +
            ", " + super.getMessage();
    }

    public String getCharacterizationType() {
        return characterizationType;
    }

    public void setCharacterizationType(String characterizationType) {
        this.characterizationType = characterizationType;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getUsedMarkerSymbol() {
        return usedMarkerSymbol;
    }

    public void setUsedMarkerSymbol(String usedMarkerSymbol) {
        this.usedMarkerSymbol = usedMarkerSymbol;
    }

    public String getSuggestedMarkerSymbol() {
        return suggestedMarkerSymbol;
    }

    public void setSuggestedMarkerSymbol(String suggestedMarkerSymbol) {
        this.suggestedMarkerSymbol = suggestedMarkerSymbol;
    }

    public String getReasonForChange() {
        return reasonForChange;
    }

    public void setReasonForChange(String reasonForChange) {
        this.reasonForChange = reasonForChange;
    }
}
