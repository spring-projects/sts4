Specifies the [VM extensions](https://bosh.io/docs/terminology.html#vm-extension) available to deployments.

Example:

	vm_extensions:
	- name: pub-lbs
	  cloud_properties:
	    elbs: [main]

Any IaaS specific configuration could be placed into a VM extension’s `cloud_properties`.