version = 0.1
[default]
[default.deploy]
[default.deploy.parameters]
stack_name = "serverlessrepo-DevOps-Guru-ServiceNow-Connector"
s3_bucket = "aws-sam-cli-managed-default-samclisourcebucket-1remvxdxqiot5"
s3_prefix = "serverlessrepo-DevOps-Guru-ServiceNow-Connector"
region = "us-east-1"
profile = "servicenow"
confirm_changeset = true
capabilities = "CAPABILITY_IAM"
disable_rollback = true
parameter_overrides = "ServiceNowHost=\"dev98071.service-now.com\" SecretName=\"servicenow_creds\""
image_repositories = []
