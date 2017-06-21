package org.pdxfinder.dao;

import java.util.HashSet;
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
    
    public void addImage(Image image){
        if(images == null){
            images = new HashSet<>();
            images.add(image);
            
        }else{
            images.add(image);
        }
    }
    
    public Set<Image> getImages(){
        return this.images;
    }

}
