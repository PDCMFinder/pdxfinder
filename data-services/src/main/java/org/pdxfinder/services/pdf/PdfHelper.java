package org.pdxfinder.services.pdf;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.apache.commons.lang3.text.WordUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/*
 * Created by abayomi on 30/10/2018.
 */
public class PdfHelper {


    public Lists listText(List<Text> dList, String style, String type) {

        Lists lists = new Lists();
        lists.setStyle(style);
        lists.setType(type);
        lists.setUl(dList);

        return lists;

    }


    public Map<String, Style> getStyles() {

        Map<String, Style> styles = new HashMap<>();
        Style style = new Style();

        style.setFontSize(10);
        style.setBold(true);
        style.setMargin(Arrays.asList(0, 0, 0, 10));
        style.setColor("#00b2d5");
        styles.put("header", style);


        style = new Style(13, true, Arrays.asList(0, 0, 0, 10));
        styles.put("subheader", style);

        style = new Style();
        style.setMargin(Arrays.asList(-25, -41, 0, 15));
        styles.put("container", style);

        style = new Style();
        style.setMargin(Arrays.asList(0, 17, 0, 15));
        styles.put("titleArea", style);

        style = new Style();
        style.setMargin(Arrays.asList(0, 5, 0, 15));
        styles.put("tableExample", style);

        style = new Style();
        style.setMargin(Arrays.asList(-25, -10, 0, 15));
        style.setFillColor("#D8E2F3");
        styles.put("tableExample2", style);


        style = new Style();
        style.setFontSize(20);
        style.setColor("#00b2d5");
        style.setBold(true);
        style.setMargin(Arrays.asList(0, 0, 0, 0));
        styles.put("h1", style);

        style = new Style();
        style.setFontSize(9);
        style.setColor("#00b2d5");
        style.setBold(true);
        style.setMargin(Arrays.asList(0, -2, 0, 0));
        styles.put("h2", style);

        style = new Style(Arrays.asList(0, -4, 0, 0));
        styles.put("shiftUp", style);

        style = new Style(Arrays.asList(10, -4, 0, 0));
        styles.put("left-spaced", style);


        style = new Style();
        style.setFontSize(10);
        style.setColor("#566573");
        style.setMargin(Arrays.asList(0, 10, 0, 0));
        style.setLineHeight("1.4");

        styles.put("bodyText", style);

        style = new Style();
        style.setFontSize(10);
        style.setColor("#566573");
        style.setMargin(Arrays.asList(10, 10, 0, 0));
        style.setLineHeight("1.4");
        styles.put("bodyTextSpaced", style);

        style = new Style();
        style.setBold(true);
        style.setFontSize(9);
        style.setColor("#000");
        style.setMargin(Arrays.asList(4, 6, 0, 0));

        styles.put("tableH3", style);

        style = new Style();
        style.setBold(true);
        style.setFontSize(8);
        style.setColor("#000");
        style.setMargin(Arrays.asList(0, 3, 0, 3));

        styles.put("thStyle", style);

        style = new Style();
        style.setFontSize(8);
        style.setColor("#566573");
        style.setMargin(Arrays.asList(0, 2, 0, 2));

        styles.put("tdStyle", style);

        style = new Style();
        style.setFontSize(10);
        style.setColor("#566573");
        style.setMargin(Arrays.asList(0, 5, 0, 0));

        styles.put("tableText", style);

        style = new Style();
        style.setFontSize(9);
        style.setColor("#566573");
        style.setMargin(Arrays.asList(4, 7, 0, 0));
        style.setLineHeight("1.2");
        styles.put(Label.STYLE_BODY_TEXT3, style);

        style = new Style();
        style.setFontSize(8);
        style.setColor("#566573");
        style.setLineHeight("1.3");
        styles.put("textSmall", style);

        style = new Style();
        style.setFontSize(7);
        style.setColor("#aaa");
        style.setLineHeight("1.3");
        styles.put("footerText", style);


        return styles;


    }


    public Object tableHelper(
            List<List<Object>> tableBody,
            List<Object> widths,
            int heights,
            String tableStyle,
            Object tableLayout
    ) {

        //Generate a Table
        Table table = new Table(widths, heights, tableBody);

        Map<String, Object> tableData = new HashMap<>();
        tableData.put("style", tableStyle);
        tableData.put("table", table);
        tableData.put("layout", tableLayout);

        return tableData;

    }


    public Object tabularData(List<List<Object>> tableBody, List<Object> widths) {

        //Generate a Table
        Table table = new Table(widths, tableBody);

        Map<String, Object> tableData = new HashMap<>();
        tableData.put("style", "bodyText");
        tableData.put("table", table);
        tableData.put("layout", "headerLineOnly");

        return tableData;

    }


