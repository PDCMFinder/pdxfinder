package org.pdxfinder.dataloaders;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.pdxfinder.dataloaders.updog.Updog;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class MinimalLoadRunner implements CommandLineRunner, ApplicationContextAware {

    @Value("${pdxfinder.root.dir}")
    private String finderRootDir;

    @Override
    public void run(String... args) throws Exception {
        OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();
        parser.accepts("loadUniversalRefactor");
        OptionSet options = parser.parse(args);
        finderRootDir = UniversalLoader.stripTrailingSlash(finderRootDir);

        String provider;
        provider = "UOC-BC";

        if (options.has("loadUniversalRefactor")) {
            Updog updog = new Updog(provider);
            updog.run();
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        // Blank override
    }
}
