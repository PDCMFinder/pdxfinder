package org.pdxfinder.dataloaders;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.pdxfinder.dataloaders.updog.Updog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class MinimalLoadRunner implements CommandLineRunner, ApplicationContextAware {

    private Updog updog;
    @Value("${provider}") private String provider;
    @Value("${data.directory}") private String dataDirectory;
    @Value("${data.directory}") private String finderRootDir;
    private Path updogDirectory;
    private boolean validateOnlyRequested;
    private List<String> allUpdogProviders;

    @Autowired
    public MinimalLoadRunner(Updog updog) {
        this.updog = updog;
    }

    @Override
    public void run(String... args) throws Exception {
        OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();
        parser.accepts("loadUniversalRefactor");
        parser.accepts("validateOnly");
        parser.accepts("provider").withRequiredArg();
        parser.accepts("dataDirectory").withRequiredArg();
        OptionSet options = parser.parse(args);

        dataDirectory = options.valueOf("dataDirectory").toString();
        provider = options.valueOf("provider").toString();
        validateOnlyRequested = options.has("validateOnly");
        allUpdogProviders = new ArrayList<>(Arrays.asList("Curie-BC", "Curie-LC", "Curie-OC", "IRCC-CRC",
            "IRCC-GC", "PMLB", "TRACE", "UOC-BC", "UOM-BC", "VHIO-BC", "VHIO-CRC"));

        if (options.has("loadUniversalRefactor")) {
            if (allUpdogProviders.contains(provider)) {
                updogDirectory = Paths.get(dataDirectory, "/data/UPDOG", provider);
                updog.run(updogDirectory, provider, validateOnlyRequested);
            } else if (provider.equalsIgnoreCase("all"))
                for (String s : allUpdogProviders) {
                    updogDirectory = Paths.get(dataDirectory, "/data/UPDOG", s);
                    updog.run(updogDirectory, s, validateOnlyRequested);
                }
            else
                throw new IllegalArgumentException(String.format("%s was not a recognized UPDOG provider", provider));
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        // Blank override
    }
}
