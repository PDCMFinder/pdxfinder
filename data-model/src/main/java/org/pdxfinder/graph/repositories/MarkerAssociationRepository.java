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

    //TODO: Verify cypher
    @Query("MATCH (n:MarkerAssociation)-[]-(m:Marker) where n.description = {type} and m.hgncSymbol={symbol} return n")
    MarkerAssociation findByTypeAndMarkerSymbol(@Param("type") String type, @Param("symbol") String symbol);

    @Query("MATCH (n:MarkerAssociation)-[]-(m:Marker) where n.description = {type} and m.name={name} return n")
    MarkerAssociation findByTypeAndMarkerName(@Param("type") String type, @Param("name") String name);

    @Query("MATCH (mc:MolecularCharacterization)--(ma:MarkerAssociation) WHERE id(mc) = {mc} RETURN ma")
    Set<MarkerAssociation> findMutationByMolChar(@Param("mc") MolecularCharacterization mc);

    @Query("MATCH (mc:MolecularCharacterization)--(ma:MarkerAssociation) WHERE id(mc) = {mc} RETURN ma")
    Set<MarkerAssociation> findByMolChar(@Param("mc") MolecularCharacterization mc);

    @Query("MATCH (mc:MolecularCharacterization)-[awr:ASSOCIATED_WITH]-(ma:MarkerAssociation) " +
            "WHERE ID(mc) = {id} " +
            "RETURN count(ma)")
    int getMarkerAssociationCountByMolCharId(@Param("id") Long id);

    @Query("MATCH (mc:MolecularCharacterization)-[awr:ASSOCIATED_WITH]-(ma:MarkerAssociation)-[mr:MARKER]-(m:Marker) " +
            "WHERE ID(mc) = {id} " +
            "RETURN ma, mr, m SKIP {from} LIMIT {to}")
    List<MarkerAssociation> findAssociationsByMolCharIdFromTo(@Param("id") Long id, @Param("from") int from, @Param("to") int to);
}
