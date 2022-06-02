*Optional*. An array of PEM-encoded CA certificates. Example:
```yaml
ca_certs:
- |
  -----BEGIN CERTIFICATE-----
  ...
  -----END CERTIFICATE-----
- |
  -----BEGIN CERTIFICATE-----
  ...
  -----END CERTIFICATE-----
``` 
Each entry specifies the x509 CA certificate for the trusted docker registry.
This is used to validate the certificate of the docker registry when the
registry's certificate is signed by a custom authority (or itself).