Concourse pipeline Secrets
--------------------------

They are in vault. Access it here:
https://vault.spring.vmware.com:8200/

Requires VPN access. Login using VMWAre / LDAP creds.

Note that if you put a secret in, you have to puts its
value in the right box and put the word `value` in the
left box.

I.e. vault is forcing you to enter a secret as a json
object. But since we typically just want it to be
a single string, you have to enter it like so:

```
{
    "value" : "the-real-value-you-want"
}
```

See also the `put-secret-in-vault.sh` script for an
example of how to use vault CLI instead.

