package org.pdxfinder.repositories;

import org.pdxfinder.dao.OntologyTerm;
import org.pdxfinder.dao.Sample;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by csaba on 07/06/2017.
 */
@Repository
public interface OntologyTermRepository extends PagingAndSortingRepository<OntologyTerm, Long> {

    OntologyTerm findById();

    OntologyTerm findByLabel(String label);

    OntologyTerm findByUrl(String url);

}
