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
    private String molecularDataString;
    private List<MolecularData> molecularDataList = new ArrayList<>();

    public MarkerAssociation() {
        molecularDataList = new ArrayList<>();
    }

    public List<MolecularData> createMolecularDataListFromString() throws IOException {

        List<MolecularData> molecularData = new ArrayList<>();
        if(molecularDataString == null || molecularDataString.isEmpty()) {
            return molecularData;
        }
        else{

            ObjectMapper mapper = new ObjectMapper();
            molecularData = mapper.readValue(molecularDataString, new TypeReference<List<MolecularData>>(){});

            return molecularData;
        }
    }

    public void createMolecularDataStringFromList(){
        molecularDataString = new Gson().toJson(molecularDataList);
        molecularDataList = null;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMolecularDataString() {
        return molecularDataString;
    }

    public void setMolecularDataString(String molecularDataString) {
        this.molecularDataString = molecularDataString;
    }

    public List<MolecularData> getMolecularDataList() {
        return molecularDataList;
    }

    public void setMolecularDataList(List<MolecularData> molecularDataList) {
        this.molecularDataList = molecularDataList;
    }

    public void addMolecularData(MolecularData md){
        this.molecularDataList.add(md);
    }
}
