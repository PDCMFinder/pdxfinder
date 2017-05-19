package org.pdxfinder.repositories;

import org.pdxfinder.dao.MarkerAssociation;
import org.pdxfinder.dao.Sample;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Repository for managing creating, finding, and deleting sample objects
 */
public interface SampleRepository extends PagingAndSortingRepository<Sample, Long> {

    Sample findBySourceSampleId(String sourceSampleId);

    Set<Sample> findByDataSource(String dataSource);

    Set<Sample> findByExternalDataSourceAbbreviation(String abbreviation);

    @Query("MATCH (s:Sample)-[o:ORIGIN_TISSUE]-(t:Tissue) where toLower(s.diagnosis) contains toLower({diag}) return s,o,t order by s.diagnosis limit 30")
    Collection<Sample> findByDiagnosisContains(@Param("diag") String diag);

    @Query("MATCH (s:Sample)--(:MolecularCharacterization)--(:MarkerAssociation)--(m:Marker) " +
            "WHERE toLower(s.diagnosis) CONTAINS toLower({diag}) " +
            "AND m.name IN {markers} return s")
    Collection<Sample> findByDiagnosisContainsAndHaveMarkers(@Param("diag") String diag, @Param("markers") String[] markers);


}
