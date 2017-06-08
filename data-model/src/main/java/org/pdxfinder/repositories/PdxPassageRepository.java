package org.pdxfinder.repositories;

import org.pdxfinder.dao.PdxPassage;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PdxPassageRepository extends Neo4jRepository<PdxPassage, Long> {


    @Query("MATCH (p:PdxPassage),(s:ModelCreation) WHERE (p)-[:PASSAGED_FROM]-(s) and p.passage={passage} and s.sourcePdxId = {modelId} RETURN p")
    PdxPassage findByPassageAndModelId(@Param("passage") String passage, @Param("modelId")String modelId);

}
