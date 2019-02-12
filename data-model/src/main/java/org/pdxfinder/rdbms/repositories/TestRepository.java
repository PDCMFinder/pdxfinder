package org.pdxfinder.rdbms.repositories;

import org.pdxfinder.rdbms.dao.TestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestRepository extends JpaRepository<TestEntity, Long> {
}
