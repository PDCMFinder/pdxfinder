package org.pdxfinder.web.controllers.pdf;

import java.util.*;

/*
 * Created by abayomi on 30/10/2018.
 */
public class PdfHelper {


    public Text boldText(String dText) {

        Text text = new Text();
        text.setText(dText);
        text.setColor("#000");
        text.setBold(true);

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

        CanvasLine canvasLine = new CanvasLine();

        canvasLine.setType("line");
        canvasLine.setX1(0);
        canvasLine.setY1(1);
        canvasLine.setX2(width);
        canvasLine.setY2(1);
        canvasLine.setLineWidth(thickness);
        canvasLine.setLineColor(color);

        Map<String, List<Object>> line = new HashMap<>();

        line.put("canvas", Arrays.asList(canvasLine));

        return line;
    }





}
