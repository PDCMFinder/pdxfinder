package org.pdxfinder.repositories;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import org.pdxfinder.dao.PdxPassage;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface PdxPassageRepository extends Neo4jRepository<PdxPassage, Long> {

    
    @Query("MATCH (p:PdxPassage),(s:PdxStrain) WHERE (p)-[:PASSAGED_FROM]-(s) and p.passage={passage} and s.sourcePdxId = {modelId} RETURN p")
    PdxPassage findByPassageAndModelId(@Param("passage") String passage, @Param("modelId")String modelId);

}
