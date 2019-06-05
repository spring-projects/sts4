*Required.* All openstack configuration must go under this key.

  * `container`: *Required.* The name of the container.

  * `item_name`: *Required.* The item name to use for the object in the container tracking
the version.

  * `region_name`: *Required.* The region the container is in.

  * `identity_endpoint`, `username`, `user_id`, `password`, `api_key`, `domain_id`, `domain_name`, `tenant_id`, `tenant_name`, `allow_reauth`, `token_id`: See below
The swift driver uses [gophercloud](https://gophercloud.io/docs/) to handle interacting
with OpenStack. All OpenStack Identity versions are supported through this library. The
Authentication properties will pass through to it. For detailed information about the
individual parameters, see https://github.com/rackspace/gophercloud/blob/master/auth_options.go