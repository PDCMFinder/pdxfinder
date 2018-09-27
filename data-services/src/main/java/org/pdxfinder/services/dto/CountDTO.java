package org.pdxfinder.services.dto;

/*
 * Created by csaba on 12/07/2018.
 */
public class CountDTO {

    private String key;
    private int value;

    public CountDTO(String key, int value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

}


