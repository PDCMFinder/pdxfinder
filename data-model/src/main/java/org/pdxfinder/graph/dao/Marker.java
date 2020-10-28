package org.pdxfinder.graph.dao;

import org.apache.commons.lang3.StringUtils;
import org.neo4j.ogm.annotation.Index;
import org.neo4j.ogm.annotation.NodeEntity;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by jmason on 17/03/2017.
 */
@NodeEntity
public class Marker {

    @Id
    @GeneratedValue
    Long id;

    @Index
    private String hgncSymbol;
    private String hgncName;
    private String hgncId;
    private String ucscGeneId;

    private String ensemblGeneId;
    private String ncbiGeneId;
    private String uniprotId;
    
    @Index
    private Set<String> prevSymbols;
    @Index
    private Set<String> aliasSymbols;



    public Marker() {
    }

    public Marker(String hgncSymbol, String hgncName) {
        this.hgncSymbol = hgncSymbol;
        this.hgncName = hgncName;
        this.prevSymbols = new HashSet<>();
        this.aliasSymbols = new HashSet<>();

    }

    public static Marker createMarker(String symbol, String ensemblId, String hgncId, String ncbiId, String[] synonyms, String[] prevSymbols) {
        Marker m = new Marker();
        m.setHgncSymbol(symbol);
        m.setEnsemblGeneId(ensemblId);
        m.setHgncId(hgncId);
        m.setNcbiGeneId(ncbiId);
        for (String s : synonyms) {
            m.addAliasSymbols(s);
        }
        for (String s : prevSymbols) {
            m.addPrevSymbol(s);
        }
        return m;
    }

    public String getHgncSymbol() {
        return hgncSymbol;
    }

    public void setHgncSymbol(String hgncSymbol) {
        this.hgncSymbol = hgncSymbol;
    }

    public boolean hasHgncSymbol() {
        return StringUtils.isNotEmpty(this.hgncSymbol);
    }

    public String getHgncName() {
        return hgncName;
    }

    public void setHgncName(String hgncName) {
        this.hgncName = hgncName;
    }

    public String getHgncId() {
        return hgncId;
    }

    public void setHgncId(String hgncId) {
        this.hgncId = hgncId;
    }

    public String getUcscGeneId() {
        return ucscGeneId;
    }

    public void setUcscGeneId(String ucscGeneId) {
        this.ucscGeneId = ucscGeneId;
    }

    public String getUniprotId() {
        return uniprotId;
    }

    public void setUniprotId(String uniprotId) {
        this.uniprotId = uniprotId;
    }

    public String getEnsemblGeneId() {
        return ensemblGeneId;
    }

    public void setEnsemblGeneId(String ensemblGeneId) {
        this.ensemblGeneId = ensemblGeneId;
    }

    public String getNcbiGeneId() {
        return ncbiGeneId;
    }

    public void setNcbiGeneId(String ncbiGeneId) {
        this.ncbiGeneId = ncbiGeneId;
    }

    public Set<String> getPrevSymbols() {
        return prevSymbols;
    }

    public Set<String> getAliasSymbols() {
        return aliasSymbols;
    }

    public void setAliasSymbols(Set<String> aliasSymbols) {
        this.aliasSymbols = aliasSymbols;
    }

    public void addPrevSymbol(String s){
        if(this.prevSymbols == null){
            this.prevSymbols = new HashSet<>();
        }
        this.prevSymbols.add(s);
    }

    public void addAliasSymbols(String s){
        if(this.aliasSymbols == null){
            this.aliasSymbols = new HashSet<>();
        }
        this.aliasSymbols.add(s);

    }

    public void setPrevSymbols(Set<String> prevSymbols) {
        this.prevSymbols = prevSymbols;
    }
}
