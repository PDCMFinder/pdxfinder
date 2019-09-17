package org.pdxfinder.services.ontology;

import org.neo4j.ogm.json.JSONArray;
import org.neo4j.ogm.json.JSONObject;
import org.pdxfinder.graph.dao.OntologyTerm;
import org.pdxfinder.services.DataImportService;
import org.pdxfinder.services.UtilityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

/*
 * Created by csaba on 30/08/2019.
 */


/**
 * Ontolia: ONTology LInking Application
 * <p>
 * This algorithm loads NCIT treatment regimens and links them to leaf treatment nodes/leaf gene products in NCIT.
 */
public class Ontolia {

    private static final String chemicalModifierBranchUrl = "http://purl.obolibrary.org/obo/NCIT_C1932";
    private static final String dietarySupplementBranchUrl = "http://purl.obolibrary.org/obo/NCIT_C1505";
    private static final String drugOrChemByStructBranchUrl = "http://purl.obolibrary.org/obo/NCIT_C1913";
    private static final String industrialAidBranchUrl = "http://purl.obolibrary.org/obo/NCIT_C45678";
    private static final String pharmaSubstanceBranchUrl = "http://purl.obolibrary.org/obo/NCIT_C1909";
    private static final String physiologyBranchUrl = "http://purl.obolibrary.org/obo/NCIT_C1899";
    private static final String hematopoieticBranchUrl = "http://purl.obolibrary.org/obo/NCIT_C15431";
    private static final String therapeuticProceduresBranchUrl = "http://purl.obolibrary.org/obo/NCIT_C49236 ";


    private static final String regimenBranchUrl = "http://purl.obolibrary.org/obo/NCIT_C12218";
    private static final String geneProductBranchUrl = "http://purl.obolibrary.org/obo/NCIT_C26548";

    private static final String ontologyUrl = "https://www.ebi.ac.uk/ols/api/ontologies/ncit/terms/";


    private List<String> unlinkedRegimens;
    private List<String> unlinkedRegimensSynonyms;
    private List<String> unlinkedRegimensWithReason;
    private List<String> linkedRegimens;
    private Map<String, Set<OntologyTerm>> linkedRegimenLabelsToTerms;


    private Map<String, OntologyTerm> loadedTreatmentTerms;
    private Map<String, OntologyTerm> loadedTreatmentTermsSynonyms;
    private Map<String, OntologyTerm> loadedRegimenTerms;

    private UtilityService utilityService;
    private DataImportService dataImportService;


    private List<String> customRegimenList = new ArrayList<>(Arrays.asList("http://purl.obolibrary.org/obo/NCIT_C11197"));

    private final static Logger log = LoggerFactory.getLogger(Ontolia.class);


    public Ontolia(UtilityService utilityService, DataImportService dataImportService) {

        this.utilityService = utilityService;
        this.dataImportService = dataImportService;

        loadedTreatmentTerms = new HashMap<>();
        loadedTreatmentTermsSynonyms = new HashMap<>();
        loadedRegimenTerms = new HashMap<>();
        linkedRegimenLabelsToTerms = new HashMap<>();


    }


    public void run() {


        log.info("Loading Chemical Modifiers from NCIT.");
        loadNCITTermsFromBranch("treatment", chemicalModifierBranchUrl, true);

        log.info("Loading Dietary Supplements from NCIT.");
        loadNCITTermsFromBranch("treatment", dietarySupplementBranchUrl, true);

        log.info("Loading Drug or Chem from NCIT.");
        loadNCITTermsFromBranch("treatment", drugOrChemByStructBranchUrl, true);

        log.info("Loading Industrial Aids from NCIT.");
        loadNCITTermsFromBranch("treatment", industrialAidBranchUrl, true);

        log.info("Loading Pharma Substance from NCIT.");
        loadNCITTermsFromBranch("treatment", pharmaSubstanceBranchUrl, true);

        log.info("Loading Physiology-Regulatory factors from NCIT.");
        loadNCITTermsFromBranch("treatment", physiologyBranchUrl, true);

        log.info("Loading Gene Products from NCIT.");
        loadNCITTermsFromBranch("treatment", geneProductBranchUrl, true);

        log.info("Loading Hematopoietic Cell Transplantations from NCIT.");
        loadNCITTermsFromBranch("treatment", hematopoieticBranchUrl, true);

        log.info("Loading Therapeutic Procedures from NCIT.");
        loadNCITTermsFromBranch("treatment", therapeuticProceduresBranchUrl, true);

        log.info("Loading regimens from NCIT.");
        loadNCITLeafDrugs("treatment regimen", regimenBranchUrl, false);

        log.info("Loading custom regimens from NCIT");
        loadCustomList("treatment regimen", customRegimenList, false);

        log.info("Linking drug regimens to individual drugs");
        linkRegimens();

        log.info("Saving treatment links to regimens");
        saveRegimensWithTreatments();
    }

