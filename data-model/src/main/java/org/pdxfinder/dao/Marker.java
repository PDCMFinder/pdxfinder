package org.pdxfinder.dao;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by jmason on 17/03/2017.
 */
@NodeEntity
public class Marker {

    @GraphId
    Long id;

    String symbol;
    String name;
    String hugoId;
    String ensemblId;
    String entrezId;
    Set<String> prevSymbols;
    Set<String> synonyms;

    public Marker() {
    }

    public Marker(String symbol, String name) {
        this.symbol = symbol;
        this.name = name;

    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHugoId() {
        return hugoId;
    }

    public void setHugoId(String hugoId) {
        this.hugoId = hugoId;
    }

    public String getEnsemblId() {
        return ensemblId;
    }

    public void setEnsemblId(String ensemblId) {
        this.ensemblId = ensemblId;
    }

    public String getEntrezId() {
        return entrezId;
    }

    public void setEntrezId(String entrezId) {
        this.entrezId = entrezId;
    }

    public Set<String> getPrevSymbols() {
        return prevSymbols;
    }

    public Set<String> getSynonyms() {
        return synonyms;
    }

    public void addPrevSymbol(String s){
        if(this.prevSymbols == null){
            this.prevSymbols = new HashSet<>();
        }
        this.prevSymbols.add(s);
    }

    public void addSynonym(String s){
        if(this.synonyms == null){
            this.synonyms = new HashSet<>();
        }
        this.synonyms.add(s);

    }


}
