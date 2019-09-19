package org.pdxfinder.rdbms.repositories;

import org.pdxfinder.rdbms.dao.Sample;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransSampleRepository extends JpaRepository<Sample, Integer> {

}
