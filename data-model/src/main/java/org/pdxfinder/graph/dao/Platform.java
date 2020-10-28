package org.pdxfinder.graph.dao;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Set;

/**
 * Created by jmason on 21/07/2017.
 */
@NodeEntity
public class Platform {

    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private String url;
    private String type;

    @Relationship(type = "GROUP", direction = Relationship.INCOMING)
    private Group group;

    @Relationship(type = "ASSOCIATED_WITH")
    private Set<PlatformAssociation> platformAssociations;

    public Platform() {
    }

    public Platform(Set<PlatformAssociation> platformAssociations) {
        this.platformAssociations = platformAssociations;
    }

    public Set<PlatformAssociation> getPlatformAssociations() {
        return platformAssociations;
    }

    public void setPlatformAssociations(Set<PlatformAssociation> platformAssociations) {
        this.platformAssociations = platformAssociations;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
