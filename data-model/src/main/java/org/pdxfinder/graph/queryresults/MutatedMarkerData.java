package org.pdxfinder.graph.queryresults;

import org.springframework.data.neo4j.annotation.QueryResult;

import java.util.Objects;

/*
 * Created by csaba on 04/09/2019.
 */
@QueryResult
public class MutatedMarkerData {

    String gene_name;
    int number_of_models;


    public String getGene_name() {
        return gene_name;
    }

    public void setGene_name(String gene_name) {
        this.gene_name = gene_name;
    }

    public int getNumber_of_models() {
        return number_of_models;
    }

    public void setNumber_of_models(int number_of_models) {
        this.number_of_models = number_of_models;
    }

    @Override
    public String toString() {
        return "{" +
                "\"gene_name\" : \""  + gene_name + "\"" +
                ", \"number_of_models\" : " + number_of_models +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MutatedMarkerData that = (MutatedMarkerData) o;
        return number_of_models == that.number_of_models &&
                gene_name.equals(that.gene_name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gene_name, number_of_models);
    }
}