    public Object singleColumnTable(List<List<Object>> tableBody, int width) {

        //Generate a Table
        Table table = new Table(
                Arrays.asList(width),
                tableBody
        );

        Map<String, Object> tableData = new HashMap<>();
        tableData.put("style", "bodyText");
        tableData.put("table", table);
        tableData.put("layout", "headerLineOnly");

        return tableData;

    }


    public Object tabularData(Map<String, String> data) {

        List<List<Object>> tableBody = new ArrayList();

        for (Map.Entry<String, String> entry : data.entrySet()) {

            List<Object> row = Arrays.asList(boldText(entry.getKey()), " ", entry.getValue());
            tableBody.add(row);
        }

        //Generate a Table
        Table table = new Table(
                Arrays.asList(140, 20, 180),
                tableBody
        );

        Map<String, Object> tableData = new HashMap<>();
        tableData.put("style", "bodyText");
        tableData.put("table", table);
        tableData.put("layout", "headerLineOnly");

        return tableData;

    }

    public Text topSpace(int topMargin) {

        List<Integer> margin = new ArrayList<>();
        margin.add(0);
        margin.add(topMargin);
        margin.add(0);
        margin.add(5);
        return headTitle("", margin);
    }


    public Text boldText(String dText) {

        Text text = new Text();
        text.setText(dText);
        text.setColor("#000");
        text.setBold(true);

        return text;
    }

    public Text linkedText(String dText, String style, String url) {

        Text text = new Text();
        text.setText(dText);
        text.setStyle(style);
        text.setLink(url);
        text.setColor("#06369d");

        return text;
    }

    public Text plainText(String dText, String style, Boolean italics) {

        Text text = new Text();
        text.setText(dText);
        text.setStyle(style);
        text.setItalics(italics);

        return text;
    }


    public List<Object> formatText(String data, String open, String close) {

        String report = "";
        String resA;
        String resB;
        String trail = "";

        List<Object> dataList = new ArrayList<>();

        int iter = data.split(open).length;


        for (int i = 0; i < iter - 1; i++) {

            resA = (i == 0) ? data.split(open)[i] : data.split(open)[i].split(close)[1];
            resB = (i == 0) ? data.split(close)[i].split(open)[1] : data.split(close)[i].split(open)[1];

            report += resA + "[" + resB + "]";

            // add to list
            dataList.add(resA);

            //make bold and update
            dataList.add(boldText(resB));

        }

        try {
            trail = data.split(open)[iter - 1].split(close)[1];
            dataList.add(trail);
        } catch (Exception e) {
        }

        return dataList;
    }


    public Object paragraphText(String dText, int columnWidth) {
        return paragraph(dText, columnWidth, "bodyText");
    }

    public Object paragraphSpaced(String dText, int columnWidth) {
        return paragraph(dText, columnWidth, "bodyTextSpaced");
    }


    public Object paragraph(String dText, int columnWidth, String style) {

        List<Object> data = formatText(dText, "<b>", "</b>");

        Column column = new Column();
        column.setWidth(columnWidth);
        column.setText(data);
        column.setStyle(style);

        List<Column> columns = new ArrayList<>();
        columns.add(column);

        Map<Object, Object> columnMap = new HashMap<>();
        columnMap.put("columns", columns);

        return columnMap;
    }


    public Text headTitle(String dText) {

        Text text = new Text(dText, "10", "left", true, Arrays.asList(0, 2, 0, 5));
        text.setStyle("header");
        return text;

    }

    public Text headTitle(String dText, List margin) {

        Text text = new Text(dText, "10", "left", true, margin);
        text.setStyle("header");
        return text;

    }


    public Column underLine(int lineWidth) {
        return mainLine(lineWidth, "shiftUp");
    }

    public Column leftSpacedLine(int lineWidth) {
        return mainLine(lineWidth, "left-spaced");
    }

    public Column mainLine(int lineWidth, String style) {

        List<List<Object>> tableBody = new ArrayList();
        Table table = new Table();

        tableBody = Arrays.asList(Arrays.asList(""), Arrays.asList(""));
        table = new Table(Arrays.asList(lineWidth), 0, tableBody, 1);

        Column column = new Column();
        column.setTable(table);
        column.setLayout("headerLineOnly");
        column.setStyle(style);

        return column;

    }


    public Map canvasLine(int width, String color, String thickness) {

        CanvasLine canvas = new CanvasLine();

        canvas.setType("line");
        canvas.setX1(0);
        canvas.setY1(-1);
        canvas.setX2(width);
        canvas.setY2(-1);
        canvas.setLineWidth(thickness);
        canvas.setLineColor(color);

        Map<String, List<Object>> line = new HashMap<>();

        line.put("canvas", Arrays.asList(canvas));

        return line;
    }


