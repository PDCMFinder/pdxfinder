package org.pdxfinder.services.reporting;

/*
 * Created by csaba on 26/02/2019.
 */
public class LogEntity {

    //the java class that reported the issue
    private String reporter;

    private String dataSource;
    private String model;

    private LogEntityType type;

    private String message;


    public LogEntity(String reporter, String dataSource, String model, LogEntityType type, String message) {
        this.reporter = reporter;
        this.dataSource = dataSource;
        this.model = model;
        this.type = type;
        this.message = message;
    }

    @Override
    public String toString() {
        return "{" +
                "reporter='" + reporter + '\'' +
                ", dataSource='" + dataSource + '\'' +
                ", model='" + model + '\'' +
                ", type=" + type.getName() +
                ", message='" + message + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LogEntity logEntity = (LogEntity) o;

        if (!reporter.equals(logEntity.reporter)) return false;
        if (!dataSource.equals(logEntity.dataSource)) return false;
        if (!model.equals(logEntity.model)) return false;
        if (type != logEntity.type) return false;
        return message.equals(logEntity.message);
    }

    @Override
    public int hashCode() {
        int result = reporter.hashCode();
        result = 31 * result + dataSource.hashCode();
        result = 31 * result + model.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + message.hashCode();
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

    public LogEntityType getType() {
        return type;
    }

    public void setType(LogEntityType type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
