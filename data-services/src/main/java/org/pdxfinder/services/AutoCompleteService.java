package org.pdxfinder.services;

import org.pdxfinder.dao.OntologyTerm;
import org.pdxfinder.repositories.OntologyTermRepository;
import org.pdxfinder.services.ds.AutoSuggestOption;
import org.springframework.stereotype.Service;

import java.util.*;

/*
 * Created by csaba on 05/02/2018.
 */
@Service
public class AutoCompleteService {


    private OntologyTermRepository ontologyTermRepository;


    public AutoCompleteService(OntologyTermRepository ontologyTermRepository) {
        this.ontologyTermRepository = ontologyTermRepository;
    }


    public List<AutoSuggestOption> getAutoSuggestions(){

        String[] doNotDisplay = {"Neoplasm by Morphology",
                "Neoplasm by Site",
                "Glandular Cell Neoplasm",
                "Digestive System Neoplasm",
                "Intestinal Neoplasm",
                "Colorectal Neoplasm",
                "Melanocytic Neoplasm",
                "Respiratory Tract Neoplasm",
                "Thoracic Neoplasm",
                "Lung Neoplasm",
                "Breast Neoplasm",
                "Colon Neoplasm",
                "Skin Neoplasm",
                "Melanocytic Skin Neoplasm",
                "Urinary System Neoplasm",
                "Nervous System Neoplasm",
                "Central Nervous System Neoplasm",
                "Astrocytic Tumor",
                "Bladder Neoplasm",
                "Pancreatic Neoplasm",
                "Bone Marrow Neoplasm",
                "Pancreatic Exocrine Neoplasm",
                "Breast Carcinoma by Gene Expression Profile",
                "Reproductive System Neoplasm",
                "Endocrine Neoplasm",
                "Myomatous Neoplasm",
                "Female Reproductive System Neoplasm",
                "Sarcoma by NCI Grade",
                "Sarcoma NCI Grade 3",
                "Ovarian Neoplasm",
                "Ovarian Epithelial Tumor",
                "Head and Neck Neoplasm",
                "Bone Neoplasm",
                "Skeletal Muscle Neoplasm",
                "Ovarian Serous Tumor",
                "Sarcoma by FNCLCC Grade",
                "FNCLCC Sarcoma Grade 3",
                "Kidney and Ureter Neoplasm",
                "Kidney Neoplasm",
                "Rectal Neoplasm",
                "Smooth Muscle Neoplasm",
                "Soft Tissue Neoplasm",
                "Hepatobiliary Neoplasm",
                "Intraductal Breast Neoplasm",
                "Ampulla of Vater Neoplasm",
                "Intraepithelial Neoplasia",
                "Eye Neoplasm",
                "Male Reproductive System Neoplasm",
                "Uterine Neoplasm",
                "Glandular Cell Intraepithelial Neoplasia",
                "High Grade Glandular Intraepithelial Neoplasia",
                "Uterine Corpus Neoplasm",
                "Uveal Neoplasm",
                "Grade III Glandular Intraepithelial Neoplasia",
                "Soft Tissue Tumor of Uncertain Differentiation",
                "Malignant Soft Tissue Tumor of Uncertain Differentiation",
                "Cecum Neoplasm",
                "Breast Cancer by AJCC v6 Stage",
                "Breast Cancer by AJCC v7 Stage",
                "Stage 0 Breast Cancer AJCC v6 and v7",
                "High Grade Intraepithelial Neoplasia",
                "Neck Neoplasm",
                "Salivary Gland Neoplasm",
                "Acute Myeloid Leukemia Not Otherwise Specified",
                "Lipomatous Neoplasm",
                "Prostate Neoplasm",
                "Appendix Neoplasm",
                "Gastric Neoplasm",
                "Anal Neoplasm",
                "Intraductal Proliferative Lesion of the Breast",
                "Intraductal Papillary Breast Neoplasm",
                "Breast Cancer by AJCC v8 Stage",
                "Breast Cancer by AJCC v8 Anatomic Stage",
                "Anatomic Stage 0 Breast Cancer AJCC v8",
                "Pharyngeal Neoplasm",
                "Oropharyngeal Neoplasm",
                "Tonsillar Neoplasm",
                "Liver and Intrahepatic Bile Duct Epithelial Neoplasm",
                "Malignant Mesothelioma",
                "Peritoneal and Retroperitoneal Neoplasms",
                "Peripheral Nervous System Neoplasm",
                "Intracranial Neoplasm",
                "Oral Neoplasm",
                "Mixed Mesodermal (Mullerian) Tumor",
                "Brain Neoplasm",
                "Retroperitoneal Neoplasm",
                "Nerve Sheath Neoplasm",
                "Stromal Neoplasm",
                "Chondrogenic Neoplasm",
                "Lobular Neoplasia",
                "Primary Brain Neoplasm",
                "Testicular Neoplasm",
                "Esophageal Neoplasm",
                "Oral Cavity Neoplasm",
                "Small Intestinal Neoplasm",
                "Duodenal Neoplasm",
                "Glioblastoma, IDH-Wildtype",
                "Uterine Corpus Smooth Muscle Neoplasm",
                "Testicular Germ Cell Tumor",
                "Testicular Pure Germ Cell Tumor",
                "Testicular Non-Seminomatous Germ Cell Tumor",
                "Adult Acute Lymphoblastic Leukemia",
                "Adrenal Gland Neoplasm",
                "Adrenal Medulla Neoplasm",
                "Childhood Kidney Neoplasm",
                "Tongue Neoplasm"};

        Set<String> termsToRemove = new HashSet<>(Arrays.asList(doNotDisplay));

        Collection<OntologyTerm> ontologyTerms = ontologyTermRepository.findAllWithMappings();

        List<AutoSuggestOption> autoSuggestList = new ArrayList<>();

        for (OntologyTerm ontologyTerm : ontologyTerms) {
            if (ontologyTerm.getLabel() != null) {

                if(!termsToRemove.contains(ontologyTerm.getLabel()) || ontologyTerm.getDirectMappedSamplesNumber() > 0){

                    autoSuggestList.add(new AutoSuggestOption(ontologyTerm.getLabel(), "OntologyTerm"));
                }

            }
        }

        Collections.sort(autoSuggestList);

        return autoSuggestList;
    }


}
