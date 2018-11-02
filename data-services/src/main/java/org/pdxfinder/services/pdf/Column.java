package org.pdxfinder.services.pdf;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/*
 * Created by abayomi on 30/10/2018.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Column {


    private int width;
    private List<Integer> margin;
    private Object layout;
    private String style;
    private Table table;
    private String image;
    private Object text;


    public Column() {
    }

    public Column(int width, List<Integer> margin, Object layout, String style, Table table, String image, Object text) {
        this.width = width;
        this.margin = margin;
        this.layout = layout;
        this.style = style;
        this.table = table;
        this.image = image;
        this.text = text;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public List<Integer> getMargin() {
        return margin;
    }

    public void setMargin(List<Integer> margin) {
        this.margin = margin;
    }

    public void setLayout(Object layout) {
        this.layout = layout;
    }

    public Object getLayout() {
        return layout;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setText(Object text) {
        this.text = text;
    }

    public Object getText() {
        return text;
    }
}
