package aws.devopsguru.partner.servicenow.usecases;

import aws.devopsguru.partner.servicenow.InsightHandler;
import aws.devopsguru.partner.servicenow.Util;
import com.amazonaws.services.devopsguru.model.InsightSeverity;
import com.amazonaws.services.devopsguru.model.InsightType;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;

/**
 * InsightProactiveHighSeverityHandler for requests to Lambda function.
 */
public class InsightProactiveHighSeverityHandler implements RequestHandler<Map<String, Object>, String> {

    public String handleRequest(Map<String, Object> input, final Context context) {
        JsonNode jsonNodeDetail = Util.getRequestDetail(input, context);

        // Filter only Proactive and High severity insights
        if (!InsightSeverity.HIGH.toString().equalsIgnoreCase(jsonNodeDetail.path("insightSeverity").asText())
                || !InsightType.PROACTIVE.toString().equalsIgnoreCase(jsonNodeDetail.path("insightType").asText())) {
            InsightHandler insightHandler = new InsightHandler();
            insightHandler.handleRequest(input, context);
        } else {
            context.getLogger().log("Ignoring other insights.");
        }
        return null;
    }
}
