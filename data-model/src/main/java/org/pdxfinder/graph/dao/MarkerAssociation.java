package org.pdxfinder.graph.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.lang3.StringUtils;
import org.neo4j.ogm.annotation.NodeEntity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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
    private int dataPoints = 0;

    private List<MolecularData> molecularDataList;

    public MarkerAssociation() {
        molecularDataList = new ArrayList<>();
    }

    public List<MolecularData> decodeMolecularData() throws IOException {
        if (StringUtils.isEmpty(molecularDataString))
            return Collections.emptyList();
        else return new ObjectMapper().readValue(
                molecularDataString,
                new TypeReference<List<MolecularData>>(){});
    }

    public void encodeMolecularData(){
        Gson gson = new Gson();
        molecularDataString = gson.toJson(molecularDataList);
        dataPoints = molecularDataList.size();
        molecularDataList = Collections.emptyList();
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

    public int getDataPoints() {
        return dataPoints;
    }

    public void setDataPoints(int dataPoints) {
        this.dataPoints = dataPoints;
    }
}
