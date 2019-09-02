package org.pdxfinder.services.ontology;

import org.pdxfinder.graph.dao.OntologyTerm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/*
 * Created by csaba on 30/08/2019.
 */
public class OntoliaMatrixRow {

    private String[] synonyms;

    private List<OntologyTerm> matchedTerms;
    private List<String> matchedTermsFoundIn;

    private int matchScore;
    private boolean completeMatch;


    public OntoliaMatrixRow(String[] synonyms) {

        this.synonyms = synonyms;
        matchedTerms = new ArrayList<>();
        matchedTermsFoundIn = new ArrayList<>();
        matchScore = 0;
        completeMatch = false;

    }


    /**
     *
     * [existingOntologyLabel]
     * (existingOntologySynonym)
     * |not found|
     *
     * @return
     */
    public String getRowString(){

        String s = "";
        for(int i = 0; i < synonyms.length; i++){

            if(matchedTerms.get(i) != null){

                if(matchedTermsFoundIn.get(i).equals("label")){
                    s += "["+matchedTerms.get(i).getLabel()+"] ";
                }
                else {
                    s += "("+matchedTerms.get(i).getLabel()+") ";
                }

            }
            else{

                s += "|"+ synonyms[i]+"| ";
            }

        }
        s += "Score: "+matchScore;

        return s;
    }


    public void calculateScore(){

        for(int i=0; i < matchedTerms.size(); i++){

            if(matchedTerms.get(i) != null){

                if(matchedTermsFoundIn.get(i).equals("synonym")) matchScore +=1;
                if(matchedTermsFoundIn.get(i).equals("label")) matchScore +=10;
            }
        }

    }









    public List<OntologyTerm> getMatchedTerms() {
        return matchedTerms;
    }

    public void setMatchedTerms(List<OntologyTerm> matchedTerms) {
        this.matchedTerms = matchedTerms;
    }

    public String[] getSynonyms() {
        return synonyms;
    }

    public void setSynonyms(String[] synonyms) {
        this.synonyms = synonyms;
    }

    public List<String> getMatchedTermsFoundIn() {
        return matchedTermsFoundIn;
    }

    public void setMatchedTermsFoundIn(List<String> matchedTermsFoundIn) {
        this.matchedTermsFoundIn = matchedTermsFoundIn;
    }

    public int getMatchScore() {
        return matchScore;
    }

    public void setMatchScore(int matchScore) {
        this.matchScore = matchScore;
    }

    public boolean isCompleteMatch() {
        return completeMatch;
    }

    public void setCompleteMatch(boolean completeMatch) {
        this.completeMatch = completeMatch;
    }
}
