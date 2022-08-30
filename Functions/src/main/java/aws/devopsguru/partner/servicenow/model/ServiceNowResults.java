package aws.devopsguru.partner.servicenow.model;

public class ServiceNowResults {
    Incident result[];

    public Incident[] getResult() {
        return result;
    }

    public void setResult(Incident[] result) {
        this.result = result;
    }
}
