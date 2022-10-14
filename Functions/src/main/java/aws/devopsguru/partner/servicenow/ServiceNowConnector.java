package aws.devopsguru.partner.servicenow;

import aws.devopsguru.partner.servicenow.model.Incident;
import aws.devopsguru.partner.servicenow.model.ServiceNowResult;
import aws.devopsguru.partner.servicenow.model.ServiceNowResults;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.util.StringUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;

public class ServiceNowConnector {
    // Configure your Environment variables in AWS Lambda.
    public static String username = System.getenv("USER_NAME");
    public static String password = System.getenv("PASSWORD");
    public static String serviceNowHost = System.getenv("SERVICE_NOW_HOST");

    // The Table API provides endpoints that allow you to perform create, read, update, and delete (CRUD) operations on existing tables.
    // This is a base URL format in ServiceNow
    private static final String baseUriPath = "/api/now/table/incident";

    //Create an incident record using a POST request.
    public Incident createIncident(Incident incident, Context context) {
        LambdaLogger logger = context.getLogger();
        logger.log("Creating new Incident. " + incident.getShortDescription() + Util.NEW_LINE);

        if (StringUtils.isNullOrEmpty(incident.getShortDescription())) {
            String now = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss Z").format(new Date());
            incident.setShortDescription("Incident created by AWS DevOpsGuru " + now);
        }

        String requestBody;
        Incident responseIncident;
        try {
            requestBody = incidentToJson(incident);
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = getBuilderWithBasicValues(baseUriPath).POST(HttpRequest.BodyPublishers.ofString(requestBody)).build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 299) {
                responseIncident = stringToIncident(response.body());
                logger.log("New incident ID: " + responseIncident.getId()
                        + " ShortDescription:" + responseIncident.getShortDescription() + Util.NEW_LINE);
            } else {
                logger.log("ERROR ServiceNow response code: " + response.statusCode() + Util.NEW_LINE);
                throw new RuntimeException("ServiceNow response code: " + response.statusCode());
            }

        } catch (IOException | InterruptedException e) {
            logger.log("ERROR Exception occurred when communicate with ServiceNow. Exception: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return responseIncident;
    }

    //Close an incident record using a PUT request.
    public Incident closeIncident(String insightId, Context context) {

        context.getLogger().log("closeIncident " + insightId + Util.NEW_LINE);
        Incident incident = findIncidentByInsightId(insightId, context);
        incident.setClosedAt(new Date());
        incident.setClosedNotes("Closed by AWS DevOpsGuru.");

        return updateIncident(context, incident);
    }

    // Upgrade an incident using a GET request.
    public Incident severityUpgrade(String insightId, Context context, int severity) {

        context.getLogger().log("severityUpgrade. New severity:" + severity + " InsightId: " + insightId + Util.NEW_LINE);
        Incident incident = findIncidentByInsightId(insightId, context);
        incident.setSeverity(severity);

        return updateIncident(context, incident);
    }

    // New anomaly association
    public Incident newAnomalyAssociation(JsonNode jsonNode, Context context) {
        JsonNode detailJsonNodeAnomaly = jsonNode.path("detail");
        String insightId = detailJsonNodeAnomaly.path("insightId").asText();

        context.getLogger().log("newAnomalyAssociation. " + insightId + Util.NEW_LINE);
        Incident incident = findIncidentByInsightId(insightId, context);
        String description = incident.getDescription() + Util.NEW_LINE + "New anomalies:" + Util.NEW_LINE +
                Util.getAnomaliesAsDescription(detailJsonNodeAnomaly.path("anomalies"));
        incident.setDescription(description);

        return updateIncident(context, incident);
    }

    public Incident newRecommendation(JsonNode jsonNode, Context context) {
        JsonNode detailJsonNode = jsonNode.path("detail");
        String insightId = detailJsonNode.path("insightId").asText();

        context.getLogger().log("newRecommendation " + insightId + Util.NEW_LINE);
        Incident incident = findIncidentByInsightId(insightId, context);
        StringBuilder description = new StringBuilder(incident.getDescription());
        description.append(Util.NEW_LINE).append("New recommendations:").append(Util.NEW_LINE);
        for (JsonNode recommendation : detailJsonNode.path("recommendations")) {
            description.append(recommendation.path("description")).append(Util.NEW_LINE)
                    .append(recommendation.path("link")).append(Util.NEW_LINE);
        }
        incident.setDescription(description.toString());

        return updateIncident(context, incident);
    }

    // To find ServiceNow Incident by DevOps Guru Insight ID using GET request.
    private Incident findIncidentByInsightId(String insightId, Context context) {
        LambdaLogger logger = context.getLogger();
        logger.log("Finding existing Incident. " + Util.NEW_LINE);
        Incident result;
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = getBuilderWithBasicValues(baseUriPath + "?sysparm_query=descriptionLIKE" + insightId + "&sysparm_limit=1").GET().build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 299) {
                Incident[] responseIncidents = stringToServiceNowResultArray(response.body());
                if (responseIncidents != null && responseIncidents.length == 1) {
                    result = responseIncidents[0];
                    logger.log("Found incident. ID: " + result.getId() + Util.NEW_LINE);
                } else {
                    logger.log("ERROR Could not find incident by InsightId: " + insightId + Util.NEW_LINE + response.body());
                    throw new RuntimeException("Could not find incident by InsightId: " + insightId);
                }
            } else {
                logger.log("ERROR ServiceNow response code: " + response.statusCode() + Util.NEW_LINE);
                throw new RuntimeException("ServiceNow response code: " + response.statusCode());
            }

        } catch (IOException | InterruptedException e) {
            logger.log("ERROR Exception occurred when communicate with ServiceNow to find. Exception: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return result;
    }

    // To update incident record using a PUT request
    private Incident updateIncident(Context context, Incident incident) {
        LambdaLogger logger = context.getLogger();
        logger.log("Updating incident. " + incident.getId());

        String requestBody;
        Incident responseIncident;
        try {
            requestBody = incidentToJson(incident);
            //An HttpClient can be used to send requests and retrieve their responses
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = getBuilderWithBasicValues(baseUriPath + "/" + incident.getId()).PUT(HttpRequest.BodyPublishers.ofString(requestBody)).build();

            // This class provides methods for accessing the response status code, headers, the response body,
            // and the HttpRequest corresponding to this response.
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 299) {
                responseIncident = stringToIncident(response.body());
                logger.log("Incident is updated. ID: " + responseIncident.getId() + Util.NEW_LINE);
            } else {
                logger.log("ERROR ServiceNow response code: " + response.statusCode() + Util.NEW_LINE);
                throw new RuntimeException("ServiceNow response code: " + response.statusCode());
            }

        } catch (IOException | InterruptedException e) {
            logger.log("ERROR Exception occurred when communicate with ServiceNow. Exception: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return responseIncident;
    }

    // An HttpRequest instance is built through an HttpRequest builder
    private HttpRequest.Builder getBuilderWithBasicValues(String uriPath) {
        if (serviceNowHost == null || username == null || password == null) {
            throw new RuntimeException("ERROR! Could not find environment variables for ServiceNow!");
        }
        String urlForCreateIncident = "https://" + serviceNowHost + uriPath;

        String headerAuth = "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());

        return HttpRequest.newBuilder().uri(URI.create(urlForCreateIncident)).header("Accept", "application/json").header("Authorization", headerAuth);
    }

    // JSON object format
    private String incidentToJson(Incident incident) throws JsonProcessingException {
        var objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(incident);
    }

    private Incident stringToIncident(String str) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(str, ServiceNowResult.class).getResult();
    }

    // Array of incidents in JSON
    private Incident[] stringToServiceNowResultArray(String str) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(str, ServiceNowResults.class).getResult();
    }

}