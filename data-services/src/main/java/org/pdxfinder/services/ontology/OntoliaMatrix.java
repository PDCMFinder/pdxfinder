package org.pdxfinder.services.ontology;

import org.pdxfinder.graph.dao.OntologyTerm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * Created by csaba on 30/08/2019.
 */
public class OntoliaMatrix {

    String separator;
    List<OntoliaMatrixRow> matrix;
    private boolean matched;

    private Map<String, OntologyTerm> termsByLabel;
    private Map<String, OntologyTerm> termsBySynonym;


    public OntoliaMatrix(String separator, Map<String, OntologyTerm> termsByLabel, Map<String, OntologyTerm> termsBySynonym) {
        this.separator = separator;
        this.matrix = new ArrayList<>();

        if(termsByLabel != null){
            this.termsByLabel = termsByLabel;
        }
        else {
            this.termsByLabel = new HashMap<>();
        }

        if(termsBySynonym != null){
            this.termsBySynonym = termsBySynonym;
        }
        else{
            this.termsBySynonym = new HashMap<>();
        }

        this.matched = false;
    }



    public void addMatrixRow(String [] synonyms){

        OntoliaMatrixRow r = new OntoliaMatrixRow(synonyms);

        this.matrix.add(r);
    }


    /**
     * Returns the best set of ontology terms or null if none
     * @return
     */
    public OntoliaMatrixRow getBestCompleteMatch(){

        if(!matched) matchMatrixRows();

        int bestScore = 0;
        OntoliaMatrixRow perfectMatch = null;

        for(OntoliaMatrixRow omr: matrix){

            if(omr.isCompleteMatch() && omr.getMatchScore() > bestScore){
                perfectMatch = omr;
                bestScore = omr.getMatchScore();
            }
        }

        return perfectMatch;
    }

    /**
     * Returns the best set of ontology terms or null if none
     * @return
     */
    public OntoliaMatrixRow getBestMatch(){

        if(!matched) matchMatrixRows();

        int bestScore = -1;
        OntoliaMatrixRow bestMatch = null;

        for(OntoliaMatrixRow omr: matrix){

            if(omr.getMatchScore() > bestScore){
                bestMatch = omr;
                bestScore = omr.getMatchScore();
            }
        }

        return bestMatch;
    }


    public int getBestMatchScore(){

        int bestScore = 0;

        for(OntoliaMatrixRow omr: matrix){

            if(omr.getMatchScore() > bestScore){
                bestScore = omr.getMatchScore();
            }
        }

        return bestScore;
    }

    /**
     * Links existing ontology terms to the elements in the matrix rows
     */
    private void matchMatrixRows(){


        for(OntoliaMatrixRow row : matrix){

            String[] synonyms = row.getSynonyms();
            boolean completeMatch = true;

            for(int i = 0; i < synonyms.length; i++){

                String label = getCleanLabel(synonyms[i]);

                if(termsByLabel.containsKey(label)){

                    row.getMatchedTerms().add(termsByLabel.get(label));
                    row.getMatchedTermsFoundIn().add("label");
                    row.setMatchScore(row.getMatchScore() +100);
                }
                else if(termsBySynonym.containsKey(label)){
                    row.getMatchedTerms().add(termsBySynonym.get(label));
                    row.getMatchedTermsFoundIn().add("synonym");
                    row.setMatchScore (row.getMatchScore() +10);
                }
                else{

                    row.getMatchedTerms().add(null);
                    row.getMatchedTermsFoundIn().add(null);
                    row.setMatchScore (row.getMatchScore() +1);
                    completeMatch = false;
                }
            }

            row.setCompleteMatch(completeMatch);
        }

        this.matched = true;
    }


    private String getCleanLabel(String label){

        String cleanLabel = label.toLowerCase();
        cleanLabel = cleanLabel.replaceAll("regimen", "");
        cleanLabel = cleanLabel.replaceAll("high dose", "");
        cleanLabel = cleanLabel.replaceAll("low-dose", "");
        cleanLabel = cleanLabel.replaceAll("low dose", "");
        cleanLabel = cleanLabel.replaceAll("dose-dense", "");
        cleanLabel = cleanLabel.replaceAll("high-dose", "");
        cleanLabel = cleanLabel.replaceAll("pulse intense", "");
        cleanLabel = cleanLabel.replaceAll("intravenous", "");
        cleanLabel = cleanLabel.replaceAll("oral", "");
        cleanLabel = cleanLabel.replaceAll("modified", "");
        cleanLabel = cleanLabel.replaceAll("hyperfractionated", "");
        cleanLabel = cleanLabel.replaceAll("infusional", "");

        cleanLabel = cleanLabel.replaceAll("([^\\s]+\\s+cancer)", "");

        return cleanLabel.trim();
    }

}
