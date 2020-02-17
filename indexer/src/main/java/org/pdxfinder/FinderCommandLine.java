package org.pdxfinder;

import org.pdxfinder.services.DataImportService;
import org.pdxfinder.services.loader.envload.LoadMarkers;
import org.pdxfinder.services.loader.envload.LoadNCIT;
import org.pdxfinder.services.loader.envload.LoadNCITDrugs;
import org.pdxfinder.utils.DataProviders.DataProvider;
import org.pdxfinder.utils.DataProviders.DataProviderGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ArgGroup;

import java.io.File;
import java.util.*;
import java.util.concurrent.Callable;

@Component
@Command(name = "indexer",
    mixinStandardHelpOptions = true,
    subcommands = {FinderCommandLine.Load.class, FinderCommandLine.Export.class})
@Order(value = -100)
public class FinderCommandLine implements Callable<Integer> {

    @Override
    public Integer call() {
        return 23;
    }

    @Component
    @Order(value = -100)
    @Command(name = "load",
        description = "Loads and transforms data into the PDXFinder.",
        mixinStandardHelpOptions = true,
        exitCodeOnExecutionException = 34)
    static class Load implements Callable<Integer> {

        Logger log = LoggerFactory.getLogger(Load.class);

        @Autowired
        private LoaderNew loaderNew;

        @Option(
            names = {"-d", "--data-dir"},
            required = true,
            description = "Path of the PDXFinder data directory " +
                "(default: [${DEFAULT-VALUE}], set in application.properties)")
        @Value("${pdxfinder.root.dir}")
        private File dataDirectory;

        @Option(
            names = {"-c", "--clear-cache"},
            description = "Clear cached data and reload, including NCIT ontology terms, etc.")
        private boolean clearCacheRequested;

        @Option(
            names = {"-k", "--keep-db"},
            description = "Skips clearing of the database before loading new data.")
        private boolean keepDatabaseRequested;


        @ArgGroup(multiplicity = "1")
        Exclusive datasetRequested;

        static class Exclusive {

            @Option(
                names = {"-g", "--group"},
                arity = "1",
                description = "Load the data for groups of dataProvider (default: [${DEFAULT-VALUE}]). " +
                    "Accepted Values: [@|cyan ${COMPLETION-CANDIDATES} |@]")
            private DataProviderGroup dataProviderGroup = DataProviderGroup.All;

            @Option(
                names = {"-o", "--only"},
                description = "Load only the data for the listed dataProvider. " +
                    "Accepted Values: [@|cyan ${COMPLETION-CANDIDATES} |@]")
            private DataProvider[] dataProvider;

            public DataProviderGroup getDataProviderGroup() {
                return dataProviderGroup;
            }

            public DataProvider[] getDataProvider() {
                return dataProvider;
            }
        }

        @Override
        public Integer call() {
            List<DataProvider> providersRequested = Arrays.asList(datasetRequested.getDataProvider());
            loaderNew.run(
                providersRequested,
                dataDirectory,
                clearCacheRequested,
                keepDatabaseRequested
            );
            return 33;
        }

    }

    @Component
    @Command(
        name = "export",
        description = "Exports data to template format.",
        mixinStandardHelpOptions = true,
        exitCodeOnExecutionException = 34)
    static class Export implements Callable<Integer> {

        Logger log = LoggerFactory.getLogger(Export.class);

        @Override
        public Integer call() {
            log.error("This command is not yet implemented");
            return 33;
        }
    }


}
