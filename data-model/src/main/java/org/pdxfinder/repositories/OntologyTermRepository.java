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



    @Query("MATCH (oTerm:OntologyTerm)<-[SUBCLASS_OF]-(subNodes) where oTerm.label CONTAINS trim(toLower({searchParam})) WITH COLLECT(subNodes) AS subNodes " +
            "UNWIND subNodes AS subN " +
            "MATCH (oTerm:OntologyTerm)<-[SUBCLASS_OF]-(subNodes2) where oTerm.label=subN.label return subN,subNodes2 limit 8")
    Collection<OntologyTerm> findByDiseaseOntologyTerm(@Param("searchParam") String searchParam);









}
