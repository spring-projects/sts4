*Optional*. An array of AWS IAM roles.
  If set, these roles will be assumed in the specified order before
  authenticating to ECR. An error will occur if `aws_role_arn`
  is also specified.