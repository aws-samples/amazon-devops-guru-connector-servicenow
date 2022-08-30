package aws.devopsguru.partner.servicenow;

import aws.devopsguru.partner.servicenow.model.Incident;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

/**
 * InsightHandler for requests to Lambda function.
 */
public class InsightHandler implements RequestHandler<Map<String, Object>, String> {
    ObjectMapper objectMapper = new ObjectMapper();
    ServiceNowConnector serviceNowConnector = new ServiceNowConnector();

    public String handleRequest(Map<String, Object> input, final Context context) {

        Incident incident;
        LambdaLogger logger = context.getLogger();
        try {
            logger.log("Request handler for ServiceNow.");

            String inputString = objectMapper.writeValueAsString(input);
            JsonNode jsonNode = objectMapper.readTree(inputString);
            JsonNode jsonNodeDetail = jsonNode.path("detail");
            String messageType = jsonNodeDetail.path("messageType").asText();
            logger.log("Message type: " + messageType + " InsightId: " + jsonNode.path("detail").path("insightId").asText());
            logger.log(jsonNode.toString());

            // Based on the messageType of the event, route to different logic for the alert
            if ("NEW_INSIGHT".equals(messageType)) {
                Incident newIncident = Util.createIncidentFromInsight(jsonNode);
                incident = serviceNowConnector.createIncident(newIncident, context);
            } else if ("CLOSED_INSIGHT".equals(messageType)) {
                incident = serviceNowConnector.closeIncident(jsonNode.path("detail").path("insightId").asText(), context);
            } else if ("NEW_ASSOCIATION".equals(messageType)) {
                incident = serviceNowConnector.newAnomalyAssociation(jsonNode, context);
            } else if ("NEW_RECOMMENDATION".equals(messageType)) {
                incident = serviceNowConnector.newRecommendation(jsonNode, context);
            } else if ("SEVERITY_UPGRADED".equals(messageType)) {
                int serviceNowSeverity = Util.getServiceNowSeverity(jsonNode.path("detail").path("insightSeverity").asText());
                incident = serviceNowConnector.severityUpgrade(jsonNode.path("detail").path("insightId").asText(), context, serviceNowSeverity);
            } else {
                logger.log("Unknown messageType: " + messageType);
                return null;
            }
        } catch (JsonProcessingException e) {
            logger.log("ERROR JsonProcessingException : " + e.getMessage());
            logger.log("Input : " + input);
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        logger.log("ServiceNow handler is done. ServiceNow incident Id: " + incident.getId());
        return null;
    }
}
