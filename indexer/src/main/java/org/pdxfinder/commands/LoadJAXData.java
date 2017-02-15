package org.pdxfinder.commands;

import org.apache.commons.cli.*;
import org.neo4j.ogm.session.Session;
import org.pdxfinder.dao.ExternalDataSource;
import org.pdxfinder.repositories.ExternalDataSourceRepository;
import org.pdxfinder.repositories.TumorTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.Date;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Load data from the UNITO-IRCC center.
 */
@Component
@Order(value = Ordered.LOWEST_PRECEDENCE)
public class LoadJAXData implements CommandLineRunner {

    private final static Logger log = LoggerFactory.getLogger(LoadJAXData.class);

    private final static String DATASOURCE_ABBREVIATION = "UNITO-IRCC";
    private final static String DATASOURCE_NAME = "University of Torino, Candiolo Cancer Institute";
    private final static String DATASOURCE_DESCRIPTION = "The Candiolo Cancer Institute works in synergy with the University of Torino Medical School. Its mission is a significant contribution to fight cancer, by understanding the basics, and by providing state-of-the-art diagnostic and therapeutic services.";

    private Options options;
    private CommandLineParser parser;
    private CommandLine cmd;
    private HelpFormatter formatter;

    private TumorTypeRepository tumorTypeRepository;
    private ExternalDataSourceRepository externalDataSourceRepository;
    private Session session;

    @PostConstruct
    public void init() {
        options = new Options();
        parser = new DefaultParser();
        formatter = new HelpFormatter();
        log.info("Setting up LoadDataCommand option");
    }

    public LoadJAXData(TumorTypeRepository tumorTypeRepository, ExternalDataSourceRepository externalDataSourceRepository, Session session) {
        Assert.notNull(tumorTypeRepository);
        Assert.notNull(externalDataSourceRepository);
        Assert.notNull(session);

        this.tumorTypeRepository = tumorTypeRepository;
        this.externalDataSourceRepository = externalDataSourceRepository;
        this.session = session;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {

        if ("load".equals(args[0])) {

            log.info("Loading data from UNITO-IRCC");
            try {
                cmd = parser.parse(options, args);
            } catch (UnrecognizedOptionException | MissingArgumentException e) {
                e.printStackTrace();
                formatter.printHelp("load", options);
                System.exit(1);
            }

            // Delete all data currently associated to this data source
            ExternalDataSource eds = externalDataSourceRepository.findByAbbreviation(DATASOURCE_ABBREVIATION);
            if (eds != null) {
                externalDataSourceRepository.delete(eds);
            }
            log.info("Tumor type has {} entries now", tumorTypeRepository.count());
            log.info("  PURGING DATABASE");
            session.purgeDatabase();
            log.info("  DONE PURGING DATABASE");
            log.info("Tumor type has {} entries now", tumorTypeRepository.count());

            // Create datasource node
            createDataSource();


        }

    }

    private void createDataSource() {
        ExternalDataSource eds = externalDataSourceRepository.findByAbbreviation(DATASOURCE_ABBREVIATION);
        if (eds == null) {
            log.info("External data source '{}' not found. Creating", DATASOURCE_ABBREVIATION);
            eds = new ExternalDataSource(
                    DATASOURCE_NAME,
                    DATASOURCE_ABBREVIATION,
                    DATASOURCE_DESCRIPTION,
                    Date.from(Instant.now()));
            externalDataSourceRepository.save(eds);
        }

    }

}
