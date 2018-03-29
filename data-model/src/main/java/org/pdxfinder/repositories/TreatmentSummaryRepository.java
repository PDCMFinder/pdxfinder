package org.pdxfinder.repositories;

import org.pdxfinder.dao.TreatmentSummary;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for managing creating, finding, and deleting tissues
 */
@Repository
public interface TreatmentSummaryRepository extends Neo4jRepository<TreatmentSummary, Long> {

    @Query("MATCH (mod:ModelCreation)--(ts:TreatmentSummary) WHERE mod.dataSource = {dataSource} AND mod.sourcePdxId = {modelId} " +
            "WITH ts " +
            "MATCH (ts)-[tpr:TREATMENT_PROTOCOL]-(tp:TreatmentProtocol)-[rr:RESPONSE]-(r:Response) " +
            "RETURN ts, tpr, tp, rr, r")
    TreatmentSummary findByDataSourceAndModelId(@Param("dataSource") String dataSource, @Param("modelId") String modelId);


    @Query("MATCH (mod:ModelCreation)--(ts:TreatmentSummary) WHERE toLower(mod.dataSource) = toLower({dataSource}) RETURN count(mod)")
    int findStudyNumberByDataSource(@Param("dataSource") String dataSource);


    @Query("MATCH (mod:ModelCreation)--(ts:TreatmentSummary) WHERE toLower(mod.dataSource) = toLower({dataSource}) AND EXISTS(ts.url) RETURN ts.url LIMIT 1")
    String findPlatformUrlByDataSource(@Param("dataSource") String dataSource);
}
