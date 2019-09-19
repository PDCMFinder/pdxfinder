package org.pdxfinder.rdbms.repositories;

import org.pdxfinder.rdbms.dao.Treatment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransTreatmentRepository extends JpaRepository<Treatment, Integer> {

}
