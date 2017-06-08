package org.pdxfinder.repositories;

import org.pdxfinder.dao.ModelCreation;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;

/**
 * Interface describing operations for adding/finding/deleting PDX strain records
 */
public interface ModelCreationRepository extends Neo4jRepository<ModelCreation, Long> {

    ModelCreation findBySourcePdxId(String sourcePdxId);


    ModelCreation findBySampleSourceSampleId(@Param("sampleId") String sampleId);

}
