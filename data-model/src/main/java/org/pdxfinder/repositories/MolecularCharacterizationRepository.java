package org.pdxfinder.repositories;

import org.pdxfinder.dao.MolecularCharacterization;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Interface to the molecular characterization experiments done
 */
public interface MolecularCharacterizationRepository extends PagingAndSortingRepository<MolecularCharacterization, Long> {

    MolecularCharacterization findByTechnology(@Param("technology") String technology);

    @Query("MATCH (mod:ModelCreation) where mod.sourcePdxId = {modelId} with mod" +
            "  optional match (mod)-[io:INSTANCE_OF]-(pdxPass:PdxPassage)-[passfrm:PASSAGED_FROM]-(spec:Specimen)-[char:CHARACTERIZED_BY]-(molchar:MolecularCharacterization)-[assoc:ASSOCIATED_WITH]-(mAss:MarkerAssociation)-[aw:MARKER]-(m:Marker) " +
            "  return mod, io, pdxPass, passfrm, spec, char, molchar, assoc, mAss, aw, m")
    List<MolecularCharacterization> findVariationDataBySourcePdxId(@Param("modelId") String modelId);

}
