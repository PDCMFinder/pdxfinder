package org.pdxfinder.transrepository;

import org.pdxfinder.transdatamodel.Validation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransValidationRepository extends JpaRepository<Validation, Integer> {

}