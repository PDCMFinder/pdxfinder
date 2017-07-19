package org.pdxfinder.accessionidtatamodel;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by csaba on 03/07/2017.
 */
public class AccessionData {

    private String symbol;
    private String hgncId;
    private String entrezId;
    private String ensemblId;
    private Set<String> synonyms;
    private Set<String> prevSymbols;

    public AccessionData(String symbol, String hgncId, String entrezId, String ensemblId) {
        this.symbol = symbol;
        this.hgncId = hgncId;
        this.entrezId = entrezId;
        this.ensemblId = ensemblId;
        this.synonyms = new HashSet<>();
        this.prevSymbols = new HashSet<>();

    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getHgncId() {
        return hgncId;
    }

    public void setHgncId(String hgncId) {
        this.hgncId = hgncId;
    }

    public String getEntrezId() {
        return entrezId;
    }

    public void setEntrezId(String entrezId) {
        this.entrezId = entrezId;
    }

    public String getEnsemblId() {
        return ensemblId;
    }

    public void setEnsemblId(String ensemblId) {
        this.ensemblId = ensemblId;
    }

    public Set<String> getSynonyms() {
        return synonyms;
    }

    public Set<String> getPrevSymbols() {
        return prevSymbols;
    }

    public void addPrevSymbol(String symbol){
        this.prevSymbols.add(symbol);
    }

    public void addSynonym(String synonym){
        this.synonyms.add(synonym);
    }
}
