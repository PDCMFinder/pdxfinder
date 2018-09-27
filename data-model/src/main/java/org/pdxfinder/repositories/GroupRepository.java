package org.pdxfinder.repositories;

import org.pdxfinder.dao.Group;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/*
 * Created by csaba on 28/06/2018.
 */
public interface GroupRepository extends PagingAndSortingRepository<Group, Long> {


    @Query("MATCH (g:Group) WHERE g.name = {name} AND g.type = {type} RETURN g")
    Group findByNameAndType(@Param("name") String name, @Param("type") String type);

    @Query("MATCH (g:Group) WHERE g.pubMedId = {pubMedId} AND g.type = {type} RETURN g")
    Group findByPubmedIdAndType(@Param("pubMedId") String pubMedId, @Param("type") String type);


    @Query("MATCH (ed:Group) RETURN DISTINCT ed.abbreviation ORDER BY ed.abbreviation")
    List<String> findAllAbbreviations();

    @Query("MATCH (g:Group) WHERE g.type = {type} RETURN g")
    List<Group> findAllByType(@Param("type") String type);

}
