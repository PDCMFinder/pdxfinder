package org.pdxfinder.services.graphqlclient;

import java.util.List;
import java.util.Map;

public class Select {

    private List<Object> columns;
    private String table;
    private Map<String, Condition> conditions;

    public Select(){
        this.table = "";
    }

    private Select(List<Object> columns, String table, Map<String, Condition> conditions) {
        this.columns = columns;
        this.table = table;
        this.conditions = conditions;
    }

    public Select columns(List<Object> columns) {
        this.columns = columns;
        return this;
    }

    public Select table(String table) {
        this.table = table;
        return this;
    }

    public Select conditions(Map<String, Condition> conditions) {
        this.conditions = conditions;
        return this;
    }

    public List<Object> getColumns() {
        return columns;
    }

    public String getTable() {
        return table;
    }

    public Map<String, Condition> getConditions() {
        return conditions;
    }

    public Select build() {
        return new Select(columns, table, conditions);
    }
}
