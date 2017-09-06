package org.pdxfinder.repositories;

import org.pdxfinder.dao.Specimen;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Created by jmason on 08/06/2017.
 */
public interface SpecimenRepository extends Neo4jRepository<Specimen, Long> {

    Specimen findByExternalId(@Param("externalId") String externalId);

    @Query("MATCH (mod:ModelCreation) where mod.sourcePdxId = {modelId} with mod" +
            "  optional match (mod)-[io:INSTANCE_OF]-(pdxPass:PdxPassage)-[passfrm:PASSAGED_FROM]-(spec:Specimen)-[char:CHARACTERIZED_BY]-(molchar:MolecularCharacterization)-[assoc:ASSOCIATED_WITH]-(mAss:MarkerAssociation)-[aw:MARKER]-(m:Marker) " +
            "  return spec")
    List<Specimen> findVariationDataBySourcePdxId(@Param("modelId") String modelId);



}
