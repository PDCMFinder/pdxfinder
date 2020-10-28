package org.pdxfinder.graph.dao;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by jmason on 06/06/2017.
 */
@NodeEntity
public class Histology {

    @Id @GeneratedValue
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
