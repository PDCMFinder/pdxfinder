package org.pdxfinder.dao;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;

import java.util.List;

/*
 * Created by csaba on 23/10/2017.
 */
@NodeEntity
public class TreatmentProtocol {

    @GraphId
    private Long id;

    private List<String> regime;

    public TreatmentProtocol() {
    }

    public TreatmentProtocol(List<String> regime) {
        this.regime = regime;
    }

    public List<String> getRegime() {
        return regime;
    }

    public void setRegime(List<String> regime) {
        this.regime = regime;
    }
}
