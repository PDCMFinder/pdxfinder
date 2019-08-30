package org.pdxfinder.services.ontology;

import java.util.ArrayList;
import java.util.List;

/*
 * Created by csaba on 30/08/2019.
 */
public class OntoliaMatrix {

    String separator;
    List<OntoliaMatrixRow> matrix;


    public OntoliaMatrix(String separator) {
        this.separator = separator;
        this.matrix = new ArrayList<>();
    }



    public void addMatrixRow(OntoliaMatrixRow omr){

        this.matrix.add(omr);
    }




    public OntoliaMatrixRow getPerfectMatch(){

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


}
