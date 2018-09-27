package org.pdxfinder.repositories;

import org.pdxfinder.dao.Response;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/*
 * Created by csaba on 20/10/2017.
 */
@Repository
public interface ResponseRepository extends PagingAndSortingRepository<Response, Long>{

    @Query("MATCH (r:Response) RETURN DISTINCT r.description")
    List<String> findAllResponses();

    @Query("MATCH (m:ModelCreation)--(ts:TreatmentSummary)--(tp:TreatmentProtocol)--(r:Response) RETURN DISTINCT r.description")
    List<String> findAllSpecimenDrugResponses();

}
