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
            "WHERE st.indirectMappedSamplesNumber > 0 " +
            "RETURN st")
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

}