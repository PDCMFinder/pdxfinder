package org.pdxfinder.web.controllers.pdf;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;


/*
 * Created by abayomi on 29/10/2018.
 */
@JsonIgnoreProperties(ignoreUnknown = true)

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Table {

    private List<Integer> widths;
    private int heights;
    private List<List<Object>> body;
    private int headerRows;

    public Table() {
    }

    public Table(List<Integer> widths, int heights, List<List<Object>> body) {
        this.widths = widths;
        this.heights = heights;
        this.body = body;
    }

    public Table(List<Integer> widths, List<List<Object>> body) {
        this.widths = widths;
        this.body = body;
    }

    public Table(List<Integer> widths, int heights, List<List<Object>> body, int headerRows) {
        this.widths = widths;
        this.heights = heights;
        this.body = body;
        this.headerRows = headerRows;
    }

    public Table(List<List<Object>> body) {
        this.body = body;
    }


    public List<Integer> getWidths() {
        return widths;
    }

    public void setWidths(List<Integer> widths) {
        this.widths = widths;
    }

    public int getHeights() {
        return heights;
    }

    public void setHeights(int heights) {
        this.heights = heights;
    }

    public List<List<Object>> getBody() {
        return body;
    }

    public void setBody(List<List<Object>> body) {
        this.body = body;
    }

    public int getHeaderRows() {
        return headerRows;
    }

    public void setHeaderRows(int headerRows) {
        this.headerRows = headerRows;
    }
}


