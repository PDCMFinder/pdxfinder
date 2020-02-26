package org.pdxfinder;

import org.pdxfinder.utils.DataProviders;
import org.pdxfinder.utils.DataProviders.DataProvider;
import org.pdxfinder.utils.DataProviders.DataProviderGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    subcommands = {FinderCommandLine.Load.class})
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
        private FinderLoader finderLoader;

        @Option(
            names = {"-d", "--data-dir"},
            required = true,
            description = "Path of the PDXFinder data directory " +
                "(default: [${DEFAULT-VALUE}], set in application.properties)")
        private File dataDirectory;

        @Option(names = {"-c", "--cache"},
                description = "Clear cached data and reload, including NCIT ontology terms, etc.")
        private boolean loadCacheRequested;

        @Option(names = {"-k", "--keep-db"},
                description = "Skips clearing of the database before loading new data.")
        private boolean keepDatabaseRequested;

        @Option(names = { "--spring.data.neo4j.uri"}, paramLabel = "Neo4j DB Directory", description = "Embedded Neo4j Database location", required=false, hidden=true)
        private String springDataNeo4jUri;

        @Option(names = { "--spring.datasource.url"}, paramLabel = "H2 DB Directory", description = "Embedded H2 Database location", required=false, hidden=true)
        private String springDatasourceUrl;

        @ArgGroup(multiplicity = "0..1")
        Exclusive datasetRequested = new Exclusive();

        static class Exclusive {

            @Option(
                    names = {"-g", "--group"},
                    arity = "1",
                    description = "Load the data for groups of dataProvider (default: [${DEFAULT-VALUE}]). " +
                            "Accepted Values: [@|cyan ${COMPLETION-CANDIDATES} |@]")
            private DataProviderGroup dataProviderGroup;

            @Option(
                    names = {"-o", "--only"}, arity = "1..*",
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

            log.info("Loading using supplied parameters:\n{}", this);
            List<DataProvider> providersRequested = getListOfRequestedProviders();

            finderLoader.run(
                providersRequested,
                loadCacheRequested,
                keepDatabaseRequested
            );
            return 0;
        }


        List<DataProvider> getListOfRequestedProviders() {

            Optional<DataProvider[]> dataProviders = Optional.ofNullable(
                    datasetRequested.getDataProvider()
            );

            Optional<DataProviderGroup> dataProviderGroup = Optional.ofNullable(
                    datasetRequested.getDataProviderGroup()
            );

            if (dataProviders.isPresent()) {

                return Arrays.asList(dataProviders.get());
            } else if (dataProviderGroup.isPresent()) {

                return DataProviders.getProvidersFrom(dataProviderGroup.get());
            } else {
                return new ArrayList<>();
            }

        }

        @Override
        public String toString() {
            return new StringJoiner("\n", Load.class.getSimpleName() + "[\n", "\n]")
                .add("dataDirectory=" + dataDirectory)
                .add("clearCacheRequested=" + loadCacheRequested)
                .add("keepDatabaseRequested=" + keepDatabaseRequested)
                .add("datasetRequested=" + getListOfRequestedProviders())
                .toString();
        }
    }

}
