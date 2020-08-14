package org.pdxfinder.graph.repositories;

import org.pdxfinder.graph.dao.OntologyTerm;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Created by csaba on 07/06/2017.
 */
@Repository
public interface OntologyTermRepository extends PagingAndSortingRepository<OntologyTerm, Long>
{

    OntologyTerm findById();

    @Query("MATCH (o:OntologyTerm) WHERE toLower(o.label) = toLower({label}) AND o.type = {type} return o")
    OntologyTerm findByLabelAndType(@Param("label") String label, @Param("type") String type);

    @Query("MATCH (o:OntologyTerm) WHERE o.type CONTAINS toLower({type}) return o")
    List<OntologyTerm> findByType(@Param("type") String type);

    @Query("MATCH (o:OntologyTerm) WHERE toLower(o.label) = toLower({label}) return o")
    OntologyTerm findByLabel(@Param("label") String label);

    @Query("MATCH (ot:OntologyTerm) WHERE ot.url = {url} RETURN ot")
    OntologyTerm findByUrl(@Param("url")String url);

    //AUTO-SUGGEST: Returns all OntologyTerms that have indirect/direct samples mapped to
    @Query("MATCH (st:OntologyTerm) " +
            "WHERE st.indirectMappedSamplesNumber > 0 OR st.directMappedSamplesNumber > 0 " +
            "RETURN distinct st")
    Collection<OntologyTerm> findAllWithMappings();

    @Query("MATCH (ot:OntologyTerm) RETURN ot")
    Collection<OntologyTerm> findAll();

    @Query("MATCH (o:OntologyTerm) RETURN count(o)")
    int getOntologyTermNumber();

    @Query("MATCH (o:OntologyTerm) WHERE o.type = {type} RETURN count(o)")
    int getOntologyTermNumberByType(@Param("type") String type);

    @Query("match (o:OntologyTerm) WHERE o.type = {type} RETURN o SKIP {from} LIMIT {to}")
    Collection<OntologyTerm> findAllByTypeFromTo(@Param("type") String type, @Param("from") int from, @Param("to") int to);

    @Query("MATCH (parent:OntologyTerm)<-[:SUBCLASS_OF]-(child:OntologyTerm) " +
            "WHERE child.url = {url} " +
            "RETURN parent")
    Collection<OntologyTerm> findAllDirectParents(@Param("url") String url);

    @Query("MATCH (o:OntologyTerm) WHERE o.directMappedSamplesNumber > 0 RETURN o")
    Collection<OntologyTerm> findAllWithNotZeroDirectMappingNumber();

    @Query("MATCH (ot:OntologyTerm) WHERE ot.directMappedSamplesNumber = 0 AND ot.indirectMappedSamplesNumber = 0 DETACH DELETE ot")
    void deleteTermsWithZeroMappings();

}