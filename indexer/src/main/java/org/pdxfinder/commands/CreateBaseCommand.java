package org.pdxfinder.commands;

import org.apache.commons.cli.*;
import org.neo4j.ogm.session.Session;
import org.pdxfinder.dao.EngraftmentType;
import org.pdxfinder.dao.HostStrain;
import org.pdxfinder.dao.EngraftmentSite;
import org.pdxfinder.dao.TumorType;
import org.pdxfinder.repositories.HostStrainRepository;
import org.pdxfinder.repositories.EngraftmentSiteRepository;
import org.pdxfinder.repositories.EngraftmentTypeRepository;
import org.pdxfinder.repositories.TumorTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Loads the required Sample Type nodes into the database
 */
@Component
@Order
public class CreateBaseCommand implements CommandLineRunner {

    private final static Logger log = LoggerFactory.getLogger(CreateBaseCommand.class);

    private Options options;
    private CommandLineParser parser;
    private CommandLine cmd;
    private HelpFormatter formatter;

    private TumorTypeRepository tumorTypeRepository;
    private HostStrainRepository hostStrainRepository;
    private EngraftmentTypeRepository engraftmentTypeRepository;
    private EngraftmentSiteRepository engraftmentSiteRepository;

    private Session session;

    @PostConstruct
    public void init() {
        options = new Options();
        parser = new DefaultParser();
        formatter = new HelpFormatter();
        log.info("Setting up CreateBaseCommand option");
    }

    public CreateBaseCommand(TumorTypeRepository tumorTypeRepository, HostStrainRepository hostStrainRepository, EngraftmentSiteRepository engraftmentSiteRepository, EngraftmentTypeRepository engraftmentTypeRepository, Session session) {
        Assert.notNull(tumorTypeRepository, "tumorTypeRepository is null");
        Assert.notNull(hostStrainRepository, "hostStrainRepository is null");
        Assert.notNull(engraftmentSiteRepository, "implantationSiteRepository is null");
        Assert.notNull(engraftmentTypeRepository, "implantationTypeRepository is null");
        Assert.notNull(session, "session is null");

        this.tumorTypeRepository = tumorTypeRepository;
        this.hostStrainRepository = hostStrainRepository;
        this.engraftmentSiteRepository = engraftmentSiteRepository;
        this.engraftmentTypeRepository = engraftmentTypeRepository;
        this.session = session;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {

        if ("create".equals(args[0])) {

            log.info("Executing CreateBaseCommand");
            try {
                cmd = parser.parse(options, args);
            } catch (UnrecognizedOptionException | MissingArgumentException e) {
                e.printStackTrace();
                formatter.printHelp("create", options);
                System.exit(1);
            }

            System.out.println("Creating the base graph");

            // Delete whole graph
            log.info("Sample type has {} entries now", tumorTypeRepository.count());
            log.info("  PURGING DATABASE");
            session.purgeDatabase();
            log.info("  DONE PURGING DATABASE");
            log.info("Sample type has {} entries now", tumorTypeRepository.count());

            // Insert the tumor types
            createTumorTypes();

            // Insert the host strains
            createBackgroundStrains();

            // Insert the implantation sites
            createImplantationSites();

            // Insert the implantation types
            createImplantationTypes();

        }

    }

    private void createTumorTypes() {
        List<String> types = Arrays.asList(
                "Metastasis",
                "Metastatic",
                "Not Specified",
                "Primary Malignancy",
                "Recurrent/Relapse"
        );

        for (String type : types) {
            TumorType foundType = tumorTypeRepository.findByName(type);
            if (foundType == null) {
                log.info("Sample type '{}' not found. Creating", type);
                foundType = new TumorType(type);
                tumorTypeRepository.save(foundType);
            }
        }

    }

    private void createBackgroundStrains() {

        List<HostStrain> strains = new ArrayList<>();

        // NOG strain
        strains.add(
                new HostStrain(
                        "NOD/Shi-scid/IL-2R\u03BB<null>",
                        "NOD scid gamma")
        );

        // NOD scid gamma strain
        strains.add(
                new HostStrain(
                        "NOD.Cg-Prkdc<scid> Il2rg<tm1Wjl>/SzJ",
                        "NOD scid gamma")
        );

        // NOD scid strain
        strains.add(
                new HostStrain(
                        "NOD.CB17-Prkdc<scid>/J",
                        "NOD scid")
        );


        // Save all background strains if they do not exist already
        for (HostStrain strain : strains) {
            if (hostStrainRepository.findBySymbol(strain.getSymbol()) == null) {
                log.info("  Creating background strain {} ({})", strain.getName(), strain.getSymbol());
                hostStrainRepository.save(strain);
            }
        }
    }

    private void createImplantationSites() {
        List<String> sites = Arrays.asList(
                "Right Flank",
                "Left Flank",
                "Subcutaneous",
                "Orthotopic",
                "Subrenal Capsule"
        );

        for (String site : sites) {
            EngraftmentSite engraftmentSite = engraftmentSiteRepository.findByName(site);
            if (engraftmentSite == null) {
                log.info("  Creating Implantation Site '{}'", site);
                engraftmentSite = new EngraftmentSite(site);
                engraftmentSiteRepository.save(engraftmentSite);
            }
        }


    }

    private void createImplantationTypes() {
        List<String> types = Arrays.asList(
                "Core needle biopsy",
                "Surgical resection (fragment)",
                "Biopsy (type unspecified)",
                "Leukapheresis",
                "Pleural effusion",
                "Not Specified",
                "Fine needle aspirate",
                "Lavage"
        );

        for (String type : types) {
            EngraftmentType engraftmentType = engraftmentTypeRepository.findByName(type);
            if (engraftmentType == null) {
                log.info("Implantation type '{}' not found. Creating", type);
                engraftmentType = new EngraftmentType(type);
                engraftmentTypeRepository.save(engraftmentType);
            }
        }


    }


}
