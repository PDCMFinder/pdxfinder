package org.pdxfinder.ontologymapping;

/*
 * Created by csaba on 17/11/2017.
 */
public class MissingMapping {

    private String dataSource;
    private String diagnosis;
    private String originTissue;
    private String tumorType;


    public MissingMapping(String dataSource, String diagnosis, String originTissue, String tumorType) {
        this.dataSource = dataSource;
        this.diagnosis = diagnosis;
        this.originTissue = originTissue;
        this.tumorType = tumorType;
    }

    public String getDataSource() {
        return dataSource;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public String getOriginTissue() {
        return originTissue;
    }

    public String getTumorType() {
        return tumorType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MissingMapping that = (MissingMapping) o;

        if (!dataSource.equals(that.dataSource)) return false;
        if (!diagnosis.equals(that.diagnosis)) return false;
        if (originTissue != null ? !originTissue.equals(that.originTissue) : that.originTissue != null) return false;
        return tumorType != null ? tumorType.equals(that.tumorType) : that.tumorType == null;
    }

    @Override
    public int hashCode() {
        int result = dataSource.hashCode();
        result = 31 * result + diagnosis.hashCode();
        result = 31 * result + (originTissue != null ? originTissue.hashCode() : 0);
        result = 31 * result + (tumorType != null ? tumorType.hashCode() : 0);
        return result;
    }
}
