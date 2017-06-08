package org.pdxfinder.dao;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.Set;

/**
 * Created by jmason on 06/06/2017.
 */
@NodeEntity
public class Histology {

    @GraphId
    Long id;

    @Relationship(type = "HAS_IMAGE")
    private Set<Image> images;

}