    public Map tinLine(int width, String color) {

        CanvasLine canvas = new CanvasLine();

        canvas.setType("line");
        canvas.setX1(0);
        canvas.setY1(-1);
        canvas.setX2(width);
        canvas.setY2(-1);
        canvas.setLineWidth("1");
        canvas.setLineColor(color);

        Map<String, List<Object>> line = new HashMap<>();

        line.put("canvas", Arrays.asList(canvas));

        return line;
    }


    public Map tinLine(int start, int width, int yPos, String color) {

        CanvasLine canvas = new CanvasLine();

        canvas.setType("line");
        canvas.setX1(start);
        canvas.setY1(yPos);
        canvas.setX2(width);
        canvas.setY2(yPos);
        canvas.setLineWidth("1");
        canvas.setLineColor(color);

        Map<String, List<Object>> line = new HashMap<>();

        line.put("canvas", Arrays.asList(canvas));

        return line;
    }


    public Text thText(String dText, String colSpan) {

        Text text = new Text();
        text.setText(dText);
        text.setStyle("thStyle");
        text.setFillColor("#D8E2F3");
        text.setColSpan(colSpan);
        text.setBold(true);

        return text;
    }

    public Text tdText(String dText, Boolean bold, String link, List<Boolean> border) {

        Text text = new Text();
        text.setText(dText);
        text.setStyle("tdStyle");
        text.setColor("#000");
        text.setBold(bold);
        text.setBorder(border);
        text.setLink(link);

        return text;
    }


    public Text tdText(String dText, String colSpan, Boolean bold,String align) {

        Text text = new Text();
        text.setText(dText);
        text.setStyle("tdStyle");
        text.setColor("#000");
        text.setColSpan(colSpan);
        text.setBold(bold);
        text.setAlignment(align);

        return text;
    }


    public Object pdxFinderTable(List<Map<String, String>> listOfMaps, List<String> tHead, List<Object> colWidths) {


        List<Boolean> leftDataBorder = Arrays.asList(true, true, false, false);
        List<Boolean> midDataBorder = Arrays.asList(false, true, false, false);
        List<Boolean> rightDataBorder = Arrays.asList(false, true, true, false);

        int columnCount = listOfMaps.get(0).size();

        List<List<Object>> tableBody = new ArrayList();

        // Add columns to the row
        List<Object> row = new ArrayList<>();

        for (String head : tHead) {
            row.add(thText(head, "1"));
        }

        tableBody.add(row);


        int listOfMapsCounter = 0;

        for (Map<String, String> data : listOfMaps) {

            listOfMapsCounter++;
            row = new ArrayList<>();
            int mapCounter = 0;

            for (Map.Entry<String, String> entry : data.entrySet()) {

                mapCounter++;
                if (listOfMapsCounter == listOfMaps.size()) {
                    leftDataBorder = Arrays.asList(true, true, false, true);
                    midDataBorder = Arrays.asList(false, true, false, true);
                    rightDataBorder = Arrays.asList(false, true, true, true);
                }

                if (mapCounter == 1) {

                    row.add(tdText(WordUtils.capitalize(entry.getValue()), false, null, leftDataBorder));
                } else if (mapCounter < columnCount) {

                    row.add(tdText(WordUtils.capitalize(entry.getValue()), false, null, midDataBorder));
                } else {

                    row.add(tdText(WordUtils.capitalize(entry.getValue()), false, null, rightDataBorder));
                }

            }

            tableBody.add(row);

        }


        //Generate a Table specifying width and body
        Table table = new Table(
                colWidths,
                tableBody
        );

        Map<String, Object> tableData = new HashMap<>();
        tableData.put("style", "tableText");
        tableData.put("table", table);

        TableLayout layout = new TableLayout(true, "#ccc", "#ccc");
        tableData.put("layout", layout);

        return tableData;

    }





    public Object emptyContentTable(String textContent,List<String> tHead, List<Object> colWidths) {


        List<Boolean> leftDataBorder = Arrays.asList(true, true, false, false);
        List<Boolean> midDataBorder = Arrays.asList(false, true, false, false);
        List<Boolean> rightDataBorder = Arrays.asList(false, true, true, false);

        List<List<Object>> tableBody = new ArrayList();

        // Add columns to the row
        List<Object> row = new ArrayList<>();

        for (String head : tHead) {
            row.add(thText(head, "1"));
        }

        // Add Col Headers to table
        tableBody.add(row);

        // Add Empty row with relevant colspan to the table
        row = new ArrayList<>();
        row.add(tdText(textContent, String.valueOf(tHead.size()),false,"center"));
        tableBody.add(row);


        //Generate a Table specifying width and body
        Table table = new Table(
                colWidths,
                tableBody
        );

        Map<String, Object> tableData = new HashMap<>();
        tableData.put("style", "tableText");
        tableData.put("table", table);

        TableLayout layout = new TableLayout(true, "#ccc", "#ccc");
        tableData.put("layout", layout);

        return tableData;

    }




