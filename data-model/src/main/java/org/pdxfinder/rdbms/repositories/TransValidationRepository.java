package org.pdxfinder.rdbms.repositories;

import org.pdxfinder.rdbms.dao.Validation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransValidationRepository extends JpaRepository<Validation, Integer> {

}