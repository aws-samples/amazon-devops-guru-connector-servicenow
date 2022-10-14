package aws.devopsguru.partner.servicenow.usecases;

import aws.devopsguru.partner.servicenow.InsightHandler;
import aws.devopsguru.partner.servicenow.Util;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;

/**
 * InsightOpenSeverityUpgradeHandler for requests to Lambda function.
 */
public class InsightOpenSeverityUpgradeHandler implements RequestHandler<Map<String, Object>, String> {

    public String handleRequest(Map<String, Object> input, final Context context) {
        JsonNode jsonNodeDetail = Util.getRequestDetail(input, context);
        LambdaLogger logger = context.getLogger();

        String messageType = jsonNodeDetail.path("messageType").asText();

        // Filter only messageType of severity upgraded
        if ("NEW_INSIGHT".equals(messageType) || "SEVERITY_UPGRADED".equals(messageType)) {
            InsightHandler insightHandler = new InsightHandler();
            insightHandler.handleRequest(input, context);
        } else {
            logger.log("Ignoring other events: " + messageType);
        }
        return null;
    }
}
