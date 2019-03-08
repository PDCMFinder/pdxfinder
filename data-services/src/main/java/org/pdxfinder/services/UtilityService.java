package org.pdxfinder.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.neo4j.ogm.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

@Service
public class UtilityService {


    private String homeDir = System.getProperty("user.home");
    private final static Logger log = LoggerFactory.getLogger(UtilityService.class);
    private ObjectMapper mapper = new ObjectMapper();


    public List<Map<String, String>> serializeCSVToMaps(String csvFile) {

        /*************************************************************************************************************
         *     LOAD DATA FROM FILE          *
         ***********************************/

        FileInputStream fileStream = null;
        try {
            fileStream = new FileInputStream(csvFile);
        } catch (Exception e) { }
        DataInputStream csvData = new DataInputStream(fileStream);


        /*************************************************************************************************************
         *     INITIALIZE PARAMETERS         *
         ************************************/

        int row = 0;
        String thisLine;
        List<String> tableHead = new ArrayList<>();
        List<Map<String, String>> csvMap = new ArrayList<>();


        /*************************************************************************************************************
         *    LOAD CSV FIRST ROW AS TABLE-HEAD, OTHER ROWS AS DATA, & LOAD TO MAP        *
         *******************************************************************************/
        try {

            while ((thisLine = csvData.readLine()) != null) {
                String rowDataArr[] = thisLine.split(",");
                int column = 0;

                if (row == 0) {

                    for (column = 0; column < rowDataArr.length; column++) {

                        tableHead.add(rowDataArr[column]);
                    }
                } else {

                    Map<String, String> rowMap = new HashMap();
                    for (String columnHead : tableHead) {

                        rowMap.put(columnHead, rowDataArr[column]);
                        column++;
                    }
                    csvMap.add(rowMap);
                }
                row++;
            }
        } catch (Exception e) { }

        return csvMap;

    }



    public List<Map<String, String>> serializeJSONToMaps(String jsonFile,String jsonKey) {

        ObjectMapper mapper = new ObjectMapper();

        JsonNode node = readJsonLocal(jsonFile);

        Map<String, Object> json = mapper.convertValue(node, Map.class);

        List<Map<String, String>> data = (List) json.get(jsonKey);

        return data;
    }


    public List<Map<String, String>> serializeJSONToMaps(String jsonFile) {

        ObjectMapper mapper = new ObjectMapper();

        JsonNode node = readJsonLocal(jsonFile);

        List<Map<String, String>> data = mapper.convertValue(node, List.class);

        return data;
    }






    public List<List<String>> serializeCSVToArrayList(String dataFile)
    {

        FileInputStream fileStream = null;
        try{
            fileStream = new FileInputStream(dataFile);
        }catch (Exception e){}
        DataInputStream myInput = new DataInputStream(fileStream);


        String thisLine;
        int i=0;
        ArrayList lineList = null;
        List<List<String>> dataArrayList = new ArrayList<>();

        try {

            while ((thisLine = myInput.readLine()) != null)
            {
                lineList = new ArrayList();
                String strar[] = thisLine.split(",");
                for(int j=0;j<strar.length;j++)
                {
                    lineList.add(strar[j]);
                }
                dataArrayList.add(lineList);
                System.out.println();
                i++;
            }

        }catch (Exception e){}


        return dataArrayList;
    }



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



    public JsonNode readJsonLocal(String jsonFileLink) {

        JsonNode jsonNode = null;
        ObjectMapper mapper = new ObjectMapper();

        try {

            BufferedReader br = new BufferedReader(new FileReader(jsonFileLink));
            jsonNode = mapper.readTree(br);

        }catch (Exception e) {}

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



    public String parseURL(String urlStr) {

        StringBuilder sb = new StringBuilder();

        try {
            URL url = new URL(urlStr);
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                sb.append(inputLine);
            }
            in.close();
        } catch (Exception e) {
            log.error("Unable to read from URL " + urlStr, e);
        }
        return sb.toString();
    }


    public String parseFile(String path) {

        StringBuilder sb = new StringBuilder();

        try {
            Stream<String> stream = Files.lines(Paths.get(path));

            Iterator itr = stream.iterator();
            while (itr.hasNext()) {
                sb.append(itr.next());
            }
        } catch (Exception e) {
            log.error("Failed to load file " + path, e);
        }
        return sb.toString();
    }


    public String splitText(String data, String delim, String seperator){

        String result = "";

        String[] splits = data.split(delim);

        for (String split : splits){

            result += split.trim()+seperator;
        }

        return result;
    }



}
