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



        @Query("MATCH (oTerm:OntologyTerm)<-[SUBCLASS_OF]-(subNodes) where oTerm.label = trim(toLower({diag})) return subNodes ")
        Collection<OntologyTerm> findDOTermDepthOne(@Param("diag") String diag);



        @Query("MATCH (oTerm:OntologyTerm)<-[SUBCLASS_OF]-(subNodes) where oTerm.label = trim(toLower({diag})) WITH COLLECT(subNodes) AS subNodesL " +
                "UNWIND subNodesL AS subN " +
                "MATCH (oTerm:OntologyTerm)<-[SUBCLASS_OF]-(subNodes2) where oTerm.label = subN.label return subNodes2 ")
        Collection<OntologyTerm> findDOTermDepthTwo(@Param("diag") String diag);






    @Query("MATCH (oTerm:OntologyTerm)<-[SUBCLASS_OF]-(subNodes) where oTerm.label = trim(toLower('cancer')) WITH COLLECT(subNodes) AS subNodesL " +
            "UNWIND subNodesL AS subN " +
            "MATCH (oTerm:OntologyTerm)<-[SUBCLASS_OF]-(subNodes2) where oTerm.label = subN.label return subNodes2 ")
    Collection<OntologyTerm> findDiseaseOntology(@Param("diagnosis") String diagnosis);







}













