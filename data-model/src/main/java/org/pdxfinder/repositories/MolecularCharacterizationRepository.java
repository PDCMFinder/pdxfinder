package org.pdxfinder.repositories;


import org.pdxfinder.dao.MolecularCharacterization;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

/**
 * Interface to the molecular characterization experiments done
 */
public interface MolecularCharacterizationRepository extends PagingAndSortingRepository<MolecularCharacterization, Long> {

    MolecularCharacterization findByTechnology(@Param("technology") String technology);

    @Query("Match (M:ModelCreation)<-[I:INSTANCE_OF]-(pdxPass:PdxPassage)-[passfrm:PASSAGED_FROM]->(spec:Specimen)-[char:CHARACTERIZED_BY]->(molchar:MolecularCharacterization)-[assoc:ASSOCIATED_WITH]->(mAss:MarkerAssociation)-[aw:MARKER]-(m:Marker)" +
            " where M.sourcePdxId={modelId} " +
            " return  M, I, pdxPass, passfrm, spec, char, molchar, assoc, mAss, aw, m, count(mAss)")
    Page<MolecularCharacterization> findVariationDataBySourcePdxId(@Param("modelId") String modelId, Pageable pageable);


    @Query("Match (M:ModelCreation)<-[I:INSTANCE_OF]-(pdxPass:PdxPassage)-[passfrm:PASSAGED_FROM]->(spec:Specimen)-[char:CHARACTERIZED_BY]->(molchar:MolecularCharacterization)-[assoc:ASSOCIATED_WITH]->(mAss:MarkerAssociation)-[aw:MARKER]-(m:Marker)" +
            " where M.sourcePdxId={modelId} " +
            " return  count(*)")
    int countVariationDataByModelId(@Param("modelId") String modelId);



}
