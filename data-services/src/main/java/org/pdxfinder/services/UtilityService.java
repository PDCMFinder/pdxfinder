package org.pdxfinder.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/*
 * Created by abayomi on 12/02/2019.
 */

@Service
public class UtilityService {


    private String homeDir = System.getProperty("user.home");
    private static final Logger log = LoggerFactory.getLogger(UtilityService.class);
    private ObjectMapper mapper = new ObjectMapper();

    //Delimiter used in CSV file
    private static final String COMMA_DELIMITER = ",";
    private static final String NEW_LINE_SEPARATOR = "\n";

    /*************************************************************************************************************
     *                                           DATA SERIALIZER METHODS SECTION                                 *
     ************************************************************************************************************/

    public List<Map<String, String>> serializeDataToMaps(String fileName) {


        String fileExtension = getFileExtension(fileName);

        List<Map<String, String>> csvMaps;

        if (fileExtension.equals("csv")) {

            csvMaps = serializeCSVToMaps(fileName);
        }else if (fileExtension.equals("json")) {

            csvMaps = (List) serializeJSONToMaps(fileName);
        } else {

            csvMaps = serializeExcelDataNoIterator(fileName,0,1);
        }

        return csvMaps;
    }

    public String serializeToCsvWithIncludeNonEmpty(List<?> pojoList) throws IOException {

        CsvMapper csvMapper = new CsvMapper();
        List<Map<String, Object>> dataList = mapper.convertValue(pojoList, new TypeReference<List<Map<String, Object>>>(){});
        List<List<String>> csvData = new ArrayList<>();
        List<String> csvHead = new ArrayList<>();

        AtomicInteger counter = new AtomicInteger();
        dataList.forEach( row ->{
            List<String> rowData = new ArrayList<>();
            row.forEach((key,value)->{
                rowData.add(String.valueOf(value));
                if (counter.get() == 0){
                    csvHead.add(key);
                }
            });
            csvData.add(rowData);
            counter.getAndIncrement();
        });

        CsvSchema.Builder builder = CsvSchema.builder();
        csvHead.forEach(builder::addColumn);

        CsvSchema  schema = builder.build().withHeader();
        return csvMapper.writer(schema).writeValueAsString(csvData);
    }

    public Map<String, List<Map<String, String>> > serializeAndGroupFileContent(String fileName, String groupColumn) {

        String fileExtension = getFileExtension(fileName);

        List<Map<String, String>> csvMaps = new ArrayList<>();

        switch (fileExtension){

            case "csv":
                csvMaps = serializeCSVToMaps(fileName);
                break;

            case "json":
                csvMaps = (List) serializeJSONToMaps(fileName);
                break;

            case "xlsx":
                csvMaps = serializeExcelDataNoIterator(fileName,0,1);
                break;
            default:
                break;

        }
        if(csvMaps == null) log.error("Map is null for {} in groupCol {}", fileName, groupColumn);

        return groupDataByColumn(csvMaps, groupColumn);
    }



    public Map<String, List<Map<String, String>> > groupDataByColumn(List<Map<String, String>> csvMaps, String groupColumn) {


        Map<String, List<Map<String, String>> > groupedMap = new LinkedHashMap<>();

        for (Map<String, String> rowData : csvMaps){

            List<Map<String, String>> tempList = new ArrayList<>();

            String rowKey = rowData.get(groupColumn);

            if (groupedMap.get(rowKey) == null) {

                tempList.add(rowData);
                groupedMap.put(rowKey, tempList);
            } else {

                tempList = groupedMap.get(rowKey);
                tempList.add(rowData);

                groupedMap.put(rowKey, tempList);
            }

        }
        return groupedMap;
    }

    /*************************************************************************************************************
     *                                  MICROSOFT EXCEL FILE HANDLER SECTION                                     *
     ************************************************************************************************************/

    // EXCEL FILE READER : READ EXCEL FILE WITHOUT USING ITERATOR, SAFER LOADING FOR EMPTY CELLS
    public List<Map<String, String>> serializeExcelDataNoIterator(String excelURL, int sheet, int startRow) {
        FileInputStream inputStream = convertFileToStream(excelURL);
        return serializeExcelDataNoIterator(inputStream, sheet, startRow);
    }

