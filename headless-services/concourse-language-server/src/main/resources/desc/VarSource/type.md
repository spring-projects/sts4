*Required.* Expected one of:
- `vault` type supports configuring a [Vault](https://www.vaultproject.io/) server as a **((var))** source.
- `ssm` type supports configuring an [AWS Secrets Manager](https://docs.aws.amazon.com/secretsmanager/) in a single region as a **((var))** source.
- `dummy` type supports configuring a static map of vars to values. This is really only useful if you have no better alternative for credential management but still have sensitive values that you would like to redact them from build output.
- `secretmanager` type supports integration with [AWS Secrets Manager for credential management](https://concourse-ci.org/aws-asm-credential-manager.html)