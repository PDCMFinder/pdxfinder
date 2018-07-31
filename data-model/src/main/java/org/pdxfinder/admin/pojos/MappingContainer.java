package org.pdxfinder.admin.pojos;

import java.util.TreeMap;

/*
 * Created by csaba on 18/06/2018.
 */
public class MappingContainer {

    private int size;

    /**
     * A container holding mapped and unmapped entities
     */
    private TreeMap<Long, MappingEntity> mappings;



    public MappingContainer(TreeMap<Long, MappingEntity> mappings) {
        this.mappings = new TreeMap<>();
        this.mappings = mappings;
        size = mappings.size();
    }

    public MappingContainer() {

        mappings = new TreeMap<>();
        size = 0;
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


        return (long) size +1;
    }


    public void add(MappingEntity me){

        mappings.put(me.getEntityId(), me);
        size += 1;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
