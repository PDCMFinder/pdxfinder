package org.pdxfinder.admin.pojos;

import java.util.TreeMap;

/*
 * Created by csaba on 18/06/2018.
 */
public class MappingContainer {

    /**
     * A container holding mapped and unmapped entities
     */
    private TreeMap<Long, MappingEntity> mappings;



    public MappingContainer(TreeMap<Long, MappingEntity> mappings) {
        this.mappings = mappings;
    }

    public TreeMap<Long, MappingEntity> getMappings() {
        return mappings;
    }

    public void setMappings(TreeMap<Long, MappingEntity> mappings) {
        this.mappings = mappings;
    }



    public MappingEntity getEntityById(Long id){

        if(mappings.containsKey(id)) return mappings.get(id);

        return null;
    }

    public Long getNextAvailableId(){

        int currentSize = mappings.size();
        currentSize++;

        return (long) currentSize;
    }
}
