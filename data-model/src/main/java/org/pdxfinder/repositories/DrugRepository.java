package org.pdxfinder.repositories;

import org.neo4j.ogm.model.Result;
import org.pdxfinder.dao.Drug;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/*
 * Created by csaba on 23/05/2018.
 */
@Repository
public interface DrugRepository extends Neo4jRepository<Drug, Long> {

    @Query("MATCH (d:Drug) RETURN DISTINCT d.name")
    List<String> findDistinctDrugNames();


    /*@Query("MATCH(p:Person{lastName:{0}}) -[r]-(e) RETURN distinct labels(e) as type ,count(e) as count")
    Result resultExample(String name);*/


    @Query("MATCH (mod:ModelCreation)--(ts:TreatmentSummary)-[tpr:TREATMENT_PROTOCOL]-(tp)-[tcr:TREATMENT_COMPONENT]-(tc:TreatmentComponent)-[dr:DRUG]-(drug:Drug) " +
            "RETURN drug.name AS drug_name, COUNT(DISTINCT mod.sourcePdxId) as model_count " +
            "ORDER BY model_count DESC")
    Result countModelsByDrug();
}
