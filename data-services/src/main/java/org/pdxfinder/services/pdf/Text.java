package org.pdxfinder.services.pdf;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/*
 * Created by abayomi on 29/10/2018.
 */
@JsonIgnoreProperties(ignoreUnknown = true)

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Text {

    private String text;
    private String style;
    private String fontSize;
    private String alignment;
    private Boolean bold;
    private List<Integer> margin;
    private String color;
    private Boolean italics;
    private String fillColor;
    private String colSpan;
    private List<Boolean> border;
    private String link;
    private String pageBreak;


    public Text() {
    }

    public Text(String text, String style, String fontSize, String alignment,
                Boolean bold, List<Integer> margin, String color,
                Boolean italics, String fillColor, String colSpan,
                List<Boolean> border, String link
    ) {
        this.text = text;
        this.style = style;
        this.fontSize = fontSize;
        this.alignment = alignment;
        this.bold = bold;
        this.margin = margin;
        this.color = color;
        this.italics = italics;
        this.fillColor = fillColor;
        this.colSpan = colSpan;
        this.border = border;
        this.link = link;
    }

    public Text(String text, String fontSize, String alignment, Boolean bold, List<Integer> margin) {
        this.text = text;
        this.fontSize = fontSize;
        this.alignment = alignment;
        this.bold = bold;
        this.margin = margin;
    }


    public Text(String text, Boolean italics, String style) {
        this.text = text;
        this.style = style;
        this.italics = italics;
    }


    public Text(String text) {
        this.text = text;
    }

    public Text(String text, List<Integer> margin) {
        this.text = text;
        this.margin = margin;
    }

    public Text(String text, String style) {
        this.text = text;
        this.style = style;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public String getFontSize() {
        return fontSize;
    }

    public void setFontSize(String fontSize) {
        this.fontSize = fontSize;
    }

    public String getAlignment() {
        return alignment;
    }

    public void setAlignment(String alignment) {
        this.alignment = alignment;
    }

    public Boolean getBold() {
        return bold;
    }

    public void setBold(Boolean bold) {
        this.bold = bold;
    }

    public List<Integer> getMargin() {
        return margin;
    }

    public void setMargin(List<Integer> margin) {
        this.margin = margin;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Boolean getItalics() {
        return italics;
    }

    public void setItalics(Boolean italics) {
        this.italics = italics;
    }

    public void setFillColor(String fillColor) {
        this.fillColor = fillColor;
    }

    public String getFillColor() {
        return fillColor;
    }

    public String getColSpan() {
        return colSpan;
    }

    public void setColSpan(String colSpan) {
        this.colSpan = colSpan;
    }

    public List<Boolean> getBorder() {
        return border;
    }

    public void setBorder(List<Boolean> border) {
        this.border = border;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public void setPageBreak(String pageBreak) {
        this.pageBreak = pageBreak;
    }

    public String getPageBreak() {
        return pageBreak;
    }
}
