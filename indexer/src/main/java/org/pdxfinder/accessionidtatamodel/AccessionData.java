package org.pdxfinder.accessionidtatamodel;

/**
 * Created by csaba on 03/07/2017.
 */
public class AccessionData {

    private String symbol;
    private String hgncId;
    private String ensemblId;

    public AccessionData(String symbol, String hgncId, String ensemblId) {
        this.symbol = symbol;
        this.hgncId = hgncId;
        this.ensemblId = ensemblId;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getHgncId() {
        return hgncId;
    }

    public String getEnsemblId() {
        return ensemblId;
    }
}
