package org.pdxfinder.commands;

import org.apache.commons.cli.*;
import org.neo4j.ogm.json.JSONArray;
import org.neo4j.ogm.json.JSONObject;
import org.neo4j.ogm.session.Session;
import org.pdxfinder.dao.*;
import org.pdxfinder.utilities.LoaderUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
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
    private final static String HISTOLOGY_NOTE = "Pathologist assessment of patient tumor and pdx model tumor histology slides.";
    private final static String ENGRAFTMENT = "Engraftment";

    // for now all samples are of tumor tissue
    private final static Boolean NORMAL_TISSUE_FALSE = false;

    private BackgroundStrain nsgBS;
    private ExternalDataSource jaxDS;

    private Options options;
    private CommandLineParser parser;
    private CommandLine cmd;
    private HelpFormatter formatter;

    private LoaderUtils loaderUtils;
    private Session session;

    @Value("${jaxpdx.file}")
    private String file;

    @Value("${jaxpdx.url}")
    private String urlStr;

    @Value("${jaxpdx.variation.url}")
    private String variationURL;

    @Value("${jaxpdx.histology.url}")
    private String histologyURL;

    @Value("${jaxpdx.variation.max}")
    private int maxVariations;

    @Value("${jaxpdx.ref.assembly}")
    private String refAssembly;

    HashMap<String, String> passageMap = null;
    HashMap<String, Image> histologyMap = null;

    @PostConstruct
    public void init() {
        options = new Options();
        parser = new DefaultParser();
        formatter = new HelpFormatter();
        log.info("Setting up LoadJAXDataCommand option");
        options.addOption("loadJAX", false, "Load Jax PDX data");
        options.addOption(LoaderUtils.loadAll);
    }

    public LoadJAXData(LoaderUtils loaderUtils) {
        this.loaderUtils = loaderUtils;
    }

    @Override
    public void run(String... args) throws Exception {

            try {
                cmd = parser.parse(options, args);


            } catch (UnrecognizedOptionException | MissingArgumentException e) {
                formatter.printHelp("loadJAX", options);
                System.exit(1);
            }

        if (cmd.hasOption("loadJAX") || cmd.hasOption(LoaderUtils.loadAll.getOpt())) {

            System.out.println("Loading JAX PDX data.");
            if (urlStr != null) {
                log.info("Loading from URL " + urlStr);
                parseJSON(parseURL(urlStr));
            } else if (file != null) {
                log.info("Loading from file " + file);
                parseJSON(parseFile(file));
            } else {
                log.error("No jaxpdx.file or jaxpdx.url provided in properties");
            }
        } else {
            System.out.println("Not loading JAX. Use '-loadJAX' switch to load JAX data.");
        }
    }

    //JSON Fields {"Model ID","Gender","Age","Race","Ethnicity","Specimen Site","Primary Site","Initial Diagnosis","Clinical Diagnosis",
    //  "Tumor Type","Grades","Tumor Stage","Markers","Sample Type","Strain","Mouse Sex","Engraftment Site"};
    private void parseJSON(String json) {

        jaxDS = loaderUtils.getExternalDataSource(JAX_DATASOURCE_ABBREVIATION, JAX_DATASOURCE_NAME, JAX_DATASOURCE_DESCRIPTION);
        nsgBS = loaderUtils.getBackgroundStrain(NSG_BS_SYMBOL, NSG_BS_NAME, NSG_BS_NAME, NSG_BS_URL);

        try {
            JSONObject job = new JSONObject(json);
            JSONArray jarray = job.getJSONArray("pdxInfo");

            for (int i = 0; i < jarray.length(); i++) {

                JSONObject j = jarray.getJSONObject(i);

                createGraphObjects(j);
            }

        } catch (Exception e) {
            log.error("Error getting JAX PDX models", e);

        }
    }

    @Transactional
    void createGraphObjects(JSONObject j) throws Exception {
        String id = j.getString("Model ID");

        histologyMap = getHistologyImageMap(id);

        // the preference is for clinical diagnosis but if not available use initial diagnosis
        String diagnosis = j.getString("Clinical Diagnosis");
        if (diagnosis.trim().length() == 0 || "Not specified".equals(diagnosis)) {
            diagnosis = j.getString("Initial Diagnosis");

        }

        String classification = j.getString("Tumor Stage") + "/" + j.getString("Grades");

        PatientSnapshot pSnap = loaderUtils.getPatientSnapshot(j.getString("Patient ID"), j.getString("Gender"),
                j.getString("Race"), j.getString("Ethnicity"), j.getString("Age"), jaxDS);

        Sample sample = loaderUtils.getSample(j.getString("Model ID"), j.getString("Tumor Type"), diagnosis,
                j.getString("Primary Site"), j.getString("Specimen Site"), j.getString("Sample Type"), classification, NORMAL_TISSUE_FALSE, jaxDS);

        if (histologyMap.containsKey("Patient")) {
            Histology histology = new Histology();
            Image image = histologyMap.get("Patient");
            histology.addImage(image);
            sample.addHistology(histology);

        }

        // For the moment, all JAX models are assumed to have been validated using Histological assessment by a pathologist
        // TODO: verify this is the case
        QualityAssurance qa = new QualityAssurance("Histology", HISTOLOGY_NOTE, ValidationTechniques.VALIDATION);
        loaderUtils.saveQualityAssurance(qa);

        pSnap.addSample(sample);
        loaderUtils.savePatientSnapshot(pSnap);

        ModelCreation mc = loaderUtils.createModelCreation(id, j.getString("Engraftment Site"), this.ENGRAFTMENT, sample, nsgBS, qa);

        loadVariationData(mc);

    }


    /*
    Use the modelID in the modelCreation object to get model specific variation data
    This is a set of makers with marker association details
    Since we are creating samples here attach any histology images to the sample based on passage #
     */
    private void loadVariationData(ModelCreation modelCreation) {

        if (maxVariations == 0) {
            return;
        }

        try {

            passageMap = new HashMap<>();

            HashMap<String, HashMap<String, Set<MarkerAssociation>>> sampleMap = new HashMap<>();
            HashMap<String, Set<MarkerAssociation>> markerMap = new HashMap<>();

            JSONObject job = new JSONObject(parseURL(this.variationURL + modelCreation.getSourcePdxId()));
            JSONArray jarray = job.getJSONArray("variation");
            String sample, symbol, id, technology, aaChange, chromosome, seqPosition, refAllele, consequence, rsVariants, readDepth, alleleFrequency, altAllele;
            log.info(jarray.length() + " gene variants for model " + modelCreation.getSourcePdxId());

            // configure the maximum variations to load in properties file
            // loading them all will take a while (hour?)
            int stop = jarray.length();
            if (maxVariations > 0 && maxVariations < jarray.length()) {
                stop = maxVariations;
            }
            for (int i = 0; i < stop;) {
                JSONObject j = jarray.getJSONObject(i);

                sample = j.getString("sample");
                symbol = j.getString("gene symbol");
                id = j.getString("gene id");
                aaChange = j.getString("amino acid change");
                technology = j.getString("platform");
                chromosome = j.getString("chromosome");
                seqPosition = j.getString("seq position");
                refAllele = j.getString("ref allele");
                consequence = j.getString("consequence");
                rsVariants = j.getString("rs variants");
                readDepth = j.getString("read depth");
                alleleFrequency = j.getString("allele frequency");
                altAllele = j.getString("alt allele");

                passageMap.put(sample, j.getString("passage num"));

                // since there are 8 fields assume (incorrectly?) all MAs are unique
                // create a new one rather than look for exisitng one
                MarkerAssociation ma = new MarkerAssociation();

                ma.setAminoAcidChange(aaChange);
                ma.setConsequence(consequence);
                ma.setAlleleFrequency(alleleFrequency);
                ma.setChromosome(chromosome);
                ma.setReadDepth(readDepth);
                ma.setRefAllele(refAllele);
                ma.setAltAllele(altAllele);
                ma.setRefAssembly(refAssembly);
                ma.setRsVariants(rsVariants);
                ma.setSeqPosition(seqPosition);
                ma.setReadDepth(readDepth);

                Marker marker = loaderUtils.getMarker(symbol);
                marker.setEntrezId(id);
                
                ma.setMarker(marker);

                Platform platform = loaderUtils.getPlatform(technology, this.jaxDS);

                // why would this happen?
                if (platform.getExternalDataSource() == null) {
                    platform.setExternalDataSource(jaxDS);
                }
                loaderUtils.createPlatformAssociation(platform, marker);
                

                markerMap = sampleMap.get(sample);
                if (markerMap == null) {
                    markerMap = new HashMap<>();
                }

                // make a map of markerAssociation collections keyed to technology
                if (markerMap.containsKey(technology)) {
                    markerMap.get(technology).add(ma);
                } else {
                    HashSet<MarkerAssociation> set = new HashSet<>();
                    set.add(ma);
                    markerMap.put(technology, set);
                }

                sampleMap.put(sample, markerMap);
                i++;
                if (i % 100 == 0) {
                    System.out.println("loaded " + i + " markers");
                }
            }
            System.out.println("loaded " + stop + " markers for " + modelCreation.getSourcePdxId());

            for (String sampleKey : sampleMap.keySet()) {

                Integer passage = getPassage(sampleKey);
                markerMap = sampleMap.get(sampleKey);

                HashSet<MolecularCharacterization> mcs = new HashSet<>();
                for (String tech : markerMap.keySet()) {
                    MolecularCharacterization mc = new MolecularCharacterization();
                    mc.setPlatform(loaderUtils.getPlatform(tech, this.jaxDS));
                    mc.setMarkerAssociations(markerMap.get(tech));
                    mcs.add(mc);

                }

                PdxPassage pdxPassage = new PdxPassage(modelCreation, passage);

                Specimen specimen = loaderUtils.getSpecimen(sampleKey);
//                specimen.setMolecularCharacterizations(mcs);
                specimen.setPdxPassage(pdxPassage);

                if (histologyMap.containsKey(pdxPassage)) {
                    Histology histology = new Histology();
                    Image image = histologyMap.get(pdxPassage);
                    histology.addImage(image);
                    specimen.addHistology(histology);

                }

                pdxPassage.setModelCreation(modelCreation);

                loaderUtils.savePdxPassage(pdxPassage);
                loaderUtils.saveSpecimen(specimen);
                System.out.println("saved passage " + passage + " for model " + modelCreation.getSourcePdxId() + " from sample " + sampleKey);
            }

        } catch (Exception e) {
            log.error("", e);
        }

    }

    private Integer getPassage(String sample) {
        Integer p = 0;
        try {
            p = new Integer(passageMap.get(sample).replaceAll("P", ""));
        } catch (Exception e) {
            log.info("Unable to determine passage from sample name " + sample + ". Assuming 0");
        }
        return p;

    }

    /*
    For a given model return a map of passage # or "Patient" -> histology image URL
     */
    private HashMap<String, Image> getHistologyImageMap(String id) {
        HashMap<String, Image> map = new HashMap<>();
        try {
            JSONObject job = new JSONObject(parseURL(this.histologyURL + id));
            JSONArray jarray = job.getJSONObject("pdxHistology").getJSONArray("Graphics");
            String comment = job.getJSONObject("pdxHistology").getString("Comment");

            for (int i = 0; i < jarray.length(); i++) {
                job = jarray.getJSONObject(i);
                String desc = job.getString("Description");

                // comments apply to all of a models histology but histologies are passage specific
                // so I guess attach the comment to all image descriptions
                if (comment != null && comment.trim().length() > 0) {
                    String sep = "";
                    if (desc != null && desc.trim().length() > 0) {
                        sep = " : ";
                    }
                    desc = comment + sep + desc;
                }

                String url = job.getString("URL");
                Image img = new Image();
                img.setDescription(desc);
                img.setUrl(url);
                if (desc.startsWith("Patient") || desc.startsWith("Primary")) {
                    map.put("Patient", img);
                } else {
                    String[] parts = desc.split(" ");
                    if (parts[0].startsWith("P")) {
                        try {
                            String passage = new Integer(parts[0].replace("P", "")).toString();
                            map.put(passage, img);
                        } catch (Exception e) {
                            log.info("Can't extract passage from description " + desc);
                        }
                    }
                }
            }

        } catch (Exception e) {
            log.error("Error getting histology for model " + id, e);
        }

        return map;
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

}
