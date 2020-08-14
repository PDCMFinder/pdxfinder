package org.pdxfinder.graph.repositories;

import org.pdxfinder.graph.dao.MarkerAssociation;
import org.pdxfinder.graph.dao.MolecularCharacterization;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

/**
 * Created by csaba on 25/04/2017.
 */
@Repository
public interface MarkerAssociationRepository extends PagingAndSortingRepository<MarkerAssociation, Long>{

    @Query("MATCH (mc:MolecularCharacterization)--(ma:MarkerAssociation) WHERE id(mc) = {mc} RETURN ma")
    Set<MarkerAssociation> findMutationByMolChar(@Param("mc") MolecularCharacterization mc);

    @Query("MATCH (mc:MolecularCharacterization)--(ma:MarkerAssociation) WHERE id(mc) = {mc} RETURN ma")
    Set<MarkerAssociation> findByMolChar(@Param("mc") MolecularCharacterization mc);
}
