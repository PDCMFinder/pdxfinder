package org.pdxfinder.commandline;

import org.pdxfinder.services.constants.DataProvider;
import org.pdxfinder.services.constants.DataProviderGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ArgGroup;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;

@Component
@Command(name = "indexer",
    description = "The PDX Finder indexer command calls various data operations on the application." +
        " Run `[COMMAND] --help` or `help [COMMAND]` for specific usage information.",
    mixinStandardHelpOptions = true,
    subcommands = {FinderCommandLine.Load.class, CommandLine.HelpCommand.class})
@Order(value = -100)
public class FinderCommandLine implements Callable<Integer> {

    @Override
    public Integer call() {
        return 23;
    }

    @Component
    @Order(value = -100)
    @Command(name = "load",
        description = "Loads and transforms data into the PDXFinder",
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

        @Option(names = {"--validate-only"},
                description = "Don't load the PDX data, only perform validation and report errors.")
        private boolean validateOnlyRequested;

        @Option(names = {"-p", "--post-load"},
                description = "Implement Post data loading Steps", required=false)
        private boolean postLoadRequested;

        @Option(names = { "--spring.data.neo4j.uri"}, paramLabel = "Neo4j DB Directory", description = "Embedded Neo4j Database location", required=false, hidden=true)
        private String springDataNeo4jUri;

        @Option(names = { "--spring.datasource.url"}, paramLabel = "H2 DB Directory", description = "Embedded H2 Database location", required=false, hidden=true)
        private String springDatasourceUrl;

        @ArgGroup(multiplicity = "0..1")
        Exclusive datasetRequested = new Exclusive();

        static class Exclusive {

            @Option(names = {"-g", "--group"}, arity = "1",
                    description = "Load the data for groups of dataProvider (default: [${DEFAULT-VALUE}]). " +
                            "Accepted Values: [@|cyan ${COMPLETION-CANDIDATES} |@]")
            private DataProviderGroup dataProviderGroup;

            @Option(names = {"-o", "--only"}, arity = "1..*",
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
                dataDirectory,
                validateOnlyRequested,
                loadCacheRequested,
                keepDatabaseRequested,
                postLoadRequested
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

                return DataProviderGroup.getProvidersFrom(dataProviderGroup.get());
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

    @Component
    @Order(value = -100)
    @Command(name = "transformer",
            description = "Utilities to convert between Pdx Finder templates, Neo4j data, and Json",
            mixinStandardHelpOptions = true,
            exitCodeOnExecutionException = 34)
    static class Transform implements Callable<Integer> {

        Logger log = LoggerFactory.getLogger(Transform.class);

        @Autowired
        private FinderTransformer finderTransformer;

        @Option(
                names = {"-d", "--data-dir"},
                description = "Path of the PDXFinder data directory " +
                        "(default: [${DEFAULT-VALUE}], set in application.properties)")
        private File dataDirectory;


        @ArgGroup(multiplicity = "0..1")
        Transform.Exclusive exclusiveArguments = new Transform.Exclusive();

        static class Exclusive{
            @Option(
                    names = {"-e", "--export"},
                    description = "Export Neo4j data to tsv templates. Requires either the provider name or use -a to export all providers")
            private String provider;

            @Option(
                    names = {"-a", "--all"},
                    description = "Export all providers data. Warning: do to large provider datasets this can be computationally intensive")
            private boolean loadAll;


            public String getProvider() {
                return provider;
            }

            public boolean isLoadAll() {
                return loadAll;
            }
        }

        @Override
        public Integer call() throws IOException {
            log.info("Loading using supplied parameters:\n{}", this);
            finderTransformer.run(dataDirectory,exclusiveArguments.getProvider(),exclusiveArguments.isLoadAll());
            return 0;
        }

        @Override
        public String toString() {
            return new StringJoiner("\n", Transform.class.getSimpleName() + "[\n", "\n]")
                    .add("dataDirectory=" + dataDirectory.getName())
                    .add("Export provider" + exclusiveArguments.provider)
                    .add("Load all" + exclusiveArguments.loadAll)
                    .toString();
        }
    }
}
