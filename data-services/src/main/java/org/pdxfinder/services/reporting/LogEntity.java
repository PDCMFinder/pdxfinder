package org.pdxfinder.services.reporting;

/*
 * Created by csaba on 26/02/2019.
 */
public class LogEntity {

    //the java class that reported the issue
    private String reporter;

    private String dataSource;
    private String model;

    //EG: INFO, ERROR, etc
    private String type;

    //Additional message;
    private String message;

    public LogEntity(String reporter, String dataSource, String model) {
        this.reporter = reporter;
        this.dataSource = dataSource;
        this.model = model;
        this.type = "INFO";
        this.message = "";

    }

    @Override
    public String toString() {
        return "{" +
                "type='" + type + '\'' +
                ", reporter='" + reporter + '\'' +
                ", dataSource='" + dataSource + '\'' +
                ", model='" + model + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LogEntity logEntity = (LogEntity) o;

        if (!reporter.equals(logEntity.reporter)) return false;
        if (!dataSource.equals(logEntity.dataSource)) return false;
        return model.equals(logEntity.model);
    }

    @Override
    public int hashCode() {
        int result = reporter.hashCode();
        result = 31 * result + dataSource.hashCode();
        result = 31 * result + model.hashCode();
        return result;
    }

    public String getReporter() {
        return reporter;
    }

    public void setReporter(String reporter) {
        this.reporter = reporter;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
