package aws.devopsguru.partner.servicenow;

import aws.devopsguru.partner.servicenow.model.Incident;
import aws.devopsguru.partner.servicenow.model.ServiceNowResult;
import aws.devopsguru.partner.servicenow.model.ServiceNowResults;
import com.amazonaws.services.devopsguru.model.InsightSeverity;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.amazonaws.util.StringUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

public class ServiceNowConnector {
    public static final String NEW_LINE = System.lineSeparator();

    // Configure your Environment variables in AWS Lambda.
    public static String secretName = System.getenv("SECRET_NAME");
    public static String serviceNowHost = System.getenv("SERVICE_NOW_HOST");

    // The Table API provides endpoints that allow you to perform create, read, update, and delete (CRUD) operations on existing tables.
    // This is a base URL format in ServiceNow
    private static final String baseUriPath = "/api/now/table/incident";

    //Create an incident record using a POST request.
    public void createIncident(Incident incident, Context context) {
        LambdaLogger logger = context.getLogger();
        logger.log("Creating new Incident. " + incident.getShortDescription() + NEW_LINE);

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
                        + " ShortDescription:" + responseIncident.getShortDescription() + NEW_LINE);
            } else {
                logger.log("ERROR ServiceNow response code: " + response.statusCode() + NEW_LINE);
                throw new RuntimeException("ServiceNow response code: " + response.statusCode());
            }

        } catch (IOException | InterruptedException e) {
            logger.log("ERROR Exception occurred when communicate with ServiceNow. Exception: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    //Close an incident record using a PUT request.
    public void closeIncident(String insightId, Context context) {

        context.getLogger().log("closeIncident " + insightId + NEW_LINE);
        Incident incident = findIncidentByInsightId(insightId, context);
        incident.setClosedAt(new Date());
        incident.setClosedNotes("Closed by AWS DevOpsGuru.");
        incident.setCloseCode("Resolved by Caller");
        incident.setState("6");

        updateIncident(context, incident);
    }

    // Upgrade an incident using a GET request.
    public void severityUpgrade(String insightId, Context context, int severity) {

        context.getLogger().log("severityUpgrade. New severity:" + severity + " InsightId: " + insightId + NEW_LINE);
        Incident incident = findIncidentByInsightId(insightId, context);
        incident.setSeverity(severity);

        updateIncident(context, incident);
    }

    // New anomaly association
    public void newAnomalyAssociation(JsonNode jsonNode, Context context) {
        JsonNode detailJsonNodeAnomaly = jsonNode.path("detail");
        String insightId = detailJsonNodeAnomaly.path("insightId").asText();

        context.getLogger().log("newAnomalyAssociation. " + insightId + NEW_LINE);
        Incident incident = findIncidentByInsightId(insightId, context);
        String description = incident.getDescription() + NEW_LINE + "New anomalies:" + NEW_LINE +
                ServiceNowConnector.getAnomaliesAsDescription(detailJsonNodeAnomaly.path("anomalies"));
        incident.setDescription(description);

        updateIncident(context, incident);
    }

    public void newRecommendation(JsonNode jsonNode, Context context) {
        JsonNode detailJsonNode = jsonNode.path("detail");
        String insightId = detailJsonNode.path("insightId").asText();

        context.getLogger().log("newRecommendation " + insightId + NEW_LINE);
        Incident incident = findIncidentByInsightId(insightId, context);
        StringBuilder description = new StringBuilder(incident.getDescription());
        description.append(NEW_LINE).append("New recommendations:").append(NEW_LINE);
        for (JsonNode recommendation : detailJsonNode.path("recommendations")) {
            description.append(recommendation.path("description")).append(NEW_LINE)
                    .append(recommendation.path("link")).append(NEW_LINE);
        }
        incident.setDescription(description.toString());

        updateIncident(context, incident);
    }

    // To find ServiceNow Incident by DevOps Guru Insight ID using GET request.
    private Incident findIncidentByInsightId(String insightId, Context context) {
        LambdaLogger logger = context.getLogger();
        logger.log("Finding existing Incident. " + NEW_LINE);
        Incident result;
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = getBuilderWithBasicValues(baseUriPath + "?sysparm_query=descriptionLIKE" + insightId + "&sysparm_limit=1").GET().build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 299) {
                Incident[] responseIncidents = stringToServiceNowResultArray(response.body());
                if (responseIncidents != null && responseIncidents.length == 1) {
                    result = responseIncidents[0];
                    logger.log("Found incident. ID: " + result.getId() + NEW_LINE);
                } else {
                    logger.log("ERROR Could not find incident by InsightId: " + insightId + NEW_LINE + response.body());
                    throw new RuntimeException("Could not find incident by InsightId: " + insightId);
                }
            } else {
                logger.log("ERROR ServiceNow response code: " + response.statusCode() + NEW_LINE);
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
                logger.log("Incident is updated. ID: " + responseIncident.getId() + NEW_LINE);
            } else {
                logger.log("ERROR ServiceNow response code: " + response.statusCode() + NEW_LINE);
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

        JSONObject serviceNowCredentials = getSecret(secretName);

        String username = new String();
        String password = new String();
        try {
            username = serviceNowCredentials.getString("username");
            password = serviceNowCredentials.getString("password");
        } catch (JSONException e) {
            throw new RuntimeException(e);}

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

            // Set all the details of the incident
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

    // Helper Function to get RequestDetails and parsing request
    public static JsonNode getRequestDetail(Map<String, Object> input, final Context context) {

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNodeDetail = null;
        LambdaLogger logger = context.getLogger();
        try {
            logger.log("Parsing request.");

            String inputString = objectMapper.writeValueAsString(input);
            JsonNode jsonNode = objectMapper.readTree(inputString);
            jsonNodeDetail = jsonNode.path("detail");

        } catch (JsonProcessingException e) {
            logger.log("ERROR JsonProcessingException : " + e.getMessage());
            logger.log("Input : " + input);
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        return jsonNodeDetail;
    }

    // Helper Function to get JsonNode from Input type
    public static JsonNode getJsonNodeFromInput(Map<String, Object> input, final Context context) {
        JsonNode jsonNode = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String inputString = objectMapper.writeValueAsString(input);
            jsonNode = objectMapper.readTree(inputString);

        } catch (JsonProcessingException e) {
            context.getLogger().log("ERROR JsonProcessingException : " + e.getMessage());
            context.getLogger().log("Input : " + input);
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return jsonNode;
    }

    public JSONObject getSecret(String secretName) {

        AWSSecretsManager client = AWSSecretsManagerClientBuilder.standard()
                .build();

        String secret = "";
        GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest()
                .withSecretId(secretName);
        GetSecretValueResult getSecretValueResult = null;

        try {
            getSecretValueResult = client.getSecretValue(getSecretValueRequest);
        } catch (Exception e) {
            throw e;
        }
        if (getSecretValueResult.getSecretString() != null) {
            secret = getSecretValueResult.getSecretString();
        }
        try {
            JSONObject jsonObject = new JSONObject(secret);
            return jsonObject;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
