package org.pdxfinder.transrepository;

import org.pdxfinder.transdatamodel.Validation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ValidationRepository extends JpaRepository<Validation, Integer> {

}