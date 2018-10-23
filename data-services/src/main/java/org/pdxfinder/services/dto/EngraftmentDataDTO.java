package org.pdxfinder.services.dto;


/*
 * Created by abayomi on 18/10/2018.
 */
public class EngraftmentDataDTO {

    private String strain;
    private String engraftmentSite;
    private String engraftmentType;
    private String engraftmentMaterial;
    private String engraftmentMaterialState;
    private String passage;

    public EngraftmentDataDTO() {
    }

    public EngraftmentDataDTO(String strain,
                              String engraftmentSite,
                              String engraftmentType,
                              String engraftmentMaterial,
                              String engraftmentMaterialState,
                              String passage) {
        this.strain = strain;
        this.engraftmentSite = engraftmentSite;
        this.engraftmentType = engraftmentType;
        this.engraftmentMaterial = engraftmentMaterial;
        this.engraftmentMaterialState = engraftmentMaterialState;
        this.passage = passage;
    }


    public String getStrain() {
        return strain;
    }

    public void setStrain(String strain) {
        this.strain = strain;
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

    public String getEngraftmentMaterialState() {
        return engraftmentMaterialState;
    }

    public void setEngraftmentMaterialState(String engraftmentMaterialState) {
        this.engraftmentMaterialState = engraftmentMaterialState;
    }

    public String getPassage() {
        return passage;
    }

    public void setPassage(String passage) {
        this.passage = passage;
    }
}
