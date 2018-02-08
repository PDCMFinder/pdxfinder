package org.pdxfinder.dao;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;

/**
 * Represents a background strain
 */
@NodeEntity
public class HostStrain {

    @GraphId
    private Long id;

    /**
     * name: NOD scid gamma (appears on the filter facet, so no special chars pls)
     * symbol: NOD.Cg-PrkdcscidIl2rgtm1Wjl/SzJ
     * description: Sentences describing the mouse strain
     * url: www.jax.org/strain/005557 (commercial url to buy a certain mouse)
     *
     * The name
     * */
    private String name;
    private String symbol;
    private String description;
    private String url;


    private HostStrain() {
        // Empty constructor required as of Neo4j API 2.0.5
    }

    public HostStrain(String symbol) {
        this.symbol = symbol;
    }

    public HostStrain(String symbol, String name) {
        this.symbol = symbol;
        this.name = name;
    }

    public HostStrain(String name, String symbol, String description, String url) {
        this.name = name;
        this.symbol = symbol;

        this.description = description;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }


}
