package org.pdxfinder.rdbms.repositories;

import org.pdxfinder.rdbms.dao.StatisticsData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StatisticsDataRepository extends JpaRepository<StatisticsData, Integer> {


}
