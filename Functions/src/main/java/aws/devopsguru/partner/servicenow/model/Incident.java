package aws.devopsguru.partner.servicenow.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
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

    public String getClosedNotes() {
        return closedNotes;
    }

    public void setClosedNotes(String closedNotes) {
        this.closedNotes = closedNotes;
    }

    public Date getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(Date closedAt) {
        this.closedAt = closedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getSeverity() {
        return severity;
    }

    public void setSeverity(int severity) {
        this.severity = severity;
    }

    public int getImpact() {
        return impact;
    }

    public void setImpact(int impact) {
        this.impact = impact;
    }

    public String getCreatedBy() { return createdBy; }

    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public int getPriority() { return priority; }

    public void setPriority(int priority) { this.priority = priority; }

    public int getUrgency() {return urgency;}

    public void setUrgency(int urgency) { this.urgency = urgency; }
}