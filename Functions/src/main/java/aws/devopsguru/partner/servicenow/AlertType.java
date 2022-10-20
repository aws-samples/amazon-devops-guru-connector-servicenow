package aws.devopsguru.partner.servicenow;

import aws.devopsguru.partner.servicenow.model.Incident;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.fasterxml.jackson.databind.JsonNode;

public class AlertType {

    private static final ServiceNowConnector serviceNowConnector = new ServiceNowConnector();

    public static void newInsight(JsonNode input, Context context) {
        Incident newIncident = ServiceNowConnector.createIncidentFromInsight(input);
        serviceNowConnector.createIncident(newIncident, context);
    }

    public static void insightClosed(JsonNode input, Context context) {
        serviceNowConnector.closeIncident(input.path("detail").path("insightId").asText(), context);
    }

    public static void newAssociation(JsonNode input, Context context) {
        serviceNowConnector.newAnomalyAssociation(input, context);
    }

    public static void newRecommendation(JsonNode input, Context context) {
        serviceNowConnector.newRecommendation(input, context);
    }

    public static void newSeverity(JsonNode input, Context context) {
        int serviceNowSeverity = ServiceNowConnector.getServiceNowSeverity(input.path("detail").path("insightSeverity").asText());
        String insightId = input.path("detail").path("insightId").asText();
        serviceNowConnector.severityUpgrade(insightId, context, serviceNowSeverity);
    }

    public static void allFeatures(JsonNode input, Context context) {
        LambdaLogger logger = context.getLogger();

        JsonNode jsonNodeDetail = input.path("detail");
        String messageType = jsonNodeDetail.path("messageType").asText();
        logger.log("Message type: " + messageType + " InsightId: " + input.path("detail").path("insightId").asText());

        // All 5 triggers
        if (input.path("detail").path("messageType").asText().equals("NEW_INSIGHT")) {
            AlertType.newInsight(input, context);
        } else if (input.path("detail").path("messageType").asText().equals("CLOSED_INSIGHT")) {
            AlertType.insightClosed(input, context);
        } else if (input.path("detail").path("messageType").asText().equals("NEW_ASSOCIATION")) {
            AlertType.newAssociation(input, context);
        } else if (input.path("detail").path("messageType").asText().equals("NEW_RECOMMENDATION")) {
            AlertType.newRecommendation(input, context);
        } else if (input.path("detail").path("messageType").asText().equals("SEVERITY_UPGRADED")) {
            AlertType.newSeverity(input, context);
        } else {
            logger.log("Unknown messageType: " + messageType);
        }
    }

    public static void reactiveHighSeverityOnly(JsonNode input, Context context) {

        // FILTER: Insight Open + Insight Closed
        if (input.path("detail").path("messageType").asText().equals("NEW_INSIGHT")) {
            // Filter only reactive high severity insights
            if (input.path("detail").path("insightSeverity").asText().equals("high") &&
                    input.path("detail").path("insightType").asText().equals("REACTIVE")) {
                AlertType.newInsight(input, context);
            }
        } else if (input.path("detail").path("messageType").asText().equals("CLOSED_INSIGHT")) {
            AlertType.insightClosed(input, context);
        }

    }

    public static void reactiveMediumSeverityOnly(JsonNode input, Context context) {

        // FILTER: Insight Open + Insight Closed
        if (input.path("detail").path("messageType").asText().equals("NEW_INSIGHT")) {
            // Filter only reactive high severity insights
            if (input.path("detail").path("insightSeverity").asText().equals("medium") &&
                    input.path("detail").path("insightType").asText().equals("REACTIVE")) {
                AlertType.newInsight(input, context);
            }
        } else if (input.path("detail").path("messageType").asText().equals("CLOSED_INSIGHT")) {
            AlertType.insightClosed(input, context);
        }

    }

