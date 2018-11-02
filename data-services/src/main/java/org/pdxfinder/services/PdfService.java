package org.pdxfinder.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.pdxfinder.services.dto.EngraftmentDataDTO;
import org.pdxfinder.services.pdf.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/*
 * Created by abayomi on 02/11/2018.
 */
@Service
public class PdfService {


    private String logoURL = "https://ebiwd.github.io/pdx-visual-framework/images/logo.png";
    String appendBase64 = "data:image/png;base64,";
    private PdfHelper pdf = new PdfHelper();


    private Logger logger = LoggerFactory.getLogger(PdfService.class);

    public List<Object> generatePdf() {

        List<Object> content = new ArrayList();
        Map<Object, Object> dataHolder = new HashMap<>();
        Map<Object, Object> inDataHolder = new HashMap<>();
        List<Object> tableRow = new ArrayList();
        List<List<Object>> tableBody = new ArrayList();
        String style = "";
        String type = "";
        List<Text> lists = new ArrayList<>();

        int heights = 0;

        Text text = new Text();
        Table table = new Table();
        TableLayout tableLayout = new TableLayout();

        // Row 1 Column 1 Data:

        List<Object> row1Column1Contents = new ArrayList<>();

        // Row 1 Column 1 1st content
        Object firstContent = logoHeaderDesign("BRC0004PR", "dev.pdxfinder.org/data/pdx/TRACE/BRC0004PR");
        row1Column1Contents.add(firstContent);

        row1Column1Contents.add(pdf.headTitle(Label.PDX_MODEL.val()));
        row1Column1Contents.add(pdf.canvasLine(380, "#00b2d5", "1"));

        Map<String, String> data = new HashMap<>();
        data.put("Sex", "Male");
        data.put("Age at Collection Time", "45");
        data.put("Race / Ethnicity", "Caucassian");
        row1Column1Contents.add(pdf.pdxFinderTable(data, "PATIENT"));


        data = new HashMap<>();
        data.put("Histology", "Cutaneous Melanoma");
        data.put("Primary Tissue", "Skin");
        data.put("Collection Site", "Lung");
        data.put("Tumor Type", "Metastatic");
        data.put("Grade", "AJCC IV");
        data.put("Stage", "Not Specifed");
        EngraftmentDataDTO edto = new EngraftmentDataDTO(
                "NOD scid gamma",
                "Right Flank",
                "Not Specified",
                "Not Specified",
                "Not Specified",
                "0,1,2,3");

        ObjectMapper mapper = new ObjectMapper();

        Map<String, String> data2 = mapper.convertValue(edto, Map.class);

        row1Column1Contents.add(pdf.pdxFinderTable(data2, "PATIENT TUMOR"));


        row1Column1Contents.add(pdf.headTitle("PDX MODEL ENGRAFTMENT / MODEL QUALITY CONTROL", Arrays.asList(0, 15, 0, 5)));
        row1Column1Contents.add(pdf.canvasLine(560, "#00b2d5", "1"));

        List<Map<String, String>> dataList = new ArrayList<>();

        dataList.add(data2);
        dataList.add(data2);
        dataList.add(data2);
        row1Column1Contents.add(pdf.pdxFinderTable(dataList, "PATIENT TUMOR 2"));

        row1Column1Contents.add(pdf.pdxFinderTable(dataList, "MODEL QUALITY CONTROL"));


        row1Column1Contents.add(pdf.topSpace(197));
        row1Column1Contents.add(pdf.tinLine(560, "#ccc"));
        text = pdf.linkedText(Label.WEBSITE, Label.NULL, Label.PDX_FINDER_URL);
        text.setMargin(Arrays.asList(0, -7, 0, -2));

        List row1 = Arrays.asList(text);
        List row3 = Arrays.asList(
                pdf.plainText(Label.PDX_DEV_INFO, "textSmall", Label.FALSE)
        );

        row1Column1Contents.add(
                pdf.singleColumnTable(Arrays.asList(row1, row3), 560)
        );


        List<Object> row1Column2Contents = new ArrayList<>();
        List<Object> td = new ArrayList<>();

        td.add(pdf.headTitle(Label.INFO, Arrays.asList(4, 121, 0, 5)));
        td.add(pdf.canvasLine(160, "#00b2d5", "1"));


        td.add(pdf.plainText(Label.DATA_PROVIDER, Label.STYLE_TABLE_H3, Label.TRUE));
        td.add(
                pdf.listText(
                        Arrays.asList(pdf.plainText(Label.JAX, Label.NULL, Label.FALSE)),
                        Label.STYLE_BODY_TEXT3,
                        Label.TYPE_SQUARE
                )
        );

        td.add(pdf.plainText(Label.SUBMIT, Label.STYLE_TABLE_H3, Label.TRUE));
        td.add(
                pdf.listText(
                        Arrays.asList(pdf.plainText(Label.SUBMISSION_MAIL, Label.NULL, Label.FALSE)),
                        Label.STYLE_BODY_TEXT3,
                        Label.TYPE_SQUARE
                )
        );

        td.add(pdf.plainText(Label.HELP_DESK, Label.STYLE_TABLE_H3, Label.TRUE));
        td.add(
                pdf.listText(
                        Arrays.asList(pdf.plainText(Label.HELP_DESK_MAIL, Label.NULL, Label.FALSE)),
                        Label.STYLE_BODY_TEXT3,
                        Label.TYPE_SQUARE
                )
        );

        td.add(pdf.plainText(Label.LINKS, Label.STYLE_TABLE_H3, Label.TRUE));
        td.add(
                pdf.listText(
                        Arrays.asList(
                                pdf.linkedText(Label.PDX_LABEL, Label.STYLE_TD, Label.JAX_URL),
                                pdf.linkedText(Label.JAX_LABEL, Label.STYLE_TD, Label.JAX_URL),
                                pdf.linkedText(Label.CONTACT_PROVIDER, Label.STYLE_TD, Label.JAX_URL)
                        ),
                        Label.STYLE_BODY_TEXT3,
                        Label.TYPE_SQUARE
                )
        );


        tableBody = Arrays.asList(Arrays.asList(td));
        List<Integer> widths = Arrays.asList(169);
        heights = 354;
        tableLayout.setDefaultBorder(false);
        style = "tableExample2";

        row1Column2Contents.add(pdf.tableHelper(tableBody, widths, heights, style, tableLayout));


        List<Object> row = Arrays.asList(row1Column1Contents, row1Column2Contents);


        // Generate a Table
        widths = Arrays.asList(400, 160);
        table = new Table(widths, 837, Arrays.asList(row));

        dataHolder.put("table", table);
        dataHolder.put("style", "container");

        tableLayout.setDefaultBorder(false);
        dataHolder.put("layout", tableLayout);


        content.add(dataHolder);


        return content;
    }


