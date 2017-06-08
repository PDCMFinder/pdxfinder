package org.pdxfinder.dao;

/**
 * Created by jmason on 06/06/2017.
 */
public enum ValidationTechniques {
    VALIDATION("Validation"),
    FINGERPRINT("Fingerprint"),
    HEALTH_CHECK("Health-check");

    private String technique;

    private ValidationTechniques(String s) {
        technique = s;
    }

    public String getTechnique() {
        return technique;
    }


}
