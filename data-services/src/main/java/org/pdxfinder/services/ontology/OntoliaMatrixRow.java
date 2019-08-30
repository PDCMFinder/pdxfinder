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

        matchedTerms = new ArrayList<>();
        matchedTermsFoundIn = new ArrayList<>();
        matchScore = 0;
        completeMatch = false;

    }


    public void matchToTerms(Map<String, OntologyTerm> termsByLabel, Map<String, OntologyTerm> termsBySynonym){


        for(int i = 0; i < synonyms.length; i++){

            String label = getCleanLabel(synonyms[i]);

            if(termsByLabel.containsKey(label)){
                matchedTerms.add(termsByLabel.get(label));
                matchedTermsFoundIn.add("label");
            }
            else if(termsBySynonym.containsKey(label)){
                matchedTerms.add(termsBySynonym.get(label));
                matchedTermsFoundIn.add("synonym");
            }
            else{

                matchedTerms.add(null);
                matchedTermsFoundIn.add(null);
            }



        }
    }



    public void calculateScore(){

        for(int i=0; i < matchedTerms.size(); i++){

            if(matchedTerms.get(i) != null){

                if(matchedTermsFoundIn.get(i).equals("synonym")) matchScore +=1;
                if(matchedTermsFoundIn.get(i).equals("label")) matchScore +=10;
            }
        }

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
