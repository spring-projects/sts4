*Optional*. List of credentials for HTTP(s) auth when pulling/pushing private git submodules which are not stored in the same git server as the container repository. Example:

```
submodule_credentials:
- host: github.com
  username: git-user
  password: git-password
- <another-configuration>
```