package org.pdxfinder.rdbms.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StatisticsDataRepository extends JpaRepository<StatisticsDataRepository, Integer> {


}
