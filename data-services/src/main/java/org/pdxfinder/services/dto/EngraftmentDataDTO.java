package org.pdxfinder.services.dto;


/*
 * Created by abayomi on 18/10/2018.
 */
public class EngraftmentDataDTO {

    private String strainName;
    private String strainSymbol;
    private String engraftmentSite;
    private String engraftmentType;
    private String engraftmentMaterial;
    private String engraftmentMaterialState;
    private String passage;

    public EngraftmentDataDTO() {
    }

    public EngraftmentDataDTO(String strainName,
                              String strainSymbol,
                              String engraftmentSite,
                              String engraftmentType,
                              String engraftmentMaterial,
                              String engraftmentMaterialState,
                              String passage) {
        this.strainName = strainName;
        this.strainSymbol = strainSymbol;
        this.engraftmentSite = engraftmentSite;
        this.engraftmentType = engraftmentType;
        this.engraftmentMaterial = engraftmentMaterial;
        this.engraftmentMaterialState = engraftmentMaterialState;
        this.passage = passage;
    }


    public String getStrainName() {
        return strainName;
    }

    public void setStrainName(String strainName) {
        this.strainName = strainName;
    }

    public String getStrainSymbol() {
        return strainSymbol;
    }

    public void setStrainSymbol(String strainSymbol) {
        this.strainSymbol = strainSymbol;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EngraftmentDataDTO that = (EngraftmentDataDTO) o;

        if (strainName != null ? !strainName.equals(that.strainName) : that.strainName != null) return false;
        if (engraftmentSite != null ? !engraftmentSite.equals(that.engraftmentSite) : that.engraftmentSite != null)
            return false;
        if (engraftmentType != null ? !engraftmentType.equals(that.engraftmentType) : that.engraftmentType != null)
            return false;
        if (engraftmentMaterial != null ? !engraftmentMaterial.equals(that.engraftmentMaterial) : that.engraftmentMaterial != null)
            return false;
        return engraftmentMaterialState != null ? engraftmentMaterialState.equals(that.engraftmentMaterialState) : that.engraftmentMaterialState == null;
    }

    @Override
    public int hashCode() {
        int result = strainName != null ? strainName.hashCode() : 0;
        result = 31 * result + (engraftmentSite != null ? engraftmentSite.hashCode() : 0);
        result = 31 * result + (engraftmentType != null ? engraftmentType.hashCode() : 0);
        result = 31 * result + (engraftmentMaterial != null ? engraftmentMaterial.hashCode() : 0);
        result = 31 * result + (engraftmentMaterialState != null ? engraftmentMaterialState.hashCode() : 0);
        return result;
    }
}
