package org.pdxfinder.graph.repositories;

import org.pdxfinder.graph.dao.Treatment;
import org.pdxfinder.graph.queryresults.TreatmentMappingData;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;

import java.util.Collection;

/*
 * Created by csaba on 08/05/2019.
 */
public interface TreatmentRepository extends Neo4jRepository<Treatment, Long> {


    @Query("MATCH (ps:PatientSnapshot)--(ts:TreatmentSummary) RETURN count(ts)")
    int findPatientTreatmentNumber();

    @Query("MATCH (gr:Group)--(p:Patient)--(ps:PatientSnapshot)--(ts:TreatmentSummary) " +
            "WHERE gr.type='Provider' AND gr.abbreviation = {ds} "+
            "RETURN count(ts)")
    int findPatientTreatmentNumberByDS(@Param("ds") String ds);


    @Query("MATCH (mod:ModelCreation)--(ts:TreatmentSummary) RETURN count(ts) ")
    int findModelTreatmentNumber();

    @Query("MATCH (mod:ModelCreation)--(ts:TreatmentSummary) " +
            "WHERE mod.dataSource = {ds} "+
            "RETURN count(ts) ")
    int findModelTreatmentNumberByDS(@Param("ds") String ds);


    @Query("MATCH (ps:PatientSnapshot)--(ts:TreatmentSummary)" +
            "WITH ts  SKIP {from} LIMIT {batch}"+
            "MATCH (ts)-[tpr:TREATMENT_PROTOCOL]-(tp:TreatmentProtocol)-[tcr:TREATMENT_COMPONENT]-(tc:TreatmentComponent)--(tr:Treatment) "+
            "RETURN tr")
    Collection<Treatment> getPatientTreatmentFrom(@Param("from") int from, @Param("batch") int batch);

    @Query("MATCH (gr:Group)--(p:Patient)--(ps:PatientSnapshot)--(ts:TreatmentSummary) " +
            "WHERE gr.type='Provider' AND gr.abbreviation = {ds} "+
            "WITH ts SKIP {from} LIMIT {batch}"+
            "MATCH (ts)-[tpr:TREATMENT_PROTOCOL]-(tp:TreatmentProtocol)-[tcr:TREATMENT_COMPONENT]-(tc:TreatmentComponent)--(tr:Treatment) "+
            "RETURN tr ")
    Collection<Treatment> getPatientTreatmentFromByDS(@Param("from") int from, @Param("batch") int batch, @Param("ds") String ds);

    @Query("MATCH (mod:ModelCreation)--(ts:TreatmentSummary) " +
            "WITH ts SKIP {from} LIMIT {batch} "+
            "MATCH (ts)-[tpr:TREATMENT_PROTOCOL]-(tp:TreatmentProtocol)-[tcr:TREATMENT_COMPONENT]-(tc:TreatmentComponent)--(tr:Treatment) " +
            "RETURN tr")
    Collection<Treatment> getModelTreatmentFrom(@Param("from") int from, @Param("batch") int batch);

    @Query("MATCH (mod:ModelCreation)--(ts:TreatmentSummary) " +
            "WHERE mod.dataSource = {ds} "+
            "WITH ts SKIP {from} LIMIT {batch} "+
            "MATCH (ts)-[tpr:TREATMENT_PROTOCOL]-(tp:TreatmentProtocol)-[tcr:TREATMENT_COMPONENT]-(tc:TreatmentComponent)--(tr:Treatment) " +
            "RETURN tr")
    Collection<Treatment> getModelTreatmentFromByDS(@Param("from") int from, @Param("batch") int batch, @Param("ds") String ds);

    @Query("match (gr:Group)--(p:Patient)--(ps:PatientSnapshot)--(ts:TreatmentSummary)--(tp:TreatmentProtocol)--(tc:TreatmentComponent)--(t:Treatment) " +
            "where gr.type='Provider' " +
            "return COLLECT(distinct gr.abbreviation+ '___' + toLower(t.name)) as abbrAndTreatmentName")
    TreatmentMappingData getUnmappedPatientTreatments();



}
