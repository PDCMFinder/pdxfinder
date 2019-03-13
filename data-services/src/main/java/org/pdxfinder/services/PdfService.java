package org.pdxfinder.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.pdxfinder.graph.dao.QualityAssurance;
import org.pdxfinder.services.dto.*;
import org.pdxfinder.services.pdf.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.xml.bind.DatatypeConverter;
import java.util.*;

/*
 * Created by abayomi on 02/11/2018.
 */
@Service
public class PdfService {


    private String logoURL = "https://ebiwd.github.io/pdx-visual-framework/images/logo.png";
    String appendBase64 = "data:image/png;base64,";

    private ObjectMapper mapper = new ObjectMapper();
    private PdfHelper pdf = new PdfHelper();


    private Logger logger = LoggerFactory.getLogger(PdfService.class);


    public List<Object> generatePdf(DetailsDTO data, String modelUrl) {
    /*
        List<Object> content = new ArrayList();
        Map<Object, Object> dataHolder = new HashMap<>();
        List<List<Object>> tableBody = new ArrayList();


        // Row 1 Column 1 Data:
        List<Object> row1Column1Contents = new ArrayList<>();

        // Row 1 Column 1 1st content
        Object firstContent = logoHeaderDesign(data, modelUrl);
        row1Column1Contents.add(firstContent);

        row1Column1Contents.add(
                pdf.headTitle(Label.PDX_MODEL.val())
        );
        row1Column1Contents.add(
                pdf.canvasLine(380, Label.COLOR_PDX_SECONDARY, "1")
        );


        Map<String, String> patient = new LinkedHashMap<>();

        patient.put(Label.TXT_SEX, data.getPatientSex());
        patient.put(Label.TXT_COLLECTION_AGE, data.getAgeAtTimeOfCollection());
        patient.put(Label.TXT_RACE, data.getRaceOrEthnicity());  /////

        row1Column1Contents.add(
                pdf.pdxFinderTable(patient, Label.TXT_PATIENT)
        );


        Map<String, String> patientTumor = new LinkedHashMap<>();

        patientTumor.put(Label.TXT_HISTOLOGY, data.getMappedOntology());
        patientTumor.put(Label.TXT_TISSUE, data.getPrimaryTissue());
        patientTumor.put(Label.TXT_SITE, data.getCollectionSite());
        patientTumor.put(Label.TXT_TUMOR, data.getTumorType());
        patientTumor.put(Label.TXT_GRADE, data.getGrade());  /////
        patientTumor.put(Label.TXT_STAGE, data.getStage());  //////

        row1Column1Contents.add(
                pdf.pdxFinderTable(patientTumor, Label.TXT_PATIENT_TUMOR)
        );


        /*row1Column1Contents.add(
                pdf.headTitle(Label.TXT_ENGRAFTMENT, Arrays.asList(0, 15, 0, 5))
        );
        row1Column1Contents.add(
                pdf.canvasLine(560, Label.COLOR_PDX_SECONDARY, "1")
        );*/

    /*
        row1Column1Contents.add(
                pdf.doubleTableHead(Label.TXT_ENGRAFTMENT, 6, Arrays.asList(90, 90, 90, 90, 73, 70), Arrays.asList(0, 7, 0, -6))
        );

        List<Map<String, String>> dataList = new ArrayList<>();
        try{
            Set<EngraftmentDataDTO> edtoSet = data.getPdxModelList();

            for (EngraftmentDataDTO edto : edtoSet) {

                Map<String, String> edtoMap = mapper.convertValue(edto, Map.class);
                edtoMap.remove("strainSymbol");

                dataList.add(edtoMap);
            }

            row1Column1Contents.add(pdf.pdxFinderTable(
                    dataList,
                    Label.TXT_ENGRAFTMENT_TABLE_HEAD,
                    Arrays.asList(90, 90, 90, 90, 73, 70))
            );

        }catch (Exception e){

            row1Column1Contents.add(pdf.emptyContentTable(
                    Label.TXT_EMPTY,
                    Label.TXT_ENGRAFTMENT_TABLE_HEAD,
                    Arrays.asList(90, 90, 90, 90, 73, 70))
            );
        }







        /*row1Column1Contents.add(
                pdf.headTitle(Label.TXT_QC, Arrays.asList(0, 15, 0, 5))
        );
        row1Column1Contents.add(
                pdf.canvasLine(560, Label.COLOR_PDX_SECONDARY, "1")
        );*/


/*
        row1Column1Contents.add(
                pdf.doubleTableHead(Label.TXT_QC, 3, Arrays.asList(170, 250, 110), Arrays.asList(0, 7, 0, -6))
        );

        dataList = new ArrayList<>();
        try{

            List<QualityControlDTO> qaList = data.getModelQualityControl();
            for (QualityControlDTO qa : qaList) {

                Map<String, String> qaMap = mapper.convertValue(qa, Map.class);
                qaMap.remove("validationHostStrain");
                dataList.add(qaMap);
            }

            row1Column1Contents.add(pdf.pdxFinderTable(
                    dataList,
                    Label.TXT_QC_TABLE_HEAD,
                    Arrays.asList(170, 250, 110))
            );

        }catch (Exception e){

            row1Column1Contents.add(pdf.emptyContentTable(
                    Label.TXT_EMPTY,
                    Label.TXT_QC_TABLE_HEAD,
                    Arrays.asList(170, 250, 110))
            );
        }






        // Generate Patients Tumor Collection For PDX Model TABLE
        row1Column1Contents.add(
                pdf.headTitle(Label.TXT_PATIENT, Arrays.asList(0, 25, 0, 5))
        );
        // Horizontal Line
        row1Column1Contents.add(pdf.tinLine(560, Label.COLOR_PDX_SECONDARY));

        int count = 0;

        //1.) Define Data Holder for Table Header with empty content on first column
        List<String> tumorCollectionHeader = new ArrayList<>();
        tumorCollectionHeader.add(" ");


        //2.)  Define Data Holder  for other rows and Initialize Contents of First Column
        Map<String, String> collectionAges = new LinkedHashMap<>();
        collectionAges.put(Label.TXT_AGE + count, Label.TXT_AGE);

        Map<String, String> collectionDiagnosis = new LinkedHashMap<>();
        collectionDiagnosis.put(Label.TXT_DIAGNOSIS + count, Label.TXT_DIAGNOSIS);

        Map<String, String> collectionType = new LinkedHashMap<>();
        collectionType.put(Label.TXT_TUMOR_TYPE + count, Label.TXT_TUMOR_TYPE);

        Map<String, String> collectionPdxMouse = new LinkedHashMap<>();
        collectionPdxMouse.put(Label.TXT_MOUSE + count, Label.TXT_MOUSE);

        Map<String, String> collectionSite = new LinkedHashMap<>();
        collectionSite.put(Label.TXT_SITE + count, Label.TXT_SITE);


        //3.) Define Data Holder for Column Widths and initialize value first column
        List<Object> columnWidths = new ArrayList<>();
        columnWidths.add(80);







        // Add data for other columns
        // PATIENT TUMOR COLLECTION DATA TABLE
        for (CollectionEventsDTO event : data.getPatient().getCollectionEvents()) {

            if (event.getPdxMouse().equals(data.getModelId())) {
                tumorCollectionHeader.add("Collection (Current Model)");
            } else {
                tumorCollectionHeader.add("Collection (Other Model)");
            }

            count++;
            collectionAges.put(Label.TXT_AGE + count, event.getAge());
            collectionDiagnosis.put(Label.TXT_DIAGNOSIS + count, event.getDiagnosis());
            collectionType.put(Label.TXT_TUMOR_TYPE + count, event.getType());
            collectionPdxMouse.put(Label.TXT_MOUSE + count, event.getPdxMouse());
            collectionSite.put(Label.TXT_SITE + count, event.getCollectionSite());

        }

        dataList = new ArrayList<>();
        dataList.add(collectionAges);
        dataList.add(collectionDiagnosis);
        dataList.add(collectionType);
        dataList.add(collectionPdxMouse);
        dataList.add(collectionSite);

        int firstColumnWidth = 80;
        int tableWidth = 550;
        int size = data.getPatient().getCollectionEvents().size();
        List<Object> widthList = pdf.dynamicColumnWidth(tableWidth, firstColumnWidth, size);


        row1Column1Contents.add(
                pdf.doubleTableHead(Label.TXT_PATIENT_COLLECTION, widthList.size(), widthList, Arrays.asList(0, 7, 0, -6))
        );
        row1Column1Contents.add(pdf.pdxFinderTable(
                dataList,
                tumorCollectionHeader,
                widthList
                )
        );









        // PATIENT THERAPIES AND RESPONSE TABLE
        Boolean treatmentExists = data.getPatient().getTreatmentExists();
        if (treatmentExists){


            row1Column1Contents.add(
                    pdf.goToNewPage()
            );
            row1Column1Contents.add(
                    pdf.doubleTableHead(Label.TXT_THERAPY, 5, Arrays.asList(60, 130, 140, 90, 90), Arrays.asList(0, 7, 0, -6))
            );

            dataList = new ArrayList<>();
            try{

                List<TreatmentSummaryDTO> tsList = data.getPatient().getTreatmentSummaries();
                for (TreatmentSummaryDTO ts : tsList) {

                    Map<String, String> tsMap = mapper.convertValue(ts, Map.class);
                    tsMap.remove("current");
                    dataList.add(tsMap);
                }

                row1Column1Contents.add(pdf.pdxFinderTable(
                        dataList,
                        Label.TXT_THERAPY_TABLE_HEAD,
                        Arrays.asList(60, 130, 140, 90, 90))
                );

            }catch (Exception e){

                row1Column1Contents.add(pdf.emptyContentTable(
                        Label.TXT_EMPTY,
                        Label.TXT_THERAPY_TABLE_HEAD,
                        Arrays.asList(60, 130, 140, 90, 90))
                );
            }

        }else {

            row1Column1Contents.add(
                    pdf.doubleTableHead(Label.TXT_THERAPY, 5, Arrays.asList(60, 130, 140, 90, 90), Arrays.asList(0, 7, 0, -6))
            );
            row1Column1Contents.add(pdf.emptyContentTable(
                    Label.TXT_EMPTY,
                    Label.TXT_THERAPY_TABLE_HEAD,
                    Arrays.asList(60, 130, 140, 90, 90))
            );

            row1Column1Contents.add(
                    pdf.goToNewPage()
            );
        }






        // MOLECULAR DATA TABLE
        row1Column1Contents.add(
                pdf.headTitle(Label.TXT_MOLECULAR_DATA, Arrays.asList(0, 25, 0, 5))
        );
        row1Column1Contents.add(
                pdf.canvasLine(560, Label.COLOR_PDX_SECONDARY, "1")
        );


        dataList = new ArrayList<>();
        try {

            List<Map> molDataList = data.getDataSummary();
            for (Map molData : molDataList) {

               // Map<String, String> molDataMap = mapper.convertValue(molData, LinkedHashMap.class);

                Map<String, String> molDataMap = new LinkedHashMap<>();
                molDataMap.put("sampleId",molData.get("sampleId").toString());
                molDataMap.put("sampleType",molData.get("sampleType").toString());
                molDataMap.put("xenograftPassage",molData.get("xenograftPassage").toString());
                molDataMap.put("dataAvailable",molData.get("dataAvailable").toString());
                molDataMap.put("platformUsed",molData.get("platformUsed").toString());
                molDataMap.put("rawData",molData.get("rawData").toString());

                dataList.add(molDataMap);
            }

            row1Column1Contents.add(pdf.pdxFinderTable(
                    dataList,
                    Label.TXT_MOLECULAR_DATA_TABLE_HEAD,
                    Arrays.asList(90, 90, 90, 90, 73, 70))
            );

        } catch (Exception e) {

            row1Column1Contents.add(pdf.emptyContentTable(
                    Label.TXT_EMPTY,
                    Label.TXT_MOLECULAR_DATA_TABLE_HEAD,
                    Arrays.asList(90, 90, 90, 90, 73, 70))
            );
        }









        // DOSING STUDY TABLE
        row1Column1Contents.add(
                pdf.headTitle(Label.TXT_DOSING, Arrays.asList(0, 25, 0, 5))
        );
        row1Column1Contents.add(
                pdf.canvasLine(560, Label.COLOR_PDX_SECONDARY, "1")
        );


        dataList = new ArrayList<>();
        try {

            List<DrugSummaryDTO> dsList = data.getDrugSummary();
            for (DrugSummaryDTO ds : dsList) {

                Map<String, String> dsMap = mapper.convertValue(ds, Map.class);
                dsMap.remove("duration");
                dataList.add(dsMap);
            }

            row1Column1Contents.add(pdf.pdxFinderTable(
                    dataList,
                    Label.TXT_DOSING_TABLE_HEAD,
                    Arrays.asList(200, 140, 190))
            );

        } catch (Exception e) {

            row1Column1Contents.add(pdf.emptyContentTable(
                    Label.TXT_EMPTY,
                    Label.TXT_DOSING_TABLE_HEAD,
                    Arrays.asList(200, 140, 190))
            );
        }















        // PDX MODEL INFO [RIGHT COLUMN, BLUE BACKGROUND]
        List<Object> row1Column2Contents = new ArrayList<>();
        List<Object> td = new ArrayList<>();

        td.add(pdf.headTitle(Label.INFO, Arrays.asList(4, 121, 0, 5)));
        td.add(pdf.canvasLine(160, "#00b2d5", "1"));



        String contact = (data.getContacts() != null) ? data.getContacts() : "Not Specified";
        String externalURL = (data.getExternalUrl() != null) ? data.getExternalUrl() : "Not Specified";



        List<Text> contactLinks = new ArrayList<>();
        contactLinks.add(
                pdf.linkedText(Label.PDX_LABEL, Label.STYLE_TD, "http://" + modelUrl)
        );


        if (contact.contains("http")) {
            contactLinks.add(
                    pdf.linkedText(Label.CONTACT_PROVIDER, Label.STYLE_TD, contact)
            );
        }

        if (contact.contains("@")) {
            contactLinks.add(
                    pdf.linkedText(Label.CONTACT_PROVIDER, Label.STYLE_TD, "mailto:" + contact + "?subject=" + data.getModelId())
            );
        }

        if (externalURL.contains("http")) {
            contactLinks.add(
                    pdf.linkedText("View on " + data.getDataSource(), Label.STYLE_TD, externalURL)
            );
        }



        td.add(pdf.plainText(Label.TXT_ABOUT_MODEL, Label.STYLE_TABLE_H3, Label.TRUE));
        td.add(
                pdf.listText(
                        contactLinks,
                        Label.STYLE_BODY_TEXT3,
                        Label.TYPE_SQUARE
                )
        );




        String dataSource = (data.getSourceName() != null) ? data.getSourceName() : "Not Specified";

        td.add(pdf.plainText(Label.DATA_PROVIDER, Label.STYLE_TABLE_H3, Label.TRUE));
        td.add(
                pdf.listText(
                        Arrays.asList(pdf.plainText(dataSource, Label.NULL, Label.FALSE)),
                        Label.STYLE_BODY_TEXT3,
                        Label.TYPE_SQUARE
                )
        );

        /*td.add(pdf.plainText(Label.SUBMIT, Label.STYLE_TABLE_H3, Label.TRUE));
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
        );*/



/*
        tableBody = Arrays.asList(Arrays.asList(td));
        List<Object> widths = Arrays.asList(169);
        int heights = 354;
        TableLayout tableLayout = new TableLayout();
        tableLayout.setDefaultBorder(false);

        logger.info(tableBody.toString()+ " AAAAAA");
        row1Column2Contents.add(pdf.tableHelper(tableBody, widths, heights, Label.STYLE_TABLE2, tableLayout));


        List<Object> row = Arrays.asList(row1Column1Contents, row1Column2Contents);


        // Generate a Table
        widths = Arrays.asList(400, 160);

        dataHolder.put("table", new Table(widths, 837, Arrays.asList(row)));
        dataHolder.put("style", "container");

        tableLayout.setDefaultBorder(false);
        dataHolder.put("layout", tableLayout);


        content.add(dataHolder);

    */
        //return content;
        return null;
    }


