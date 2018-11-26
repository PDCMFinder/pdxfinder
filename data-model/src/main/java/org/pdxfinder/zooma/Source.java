package org.pdxfinder.zooma;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "uri",
        "name",
        "topic",
        "type"
})
public class Source {

    private String uri;
    private String name;
    private List<String> topic = null;
    private String type;

    public Source() {
    }

    public Source(String uri, String name, List<String> topic, String type) {
        this.uri = uri;
        this.name = name;
        this.topic = topic;
        this.type = type;
    }

    @JsonProperty("uri")
    public String getUri() {
        return uri;
    }

    @JsonProperty("uri")
    public void setUri(String uri) {
        this.uri = uri;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("topic")
    public List<String> getTopic() {
        return topic;
    }

    @JsonProperty("topic")
    public void setTopic(List<String> topic) {
        this.topic = topic;
    }

    @JsonProperty("type")
    public String getType() {
        return type;
    }

    @JsonProperty("type")
    public void setType(String type) {
        this.type = type;
    }

}