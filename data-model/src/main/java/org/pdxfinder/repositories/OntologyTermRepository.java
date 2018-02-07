package org.pdxfinder.repositories;

import org.pdxfinder.dao.OntologyTerm;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Set;

/**
 * Created by csaba on 07/06/2017.
 */
@Repository
public interface OntologyTermRepository extends PagingAndSortingRepository<OntologyTerm, Long>
{

    OntologyTerm findById();

    @Query("MATCH (o:OntologyTerm) WHERE toLower(o.label) = toLower({label}) return o")
    OntologyTerm findByLabel(@Param("label") String label);

    OntologyTerm findByUrl(String url);

    //AUTO-SUGGEST: Returns all OntologyTerms that have indirect/direct samples mapped to
    @Query("MATCH (st:OntologyTerm) " +
            "WHERE st.indirectMappedSamplesNumber > 0 OR st.directMappedSamplesNumber > 0 " +
            "RETURN distinct st")
    Collection<OntologyTerm> findAllWithMappings();

    @Query("MATCH (ot:OntologyTerm) RETURN ot")
    Collection<OntologyTerm> findAll();

    @Query("MATCH (st:OntologyTerm)<-[*]-(term:OntologyTerm) " +
            "WHERE st.label = {label} " +
            "RETURN sum(term.indirectMappedSamplesNumber)")
    int getIndirectMappingNumber(@Param("label") String label);

    @Query("MATCH (st:OntologyTerm)<-[*]-(term:OntologyTerm) " +
            "WHERE st.label = {label} " +
            "RETURN term, st")
    Set<OntologyTerm> getDistinctSubTreeNodes(@Param("label") String label);

    @Query("MATCH (o:OntologyTerm) RETURN count(o)")
    int getOntologyTermNumber();

    @Query("match (o:OntologyTerm) RETURN o SKIP {from} LIMIT {to}")
    Collection<OntologyTerm> findAllFromTo(@Param("from") int from, @Param("to") int to);

    @Query("MATCH (parent:OntologyTerm)<-[:SUBCLASS_OF]-(child:OntologyTerm) " +
            "WHERE child.url = {url} " +
            "RETURN parent")
    Collection<OntologyTerm> findAllDirectParents(@Param("url") String url);


    @Query("MATCH (o:OntologyTerm) WHERE o.directMappedSamplesNumber > 0 RETURN o")
    Collection<OntologyTerm> findAllWithNotZeroDirectMappingNumber();

    @Query("MATCH (ot:OntologyTerm) WHERE ot.directMappedSamplesNumber = 0 AND ot.indirectMappedSamplesNumber = 0 DETACH DELETE ot")
    void deleteTermsWithZeroMappings();

}