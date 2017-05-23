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


    PdxStrain findBySampleSourceSampleId(@Param("sampleId") String sampleId);

}
