package org.pdxfinder.repositories;

import org.pdxfinder.dao.PdxStrain;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;

/**
 * Interface describing operations for adding/finding/deleting PDX strain records
 */
public interface PdxStrainRepository extends Neo4jRepository<PdxStrain, Long> {

    PdxStrain findBySourcePdxId(String sourcePdxId);

    @Query("MATCH (s:Sample)--(pdx:PdxStrain) " +
            "WHERE s.sourceSampleId = {sampleId} " +
            "RETURN pdx")
    PdxStrain findBySampleId(@Param("sampleId") String sampleId);

}
