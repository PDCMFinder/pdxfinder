package org.pdxfinder.services.pdf;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;


/*
 * Created by abayomi on 31/10/2018.
 */
@JsonIgnoreProperties(ignoreUnknown = true)

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Style {


    private int fontSize;
    private Boolean bold;
    private List<Integer> margin;
    private Boolean italic;
    private String alignment;
    private String fillColor;
    private String color;
    private String lineHeight;

    public Style() {
    }

    public Style(int fontSize, Boolean bold, List<Integer> margin) {
        this.fontSize = fontSize;
        this.bold = bold;
        this.margin = margin;
    }

    public Style(List<Integer> margin) {
        this.margin = margin;
    }

    public Style(int fontSize, Boolean bold, List<Integer> margin, Boolean italic, String alignment, String fillColor, String color) {
        this.fontSize = fontSize;
        this.bold = bold;
        this.margin = margin;
        this.italic = italic;
        this.alignment = alignment;
        this.fillColor = fillColor;
        this.color = color;
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
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

    public Boolean getItalic() {
        return italic;
    }

    public void setItalic(Boolean italic) {
        this.italic = italic;
    }

    public String getAlignment() {
        return alignment;
    }

    public void setAlignment(String alignment) {
        this.alignment = alignment;
    }

    public String getFillColor() {
        return fillColor;
    }

    public void setFillColor(String fillColor) {
        this.fillColor = fillColor;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getLineHeight() {
        return lineHeight;
    }

    public void setLineHeight(String lineHeight) {
        this.lineHeight = lineHeight;
    }
}
