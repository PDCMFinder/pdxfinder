package org.pdxfinder.commands;

import org.apache.commons.cli.*;
import org.neo4j.ogm.json.JSONArray;
import org.neo4j.ogm.json.JSONObject;
import org.neo4j.ogm.session.Session;
import org.pdxfinder.dao.*;
import org.pdxfinder.repositories.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Date;
import java.util.Iterator;
import java.util.stream.Stream;

/**
 * Load data from JAX.
 */
@Component
@Order(value = Ordered.LOWEST_PRECEDENCE)
public class LoadJAXData implements CommandLineRunner {

    private final static Logger log = LoggerFactory.getLogger(LoadJAXData.class);

    private final static String JAX_DATASOURCE_ABBREVIATION = "JAX";
    private final static String JAX_DATASOURCE_NAME = "The Jackson Laboratory";
    private final static String JAX_DATASOURCE_DESCRIPTION = "The Jackson Laboratory PDX mouse models.";
    private final static String NSG_BS_NAME = "NSG (NOD scid gamma)";
    private final static String NSG_BS_SYMBOL = "NOD.Cg-Prkdc<sup>scid</sup> Il2rg<sup>tm1Wjl</sup>/SzJ"; //yay HTML in name
    private final static String NSG_BS_URL = "http://jax.org/strain/005557";

    private BackgroundStrain nsgBS;
    private ExternalDataSource jaxDS;

    private Options options;
    private CommandLineParser parser;
    private CommandLine cmd;
    private HelpFormatter formatter;

    private TumorTypeRepository tumorTypeRepository;
    private BackgroundStrainRepository backgroundStrainRepository;
    private ImplantationTypeRepository implantationTypeRepository;
    private ImplantationSiteRepository implantationSiteRepository;
    private ExternalDataSourceRepository externalDataSourceRepository;
    private PatientRepository patientRepository;
    private PdxStrainRepository pdxStrainRepository;
    private TissueRepository tissueRepository;
    private SampleRepository sampleRepository;
    private Session session;

    @Value("${jaxpdx.file}")
    private String file;

    @Value("${jaxpdx.url}")
    private String urlStr;

    @PostConstruct
    public void init() {
        options = new Options();
        parser = new DefaultParser();
        formatter = new HelpFormatter();
        log.info("Setting up LoadJAXDataCommand option");
    }

