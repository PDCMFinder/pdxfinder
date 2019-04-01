package org.pdxfinder.services.dto;

import org.pdxfinder.services.reporting.LogEntity;

/*
 * Created by csaba on 27/02/2019.
 */
public class NodeSuggestionDTO {

    private Object node;

    private LogEntity logEntity;

    public NodeSuggestionDTO() {
    }

    public Object getNode() {
        return node;
    }

    public void setNode(Object node) {
        this.node = node;
    }

    public LogEntity getLogEntity() {
        return logEntity;
    }

    public void setLogEntity(LogEntity logEntity) {
        this.logEntity = logEntity;
    }
}
