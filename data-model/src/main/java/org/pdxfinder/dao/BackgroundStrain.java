package org.pdxfinder.dao;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;

/**
 * Represents a background strain
 */
@NodeEntity
public class BackgroundStrain {

    @GraphId
    private Long id;

    private String symbol;
    private String name;
    private String description;
    private String url;

    private BackgroundStrain() {
        // Empty constructor required as of Neo4j API 2.0.5
    }

    public BackgroundStrain(String symbol) {
        this.symbol = symbol;
    }

    public BackgroundStrain(String symbol, String name) {
        this.symbol = symbol;
        this.name = name;
    }

    public BackgroundStrain(String symbol, String name, String description, String url) {
        this.symbol = symbol;
        this.name = name;
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
