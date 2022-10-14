package aws.devopsguru.partner.servicenow.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class Incident {
    @JsonProperty("sys_id")
    private String id;

    @JsonProperty("number")
    private String number;

    @JsonProperty("short_description")
    private String shortDescription;

    @JsonProperty("description")
    private String description;

    @JsonProperty("severity")
    private int severity;

    @JsonProperty("impact")
    private int impact;

    @JsonProperty("sys_created_by")
    private String createdBy;

    @JsonProperty("priority")
    private int priority;

    @JsonProperty("urgency")
    private int urgency;

    @JsonProperty("closed_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date closedAt;

    @JsonProperty("close_notes")
    private String closedNotes;
}