package org.pdxfinder.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Date;

@Service
public class UtilityService {


    private String homeDir = System.getProperty("user.home");

    public JsonNode readJsonURL(String apiLink) {

        JsonNode jsonNode = null;
        ObjectMapper mapper = new ObjectMapper();

        try {

            URL url = new URL(apiLink);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

            jsonNode = mapper.readTree(br);
            conn.disconnect();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return jsonNode;

    }


    public void writeToFile(String data, String name){

        String fileName = homeDir+"/Documents/"+name;

        // Write to the file using BufferedReader and FileWriter
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true));
            writer.append(data);
            writer.close();

        } catch (Exception e) {}

    }


    public Boolean deleteFile(String name) {

        String fileURL = homeDir+"/Documents/"+name;

        Boolean report = false;
        try {

            Path path = Paths.get(fileURL);
            Files.deleteIfExists(path);

            report = true;
        } catch (Exception e) {
        }

        return report;
    }



}
