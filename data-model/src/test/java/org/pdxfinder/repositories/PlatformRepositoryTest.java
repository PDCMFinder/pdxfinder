package org.pdxfinder.repositories;

import org.junit.Before;
import org.junit.Test;
import org.pdxfinder.BaseTest;
import org.pdxfinder.dao.ExternalDataSource;
import org.pdxfinder.dao.Marker;
import org.pdxfinder.dao.Platform;
import org.pdxfinder.dao.PlatformAssociation;
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

    @Autowired
    private PlatformRepository platformRepository;

    @Autowired
    private PlatformAssociationRepository platformAssociationRepository;

    @Autowired
    private ExternalDataSourceRepository externalDataSourceRepository;

    @Autowired
    private MarkerRepository markerRepository;

    @Before
    public void cleanDb() {

        platformRepository.deleteAll();
        platformAssociationRepository.deleteAll();
        externalDataSourceRepository.deleteAll();
        markerRepository.deleteAll();

        ExternalDataSource ds = externalDataSourceRepository.findByName(extDsName);
        if (ds == null) {
            log.debug("External data source {} not found. Creating", extDsName);
            ds = new ExternalDataSource(extDsName, extDsName, extDsName, extDsName, Date.from(Instant.now()));
            externalDataSourceRepository.save(ds);
        }

    }

    @Test
    public void createPlatformAndAssociateMarkers() throws Exception {

        Set<PlatformAssociation> pas = new HashSet<>();

        for (int i = 0; i < 10; i++) {
            Marker marker = new Marker();
            marker.setName("Marker" + i);

            PlatformAssociation p = new PlatformAssociation();
            p.setMarker(marker);
            p.setExon("exon " + i);
            pas.add(p);

        }

        Platform p = new Platform();
        p.setPlatformAssociations(pas);
        p.setName("Gene Panel");
        p.setExternalDataSource(externalDataSourceRepository.findByName(extDsName));

        platformRepository.save(p);

        assert (platformAssociationRepository.findByPlatform_ExternalDataSource_Name(extDsName).size() > 0);
        assert platformRepository.findByName("Gene Panel").getPlatformAssociations().stream().count() > 5;


    }


}
