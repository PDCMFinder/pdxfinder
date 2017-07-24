package org.pdxfinder.repositories;

import org.pdxfinder.dao.OntologyTerm;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;

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

    //AUTO-SUGGEST: Returns all OntologyTerms with mapped samples and all of their ancestors
    @Query("MATCH (st:OntologyTerm)<-[*]-(ot:OntologyTerm) " +
            "MATCH (ot)-[m:MAPPED_TO]-(s:Sample) " +
            "RETURN ot,st")
    Collection<OntologyTerm> findAllWithMappings();

    // AUTO-SUGGEST: Query terms that contains the parameter
    @Query("MATCH (oTerm:OntologyTerm) where oTerm.label  =~ trim(toLower({param2})) AND NOT  oTerm.label  =~ trim(toLower({param1})) return oTerm as result UNION " +
            "MATCH (oTerm:OntologyTerm)<-[*]-(subnode:OntologyTerm) where oTerm.label = trim(toLower({param})) return subnode as result ")
    Collection<OntologyTerm> findByDiseaseOntologyTerm2(@Param("param2") String param2,@Param("param1") String param1,@Param("param") String param);



    @Query("MATCH (oTerm:OntologyTerm)<-[*]-(subnode:OntologyTerm) where oTerm.label = trim(toLower({diag})) return subnode as result  ")
    Collection<OntologyTerm> findDOTermAll(@Param("diag") String diag);






}