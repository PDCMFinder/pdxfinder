package org.pdxfinder.repositories;


import org.pdxfinder.dao.MolecularCharacterization;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

/**
 * Interface to the molecular characterization experiments done
 */
public interface MolecularCharacterizationRepository extends PagingAndSortingRepository<MolecularCharacterization, Long> {

    MolecularCharacterization findByTechnology(@Param("technology") String technology);


    @Query("Match (mc:ModelCreation)<--(pdxPass:PdxPassage)-->(spec:Specimen)-->(sample:Sample)<--(molchar:MolecularCharacterization)-->(mAss:MarkerAssociation)--(m:Marker)" +
            " where mc.sourcePdxId={modelId} AND " +
            "( toLower(spec.externalId) CONTAINS toLower({search})" +
            " OR toLower(m.symbol) CONTAINS toLower({search})" +
            " OR toLower(molchar.technology) CONTAINS toLower({search})" +
            " OR any( property in keys(mAss) where toLower(mAss[property]) CONTAINS toLower({search}) ) ) " +
            "return  count(*) ")
    Integer countBySearchParameter(@Param("modelId") String modelId,@Param("search") String search);





}
