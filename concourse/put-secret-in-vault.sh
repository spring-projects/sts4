#!/bin/bash
export VAULT_ADDR=https://vault.spring.vmware.com:8200
vault kv put concourse/tools/vsce_token value=${vsce_token}
