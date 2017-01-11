package org.pdxi.repositories;

import org.pdxi.dao.TumorType;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;

/**
 * Created by jmason on 09/01/2017.
 */
public interface TumorTypeRepository extends GraphRepository<TumorType> {

    @Query("MATCH (t:TumorType) WHERE t.name = {0} RETURN t")
    TumorType findByName(String name);

}
