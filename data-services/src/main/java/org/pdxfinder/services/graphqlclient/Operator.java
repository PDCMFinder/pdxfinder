package org.pdxfinder.services.graphqlclient;

public enum Operator {

    EQUAL("_eq:"),
    IN("_in:");

    private String value;
    private Operator(String val) {
        value = val;
    }
    public String get() {
        return value;
    }
}
