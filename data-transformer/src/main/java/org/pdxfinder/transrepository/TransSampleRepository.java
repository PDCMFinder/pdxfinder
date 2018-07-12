package org.pdxfinder.transrepository;

import org.pdxfinder.transdatamodel.Sample;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransSampleRepository extends JpaRepository<Sample, Integer> {

}
