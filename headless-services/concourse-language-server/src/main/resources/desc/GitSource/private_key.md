*Optional.* Private key to use when pulling/pushing.

Example:

    private_key: |
      -----BEGIN RSA PRIVATE KEY-----
	    MIIEowIBAAKCAQEAtCS10/f7W7lkQaSgD/mVeaSOvSF9ql4hf/zfMwfVGgHWjj+W
	    <Lots more text>
	    DWiJL+OFeg9kawcUL6hQ8JeXPhlImG6RTUffma9+iGQyyBMCGd1l
	    -----END RSA PRIVATE KEY-----

Note: You can also use pipeline templating to hide this private key in source control. (For more information: https://concourse-ci.org/fly-set-pipeline.html)
