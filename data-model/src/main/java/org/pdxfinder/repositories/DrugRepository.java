package org.pdxfinder.repositories;

import org.neo4j.ogm.model.Result;
import org.pdxfinder.dao.Drug;
import org.pdxfinder.dao.ModelCreation;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

/*
 * Created by csaba on 23/05/2018.
 */
@Repository
public interface DrugRepository extends Neo4jRepository<Drug, Long> {

    @Query("MATCH (d:Drug) RETURN DISTINCT d.name")
    List<String> findDistinctDrugNames();


    @Query("MATCH (mod:ModelCreation)--(ts:TreatmentSummary)-[tpr:TREATMENT_PROTOCOL]-(tp)-[tcr:TREATMENT_COMPONENT]-(tc:TreatmentComponent)-[dr:DRUG]-(drug:Drug) " +
            "RETURN drug.name AS drug_name, COUNT(DISTINCT mod.sourcePdxId) as model_count " +
            "ORDER BY model_count DESC")
    Result countModelsByDrug();

    @Query("MATCH (mod:ModelCreation)--(ts:TreatmentSummary)-[tpr:TREATMENT_PROTOCOL]-(tp)-[tcr:TREATMENT_COMPONENT]-(tc:TreatmentComponent)-[dr:DRUG]-(drug:Drug) " +
            "WHERE tc.type = {type} " +
            "RETURN drug.name AS drug_name, COUNT(DISTINCT mod.sourcePdxId) as model_count " +
            "ORDER BY model_count DESC")
    Result countModelsByDrugAndComponentType(@Param("type") String type);


    @Query("MATCH (mod:ModelCreation)-[tsr:SUMMARY_OF_TREATMENT]-(ts:TreatmentSummary)-[tpr:TREATMENT_PROTOCOL]-(tp:TreatmentProtocol)-[tcr:TREATMENT_COMPONENT]-(tc:TreatmentComponent)-[dr:DRUG]-(d:Drug) " +
            "WHERE tc.type = {type} " +
            "RETURN mod, tsr, ts, tpr, tp, tcr, tc, dr, d")
    Set<ModelCreation> getModelsTreatmentsAndDrugs(@Param("type") String type);
}
