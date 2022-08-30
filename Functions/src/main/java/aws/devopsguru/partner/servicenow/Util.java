package aws.devopsguru.partner.servicenow;

import aws.devopsguru.partner.servicenow.model.Incident;
import com.amazonaws.services.devopsguru.model.InsightSeverity;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Date;
import java.util.Iterator;

public class Util {

    public static final String NEW_LINE = System.lineSeparator();

    /**
     * Create and customize new incident
     *
     * @param input insight
     * @return new Incident
     */
    public static Incident createIncidentFromInsight(JsonNode input) {
        Incident incident = new Incident();
        String severity = null;
        if (input != null && input.path("detail") != null) {
            JsonNode detail = input.path("detail");
            incident.setShortDescription(detail.path("insightDescription").asText()
                    + " (Timestamp: " + new Date().getTime() + ")");
            severity = detail.path("insightSeverity").asText();

            StringBuilder description = new StringBuilder(String.format("Insight Id: %s" + NEW_LINE
                            + "Source: %s" + NEW_LINE
                            + "Message type: %s" + NEW_LINE
                            + "Detailed type: %s" + NEW_LINE
                            + "Creation time: %s" + NEW_LINE
                            + "Region: %s" + NEW_LINE
                            + "Insight type: %s" + NEW_LINE
                            + "Insight URL: %s" + NEW_LINE,
                    input.path("detail").path("insightId").asText(),
                    input.path("source").asText(),
                    input.path("detail").path("messageType").asText(),
                    input.path("detail-type"),
                    input.path("time").asText(),
                    input.path("region").asText(),
                    input.path("detail").path("insightType").asText(),
                    input.path("detail").path("insightUrl").asText()));

            description.append("Anomalies: ").append(NEW_LINE);
            description.append(getAnomaliesAsDescription(input.path("detail").path("anomalies")));

            incident.setDescription(description.toString());

            incident.setSeverity(getServiceNowSeverity(severity));
        }

        return incident;
    }

    public static String getAnomaliesAsDescription(JsonNode anomalies) {
        StringBuilder anomalyDetails = new StringBuilder();

        // Add the new anomalies into 1 string
        for (JsonNode anomaly : anomalies) {
            for (JsonNode sourceDetail : anomaly.path("sourceDetails")) {
                anomalyDetails.append(String.format("Data Source: %s" + NEW_LINE
                                + "Name: %s" + NEW_LINE
                                + "Stat: %s" + NEW_LINE,
                        sourceDetail.path("dataSource").asText(),
                        sourceDetail.path("dataIdentifiers").path("name").asText(),
                        sourceDetail.path("dataIdentifiers").path("stat").asText()));
            }
            anomalyDetails.append(NEW_LINE);
        }
        return anomalyDetails.toString();
    }

    // Mapping DevOps Guru insight to ServiceNow incident
    public static int getServiceNowSeverity(String awsSeverity) {
        if (InsightSeverity.HIGH.toString().equals(awsSeverity)) {
            return 5;
        } else if (InsightSeverity.MEDIUM.toString().equals(awsSeverity)) {
            return 3;
        } else {
            return 1;
        }
    }
}
