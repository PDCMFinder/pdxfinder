package org.pdxfinder.repositories;


import org.pdxfinder.dao.DataProjection;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

/*
 * Created by csaba on 09/03/2018.
 */
@Repository
public interface DataProjectionRepository extends Neo4jRepository<DataProjection, Long> {


    @Query("MATCH (dp:DataProjection) WHERE dp.label = {label} return dp ")
    DataProjection findByLabel(@Param("label") String label);






    //VALIDATION QUERIES

    //find nodes that are not linked to anything
    @Query("MATCH (n) WHERE NOT (n)--() RETURN n")
    Set<Object> findUnlinkedNodes();


    //find patients that have multiple treatment summaries
    @Query("MATCH (p:Patient)--(ps:PatientSnapshot)--(ts1:TreatmentSummary) " +
            "WITH p, ps, ts1 " +
            "MATCH (ps)--(ts2:TreatmentSummary) " +
            "WHERE ts1 <> ts2 " +
            "RETURN p")
    Set<Object> findPatientsWithMultipleTreatmentSummaries();


    @Query("MATCH (pl:Platform) " +
            "WHERE NOT exists(pl.url) " +
            "OR pl.url = \"\" " +
            "RETURN pl")
    Set<Object> findPlatformsWithoutUrl();


}