    public Object pdxFinderTable(Map<String, String> data, String tableHead) {


        List<Boolean> leftDataBorder = Arrays.asList(true, true, false, false);
        List<Boolean> rightDataBorder = Arrays.asList(false, true, true, false);

        List<List<Object>> tableBody = new ArrayList();

        List<Object> row = Arrays.asList(
                thText(tableHead, "2"),
                thText("", null)
        );
        tableBody.add(row);

        int loopCounter = 0;

        for (Map.Entry<String, String> entry : data.entrySet()) {

            loopCounter++;

            if (loopCounter == data.size()) {
                leftDataBorder = Arrays.asList(true, true, false, true);
                rightDataBorder = Arrays.asList(false, true, true, true);
            }

            row = Arrays.asList(
                    tdText(entry.getKey(), true, null, leftDataBorder),
                    tdText(entry.getValue(), false, null, rightDataBorder)
            );

            tableBody.add(row);
        }


        //Generate a Table specifying width and body
        Table table = new Table(
                Arrays.asList(140, 220),
                tableBody
        );

        Map<String, Object> tableData = new HashMap<>();
        tableData.put("style", "tableText");
        tableData.put("table", table);

        TableLayout layout = new TableLayout(true, "#ccc", "#ccc");
        tableData.put("layout", layout);

        return tableData;

    }


    public List<Object> dynamicColumnWidth(int width, int offset, int size) {

        // Adjust width for table:
        width = width - 10 * size;

        List<Object> widthList = new ArrayList<>();
        widthList.add(offset);

        int single = (width - offset) / size;

        int remainder = (width - offset) - (single * size);

        for (int i = 0; i < size; i++) {

            if (i == (size - 1)) {
                single += remainder;
            }
            widthList.add(single);

        }

        return widthList;
    }


    public static byte[] imageToByteRemote(String stringURL) {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {

            URL toDownload = new URL(stringURL);

            byte[] chunk = new byte[4096];
            int bytesRead;
            InputStream stream = toDownload.openStream();

            while ((bytesRead = stream.read(chunk)) > 0) {
                outputStream.write(chunk, 0, bytesRead);
            }
            stream.close();

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return outputStream.toByteArray();
    }


    public static byte[] imageToByteLocal(String imagePath) {

        byte imageData[] = null;
        File file = new File(imagePath);

        // BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(serverFile));
        try (FileInputStream imageInFile = new FileInputStream(file)) {
            // Reading a Image file from file system
            imageData = new byte[(int) file.length()];
            imageInFile.read(imageData);
        } catch (Exception e) {
            System.out.println("Exception while reading the Image " + e);
        }
        return imageData;
    }


    public Boolean deleteFile(String fileURL) {

        Boolean report = false;
        try {

            Path path = Paths.get(fileURL);
            Files.deleteIfExists(path);

            report = true;
        } catch (Exception e) {
        }

        return report;
    }


    public static String base64EncodeToString(byte imageData[]) {

        String base64Image = "";
        base64Image = Base64.getEncoder().encodeToString(imageData);

        return base64Image;
    }


    public byte[] generateQRCode(String modelID, String qrCodeData) {

        // String filePath = "";
        byte[] imageInByte = null;

        try {

        /*String pdxTempFolder = System.getProperty("user.home") + "/PDX_TEMP/";

        filePath = pdxTempFolder + modelID + ".png";

        // Create directory if it not exists.
        File uploadRootDir = new File(pdxTempFolder);
        if (!uploadRootDir.exists()) {
            uploadRootDir.mkdirs();
        }*/

            String charset = "ISO-8859-1"; //"UTF-8"; // or

            Map<EncodeHintType, ErrorCorrectionLevel> hintMap = new HashMap<EncodeHintType, ErrorCorrectionLevel>();
            hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);

            BitMatrix matrix =
                    new MultiFormatWriter().encode(new String(qrCodeData.getBytes(charset), charset),
                            BarcodeFormat.QR_CODE, 200, 200, hintMap);

            //MatrixToImageWriter.writeToFile(matrix, filePath.substring(filePath.lastIndexOf('.') + 1), new File(filePath));


            BufferedImage originalImage = MatrixToImageWriter.toBufferedImage(matrix);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(originalImage, "png", baos);
            baos.flush();
            imageInByte = baos.toByteArray();
            baos.close();


            System.out.println("QR Code image created successfully! for " + qrCodeData);

        } catch (Exception e) {

            System.err.println(e);
        }

        return imageInByte;
    }







}