    private void loadNCITTermsFromBranch(String type, String branchRootUrl, boolean mapSynonyms) {

        int totalDrugs = 0;
        int totalPages = 0;
        boolean totalPagesDetermined = false;


        for (int currentPage = 0; currentPage <= totalPages; currentPage++) {

            String encodedTermUrl = "";
            try {
                //have to double encode the url to get the desired result
                encodedTermUrl = URLEncoder.encode(branchRootUrl, "UTF-8");
                encodedTermUrl = URLEncoder.encode(encodedTermUrl, "UTF-8");

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            String url = ontologyUrl + encodedTermUrl + "/hierarchicalDescendants?size=500&page=" + currentPage;

            String json = utilityService.parseURL(url);

            try {
                JSONObject job = new JSONObject(json);

                String embedded = job.getString("_embedded");

                JSONObject job2 = new JSONObject(embedded);
                JSONArray terms = job2.getJSONArray("terms");

                for (int j = 0; j < terms.length(); j++) {

                    JSONObject term = terms.getJSONObject(j);

                    boolean hasChildren = Boolean.parseBoolean(term.getString("has_children"));

                    //if the current node has previously been created, skip
                    if (type.equals("treatment") && loadedTreatmentTerms.containsKey(term.getString("label").toLowerCase()))
                        continue;
                    if (type.equals("treatment regimen") && loadedRegimenTerms.containsKey(term.getString("label").toLowerCase()))
                        continue;

                    OntologyTerm ot = new OntologyTerm();
                    ot.setType(type);
                    ot.setLabel(term.getString("label"));
                    ot.setUrl(term.getString("iri"));
                    ot.setAllowAsSuggestion(false);

                    if (term.has("description")) {
                        ot.setDescription(term.getString("description"));
                    }

                    if (term.has("synonyms")) {
                        JSONArray synonyms = term.getJSONArray("synonyms");
                        Set<String> synonymsSet = new HashSet<>();

                        for (int i = 0; i < synonyms.length(); i++) {
                            synonymsSet.add(synonyms.getString(i));
                        }

                        ot.setSynonyms(synonymsSet);

                        if (mapSynonyms) {

                            for (int i = 0; i < synonyms.length(); i++) {
                                loadedTreatmentTermsSynonyms.put(synonyms.getString(i).toLowerCase(), ot);
                            }
                        }
                    }

                    OntologyTerm savedOt = dataImportService.saveOntologyTerm(ot);

                    if (type.equals("treatment")) loadedTreatmentTerms.put(ot.getLabel().toLowerCase(), savedOt);
                    if (type.equals("treatment regimen")) loadedRegimenTerms.put(ot.getLabel().toLowerCase(), savedOt);


                    totalDrugs++;

                    if (totalDrugs != 0 && totalDrugs % 500 == 0) {
                        log.info("Loaded " + totalDrugs + " drugs from NCIT.");
                    }

                }

                if (!totalPagesDetermined) {
                    String page = job.getString("page");
                    JSONObject pageObj = new JSONObject(page);
                    totalPages = Integer.parseInt(pageObj.getString("totalPages")) - 1;
                    totalPagesDetermined = true;
                }


            } catch (Exception e) {
                log.error("", e);

            }

        }

        log.info("Finished loading " + totalDrugs + " drugs from NCIT.");
    }

    private void loadNCITLeafDrugs(String type, String branchRootUrl, boolean mapSynonyms) {

        int totalDrugs = 0;
        int totalPages = 0;
        boolean totalPagesDetermined = false;


        for (int currentPage = 0; currentPage <= totalPages; currentPage++) {

            String encodedTermUrl = "";
            try {
                //have to double encode the url to get the desired result
                encodedTermUrl = URLEncoder.encode(branchRootUrl, "UTF-8");
                encodedTermUrl = URLEncoder.encode(encodedTermUrl, "UTF-8");

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            String url = ontologyUrl + encodedTermUrl + "/hierarchicalDescendants?size=500&page=" + currentPage;

            String json = utilityService.parseURL(url);

            try {
                JSONObject job = new JSONObject(json);

                String embedded = job.getString("_embedded");

                JSONObject job2 = new JSONObject(embedded);
                JSONArray terms = job2.getJSONArray("terms");

                for (int j = 0; j < terms.length(); j++) {

                    JSONObject term = terms.getJSONObject(j);

                    boolean hasChildren = Boolean.parseBoolean(term.getString("has_children"));

                    if (!hasChildren) {

                        //if the current node has previously been created, skip
                        if (type.equals("treatment") && loadedTreatmentTerms.containsKey(term.getString("label").toLowerCase()))
                            continue;
                        if (type.equals("treatment regimen") && loadedRegimenTerms.containsKey(term.getString("label").toLowerCase()))
                            continue;

                        OntologyTerm ot = new OntologyTerm();
                        ot.setType(type);
                        ot.setLabel(term.getString("label"));
                        ot.setUrl(term.getString("iri"));
                        ot.setAllowAsSuggestion(false);

                        if (term.has("description")) {
                            ot.setDescription(term.getString("description"));
                        }

                        if (term.has("synonyms")) {
                            JSONArray synonyms = term.getJSONArray("synonyms");
                            Set<String> synonymsSet = new HashSet<>();

                            for (int i = 0; i < synonyms.length(); i++) {
                                synonymsSet.add(synonyms.getString(i));
                            }

                            ot.setSynonyms(synonymsSet);

                            if (mapSynonyms) {

                                for (int i = 0; i < synonyms.length(); i++) {
                                    loadedTreatmentTermsSynonyms.put(synonyms.getString(i).toLowerCase(), ot);
                                }
                            }
                        }

                        OntologyTerm savedOt = dataImportService.saveOntologyTerm(ot);

                        if (type.equals("treatment")) loadedTreatmentTerms.put(ot.getLabel().toLowerCase(), savedOt);
                        if (type.equals("treatment regimen"))
                            loadedRegimenTerms.put(ot.getLabel().toLowerCase(), savedOt);


                        totalDrugs++;

                    }

                    if (totalDrugs != 0 && totalDrugs % 500 == 0) {
                        log.info("Loaded " + totalDrugs + " drugs from NCIT.");
                    }


                }

                if (!totalPagesDetermined) {
                    String page = job.getString("page");
                    JSONObject pageObj = new JSONObject(page);
                    totalPages = Integer.parseInt(pageObj.getString("totalPages")) - 1;
                    totalPagesDetermined = true;
                }


            } catch (Exception e) {
                log.error("", e);

            }

        }

        log.info("Finished loading " + totalDrugs + " drugs from NCIT.");
    }


    private void loadCustomList(String type, List<String> list, boolean mapSynonyms) {


        for (String termUrl : list) {

            String encodedTermUrl = "";
            try {
                //have to double encode the url to get the desired result
                encodedTermUrl = URLEncoder.encode(termUrl, "UTF-8");
                encodedTermUrl = URLEncoder.encode(encodedTermUrl, "UTF-8");

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            String url = ontologyUrl + encodedTermUrl;

            String json = utilityService.parseURL(url);

            try {
                JSONObject term = new JSONObject(json);


                OntologyTerm ot = new OntologyTerm();
                ot.setType(type);
                ot.setLabel(term.getString("label"));
                ot.setUrl(term.getString("iri"));
                ot.setAllowAsSuggestion(false);

                if (term.has("description")) {
                    ot.setDescription(term.getString("description"));
                }

                if (term.has("synonyms")) {
                    JSONArray synonyms = term.getJSONArray("synonyms");
                    Set<String> synonymsSet = new HashSet<>();

                    for (int i = 0; i < synonyms.length(); i++) {
                        synonymsSet.add(synonyms.getString(i));
                    }

                    ot.setSynonyms(synonymsSet);

                    if (mapSynonyms) {

                        for (int i = 0; i < synonyms.length(); i++) {
                            loadedTreatmentTermsSynonyms.put(synonyms.getString(i).toLowerCase(), ot);
                        }
                    }
                }

                OntologyTerm savedOt = dataImportService.saveOntologyTerm(ot);

                if (type.equals("treatment")) loadedTreatmentTerms.put(ot.getLabel().toLowerCase(), savedOt);
                if (type.equals("treatment regimen")) loadedRegimenTerms.put(ot.getLabel().toLowerCase(), savedOt);


            } catch (Exception e) {

                e.printStackTrace();
            }
        }
    }

    private void linkRegimens() {

        unlinkedRegimens = new ArrayList<>();
        unlinkedRegimensWithReason = new ArrayList<>();
        linkedRegimens = new ArrayList<>();
        unlinkedRegimensSynonyms = new ArrayList<>();

        int regimenNumber = dataImportService.getOntologyTermNumberByType("treatment regimen");
        int batch = 100;

        int i;
        for (i = 0; i < regimenNumber; i += batch) {

            Collection<OntologyTerm> regimens = dataImportService.getAllOntologyTermsByTypeFromTo("treatment regimen", i, batch);

            for (OntologyTerm regimen : regimens) {

                linkRegimenToTreatments(regimen);
            }

            log.info("Linked regimens: " + Integer.toString(i + batch));
        }


        System.out.println("***********************************");

        for (int j = 0; j < unlinkedRegimensWithReason.size(); j++) {
            System.out.println(unlinkedRegimensWithReason.get(j));

        }
        System.out.println("***********************************");

        System.out.println();
        System.out.println("Linked regimens: " + linkedRegimens.size());
        System.out.println("Unlinked regimens: " + unlinkedRegimens.size());


    }


    private void linkRegimenToTreatments(OntologyTerm regimen) {


        Set<String> synonyms = regimen.getSynonyms();

        OntoliaMatrix slashOntoliaMatrix = new OntoliaMatrix("/", loadedTreatmentTerms, loadedTreatmentTermsSynonyms);
        OntoliaMatrix dashOntoliaMatrix = new OntoliaMatrix("-", loadedTreatmentTerms, loadedTreatmentTermsSynonyms);

        for (String synonym : synonyms) {

            String[] synSlashPre = synonym.split("/");
            String[] synDash = synonym.split("-");

            String[] synSlash = splitCombos(synSlashPre);

            slashOntoliaMatrix.addMatrixRow(synSlash);
            dashOntoliaMatrix.addMatrixRow(synDash);

        }

        OntoliaMatrixRow slashOMR = slashOntoliaMatrix.getBestCompleteMatch();
        OntoliaMatrixRow dashOMR = dashOntoliaMatrix.getBestCompleteMatch();

        OntoliaMatrixRow completeMatch = null;


        if (slashOMR != null && dashOMR != null && slashOMR.getMatchScore() >= dashOMR.getMatchScore())
            completeMatch = slashOMR;
        if (slashOMR != null && dashOMR != null && slashOMR.getMatchScore() < dashOMR.getMatchScore())
            completeMatch = dashOMR;
        if (slashOMR == null && dashOMR != null) completeMatch = dashOMR;
        if (slashOMR != null && dashOMR == null) completeMatch = slashOMR;

        if (completeMatch != null) {

            linkedRegimens.add(regimen.getLabel());
            linkedRegimenLabelsToTerms.put(regimen.getLabel(), new HashSet<>(completeMatch.getMatchedTerms()));
        } else {
            //we couldn't match all synonyms to a term

            slashOMR = slashOntoliaMatrix.getBestMatch();
            dashOMR = dashOntoliaMatrix.getBestMatch();

            OntoliaMatrixRow bestMatch = null;


            if (slashOMR != null && dashOMR != null && slashOMR.getMatchScore() >= dashOMR.getMatchScore())
                bestMatch = slashOMR;
            if (slashOMR != null && dashOMR != null && slashOMR.getMatchScore() < dashOMR.getMatchScore())
                bestMatch = dashOMR;
            if (slashOMR == null && dashOMR != null) bestMatch = dashOMR;
            if (slashOMR != null && dashOMR == null) bestMatch = slashOMR;

            String matchString = "NOT AVAILABLE";

            if (bestMatch != null) {
                matchString = bestMatch.getRowString();
            }


            unlinkedRegimens.add(regimen.getLabel());
            unlinkedRegimensWithReason.add(regimen.getLabel() + " => " + matchString);


        }


    }


    private void saveRegimensWithTreatments() {

        for (Map.Entry<String, Set<OntologyTerm>> entry : linkedRegimenLabelsToTerms.entrySet()) {

            String regimenLabel = entry.getKey();

            OntologyTerm regimen = loadedRegimenTerms.get(regimenLabel.toLowerCase());

            try {
                regimen.setSubclassOf(entry.getValue());
                dataImportService.saveOntologyTerm(regimen);
            } catch (NullPointerException e) {
                log.error("Error saving " + regimenLabel);
                log.error("");
            }
        }


    }


    private String getCleanLabel(String label) {

        String cleanLabel = label.toLowerCase();
        cleanLabel = cleanLabel.replaceAll("regimen", "");
        cleanLabel = cleanLabel.replaceAll("high dose", "");
        cleanLabel = cleanLabel.replaceAll("low-dose", "");
        cleanLabel = cleanLabel.replaceAll("low dose", "");
        cleanLabel = cleanLabel.replaceAll("dose-dense", "");
        cleanLabel = cleanLabel.replaceAll("high-dose", "");
        cleanLabel = cleanLabel.replaceAll("pulse intense", "");
        cleanLabel = cleanLabel.replaceAll("intravenous", "");
        cleanLabel = cleanLabel.replaceAll("oral", "");
        cleanLabel = cleanLabel.replaceAll("modified", "");
        cleanLabel = cleanLabel.replaceAll("hyperfractionated", "");
        cleanLabel = cleanLabel.replaceAll("infusional", "");

        cleanLabel = cleanLabel.replaceAll("([^\\s]+\\s+cancer)", "");

        return cleanLabel.trim();
    }


    private String[] splitCombos(String[] combos) {
        //cyclophosphamide followed by paclitaxel + trastuzumab
        List<String> list = new ArrayList<>();

        for (String c : combos) {

            if (c.toLowerCase().contains("followed by")) {

                String[] followedBy = c.toLowerCase().split("followed by");
                list.add(followedBy[0]);

                if (followedBy[1].contains("+")) {

                    String[] plusDrugs = followedBy[1].split("\\+");
                    list.addAll(Arrays.asList(plusDrugs));

                } else if (followedBy[1].contains("/")) {

                    String[] dashDrugs = followedBy[1].split("/");
                    list.addAll(Arrays.asList(dashDrugs));
                } else {
                    list.add(followedBy[1]);
                }

            } else {

                list.add(c);

            }
        }

        return list.stream().toArray(String[]::new);

    }

    /**
     * Returns true if there is no null elements in the list
     * Returns false otherwise
     *
     * @param list
     * @return
     */
    private boolean isAllNotNull(List<OntologyTerm> list) {

        for (OntologyTerm ot : list) {
            if (ot == null) return false;
        }
        return true;
    }

    /**
     * Returns true if in the list has at least 2 elements and
     * there is exactly one null element
     * Returns false otherwise
     *
     * @param list
     * @return
     */
    private boolean isAllNotNullButOne(List<OntologyTerm> list) {

        if (list.size() == 1) return false;

        boolean oneNull = false;

        for (OntologyTerm ot : list) {

            if (ot == null) {
                //this is the first null in the list
                if (oneNull == false) {
                    oneNull = true;
                }
                //this is not the first null in the list
                else {

                    return false;
                }

            }
        }
        return oneNull;
    }


    /**
     * Returns the null element's index in the list
     *
     * @param list
     * @return
     */
    private int getNullPosition(List<OntologyTerm> list) {

        for (int i = 0; i < list.size(); i++) {

            if (list.get(i) == null) return i;
        }

        return -1;
    }


}