    public Map<String, Object> logoHeaderDesign(String modelID, String qrCodeData) {


        //String qrCodePath = pdf.generateQRCode(modelID, qrCodeData);

        String base64Image = pdf.base64EncodeToString(pdf.imageToByteRemote(logoURL));

        String qrCodeImage = pdf.base64EncodeToString(pdf.generateQRCode(modelID, qrCodeData));

        // delete the qrCode File:
        //pdf.deleteFile(qrCodePath);

        List<Object> content = new ArrayList();
        Map<Object, Object> dataHolder = new HashMap<>();
        Map<Object, Object> inDataHolder = new HashMap<>();
        List<Object> tableRow = new ArrayList();
        List<List<Object>> tableBody = new ArrayList();

        Text text = new Text();
        Table table = new Table();
        TableLayout tableLayout = new TableLayout();

        Map<String, Object> row1Column1FirstContent = new HashMap<>();

        List<Column> columns = new ArrayList<>();
        Column column = new Column();
        column.setWidth(90);
        column.setMargin(Arrays.asList(20, 15, 0, 5));
        column.setImage(appendBase64 + base64Image);
        columns.add(column);

        // Create a new column
        column = new Column();
        column.setWidth(315);
        column.setMargin(Arrays.asList(80, 17, 0, 5));
        column.setLayout("headerLineOnly");
        column.setStyle("tableExample");


        // create row 1 and add to table body
        text = new Text("BRC0021PR", "h1");
        text.setAlignment("center");
        tableRow = Arrays.asList(text);
        tableBody.add(tableRow);

        // create row 2 and add to table body
        text = new Text("Invasive Ductal Carcinoma Not Otherwise Specified", "h2");
        tableRow = Arrays.asList(text);
        tableBody.add(tableRow);

        // Add table body to table
        table = new Table(tableBody);

        column.setTable(table);
        columns.add(column);

        //create a new column.
        column = new Column();
        column.setWidth(60);
        column.setMargin(Arrays.asList(40, 10, 0, 5));
        column.setImage(appendBase64 + qrCodeImage);
        columns.add(column);

        Map<Object, Object> tempData = new HashMap<>();
        tempData.put("fillColor", "#fff");
        tempData.put("columns", columns);


        tableBody = new ArrayList<>();
        tableRow = Arrays.asList(tempData);

        tableBody.add(tableRow);
        table = new Table(Arrays.asList(545), 20, tableBody);

        row1Column1FirstContent.put("table", table);
        row1Column1FirstContent.put("style", "titleArea");
        row1Column1FirstContent.put("layout", new TableLayout(true, "#ccc", "#ccc"));


        return row1Column1FirstContent;

    }


}
