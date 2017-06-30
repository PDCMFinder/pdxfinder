package org.pdxfinder.dao;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;

/**
 * Created by jmason on 17/03/2017.
 */
@NodeEntity
public class Marker {

    @GraphId
    Long id;

    String symbol;
    String name;
    String hugoId;
    String ensemblId;

    public Marker() {
    }

    public Marker(String symbol, String name) {
        this.symbol = symbol;
        this.name = name;

    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHugoId() {
        return hugoId;
    }

    public void setHugoId(String hugoId) {
        this.hugoId = hugoId;
    }

    public String getEnsemblId() {
        return ensemblId;
    }

    public void setEnsemblId(String ensemblId) {
        this.ensemblId = ensemblId;
    }
}
