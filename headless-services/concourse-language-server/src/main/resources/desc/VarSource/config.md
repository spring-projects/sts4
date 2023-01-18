*Required.* Depending on the chosen `type` corresponding config:
- The **Vault** for configuring a [Vault](https://www.vaultproject.io/) server as a **((var))** source.
- The **SSM** for configuring an [AWS Secrets Manager](https://docs.aws.amazon.com/secretsmanager/) in a single region as a **((var))** source.
- The **Dummy** for configuring a static map of vars to values. This is really only useful if you have no better alternative for credential management but still have sensitive values that you would like to redact them from build output.
- The **Secret Manager** for configuring integration with [AWS Secrets Manager for credential management](https://concourse-ci.org/aws-asm-credential-manager.html)