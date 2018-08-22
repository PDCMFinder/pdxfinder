package org.pdxfinder.repositories;

import org.pdxfinder.dao.EngraftmentMaterial;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

/*
 * Created by csaba on 26/04/2018.
 */
@Repository
public interface EngraftmentMaterialRepository extends PagingAndSortingRepository<EngraftmentMaterial, Long>{



    @Query("MATCH (t:EngraftmentMaterial) WHERE t.name = {0} RETURN t")
    EngraftmentMaterial findByName(String name);

}
