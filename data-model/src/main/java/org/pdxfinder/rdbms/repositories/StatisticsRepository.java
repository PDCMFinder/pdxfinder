package org.pdxfinder.rdbms.repositories;

import org.pdxfinder.rdbms.dao.Statistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StatisticsRepository extends JpaRepository<Statistics, Integer> {
}
