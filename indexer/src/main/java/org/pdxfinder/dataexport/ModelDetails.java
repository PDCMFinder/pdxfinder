package org.pdxfinder.dataexport;

import java.util.*;

/*
 * Created by csaba on 03/10/2019.
 */
public class ModelDetails {


    private String hostStrainName;
    private String hostStrainNomenclature;

    private String engraftmentSite;
    private String engraftmentType;
    private String engraftmentMaterial;
    private String engraftmentMaterialStatus;

    private Set<String> passages;

    public ModelDetails(String hostStrainName, String hostStrainNomenclature, String engraftmentSite,
                        String engraftmentType, String engraftmentMaterial, String engraftmentMaterialStatus, String passage) {

        this.hostStrainName = hostStrainName;
        this.hostStrainNomenclature = hostStrainNomenclature;
        this.engraftmentSite = engraftmentSite;
        this.engraftmentType = engraftmentType;
        this.engraftmentMaterial = engraftmentMaterial;
        this.engraftmentMaterialStatus = engraftmentMaterialStatus;

        this.passages = new HashSet<>();
        this.passages.add(passage);
    }

    public String getHostStrainName() {
        return hostStrainName;
    }

    public void setHostStrainName(String hostStrainName) {
        this.hostStrainName = hostStrainName;
    }

    public String getHostStrainNomenclature() {
        return hostStrainNomenclature;
    }

    public void setHostStrainNomenclature(String hostStrainNomenclature) {
        this.hostStrainNomenclature = hostStrainNomenclature;
    }

    public String getEngraftmentSite() {
        return engraftmentSite;
    }

    public void setEngraftmentSite(String engraftmentSite) {
        this.engraftmentSite = engraftmentSite;
    }

    public String getEngraftmentType() {
        return engraftmentType;
    }

    public void setEngraftmentType(String engraftmentType) {
        this.engraftmentType = engraftmentType;
    }

    public String getEngraftmentMaterial() {
        return engraftmentMaterial;
    }

    public void setEngraftmentMaterial(String engraftmentMaterial) {
        this.engraftmentMaterial = engraftmentMaterial;
    }

    public String getEngraftmentMaterialStatus() {
        return engraftmentMaterialStatus;
    }

    public void setEngraftmentMaterialStatus(String engraftmentMaterialStatus) {
        this.engraftmentMaterialStatus = engraftmentMaterialStatus;
    }

    public Set<String> getPassages() {
        return passages;
    }

    public void setPassages(Set<String> passages) {
        this.passages = passages;
    }

    public String getSortedPassages(){

        List<Integer> intList = new ArrayList<>();
        for(String p : passages){
            intList.add(Integer.valueOf(p));
        }

        Collections.sort(intList);

        List<String> list = new ArrayList<>();

        for(Integer i: intList){
            list.add(i.toString());
        }

        return String.join(",",list);
    }
}
