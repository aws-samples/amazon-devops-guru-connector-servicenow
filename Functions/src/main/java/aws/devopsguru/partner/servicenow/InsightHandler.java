package aws.devopsguru.partner.servicenow;

import aws.devopsguru.partner.servicenow.model.Incident;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;

/**
 * InsightHandler for requests to Lambda function.
 */
public class InsightHandler implements RequestHandler<Map<String, Object>, String> {

    enum useCase {
        ALLEVENTS,
        ANOMALIES,
        RECOMMENDATIONS,
        SEVERITYUPGRADED,
        PROACTIVEANDHIGHONLY,
        PROACTIVEANDMEDIUMONLY,
        PROACTIVEANDLOWONLY,
        REACTIVEANDHIGHONLY,
        REACTIVEANDMEDIUMONLY,
        REACTIVEANDLOWONLY,
    }

    public void pickUseCase(useCase caseChoice, JsonNode jsonNode, Context context) {
        LambdaLogger logger = context.getLogger();
        JsonNode jsonNodeDetail = jsonNode.path("detail");
        String messageType = jsonNodeDetail.path("messageType").asText();
        logger.log("Message type: " + messageType + " InsightId: " + jsonNode.path("detail").path("insightId").asText());
        logger.log(jsonNode.toString());

        switch (caseChoice) {
            case ALLEVENTS:
                AlertType.allFeatures(jsonNode, context);
                break;
            case ANOMALIES:
                AlertType.InsightOpenAndAnomalies(jsonNode, context);
                break;
            case RECOMMENDATIONS:
                AlertType.InsightOpenAndRecommendations(jsonNode, context);
                break;
            case SEVERITYUPGRADED:
                AlertType.InsightOpenAndSeverityUpgraded(jsonNode, context);
                break;
            case PROACTIVEANDHIGHONLY:
                AlertType.proactiveHighSeverityOnly(jsonNode, context);
                break;
            case PROACTIVEANDMEDIUMONLY:
                AlertType.proactiveMediumSeverityOnly(jsonNode, context);
                break;
            case PROACTIVEANDLOWONLY:
                AlertType.proactiveLowSeverityOnly(jsonNode, context);
                break;
            case REACTIVEANDHIGHONLY:
                AlertType.reactiveHighSeverityOnly(jsonNode, context);
                break;
            case REACTIVEANDMEDIUMONLY:
                AlertType.reactiveMediumSeverityOnly(jsonNode, context);
                break;
            case REACTIVEANDLOWONLY:
                AlertType.reactiveLowSeverityOnly(jsonNode, context);
                break;
        }
    }

    public String handleRequest(Map<String, Object> input, final Context context) {

        LambdaLogger logger = context.getLogger();
        JsonNode jsonNode = ServiceNowConnector.getJsonNodeFromInput(input, context);

        logger.log("Request handler for ServiceNow.");


        // Change the enum value here for the desired use case. Enum values are listed above.
        // Example: I want only Recommendations so i change the value to useCase.RECOMMENDATIONS
        useCase caseChoice = useCase.ALLEVENTS;
        pickUseCase(caseChoice, jsonNode, context);

        logger.log("ServiceNow handler is done.");
        return null;
    }
}
