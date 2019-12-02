package org.pdxfinder.graph.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.fasterxml.jackson.core.type.TypeReference;
import org.neo4j.ogm.annotation.NodeEntity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by csaba on 25/04/2017.
 */
@NodeEntity
public class MarkerAssociation {

    @Id
    @GeneratedValue
    private Long id;

    private String data;

    private List<MolecularData> molecularData = new ArrayList<>();


    public MarkerAssociation() {
    }

    public List<MolecularData> getMolecularDataFromDataString() throws IOException {

        List<MolecularData> molecularData = new ArrayList<>();

        if(data == null || data.isEmpty()) {
            return molecularData;
        }
        else{

            ObjectMapper mapper = new ObjectMapper();
            molecularData = mapper.readValue(data, new TypeReference<List<MolecularData>>(){});

            return molecularData;
        }
    }

    public void setDataFromList(List<MolecularData> listData){

        data = new Gson().toJson(listData);
    }

    public void setDataFromInternalList(){

        data = new Gson().toJson(molecularData);
        molecularData = null;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public List<MolecularData> getMolecularData() {
        return molecularData;
    }

    public void setMolecularData(List<MolecularData> molecularData) {
        this.molecularData = molecularData;
    }

    public void addMolecularData(MolecularData md){
        this.molecularData.add(md);
    }
}
