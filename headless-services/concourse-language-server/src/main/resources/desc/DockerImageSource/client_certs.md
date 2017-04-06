*Optional.* An array of objects with the following format:

```yaml
client_certs:
- domain: example.com:443
  cert: |
    -----BEGIN CERTIFICATE-----
    ...
    -----END CERTIFICATE-----
  key: |
    -----BEGIN RSA PRIVATE KEY-----
    ...
    -----END RSA PRIVATE KEY-----
- domain: 10.244.6.2:443
  cert: |
    -----BEGIN CERTIFICATE-----
    ...
    -----END CERTIFICATE-----
  key: |
    -----BEGIN RSA PRIVATE KEY-----
    ...
    -----END RSA PRIVATE KEY-----
```

Each entry specifies the x509 certificate and key to use for authenticating
against the docker registry residing at the specified domain. The domain
should match the first component of `repository`, including the port.
