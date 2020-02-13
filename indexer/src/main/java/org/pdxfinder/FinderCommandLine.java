package org.pdxfinder;

import org.pdxfinder.services.constants.DataUrl;
import org.pdxfinder.services.loader.envload.LoadMarkers;
import org.pdxfinder.services.loader.envload.LoadNCIT;
import org.pdxfinder.services.loader.envload.LoadNCITDrugs;
import org.pdxfinder.services.DataImportService;
import org.pdxfinder.utils.DataProviders.DataProvider;
import org.pdxfinder.utils.DataProviders.DataProviderGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ArgGroup;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Callable;

@Component
@Command(
    name="indexer",
    mixinStandardHelpOptions = true,
    subcommands = {
        FinderCommandLine.Load.class,
        FinderCommandLine.Export.class})
@Order(value = -100)
public class FinderCommandLine implements Callable<Integer> {

    @Override
    public Integer call() {
        return 23;
    }

    @Component
    @Order(value=-100)
    @Command(
        name = "load",
        description = "Loads and transforms data into the PDXFinder.",
        mixinStandardHelpOptions = true,
        exitCodeOnExecutionException = 34)
    static class Load implements Callable<Integer> {

        Logger log = LoggerFactory.getLogger(FinderCommandLine.class);

        @Autowired private DataImportService dataImportService;
        @Value("${spring.data.neo4j.uri}") private Path databasePath;

        @Autowired private LoadDiseaseOntology loadDiseaseOntology;
        @Autowired private LoadMarkers loadMarkers;
        @Autowired private LoadNCITDrugs loadNCITDrugs;
        @Autowired private LoadNCIT loadNCIT;

        @ArgGroup(multiplicity = "1") Exclusive datasetRequested;

        static class Exclusive {
            @Option(names = {"-g", "--group"},
                    arity = "1",
                    description = "Load the data for groups of dataProvider (default: [${DEFAULT-VALUE}]). " +
                            "Accepted Values: [@|cyan ${COMPLETION-CANDIDATES} |@]")
            private DataProviderGroup dataProviderGroup = DataProviderGroup.All;

            @Option(names = {"-o", "--only"},
                    description = "Load only the data for the listed dataProvider. "  +
                            "Accepted Values: [@|cyan ${COMPLETION-CANDIDATES} |@]")
            private DataProvider[] dataProvider;

            public DataProviderGroup getDataProviderGroup() { return dataProviderGroup; }
            public DataProvider[] getDataProvider() {
                return dataProvider;
            }

        }

        @Option(names = {"-d", "--data-dir"},
                required = true,
                description = "Path of the PDXFinder data directory " +
                        "(default: [${DEFAULT-VALUE}], set in application.properties)")
        @Value("${pdxfinder.root.dir}")
        public Path dataDirectory;

        @Option(names = {"-c", "--clear-cache"},
                description = "Clear cached data and reload, including NCIT ontologies, etc.")
        private boolean clearCacheRequested;

        @Option(names = {"-k", "--keep-db"},
                description = "Skips clearing of the database before loading new data.")
        private boolean keepDatabaseRequested;


        @Override
        public Integer call() {
            keepDatabaseIfRequested();
            loadOntologies();
            loadRequestedPDXData();

            return 33;
        }

        private void keepDatabaseIfRequested() {
            if (keepDatabaseRequested) {
                log.info("Using existing database [{}]", databasePath);
            } else {
                clearDatabase();
            }
        }

        private void clearDatabase() {
            log.info("Deleting all nodes and edges in existing database [{}]", databasePath);
            try {
//                dataImportService.deleteAll();
            }
            catch (DataAccessException e) {
                log.error("Failed to delete database nodes and edges: {}", e);
            }
        }

        private void loadOntologies() {
            log.info("Loading ontologies...");

            try { loadDiseaseOntology.run(); }
            catch (Exception e) { log.error("Failed to load disease ontology: {}", e); }

            try { loadMarkers.loadGenes(DataUrl.HUGO_FILE_URL.get()); }
            catch (Exception e) { log.error("Failed to load markers: {}", e); }

            try { loadNCITDrugs.loadRegimens(); }
            catch (Exception e) { log.error("Failed to load NCIT Drugs: {}", e); }

            try { loadNCIT.loadOntology(DataUrl.DISEASES_BRANCH_URL.get()); }
            catch (Exception e) { log.error("Failed to load NCIT: {}", e); }

        }

        private void loadRequestedPDXData() {
            List<DataProvider> providersRequested = Arrays.asList(datasetRequested.getDataProvider());
            log.info("Running requested PDX dataset loaders {}...", providersRequested);

            for (DataProvider i : providersRequested) {
                callRelevantLoader(i);
            }

        }

        private void callRelevantLoader(DataProvider provider) {
            try {
                provider.load();
            } catch (Exception e) {
                log.error("Error calling loader for {}: {}", provider, e);
            }
        }

    }

    @Component
    @Command(
        name = "export",
        description = "Exports data to template format.",
        mixinStandardHelpOptions = true,
        exitCodeOnExecutionException = 34)
    static class Export implements Callable<Integer> {

        Logger log = LoggerFactory.getLogger(FinderCommandLine.class);

        @Override
        public Integer call() {
            log.error("This command is not yet implemented");
            return 33;
        }
    }


}