    public Map<String, Object> logoHeaderDesign(DetailsDTO data, String qrCodeData) {


        //String qrCodePath = pdf.generateQRCode(modelID, qrCodeData);

        String base64Image = "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";
        //pdf.base64EncodeToString(pdf.imageToByteRemote(logoURL));

        String qrCodeImage = pdf.base64EncodeToString(pdf.generateQRCode(data.getModelId(), qrCodeData));

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
        column.setWidth(60);
        column.setMargin(Arrays.asList(20, 25, 0, 5));
        column.setImage(appendBase64 + base64Image);
        columns.add(column);

        // Create a new column
        column = new Column();
        column.setWidth(315);
        column.setMargin(Arrays.asList(80, 17, 0, 5));
        column.setLayout("headerLineOnly");
        column.setStyle("tableExample");


        // create row 1 and add to table body
        text = new Text(data.getModelId(), "h1");
        text.setAlignment("center");
        tableRow = Arrays.asList(text);
        tableBody.add(tableRow);

        // create row 2 and add to table body
        text = new Text(data.getMappedOntology()+"\n"+data.getDataSourceAbbrev(), "h2");
        text.setAlignment("center");
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


    public Object generateFooter() {


        Text text = pdf.linkedText(Label.WEBSITE, Label.NULL, Label.PDX_FINDER_URL);
        text.setMargin(Arrays.asList(20, -18, 0, -2));
        text.setStyle("footerText");
        List row1 = Arrays.asList(text);

        List row2 = Arrays.asList(
                pdf.tinLine(20, 575, -10, "#ccc")
        );

        text = pdf.plainText(Label.PDX_DEV_INFO, "footerText", Label.FALSE);
        text.setMargin(Arrays.asList(20, -10, 0, -2));
        List row3 = Arrays.asList(text);

        return pdf.singleColumnTable(Arrays.asList(row1, row2, row3), 560);


    }


}
