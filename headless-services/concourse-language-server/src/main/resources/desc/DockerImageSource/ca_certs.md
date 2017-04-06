*Optional.* An array of objects with the following format:

```yaml
ca_certs:
- domain: example.com:443
  cert: |
    -----BEGIN CERTIFICATE-----
    ...
    -----END CERTIFICATE-----
- domain: 10.244.6.2:443
  cert: |
    -----BEGIN CERTIFICATE-----
    ...
    -----END CERTIFICATE-----
```

Each entry specifies the x509 CA certificate for the trusted docker registry
residing at the specified domain. This is used to validate the certificate of
the docker registry when the registry's certificate is signed by a custom
authority (or itself).

The domain should match the first component of `repository`, including the
port. If the registry specified in `repository` does not use a custom cert,
adding `ca_certs` will break the check script. This option is overridden by
entries in `insecure_registries` with the same address or a matching CIDR.
