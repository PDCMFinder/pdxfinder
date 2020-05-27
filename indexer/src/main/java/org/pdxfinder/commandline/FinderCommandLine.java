package org.pdxfinder.commandline;

import org.pdxfinder.services.constants.DataProvider;
import org.pdxfinder.services.constants.DataProviderGroup;
import org.pdxfinder.utils.CbpTransformer;
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
    subcommands = {
        FinderCommandLine.Load.class,
        FinderCommandLine.Export.class,
        FinderCommandLine.Transform.class,
        CommandLine.HelpCommand.class
    }
)

@Order(value = -100)
public class FinderCommandLine implements Callable<Integer> {

    @Override
    public Integer call() {
        return 23;
    }

    @Component
    @Order(value = -100)
    @Command(name = "load",
        description = "Loads and transforms data into the PDX Finder",
        mixinStandardHelpOptions = true,
        exitCodeOnExecutionException = 34)

    static class Load implements Callable<Integer> {

        Logger log = LoggerFactory.getLogger(Load.class);

        @Autowired
        private FinderLoader finderLoader;

        @Option(
            names = {"-d", "--data-dir"},
            required = true,
            description = "Path of the PDX Finder data directory " +
                "(default: [${DEFAULT-VALUE}], set in application.properties)")

        private File dataDirectory;

        @Option(names = {"-c", "--cache"},
                description = "Clear cached data and reload, including NCIT ontology terms, etc.")
        private boolean loadCacheRequested;

        @Option(names = {"-m", "--mapping"},
                description = "Delete mapping database content, and reload from mapping file")
        private boolean initializeMappingDB;

        @Option(names = {"-k", "--keep-db"},
                description = "Skips clearing of the database before loading new data.")
        private boolean keepDatabaseRequested;

        @Option(names = {"--validate-only"},
                description = "Don't load the PDX data, only perform validation and report errors.")
        private boolean validateOnlyRequested;

        @Option(names = {"-p", "--post-load"},
                description = "Implement Post data loading Steps")
        private boolean postLoadRequested;

        @Option(names = {"--spring.data.neo4j.uri"}, paramLabel = "Neo4j DB Directory", description = "Embedded Neo4j Database location", hidden = true)
        private String springDataNeo4jUri;

        @Option(names = {"--db-refresh"}, paramLabel = "Neo4j DB Delete and ReInitialize", description = "clear off database and intialize cache before loading new data.", hidden = true)
        private String debReload;

        @Option(names = {"--spring.datasource.url"}, paramLabel = "H2 DB Directory", description = "Embedded H2 Database location",  hidden = true)
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
                    postLoadRequested,
                    initializeMappingDB
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
    @Command(name = "export",
            description = "Convert between PDX Finder templates, Neo4j data, and Json",
            mixinStandardHelpOptions = true,
            exitCodeOnExecutionException = 34)
    static class Export implements Callable<Integer> {

        @Autowired
        private FinderExporter finderExporter;

        @Option(
                names = {"-d", "--data-dir"},
                description = "Path of the PDX Finder data directory " +
                        "(default: [${DEFAULT-VALUE}], set in application.properties)")
        private File dataDirectory;

        @ArgGroup(multiplicity = "0..1")
        Export.Exclusive datasetRequested = new Export.Exclusive();
        static class Exclusive{

            @Option(
                    names = {"-e", "--export"},
                    description = "Export database to TSV templates." +
                            " Requires either the provider name or use -a to export all providers")
            private DataProvider provider;

            @Option(
                    names = {"-a", "--all"},
                    description = "Export all providers data." +
                            " Warning: this can be computationally intensive")

            private boolean loadAll;

            public DataProvider getProvider() {
                return provider;
            }

            public boolean isLoadAll() {
                return loadAll;
            }
        }
        @Override
        public Integer call() throws IOException {
            finderExporter.run(dataDirectory, datasetRequested.provider.toString(), datasetRequested.isLoadAll());
            return 0;
        }

        @Override
        public String toString() {
            return new StringJoiner("\n", Transform.class.getSimpleName() + "[\n", "\n]")
                    .add("dataDirectory=" + dataDirectory)
                    .add("Export provider" + datasetRequested.getProvider())
                    .add("Load all" + datasetRequested.isLoadAll())
                    .toString();
        }
    }



    @Component
    @Order(value = -100)
    @Command(name = "transform",
            description = "Convert between PDX Finder templates, Neo4j data, and Json",
            mixinStandardHelpOptions = true,
            exitCodeOnExecutionException = 34)
    static class Transform implements Callable<Integer> {

        Logger log = LoggerFactory.getLogger(Transform.class);

        @Autowired
        private FinderTransformer finderTransformer;

        @Option(
                names = {"-d", "--data-dir"},
                description = "Path of the PDX Finder data directory (overrides existing value)" +
                        "(default: [${DEFAULT-VALUE}], set in application.properties)")
        private File dataDirectory;

        @Option(
                names = {"-f", "--file"},
                description = "File location for utilities that require a File parameter." +
                    " Includes cbioTransformer and LiftOver")
        private File ingestFile;

        @Option(
                names = {"--template-dir"},
                description = "Set template directory. Default is the data-directory/template folder." +
                    " This is not completely implemented.")
        private File templateDirectory;

        @Option(
                names = {"o", "--exportDir"},
                description = "Set export directory. Default is the data-directory/export folder." +
                    " This is not completely implemented.")
        private File exportDirectory;

        @ArgGroup(multiplicity = "0..1")
        Transform.Exclusive exclusiveArguments = new Transform.Exclusive();

        static class Exclusive{

            @Option(
                    names = {"-c","-cbio"},
                    description = "Transform Cbioportal Json into PdxFinder Templates for ingest into the finder." +
                        " Only arguments supported 'mut' or 'gistic' ")
            private CbpTransformer.cbioType cbioType;

            @Option(
                    names = {"-entrez2hugo"},
                    description = "Convert entrez id's to hugo",
                    split = " ")
            private List<String> entrezToHugo;

            public CbpTransformer.cbioType getCbioDataType() {
                return cbioType;
            }
            public List<String> getEntrezToHugo() { return entrezToHugo;}
        }

        @Override
        public Integer call() throws IOException {
            log.info("Loading using supplied parameters:\n{}", this);
            finderTransformer.run(
                dataDirectory,
                templateDirectory,
                exportDirectory,
                ingestFile,
                exclusiveArguments.getCbioDataType(),
                    exclusiveArguments.getEntrezToHugo()
            );

            return 0;
        }

        @Override
        public String toString() {
            return new StringJoiner("\n", Transform.class.getSimpleName() + "[\n", "\n]")
                    .add("dataDirectory=" + dataDirectory)
                    .add("File=" + ingestFile)
                    .add("Template Dir=" + templateDirectory)
                    .add("export Dir=" + exportDirectory)
                    .add("CbioDataType()" + exclusiveArguments.getCbioDataType())
                    .toString();
        }
    }
}
