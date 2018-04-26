package org.pdxfinder.dao;


import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;

/**
 * ExternalUrl represents external urls of the data provider
 */
@NodeEntity
public class ExternalUrl {

    @GraphId
    private Long id;
    private String type;
    private String url;

    public ExternalUrl() {
    }

    public ExternalUrl(Type type, String url) {
        this.type = type.getValue();
        this.url = url;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public enum Type {

        SOURCE("source"),
        CONTACT("contact");

        private String value;

        private Type(String val) {
            value = val;
        }

        public String getValue() {
            return value;
        }
    }
}
