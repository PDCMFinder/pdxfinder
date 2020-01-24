package org.pdxfinder.envload;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.pdxfinder.services.DataImportService;
import org.pdxfinder.services.constants.DataUrl;
import org.pdxfinder.services.loader.envload.LoadMarkers;
import org.pdxfinder.constants.Option;
import org.pdxfinder.services.loader.envload.LoadNCITDrugs;
import org.pdxfinder.services.loader.envload.LoadNCIT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(value = -70)
public class DataCommand implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(LoadMarkers.class);
    private DataImportService dataImportService;
    private LoadMarkers loadMarkers;
    private LoadNCIT loadNCIT;
    private LoadNCITDrugs loadNCITDrugs;

    @Value("${ncitpredef.file}")
    private String ncitFile;

    @Autowired
    public DataCommand(DataImportService dataImportService,
                       LoadMarkers loadMarkers,
                       LoadNCIT loadNCIT,
                       LoadNCITDrugs loadNCITDrugs) {

        this.dataImportService = dataImportService;
        this.loadMarkers = loadMarkers;
        this.loadNCIT = loadNCIT;
        this.loadNCITDrugs = loadNCITDrugs;
    }

    @Override
    public void run(String... args) throws Exception {

        OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();
        parser.accepts(Option.loadMarkers.get());
        parser.accepts(Option.reloadCache.get());
        parser.accepts(Option.loadALL.get());
        parser.accepts(Option.loadEssentials.get());

        parser.accepts(Option.loadNCIT.get());
        parser.accepts(Option.loadNCITDrugs.get());
        parser.accepts(Option.loadNCITPreDef.get());
        parser.accepts(Option.loadSlim.get());

        OptionSet options = parser.parse(args);




        if (options.has(Option.reloadCache.get()) || options.has(Option.loadSlim.get()) ||
                options.has(Option.loadEssentials.get()) || (options.has(Option.loadALL.get()) && dataImportService.countAllMarkers() == 0)) {

            loadMarkers.loadGenes(DataUrl.HUGO_FILE_URL.get());

            loadNCIT.loadOntology(DataUrl.DISEASES_BRANCH_URL.get());

            loadNCITDrugs.loadRegimens();

        } else {

            if (options.has(Option.loadMarkers.get())) {
                loadMarkers.loadGenes(DataUrl.HUGO_FILE_URL.get());
            }

            if (options.has(Option.loadNCIT.get())) {

                loadNCIT.loadOntology(DataUrl.DISEASES_BRANCH_URL.get());
            } else if (options.has(Option.loadNCITPreDef.get())) {

                loadNCIT.loadNCITPreDef(ncitFile);
            }

            if (options.has(Option.loadNCITDrugs.get())) {
                loadNCITDrugs.loadRegimens();
            }

        }


    }
}
