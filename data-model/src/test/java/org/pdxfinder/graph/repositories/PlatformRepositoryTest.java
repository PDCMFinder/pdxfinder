package org.pdxfinder.graph.repositories;

import org.junit.Before;
import org.junit.Test;
import org.pdxfinder.BaseTest;
import org.pdxfinder.graph.dao.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Date;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 * Testing repository for managing tumor objects
 */
public class PlatformRepositoryTest extends BaseTest {

    private final static Logger log = LoggerFactory.getLogger(PlatformRepositoryTest.class);
    private String extDsName = "TEST_SOURCE";
    private String extDsAbbrev = "TS";
    @Autowired
    private PlatformRepository platformRepository;

    @Autowired
    private PlatformAssociationRepository platformAssociationRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private MarkerRepository markerRepository;

    @Before
    public void cleanDb() {

        platformRepository.deleteAll();
        platformAssociationRepository.deleteAll();
        groupRepository.deleteAll();
        markerRepository.deleteAll();

        Group ds = groupRepository.findByNameAndType(extDsName, "Provider");
        if(ds == null){
            log.info("Group not found. Creating", extDsName);

            ds = new Group(extDsName, extDsAbbrev, "Provider");
            groupRepository.save(ds);

        }

    }

    @Test
    public void createPlatformAndAssociateMarkers() throws Exception {

        Set<PlatformAssociation> pas = new HashSet<>();

        for (int i = 0; i < 10; i++) {
            Marker marker = new Marker();
            marker.setHgncName("Marker" + i);

            PlatformAssociation p = new PlatformAssociation();
            p.setMarker(marker);
            p.setExon("exon " + i);
            pas.add(p);

        }

        Platform p = new Platform();
        p.setPlatformAssociations(pas);
        p.setName("Gene Panel");
        p.setGroup(groupRepository.findByNameAndType(extDsName, "Provider"));

        platformRepository.save(p);

        assert (platformAssociationRepository.findByPlatform_ExternalDataSource_Name(extDsName).size() > 0);
        assert platformRepository.findByName("Gene Panel").getPlatformAssociations().stream().count() > 5;


    }


}
