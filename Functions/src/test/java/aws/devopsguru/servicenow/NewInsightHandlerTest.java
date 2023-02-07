package aws.devopsguru.servicenow;

import aws.devopsguru.partner.servicenow.InsightHandler;
import aws.devopsguru.partner.servicenow.ServiceNowConnector;
import org.junit.Test;

public class NewInsightHandlerTest {
    @Test
    public void successfulResponse() {
        InsightHandler app = new InsightHandler();
        ServiceNowConnector.serviceNowHost = "www.servicenow.com";
    }

}
