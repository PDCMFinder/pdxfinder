package org.pdxfinder.repositories;


import org.pdxfinder.dao.MolecularCharacterization;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Interface to the molecular characterization experiments done
 */
public interface MolecularCharacterizationRepository extends PagingAndSortingRepository<MolecularCharacterization, Long> {

    MolecularCharacterization findByTechnology(@Param("technology") String technology);

    @Query("Match (M:ModelCreation)<-[I:INSTANCE_OF]-(pdxPass:PdxPassage)-[passfrm:PASSAGED_FROM]->(spec:Specimen)-[char:CHARACTERIZED_BY]->(molchar:MolecularCharacterization)-[assoc:ASSOCIATED_WITH]->(mAss:MarkerAssociation)-[aw:MARKER]-(m:Marker)" +
            " where M.sourcePdxId={modelId} " +
            " return  M, I, pdxPass, passfrm, spec, char, molchar, assoc, mAss, aw, m")
    Page<MolecularCharacterization> findVariationDataBySourcePdxId(@Param("modelId") String modelId, Pageable pageable);


    @Query("Match (mc:ModelCreation)<-[I:INSTANCE_OF]-(pdxPass:PdxPassage)-[passfrm:PASSAGED_FROM]->(spec:Specimen)-[char:CHARACTERIZED_BY]->(molchar:MolecularCharacterization)-[assoc:ASSOCIATED_WITH]->(mAss:MarkerAssociation)-[aw:MARKER]-(m:Marker)" +
            " where mc.sourcePdxId={modelId} AND " +
            "( toLower(spec.externalId) CONTAINS toLower({search})" +
            " OR toLower(m.symbol) CONTAINS toLower({search})" +
            " OR toLower(molchar.technology) CONTAINS toLower({search})" +
            " OR any( property in keys(mAss) where toLower(mAss[property]) CONTAINS toLower({search}) ) ) " +
            "return  count(*) ")
    Integer countBySearchParameter(@Param("modelId") String modelId,@Param("search") String search);



    @Query("Match (mc:ModelCreation)<-[I:INSTANCE_OF]-(pdxPass:PdxPassage)-[passfrm:PASSAGED_FROM]->(spec:Specimen)-[char:CHARACTERIZED_BY]->(molchar:MolecularCharacterization)-[assoc:ASSOCIATED_WITH]->(mAss:MarkerAssociation)-[aw:MARKER]-(m:Marker)" +
            " where mc.sourcePdxId={modelId} AND " +
            "( toLower(spec.externalId) CONTAINS toLower({search})" +
            " OR toLower(m.symbol) CONTAINS toLower({search})" +
            " OR toLower(molchar.technology) CONTAINS toLower({search})" +
            " OR any( property in keys(mAss) where toLower(mAss[property]) CONTAINS toLower({search}) ) ) " +
            " return  mc, I, pdxPass, passfrm, spec, char, molchar, assoc, mAss, aw, m order by mAss.readDepth asc SKIP {skip} LIMIT {lim} ")
    List<MolecularCharacterization> findBySearchParameter(@Param("modelId") String modelId, @Param("search") String search,
                                                          @Param("skip") int skip, @Param("lim") int lim);


}
