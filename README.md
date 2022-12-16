# ServiceNowFunctions

This project contains source code and supporting files for a serverless application that you can deploy with the SAM CLI. It includes the following files and folders.

- ServiceNowFunctions/Functions/src/main - Code for the application's Lambda function.
- events - Invocation events that you can use to invoke the function.
- ServiceNowFunctions/Functions/src/test - Unit tests for the application code. 
- template.yaml - A template that defines the application's AWS resources.

The application uses several AWS resources, including Lambda functions and an API Gateway API. These resources are defined in the `template.yaml` file in this project. You can update the template to add AWS resources through the same deployment process that updates your application code.

If you prefer to use an integrated development environment (IDE) to build and test your application, you can use the AWS Toolkit.  
The AWS Toolkit is an open source plug-in for popular IDEs that uses the SAM CLI to build and deploy serverless applications on AWS. The AWS Toolkit also adds a simplified step-through debugging experience for Lambda function code. See the following links to get started.

* [CLion](https://docs.aws.amazon.com/toolkit-for-jetbrains/latest/userguide/welcome.html)
* [GoLand](https://docs.aws.amazon.com/toolkit-for-jetbrains/latest/userguide/welcome.html)
* [IntelliJ](https://docs.aws.amazon.com/toolkit-for-jetbrains/latest/userguide/welcome.html)
* [WebStorm](https://docs.aws.amazon.com/toolkit-for-jetbrains/latest/userguide/welcome.html)
* [Rider](https://docs.aws.amazon.com/toolkit-for-jetbrains/latest/userguide/welcome.html)
* [PhpStorm](https://docs.aws.amazon.com/toolkit-for-jetbrains/latest/userguide/welcome.html)
* [PyCharm](https://docs.aws.amazon.com/toolkit-for-jetbrains/latest/userguide/welcome.html)
* [RubyMine](https://docs.aws.amazon.com/toolkit-for-jetbrains/latest/userguide/welcome.html)
* [VS Code](https://docs.aws.amazon.com/toolkit-for-vscode/latest/userguide/welcome.html)
* [Visual Studio](https://docs.aws.amazon.com/toolkit-for-visual-studio/latest/user-guide/welcome.html)

## Deploy the sample application

The Serverless Application Model Command Line Interface (SAM CLI) is an extension of the AWS CLI that adds functionality for building and testing Lambda applications. It uses Docker to run your functions in an Amazon Linux environment that matches Lambda. It can also emulate your application's build environment and API.

To use the SAM CLI, you need the following tools.

* SAM CLI - [Install the SAM CLI](https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/serverless-sam-cli-install.html)
* Java11 - [Install the Java SE Development Kit 11](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
* Maven - [Install Maven](https://maven.apache.org/install.html)
* Docker - [Install Docker community edition](https://hub.docker.com/search/?type=edition&offering=community)

To build and deploy your application for the first time, run the following in your shell:

```bash
sam build
sam deploy --guided
```

The first command will build the source of your application. The second command will package and deploy your application to AWS, with a series of prompts:

* **Stack Name**: The name of the stack to deploy to CloudFormation. This should be unique to your account and region, and a good starting point would be something matching your project name.
* **AWS Region**: The AWS region you want to deploy your app to.
* **Confirm changes before deploy**: If set to yes, any change sets will be shown to you before execution for manual review. If set to no, the AWS SAM CLI will automatically deploy application changes.
* **Allow SAM CLI IAM role creation**: Many AWS SAM templates, including this example, create AWS IAM roles required for the AWS Lambda function(s) included to access AWS services. By default, these are scoped down to minimum required permissions. To deploy an AWS CloudFormation stack which creates or modifies IAM roles, the `CAPABILITY_IAM` value for `capabilities` must be provided. If permission isn't provided through this prompt, to deploy this example you must explicitly pass `--capabilities CAPABILITY_IAM` to the `sam deploy` command.
* **Save arguments to samconfig.toml**: If set to yes, your choices will be saved to a configuration file inside the project, so that in the future you can just re-run `sam deploy` without parameters to deploy changes to your application.

You can find your API Gateway Endpoint URL in the output values displayed after deployment.

## Use the SAM CLI to build and test locally

Build your application with the `sam build` command.

```bash
sam build
```

The SAM CLI installs dependencies defined in `ServiceNowFunctions/pom.xml`, creates a deployment package, and saves it in the `.aws-sam/build` folder.

Test a single function by invoking it directly with a test event. An event is a JSON document that represents the input that the function receives from the event source. Test events are included in the `Functions/src/test/Events` folder in this project.

Run functions locally and invoke them with the `sam local invoke` command.

```bash
sam local invoke ServiceNowFunctions --event Functions/src/test/Events/CreateIncident.json
```

The SAM CLI can also emulate your application's API. Use the `sam local start-api` to run the API locally on port 3000.

```bash
sam local start-api
curl http://localhost:3000/
```

The SAM CLI reads the application template to determine the API's routes and the functions that they invoke. The `Events` property on each function's definition includes the route and method for each path.

```yaml
      Events:
        DevOpsGuru:
          Type: EventBridgeRule
          Properties:
            Pattern:
              source: 
                - "aws.devops-guru"
```

## Add a resource to your application
The application template uses AWS Serverless Application Model (AWS SAM) to define application resources. AWS SAM is an extension of AWS CloudFormation with a simpler syntax for configuring common serverless application resources such as functions, triggers, and APIs. For resources not included in [the SAM specification](https://github.com/awslabs/serverless-application-model/blob/master/versions/2016-10-31.md), you can use standard [AWS CloudFormation](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/aws-template-resource-type-ref.html) resource types.

## Fetch, tail, and filter Lambda function logs

To simplify troubleshooting, SAM CLI has a command called `sam logs`. `sam logs` lets you fetch logs generated by your deployed Lambda function from the command line. In addition to printing the logs on the terminal, this command has several nifty features to help you quickly find the bug.

`NOTE`: This command works for all AWS Lambda functions; not just the ones you deploy using SAM.

```bash
sam logs -n ServiceNowFunctions --stack-name ServiceNowFunctions --tail
```

You can find more information and examples about filtering Lambda function logs in the [SAM CLI Documentation](https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/serverless-sam-cli-logging.html).

## Unit tests

Tests are defined in the `ServiceNowFunction/src/test` folder in this project.

```bash
cd ServiceNowFunctions
mvn test
```

## Cleanup

To delete the sample application that you created, use the AWS CLI. Assuming you used your project name for the stack name, you can run the following:

```bash
aws cloudformation delete-stack --stack-name ServiceNowFunctions
```

## Resources

See the [AWS SAM developer guide](https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/what-is-sam.html) for an introduction to SAM specification, the SAM CLI, and serverless application concepts.

Next, you can use AWS Serverless Application Repository to deploy ready to use Apps that go beyond hello world samples and learn how authors developed their applications: [AWS Serverless Application Repository main page](https://aws.amazon.com/serverless/serverlessrepo/)


## ServiceNow

1. The registration process for the ServiceNow developer portal is fairly simple. You can click on the REGISTER link in the upper-right, if you are not already registered, and fill in your name, e-mail, and choose a password to complete your registration.
[Create your account in ServiceNow](https://developer.servicenow.com/)

2. Getting started with credentials
Go to (https://developer.servicenow.com/) and click on your profile. After activating your instance you can see instance actions on your profile page, click "Manage instance password". There are the instance name, instance URL, username, and password. You need only intstance URL, username, and password. 

* Basic Authentication (recommended): A user name and password combination used to authenticate to the REST API. 
* API Key: A unique code provided by the REST API to identify the calling application or user.
* OAuth 2.0: An authentication provider provides a secret and uses that information to grant access to a resource. OAuth is beyond the scope of this training module.

Basic Authentication Credentials
In the list of credential types, click the Basic Auth Credentials link. Configure the User name, and Password for the Credential.
 
* [SERVICE_NOW_HOST]: Your ServiceNow host name/instance URL. Example: dev92031.service-now.com
* [USER_NAME]: The user name for the account to access the REST API.
* [PASSWORD]: The password for the account to access the REST API.

When the Basic Authentication credential is used, ServiceNow generates an encoded string from the user name and password, which is passed to the Authorization header as a Basic encoded-string.

* Configuring environment variables 
  - Open the Functions page of the Lambda console
  - Choose a function. 
  - Choose Configuration, then choose Environment variables.
  - Enter a key and value.
Requirements
  - Keys start with a letter and are at least two characters.
  - Keys only contain letters, numbers, and the underscore character (_).
  - Keys aren't reserved by Lambda.
  - The total size of all environment variables doesn't exceed 4 KB.

3. There is no SDK provided by ServiceNow. So, we are using Rest APIs.
ServiceNow REST APIs support Basic Authentication.

We use java HttpClient to send requests and retrieve their responses.

ServiceNow Table API provides endpoints that allow you to perform create, read, update, and delete (CRUD) operations on existing tables.
The calling user must have sufficient roles to access the data in the table specified in the request.
* [DELETE] /now/table/{tableName}/{sys_id} - Deletes the specified record from the specified table.
* [GET] /now/table/{tableName} - Retrieves multiple records for the specified table
* [GET] /now/table/{tableName}/{sys_id}) - Retrieves the record identified by the specified sys_id from the specified table.
* [PATCH] /now/table/{tableName}/{sys_id} - Updates the specified record with the name-value pairs included in the request body
* [POST] /now/table/{tableName} - Inserts one record in the specified table. Multiple record insertion is not supported by this method.
* [PUT] /now/table/{tableName}/{sys_id} - Updates the specified record with the request body.

4. Save your code and do the instructions below in the AFTER YOU CUSTOMIZE YOUR CODE section.

INSTRUCTIONS FOR CUSTOMIZING CODE:

1. Go to the Functions/src/main/java/aws/devopsguru/partner/servicenow folder, and you'll see a "ServiceNowConnector" and "AlertType" classes.
2. CUSTOMIZING ALERT DETAILS
   -If you would like to customize the details of a specific alert, go to the "AlertType" file and look at the function corresponding to the event trigger you would like to change. There is 1 function for each trigger should be clear to pick out.

## Additional


* [What is the AWS Serverless Application Model (AWS SAM)](https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/what-is-sam.html)

* [Publishing serverless applications using the AWS SAM CLI](https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/serverless-sam-template-publishing-applications.html)

