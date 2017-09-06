package org.pdxfinder.repositories;

import org.pdxfinder.dao.PdxPassage;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PdxPassageRepository extends Neo4jRepository<PdxPassage, Long> {


    @Query("MATCH (s:Sample)--(mod:ModelCreation)--(p:PdxPassage) " +
            "WHERE s.dataSource = {dataSource} " +
            "AND p.passage = {passage} " +
            "AND mod.sourcePdxId = {modelId} RETURN p")
    PdxPassage findByPassageAndModelIdAndDataSource(@Param("passage") int passage, @Param("modelId")String modelId, @Param("dataSource") String dataSource);


}