    public List<Map<String, String>> serializeExcelDataNoIterator(InputStream inputStream, int sheet, int startRow) {
        List<String> tableHead = new ArrayList<>();
        List<Map<String, String>> csvMap = new ArrayList<>();

        startRow--;

        try (Workbook workbook = WorkbookFactory.create(inputStream)){
            Row row;
            int rowCount = 0;
            Sheet spreadsheet = workbook.getSheetAt(sheet);
            Iterator<Row> rowIterator = spreadsheet.iterator();


            while (rowIterator.hasNext()) // Read the head row
            {
                row = rowIterator.next();

                if (rowCount == startRow) {

                    Iterator<Cell> cellIterator = row.cellIterator();
                    tableHead = getXlsTableHeadData(cellIterator);
                    break;
                }
                rowCount++;
            }

            rowCount = 0;
            for (Row dRow : spreadsheet) {

                if (rowCount <= startRow) {
                    rowCount++;
                    continue;
                }

                Map<String, String> rowMap = new LinkedHashMap<>();
                String rowDataTracker = "";
                StringBuilder rowBuilder = new StringBuilder();

                for (int i = 0; i < tableHead.size(); i++) {

                    Cell nameCell = dRow.getCell(i, Row.RETURN_BLANK_AS_NULL);
                    String key = tableHead.get(i).trim();

                    if (nameCell != null) {

                        nameCell.setCellType(Cell.CELL_TYPE_STRING);
                        String data = nameCell.getStringCellValue();

                        rowMap.put(key, data);
                        rowBuilder.append(data);

                    } else {
                        if (!key.equals("")) rowMap.put(key, "");
                    }
                }

                rowDataTracker = rowBuilder.toString();
                if (rowDataTracker.length() != 0) csvMap.add(rowMap);

                rowCount++;
            }

            inputStream.close();
        } catch (IOException | InvalidFormatException ex) {
            log.warn(ex.getMessage());
        }

        return csvMap;
    }

    public void writeXLSXFile(List<Map<String, String>> dataList,String fileName, String sheetName) {

        String excelFileName = homeDir+"/Downloads/"+fileName;

        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet(sheetName) ;

        CellStyle headerCellStyle = setXLSFont(wb, 14, IndexedColors.BROWN);

        //iterating r number of rows
        int rowCount = 0;
        for (Map<String, String> data : dataList){

            XSSFRow row = sheet.createRow(rowCount);

            int columnCount = 0;
            for (Map.Entry<String, String> entry : data.entrySet() ) {

                XSSFCell cell = row.createCell(columnCount);

                if (rowCount == 0){

                    cell.setCellValue(entry.getKey());
                    cell.setCellStyle(headerCellStyle);

                    sheet.autoSizeColumn(columnCount);
                }else {

                    cell.setCellValue(data.get(entry.getKey()));
                }
                columnCount++;
            }

            rowCount++;
        }


        FileOutputStream fileOut;
        try {
            fileOut = new FileOutputStream(excelFileName);

            //write this workbook to an Outputstream.
            wb.write(fileOut);
            fileOut.flush();
            fileOut.close();

        }catch (Exception e){
            log.warn(e.getMessage());
        }

    }

