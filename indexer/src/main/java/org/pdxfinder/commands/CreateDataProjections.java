package org.pdxfinder.commands;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.neo4j.ogm.json.JSONObject;
import org.pdxfinder.dao.*;
import org.pdxfinder.utilities.LoaderUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.*;

/*
 * Created by csaba on 09/03/2018.
 */
@Component
@Order(value = 99)
public class CreateDataProjections implements CommandLineRunner{

    private final static Logger log = LoggerFactory.getLogger(CreateDataProjections.class);
    private LoaderUtils loaderUtils;

    @Value("${user.home}")
    String homeDir;

    //"platform"=>"marker"=>"variant"=>"set of model ids"
    private Map<String, Map<String, Map<String, Set<String>>>> mutatedPlatformMarkerVariantModelDP = new HashMap<>();

    //"marker"=>"set of variants"
    private Map<String, Set<String>> mutatedMarkerVariantDP = new HashMap<>();

    //"platform"=>"marker"=>"set of model ids"
    private Map<String, Map<String, Set<String>>> wtMarkersDataProjection = new HashMap<>();


    @Autowired
    public CreateDataProjections(LoaderUtils loaderUtils) {
        this.loaderUtils = loaderUtils;
    }

    @Override
    public void run(String... args) throws Exception {

        OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();
        parser.accepts("createDataProjections", "Creating data projections");
        parser.accepts("loadALL", "Load all, including linking samples to NCIT terms");
        parser.accepts("loadSlim", "Load slim, then link samples to NCIT terms");

        OptionSet options = parser.parse(args);
        long startTime = System.currentTimeMillis();

        if (options.has("createDataProjections") || options.has("loadALL")  || options.has("loadSlim")) {

            log.info("Creating data projections");

            createMutationDataProjection();

        }

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        int seconds = (int) (totalTime / 1000) % 60;
        int minutes = (int) ((totalTime / (1000 * 60)) % 60);

        log.info(this.getClass().getSimpleName() + " finished after " + minutes + " minute(s) and " + seconds + " second(s)");

    }




    private void createMutationDataProjection(){

        Collection<MolecularCharacterization> mutatedMolchars = loaderUtils.getMolCharsByType("mutation");

        log.info("Looking at "+mutatedMolchars.size()+" MolChar objects. This may take a while folks...");

        int count = 0;
        for(MolecularCharacterization mc:mutatedMolchars){



            ModelCreation model = loaderUtils.getModelByMolChar(mc);

            String modelId = model.getId().toString();

            String platformName = "Not Specified";

            if(mc.getPlatform() != null && mc.getPlatform().getName() != null && !mc.getPlatform().getName().isEmpty()){

                platformName = mc.getPlatform().getName();
            }

            Set<MarkerAssociation> mas = loaderUtils.getMarkerAssocsByMolChar(mc);

            if(mas != null){


                for(MarkerAssociation ma: mas){


                    Marker m = ma.getMarker();

                    if(m != null){

                        String variantName = ma.getAminoAcidChange();
                        String markerName = m.getName();

                        if(variantName != null && !variantName.isEmpty() && markerName != null && !markerName.isEmpty()){


                            addToMutatedPlatformMarkerVariantModelDP(platformName, markerName, variantName, modelId);
                            addToMutatedMarkerVariantDP(markerName, variantName);

                        }

                    }
                    count++;
                    if(count%10000 == 0) {log.info("Processed "+count+" MA objects");}
                    //if (count > 40000) break;
                }

            }

        }



        log.info("Saving DataProjection");

        DataProjection pmvmDP = loaderUtils.getDataProjectionByLabel("PlatformMarkerVariantModel");

        if (pmvmDP == null){

            pmvmDP = new DataProjection();
            pmvmDP.setLabel("PlatformMarkerVariantModel");
        }

        DataProjection mvDP = loaderUtils.getDataProjectionByLabel("MarkerVariant");

        if(mvDP == null){

            mvDP = new DataProjection();
            mvDP.setLabel("MarkerVariant");
        }

        JSONObject j1 ,j2;

        try{

            j1 = new JSONObject(this.mutatedPlatformMarkerVariantModelDP.toString());
            j2 = new JSONObject(this.mutatedMarkerVariantDP.toString());

            pmvmDP.setValue(j1.toString());
            mvDP.setValue(j2.toString());

        }
        catch(Exception e){

            e.printStackTrace();
        }


        loaderUtils.saveDataProjection(pmvmDP);
        loaderUtils.saveDataProjection(mvDP);

        /*
        try {
            Files.write(Paths.get(homeDir + "/PDX/mutated.json"), createJsonString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        */

    }

    /**
     *
     * Adds platforms, markers, variants and models to a nested structure
     * "platform"=>"marker"=>"variation"=>"set of model ids"
     * Map<String, Map<String, Map<String, Set<String>>>>
     *
     * @param platformName
     * @param markerName
     * @param variantName
     * @param modelId
     */
    private void addToMutatedPlatformMarkerVariantModelDP(String platformName, String markerName, String variantName, String modelId){

        if(this.mutatedPlatformMarkerVariantModelDP.containsKey(platformName)){

            if(this.mutatedPlatformMarkerVariantModelDP.get(platformName).containsKey(markerName)){

                if(this.mutatedPlatformMarkerVariantModelDP.get(platformName).get(markerName).containsKey(variantName)){

                    this.mutatedPlatformMarkerVariantModelDP.get(platformName).get(markerName).get(variantName).add(modelId);
                }
                //platform and marker is there, variant is missing
                else{

                    Set<String> models = new HashSet<>(Arrays.asList(modelId));

                    this.mutatedPlatformMarkerVariantModelDP.get(platformName).get(markerName).put(variantName, models);
                }
            }
            //platform is there, marker is missing
            else{

                Set<String> models = new HashSet<>(Arrays.asList(modelId));

                Map<String, Set<String>> variants = new HashMap<>();
                variants.put(variantName, models);

                this.mutatedPlatformMarkerVariantModelDP.get(platformName).put(markerName, variants);
            }
        }
        //if the platform is missing, combine all keys
        else{

            Set<String> models = new HashSet<>(Arrays.asList(modelId));

            Map<String, Set<String>> variants = new HashMap<>();
            variants.put(variantName, models);

            Map<String, Map<String, Set<String>>> markers = new HashMap<>();
            markers.put(markerName, variants);

            this.mutatedPlatformMarkerVariantModelDP.put(platformName, markers);
        }

    }


    /**
     * Inserts a variant for a marker
     *
     * @param markerName
     * @param variantName
     */
    private void addToMutatedMarkerVariantDP(String markerName, String variantName){

        if(this.mutatedMarkerVariantDP.containsKey(markerName)){

            this.mutatedMarkerVariantDP.get(markerName).add(variantName);
        }
        else{

            Set s = new HashSet();
            s.add(variantName);

            this.mutatedMarkerVariantDP.put(markerName, s);
        }

    }



    private String createJsonString(Object jstring){

        try{

            JSONObject json = new JSONObject(jstring);
            return json.toString();
        }
        catch(Exception e){

            e.printStackTrace();
        }

        return "";
    }


}
