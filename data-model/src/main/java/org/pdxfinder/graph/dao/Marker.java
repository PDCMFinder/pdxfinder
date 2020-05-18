package org.pdxfinder.graph.dao;

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
    String hgncSymbol;              // This is the approved Symbol in HGNC - KRAS
    String hgncName;                // This is the approved name in HGNC KRAS proto-oncogene, GTPase
    String hgncId;                  // This is the hugo unique ID - e.g HGNC:6407
    String ucscGeneId;

    String ensemblGeneId;
    String ncbiGeneId;
    String uniprotId;
    
    @Index
    Set<String> prevSymbols;
    @Index
    Set<String> aliasSymbols;       // This was formerly called synonymns



    public Marker() {
    }

    public Marker(String hgncSymbol, String hgncName) {
        this.hgncSymbol = hgncSymbol;
        this.hgncName = hgncName;

    }

    public String getHgncSymbol() {
        return hgncSymbol;
    }

    public void setHgncSymbol(String hgncSymbol) {
        this.hgncSymbol = hgncSymbol;
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