    // GET SMALL SECTIONS OF EXCEL: RETRIEVE FIRST ROW OF EXCEL SHEET
    public List<String> getXlsTableHeadData(Iterator<Cell> cellIterator){

        List<String> data = new ArrayList<>();

        while (cellIterator.hasNext())
        {
            Cell cell = cellIterator.next();
            if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC){
                data.add(cell.getNumericCellValue() + "");
            }else {
                data.add(cell.getStringCellValue());
            }
        }
        return data;
    }

    // EXCEL CUSTOMIZER : SET EXCEL CELL FONT SIZE AND COLOR
    private CellStyle setXLSFont(Workbook wb, int fontSize, IndexedColors colors){

        // Create a Font for styling header cells
        Font headerFont = wb.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) fontSize);
        headerFont.setColor(colors.getIndex());

        // Create a CellStyle with the font
        CellStyle headerCellStyle = wb.createCellStyle();
        headerCellStyle.setFont(headerFont);

        return headerCellStyle;
    }

    /*************************************************************************************************************
     *                                           CSV FILE HANDLER SECTION                                      *
     ************************************************************************************************************/

    public List<Map<String, String>> serializeMultipartFile(MultipartFile multipartFile) {
        List<Map<String, String>> dataList = new ArrayList<>();
        if(multipartFile.getOriginalFilename() != null) {

            InputStream inputStream = null;
            String type = "";
            try {
                String[] stringArr = multipartFile.getOriginalFilename().split("\\.");
                type = stringArr[stringArr.length - 1];

                inputStream = multipartFile.getInputStream();
            } catch (Exception e) {
                log.warn(e.getMessage());
            }
            if (type.equals("xlsx")) {

                dataList = serializeExcelDataNoIterator(inputStream, 0, 1);

            } else if (type.equals("csv")) {

                assert inputStream != null;
                DataInputStream csvData = new DataInputStream(inputStream);
                dataList = serializeCSVToMaps(csvData);
            }
        }

        return dataList;
    }


    // SERIALIZE CSV TO LIST OF MAPS
    public List<Map<String, String>> serializeCSVToMaps(String csvFile) {

        FileInputStream fileStream = null;
        try {
            fileStream = new FileInputStream(csvFile);
        } catch (Exception e) {
            log.warn(e.getMessage());
        }
        DataInputStream csvData = new DataInputStream(fileStream);

        return serializeCSVToMaps(csvData);
    }


    public List<Map<String, String>> serializeCSVToMaps(DataInputStream csvData) {

        int row = 0;
        String thisLine;
        List<String> tableHead = new ArrayList<>();
        List<Map<String, String>> csvMap = new ArrayList<>();

        try {

            while ((thisLine = csvData.readLine()) != null) {
                String[] rowDataArr = thisLine.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");

                int column = 0;

                if (row == 0) {

                    for (column = 0; column < rowDataArr.length; column++) {

                        tableHead.add(rowDataArr[column]);
                    }
                } else {

                    Map<String, String> rowMap = new LinkedHashMap<>();
                    for (String columnHead : tableHead) {

                        String dKey = columnHead.trim().replaceAll("\"","");
                        String dValue = rowDataArr[column].trim().replaceAll("\"","");

                        rowMap.put(dKey, dValue);
                        column++;
                    }
                    csvMap.add(rowMap);
                }
                row++;
            }
        } catch (Exception e) {
            log.warn(e.getMessage());
        }

        return csvMap;

    }


    // SERIALIZE CSV TO ARRAY LIST
    public List<List<String>> serializeCSVToArrayList(String dataFile)
    {

        FileInputStream fileStream = null;
        try{
            fileStream = new FileInputStream(dataFile);
        }catch (IOException e){
            log.warn(e.getMessage());
        }

        String thisLine;

        ArrayList lineList = null;
        List<List<String>> dataArrayList = new ArrayList<>();

        try(DataInputStream myInput = new DataInputStream(fileStream)) {

            while ((thisLine = myInput.readLine()) != null)
            {
                lineList = new ArrayList();
                String[] strar = thisLine.split(",");
                for(int j=0;j<strar.length;j++)
                {
                    lineList.add(strar[j]);
                }
                dataArrayList.add(lineList);
            }

        }catch (IOException e){
            log.warn(e.getMessage());
        }


        return dataArrayList;
    }



    // CREATE CSV FILE
    public void writeCsvFile(List<Map<String, String>> dataList, String destination) {

        try (FileWriter fileWriter = new FileWriter(destination)){

            //Write the CSV file header
            List<String> csvHead = new ArrayList<>();
            Map<String, String> refData = dataList.get(0);

            for (Map.Entry<String, String> entry : refData.entrySet() ) {      // GET THE JSON KEY
                csvHead.add(entry.getKey());
            }

            fileWriter.append(String.join(COMMA_DELIMITER,csvHead));

            //Add a new line separator after the header
            fileWriter.append(NEW_LINE_SEPARATOR);


            for (Map<String, String> data : dataList) {

                for (String dKey : csvHead){
                    if(data.get(dKey).contains(",")){
                        fileWriter.append(String.format("\"%s\"", data.get(dKey)));
                    }else {
                        fileWriter.append(String.valueOf(data.get(dKey)));
                    }
                    fileWriter.append(COMMA_DELIMITER);
                }

                fileWriter.append(NEW_LINE_SEPARATOR);

            }

            log.info("CSV file was created successfully !!!");

        } catch (Exception e) {
            log.warn(e.getMessage());
        }
    }


    public void writeCsvFile(List<String> headers, List<List<String>> dataList, String destination){

        List<Map<String, String>> mapList = new ArrayList<>();

        for(int i = 0; i < dataList.size(); i++){

            Map<String, String> rowMap = new HashMap<>();

            List<String> row = dataList.get(i);

            for(int j = 0; j < row.size(); j++){

                rowMap.put(headers.get(j), row.get(j));
            }

            mapList.add(rowMap);
        }

        writeCsvFile(mapList, destination);
    }

    public void writeCsvFileGenerics(List<Map<?, ?>> genericMapList, String destination) {

        List<Map<String, String>> stringMapList = new ArrayList<>();

        // Convert the generic key and value types to string data types
        genericMapList.forEach(genericMap->{

            // Empty map of strings to temporarily hold retrieved string data
            Map<String, String> temp = new LinkedHashMap<>();

            // Get Map of Generics, stringify the keys and values and put in stringMapList
            genericMap.entrySet().forEach(map -> {
                temp.put(String.valueOf(map.getKey()), String.valueOf(map.getValue()));
            });

            stringMapList.add(temp);
        });

        // write to csv at the specified destination
        writeCsvFile(stringMapList, destination);
    }
    /*************************************************************************************************************
     *                                           JSON FILE HANDLER SECTION                                      *
     ************************************************************************************************************/

    // SERIALIZER : CONVERT JSON URL TO JAVA MAPS
    public List<Map<String, Object>> serializeJSONToMaps(String jsonFile) {

        List<Map<String, Object>> data;

        JsonNode node = readJsonLocal(jsonFile);
        try {
            data = mapper.convertValue(node, List.class);

        }catch (Exception e){

            Map<String, Object> json = mapper.convertValue(node, Map.class);

            String jsonKey = "";
            for (Map.Entry<String, Object> entry : json.entrySet() ) {      // GET THE JSON KEY
                jsonKey = entry.getKey();
            }

            data = (List) json.get(jsonKey);
        }

        return data;
    }


    // SERIALIZER : CONVERT JSON URL TO JAVA MAPS
    public List<Map<String, Object>> serializeJSONToMaps(String jsonFile,String jsonKey) {

        JsonNode node = readJsonLocal(jsonFile);

        Map<String, Object> json = mapper.convertValue(node, Map.class);

        return (List) json.get(jsonKey);

    }



    // PARSER : LOAD JSON NODES FROM  REMOTE JSON HTTP URL
    public JsonNode readJsonURL(String apiLink) {

        JsonNode jsonNode = null;
        try {

            URL url = new URL(apiLink);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != 200) {
                log.error("Failed : HTTP error code : {}", conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

            jsonNode = mapper.readTree(br);
            conn.disconnect();

        } catch (IOException e) {
            log.warn(e.getMessage());
        }

        return jsonNode;

    }


    // PARSER : LOAD JSON NODES FROM  LOCAL JSON
    public JsonNode readJsonLocal(String jsonFileLink) {

        JsonNode jsonNode = null;

        try {

            BufferedReader br = new BufferedReader(new FileReader(jsonFileLink));
            jsonNode = mapper.readTree(br);

        }catch (Exception e) {
            log.error("Hey, could not read local directory: {}", jsonFileLink);
        }

        return jsonNode;
    }








    /*************************************************************************************************************
     *                              LOCAL OR REMOTE FILE HANDLERS PARSERS SECTION                               *
     ************************************************************************************************************/

    // PARSER : CONVERT ANY FILE TO INPUTSTREAM
    public FileInputStream convertFileToStream(String filePath){

        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(new File(filePath));
            log.info("Loading template from : {}", filePath);
        }catch (Exception e) {
            log.error("UtilityService convertFileToStream says Data File {} Not Found", filePath);
        }

        return inputStream;
    }



    // PARSER : LOAD FILE CONTENT FROM HTTP URL
    public String parseURL(String urlStr) {

        StringBuilder sb = new StringBuilder();

        URL url = null;
        try {
            url = new URL(urlStr);
        } catch (MalformedURLException e) {
            log.warn(e.getMessage());
        }

        assert url != null;
        try (BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()))){
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                sb.append(inputLine);
            }

        } catch (Exception e) {
            log.error("Unable to read from URL " + urlStr, e);
        }
        return sb.toString();
    }



    // LOAD FILE CONTENT FROM LOCAL DIRECTORY
    public String parseFile(String path) {

        StringBuilder sb = new StringBuilder();

        try (Stream<String> stream = Files.lines(Paths.get(path))){

            Iterator itr = stream.iterator();
            while (itr.hasNext()) {
                sb.append(itr.next());
            }
        } catch (Exception e) {
            log.error("Failed to load file " + path, e);
        }
        return sb.toString();
    }


    // GET THE EXTENSION OF A FILE
    private String getFileExtension(String fileName){

        String[] check = fileName.split("\\.");
        return check[check.length-1];

    }


    // FILE WRITE: WRITE STRING DATA TO FILE
    public void writeToFile(String data, String destination, Boolean shouldAppend){

        // Write to the file using BufferedReader and FileWriter
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(destination, shouldAppend))){
            writer.append(data);
        } catch (IOException e) {
            log.warn(e.getMessage());
        }

    }


    // DELETE FILE FROM A LOCAL DIRECTORY
    public Boolean deleteFile(String localDirectory) {

        boolean report = false;
        try {

            Path path = Paths.get(localDirectory);
            Files.deleteIfExists(path);

            report = true;
        } catch (Exception e) {
            log.warn(e.getMessage());
        }

        return report;
    }


    public List<String> listAllFilesInADirectory(String directory) {

        List<String> fileNames = new ArrayList<>();

        File folder = new File(directory);

        File[] filDir = folder.listFiles();

        assert filDir != null;
        if (filDir.length == 0) {

            log.warn("No subdirs found for the universal loader, skipping");
        }
        else {

            for (File file : filDir) {

                if (file.isFile()) {
                    fileNames.add(file.getName());
                }
            }
        }

        return fileNames;
    }


















    /*************************************************************************************************************
     *                                          OTHER UTILITIES                                                  *
     ************************************************************************************************************/

    public String camelCaseToSentence(String s) {

        String converted = s.replaceAll(
                String.format("%s|%s|%s",
                              "(?<=[A-Z])(?=[A-Z][a-z])",
                              "(?<=[^A-Z])(?=[A-Z])",
                              "(?<=[A-Za-z])(?=[^A-Za-z])"
                ),
                " "
        );

        // Fix situations when non camel case is sent, remove double spaces generated
        return converted.trim().replaceAll(" +", " ");
    }


    public String sentenceToCamelCase(String input) {

        StringBuilder result = new StringBuilder();

        for (String s : input.split("\\s+")){

            result.append(StringUtils.capitalize(s));
        }

        return result.toString();

    }


    public String splitText(String data, String delim, String seperator){

        StringBuilder result = new StringBuilder();

        String[] splits = data.split(delim);

        for (String split : splits){

            result.append(split.trim()).append(seperator);
        }

        return result.toString().trim();
    }


    // File to Byte
    public void moveFile(String source,String destination){

        // Create Directory if it does not exist
        this.mkDirectoryFromFilePathName(destination);

        byte[] bytes = convertLocalFileToByte(source);

        Path path = Paths.get(destination);
        try{
            Files.write(path, bytes);
        }catch (IOException e){
            log.warn(e.getMessage());
        }

    }


    public void mkDirectoryFromFilePathName(String filePath){

        // Get Directory from file path string
        String directoryName = filePath.substring(0, filePath.lastIndexOf('/'));

        // Create Directory if it does not exist
        File directory = new File(directoryName);

        if (!directory.exists()){
            directory.mkdirs();
        }
    }


    public  byte[] convertLocalFileToByte(String filePath) {

        byte[] fileData = null;
        File file = new File(filePath);

        if (file.exists()){
            try (FileInputStream inputStream = new FileInputStream(file)) {

                fileData = new byte[(int) file.length()];
                //inputStream . read( fileData )

            } catch (IOException e) {
                log.warn(e.getMessage());
            }
        }

        return fileData;
    }



    public List<Map> objectArrayListToMapList(List<Object[]> dataList, List<String> keys){

        List<Map> result = new ArrayList<>();

        for (Object[] data : dataList) {

            Map<String, Object> dataMap = new LinkedHashMap<>();
            int count = 0;
            for (Object content : data){

                dataMap.put(keys.get(count), content);
                count++;
            }
            result.add(dataMap);
        }
        return result;
    }




    public JsonNode jsonStringToNode(String jsonString){

        JsonNode jsonNode = null;

        try {
            jsonNode = mapper.readTree(jsonString);
        } catch (IOException e) {
            log.warn(e.getMessage());
        }

        return jsonNode;
    }
}
