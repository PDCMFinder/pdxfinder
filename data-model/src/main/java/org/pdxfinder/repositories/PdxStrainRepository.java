package org.pdxfinder.repositories;

import org.pdxfinder.dao.PdxStrain;
import org.springframework.data.neo4j.repository.Neo4jRepository;

/**
 * Interface describing operations for adding/finding/deleting PDX strain records
 */
public interface PdxStrainRepository extends Neo4jRepository<PdxStrain, Long> {

    PdxStrain findBySourcePdxId(String sourcePdxId);

}
