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


    @Query("match(o:OntologyTerm) return o limit 40")
    Collection<OntologyTerm>  findByOntologyTermLabel(@Param("label") String label);


    @Query("MATCH (oTerm:OntologyTerm) where oTerm.label  =~ trim(toLower({searchParam})) return oTerm limit 40 ")
    Collection<OntologyTerm> findByDiseaseOntologyTerm(@Param("searchParam") String searchParam);

    @Query("MATCH (oTerm:OntologyTerm) where oTerm.label  =~ trim(toLower({searchParam2})) " +
            "AND NOT  oTerm.label  =~ trim(toLower({searchParam})) " +
            "return oTerm limit 40 ")
    Collection<OntologyTerm> findByDiseaseOntologyTerm2(@Param("searchParam2") String searchParam2,
                                                       @Param("searchParam") String searchParam);

}
