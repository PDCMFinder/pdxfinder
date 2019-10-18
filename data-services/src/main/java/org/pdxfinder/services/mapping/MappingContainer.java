package org.pdxfinder.services.mapping;

import org.pdxfinder.rdbms.dao.MappingEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/*
 * Created by csaba on 18/06/2018.
 */
public class MappingContainer {

    private int size;

    /**
     * A container holding mapped and unmapped entities
     */
    private TreeMap<String, MappingEntity> mappings;



    public MappingContainer(TreeMap<String, MappingEntity> mappings) {
        this.mappings = mappings;
        size = mappings.size();
    }

    public MappingContainer() {

        mappings = new TreeMap<>();
        size = 0;
    }

    public TreeMap<String, MappingEntity> getMappings() {
        return mappings;
    }

    public void setMappings(TreeMap<String, MappingEntity> mappings) {
        this.mappings = mappings;
    }



    public MappingEntity getEntityById(String id){

        if(mappings.containsKey(id)) return mappings.get(id);

        return null;
    }

    public Long getNextAvailableId(){

        return (long) size +1;
    }


    public void addEntity(MappingEntity me){

        if(!mappings.containsKey(me.getMappingKey())){

            mappings.put(me.getMappingKey(), me);
            size += 1;
        }

    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }


    //method to transform the container something more processable by the frontend
    public Map<String, List<MappingEntity>> getEntityMap(){

        List<MappingEntity> list = mappings.values().stream().collect(Collectors.toList());

        Map<String, List<MappingEntity>> entityMap = new HashMap<>();
        entityMap.put("mappings", list);


        return entityMap;
    }


    //method to transform the container something more processable by the frontend
    public List<MappingEntity> getEntityList(){

        List<MappingEntity> list = mappings.values().stream().collect(Collectors.toList());


        return list;
    }


    @Override
    public String toString() {
        return "[" +
                  mappings +
                ']';
    }
}
