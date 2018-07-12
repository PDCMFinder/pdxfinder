package org.pdxfinder.transrepository;

import org.pdxfinder.transdatamodel.Treatment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransTreatmentRepository extends JpaRepository<Treatment, Integer> {

}
