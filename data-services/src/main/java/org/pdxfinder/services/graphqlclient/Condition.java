package org.pdxfinder.services.graphqlclient;

import java.util.List;

public class Condition {

    private Operator operator;
    private List<String> value;

    public Operator getOperator() {
        return operator;
    }

    public Condition setOperator(Operator operator) {
        this.operator = operator;
        return this;
    }

    public List<String> getValue() {
        return value;
    }

    public Condition setValue(List<String> value) {
        this.value = value;
        return this;
    }

}