    public static void reactiveLowSeverityOnly(JsonNode input, Context context) {

        // FILTER: Insight Open + Insight Closed
        if (input.path("detail").path("messageType").asText().equals("NEW_INSIGHT")) {
            // Filter only reactive high severity insights
            if (input.path("detail").path("insightSeverity").asText().equals("low") &&
                    input.path("detail").path("insightType").asText().equals("REACTIVE")) {
                AlertType.newInsight(input, context);
            }
        } else if (input.path("detail").path("messageType").asText().equals("CLOSED_INSIGHT")) {
            AlertType.insightClosed(input, context);
        }

    }

    public static void proactiveHighSeverityOnly(JsonNode input, Context context) {

        // FILTER: Insight Open + Insight Closed
        if (input.path("detail").path("messageType").asText().equals("NEW_INSIGHT")) {
            // Filter only reactive high severity insights
            if (input.path("detail").path("insightSeverity").asText().equals("high") &&
                    input.path("detail").path("insightType").asText().equals("PROACTIVE")) {
                AlertType.newInsight(input, context);
            }
        } else if (input.path("detail").path("messageType").asText().equals("CLOSED_INSIGHT")) {
            AlertType.insightClosed(input, context);
        }

    }

    public static void proactiveMediumSeverityOnly(JsonNode input, Context context) {

        // FILTER: Insight Open + Insight Closed
        if (input.path("detail").path("messageType").asText().equals("NEW_INSIGHT")) {
            // Filter only reactive high severity insights
            if (input.path("detail").path("insightSeverity").asText().equals("medium") &&
                    input.path("detail").path("insightType").asText().equals("PROACTIVE")) {
                AlertType.newInsight(input, context);
            }
        } else if (input.path("detail").path("messageType").asText().equals("CLOSED_INSIGHT")) {
            AlertType.insightClosed(input, context);
        }

    }

    public static void proactiveLowSeverityOnly(JsonNode input, Context context) {

        // FILTER: New Insight + Insight Closed
        if (input.path("detail").path("messageType").asText().equals("NEW_INSIGHT")) {
            // Filter only reactive high severity insights
            if (input.path("detail").path("insightSeverity").asText().equals("low") &&
                    input.path("detail").path("insightType").asText().equals("PROACTIVE")) {
                AlertType.newInsight(input, context);
            }
        } else if (input.path("detail").path("messageType").asText().equals("CLOSED_INSIGHT")) {
            AlertType.insightClosed(input, context);
        }

    }

    public static void InsightOpenAndRecommendations(JsonNode input, Context context) {

        // FILTER: New Insight + Insight Closed + New Recommendation
        if (input.path("detail").path("messageType").asText().equals("NEW_INSIGHT")) {
            AlertType.newInsight(input, context);
        } else if (input.path("detail").path("messageType").asText().equals("CLOSED_INSIGHT")) {
            AlertType.insightClosed(input, context);
        } else if (input.path("detail").path("messageType").asText().equals("NEW_RECOMMENDATION")) {
            AlertType.newRecommendation(input, context);
        }
    }

    public static void InsightOpenAndAnomalies(JsonNode input, Context context) {

        // FILTER: New Insight + Insight Closed + New Recommendation
        if (input.path("detail").path("messageType").asText().equals("NEW_INSIGHT")) {
            AlertType.newInsight(input, context);
        } else if (input.path("detail").path("messageType").asText().equals("CLOSED_INSIGHT")) {
            AlertType.insightClosed(input, context);
        } else if (input.path("detail").path("messageType").asText().equals("NEW_ASSOCIATION")) {
            AlertType.newAssociation(input, context);
        }
    }

    public static void InsightOpenAndSeverityUpgraded(JsonNode input, Context context) {

        // FILTER: New Insight + Insight Closed + New Recommendation
        if (input.path("detail").path("messageType").asText().equals("NEW_INSIGHT")) {
            AlertType.newInsight(input, context);
        } else if (input.path("detail").path("messageType").asText().equals("CLOSED_INSIGHT")) {
            AlertType.insightClosed(input, context);
        } else if (input.path("detail").path("messageType").asText().equals("SEVERITY_UPGRADED")) {
            AlertType.newSeverity(input, context);
        }
    }
}