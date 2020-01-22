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

@Component
public class MinimalLoadRunner implements CommandLineRunner, ApplicationContextAware {

    private Updog updog;
    @Value("${provider}")
    private String provider;
    @Value("${data.directory}")
    private String dataDirectory;
    private Path updogDirectory;

    @Autowired
    public MinimalLoadRunner(Updog updog) {
        this.updog = updog;
    }

    @Value("${data.directory}")
    private String finderRootDir;

    @Override
    public void run(String... args) throws Exception {
        OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();
        parser.accepts("loadUniversalRefactor");
        parser.accepts("provider").withRequiredArg();
        parser.accepts("dataDirectory").withRequiredArg();
        OptionSet options = parser.parse(args);

        dataDirectory = options.valueOf("dataDirectory").toString();
        provider = options.valueOf("provider").toString();
        updogDirectory = Paths.get(dataDirectory, "/data/UPDOG", provider);

        if (options.has("loadUniversalRefactor")) {
            updog.run(updogDirectory, provider);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        // Blank override
    }
}
