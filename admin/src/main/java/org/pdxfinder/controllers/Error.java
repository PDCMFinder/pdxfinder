package org.pdxfinder.controllers;

import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

/*
 * Created by abayomi on 06/08/2019.
 */
public class Error {

    private HttpStatus status;
    private String message;
    private Map<String,Object> error = new HashMap<>();

    public Error(String message, HttpStatus status){
        this.status = status;
        this.message = message;
    }

    public Map<String, Object> getError() {

        this.error.put("report", this.status);
        this.error.put("Code",this.status.value());
        this.error.put("message", this.message);

        return this.error;
    }

}