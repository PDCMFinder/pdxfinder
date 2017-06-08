package org.pdxfinder.repositories;

import org.pdxfinder.dao.OntologyTerm;
import org.pdxfinder.dao.Sample;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Created by csaba on 07/06/2017.
 */
public interface OntologyTermRepository extends PagingAndSortingRepository<Sample, Long> {

    OntologyTerm findById();

    OntologyTerm findByLabel();

    OntologyTerm findByUrl();

}