    public LoadJAXData(SampleRepository sampleRepository, TissueRepository tissueRepository, PdxStrainRepository pdxStrainRepository, PatientRepository patientRepository, ExternalDataSourceRepository externalDataSourceRepository, TumorTypeRepository tumorTypeRepository, BackgroundStrainRepository backgroundStrainRepository, ImplantationSiteRepository implantationSiteRepository, ImplantationTypeRepository implantationTypeRepository, Session session) {

        Assert.notNull(patientRepository);
        Assert.notNull(pdxStrainRepository);
        Assert.notNull(tissueRepository);
        Assert.notNull(sampleRepository);
        Assert.notNull(externalDataSourceRepository);
        Assert.notNull(tumorTypeRepository);
        Assert.notNull(backgroundStrainRepository);
        Assert.notNull(implantationSiteRepository);
        Assert.notNull(implantationTypeRepository);
        Assert.notNull(session);

        this.patientRepository = patientRepository;
        this.pdxStrainRepository = pdxStrainRepository;
        this.tissueRepository = tissueRepository;
        this.sampleRepository = sampleRepository;

        this.externalDataSourceRepository = externalDataSourceRepository;
        this.tumorTypeRepository = tumorTypeRepository;
        this.backgroundStrainRepository = backgroundStrainRepository;
        this.implantationSiteRepository = implantationSiteRepository;
        this.implantationTypeRepository = implantationTypeRepository;

        this.session = session;

    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {

        log.info(args[0]);

        if ("loadJAX".equals(args[0]) || "-loadJAX".equals(args[0])) {

            log.info("Loading JAX PDX data.");
            try {
                cmd = parser.parse(options, args);

            } catch (UnrecognizedOptionException | MissingArgumentException e) {
                formatter.printHelp("load", options);
                System.exit(1);
            }

            // Delete all(?how?) data currently associated to this data source
            ExternalDataSource jaxDS = externalDataSourceRepository.findByAbbreviation(JAX_DATASOURCE_ABBREVIATION);
            if (jaxDS != null) {
                externalDataSourceRepository.delete(jaxDS);
                // delete all associated data....
            }
            this.jaxDS = createJAXDataSource();
            this.nsgBS = createNSGMouse();

            if (urlStr != null) {
                log.info("Loading from URL " + urlStr);
                parseJSON(parseURL(urlStr));
            } else if (file != null) {
                log.info("Loading from file " + file);
                parseJSON(parseFile(file));
            } else {
                log.error("No jaxpdx.file or jaxpdx.url provided in properties");
            }
        }

    }

    private String parseURL(String urlStr) {
        StringBuilder sb = new StringBuilder();

        try {
            URL url = new URL(urlStr);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(url.openStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                sb.append(inputLine);
            }
            in.close();
        } catch (Exception e) {
            log.error("Unable to read from URL " + urlStr, e);
        }
        return sb.toString();
    }

    private String parseFile(String path) {

        StringBuilder sb = new StringBuilder();

        try {
            Stream<String> stream = Files.lines(Paths.get(path));

            Iterator itr = stream.iterator();
            while (itr.hasNext()) {
                sb.append(itr.next());
            }
        } catch (Exception e) {
            log.error("Failed to load file " + path, e);
        }
        return sb.toString();
    }

    private void parseJSON(String json) {

        // {"Model ID","Gender","Age","Race","Ethnicity","Specimen Site","Primary Site","Initial Diagnosis","Clinical Diagnosis",
        //  "Sample Type","Grades","Sample Stage","Markers","Sample Type","Strain","Mouse Sex","Engraftment Site"};
        try {
            JSONObject job = new JSONObject(json);
            JSONArray jarray = job.getJSONArray("pdxInfo");
            String id = "";
            for (int i = 0; i < jarray.length(); i++) {
                JSONObject j = jarray.getJSONObject(i);

                Patient p = createPatient("JAX " + i, j.getString("Gender"), j.getString("Age"), j.getString("Race"), j.getString("Ethnicity"));
                Tissue originSite = createTissue(j.getString("Specimen Site"));
                Tissue primarySite = createTissue(j.getString("Primary Site"));
                TumorType tumorType = createTumorType(j.getString("Sample Type"));
                ImplantationSite is = createImplantationSite(j.getString("Engraftment Site"));
                ImplantationType it = createImplantationType(j.getString("Sample Type"));
                String classification = j.getString("Sample Stage") + "/" + j.getString("Grades");

                Sample sample = createTumor("JAX " + i, tumorType, j.getString("Clinical Diagnosis"), originSite, primarySite, classification, jaxDS);
                id = j.getString("Model ID");
                
                // models IDs that are numeric should start with 'TM' then the value padded to 5 digits with leading 0s
                try {
                    id = "TM" + String.format("%05d", new Integer(j.getString("Model ID")));
                } catch (Exception e) {
                    // a J#### model
                }

                // for JAX, passages are associated with samples, but i think valid modles are all passaged 3 times
                createPDXStrain(id, is, it, sample, this.nsgBS, "3");
               
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private PdxStrain createPDXStrain(String pdxId, ImplantationSite implantationSite, ImplantationType implantationType, Sample sample, BackgroundStrain backgroundStrain, String passage) {
        
        PdxStrain pdxStrain = pdxStrainRepository.findBySourcePdxId(pdxId);
        if(pdxStrain != null){
            log.info("Deleting existing PdxStrain "+pdxId);
            pdxStrainRepository.delete(pdxStrain);
        }
        pdxStrain = new PdxStrain(pdxId, implantationSite, implantationType, sample, backgroundStrain, passage);
        pdxStrainRepository.save(pdxStrain);
        return pdxStrain;
    }

    private Sample createTumor(String id, TumorType tumorType, String diagnosis, Tissue originSite, Tissue primarySite, String classification, ExternalDataSource externalDataSource) {

        Sample sample = sampleRepository.findBySourceSampleId(id);
        if (sample != null) {
            log.info("Deleting existing sample " + id);
            sampleRepository.delete(sample);
        }
        sample = new Sample(id, tumorType, diagnosis, originSite, primarySite, classification, externalDataSource);
        sampleRepository.save(sample);
        return sample;
    }

    private ExternalDataSource createJAXDataSource() {
        ExternalDataSource jaxDS = externalDataSourceRepository.findByAbbreviation(JAX_DATASOURCE_ABBREVIATION);
        if (jaxDS == null) {
            log.info("External data source '{}' not found. Creating", JAX_DATASOURCE_ABBREVIATION);
            jaxDS = new ExternalDataSource(
                    JAX_DATASOURCE_NAME,
                    JAX_DATASOURCE_ABBREVIATION,
                    JAX_DATASOURCE_DESCRIPTION,
                    Date.from(Instant.now()));
            externalDataSourceRepository.save(jaxDS);
        }

        return jaxDS;

    }

    private BackgroundStrain createNSGMouse() {
        BackgroundStrain nsgMouse = backgroundStrainRepository.findByName(NSG_BS_NAME);
        if (nsgMouse == null) {
            log.info("NSG Mouse '{}' not found. Creating", NSG_BS_NAME);
            nsgMouse = new BackgroundStrain(NSG_BS_SYMBOL, NSG_BS_NAME, NSG_BS_NAME, NSG_BS_URL);
            backgroundStrainRepository.save(nsgMouse);
        }
        return nsgMouse;
    }

    private Patient createPatient(String externalId, String sex, String age, String race, String ethnicity) {
        Patient patient = patientRepository.findByExternalId(externalId);
        if (patient == null) {
            log.info("Patient '{}' not found. Creating", externalId);
            patient = new Patient(externalId, sex, age, race, ethnicity, jaxDS);
            patientRepository.save(patient);
        }

        return patient;
    }

    private ImplantationSite createImplantationSite(String iSite) {
        ImplantationSite site = implantationSiteRepository.findByName(iSite);
        if (site == null) {
            log.info("Implantation Site '{}' not found. Creating.", iSite);
            site = new ImplantationSite(iSite);
            implantationSiteRepository.save(site);
        }

        return site;
    }

    private ImplantationType createImplantationType(String iType) {
        ImplantationType type = implantationTypeRepository.findByName(iType);
        if (type == null) {
            log.info("Implantation Site '{}' not found. Creating.", iType);
            type = new ImplantationType(iType);
            implantationTypeRepository.save(type);
        }

        return type;
    }

    private Tissue createTissue(String t) {
        Tissue tissue = tissueRepository.findByName(t);
        if (tissue == null) {
            log.info("Tissue '{}' not found. Creating.", t);
            tissue = new Tissue(t);
            tissueRepository.save(tissue);
        }

        return tissue;
    }

    private TumorType createTumorType(String name) {
        TumorType tumorType = tumorTypeRepository.findByName(name);
        if (tumorType == null) {
            log.info("TumorType '{}' not found. Creating.", name);
            tumorType = new TumorType(name);
            tumorTypeRepository.save(tumorType);
        }

        return tumorType;
    }

}
