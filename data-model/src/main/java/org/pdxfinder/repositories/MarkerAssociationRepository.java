package org.pdxfinder.repositories;

import org.neo4j.ogm.cypher.query.PagingAndSorting;
import org.pdxfinder.dao.Marker;
import org.pdxfinder.dao.MarkerAssociation;
import org.pdxfinder.dao.MolecularCharacterization;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

/**
 * Created by csaba on 25/04/2017.
 */
@Repository
public interface MarkerAssociationRepository extends PagingAndSortingRepository<MarkerAssociation, Long>{

    //TODO: Verify cypher
    @Query("MATCH (n:MarkerAssociation)-[]-(m:Marker) where n.description = {type} and m.symbol={symbol} return n")
    MarkerAssociation findByTypeAndMarkerSymbol(@Param("type") String type, @Param("symbol") String symbol);
    
    @Query("MATCH (n:MarkerAssociation)-[]-(m:Marker) where n.description = {type} and m.name={name} return n")
    MarkerAssociation findByTypeAndMarkerName(@Param("type") String type, @Param("name") String name);

    @Query("MATCH (mc:MolecularCharacterization)--(ma:MarkerAssociation)-[mr:MARKER]-(m:Marker) WHERE id(mc) = {mc} AND exists(ma.aminoAcidChange) RETURN ma, mr, m")
    Set<MarkerAssociation> findByMolChar(@Param("mc") MolecularCharacterization mc);
}
