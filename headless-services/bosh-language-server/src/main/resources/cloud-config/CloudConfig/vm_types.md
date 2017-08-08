*Required*. Specifies the [VM types](https://bosh.io/docs/terminology.html#vm-type) available to deployments. At least one should be specified.

Example:

	vm_types:
	- name: default
	  cloud_properties:
	    instance_type: m1.small
