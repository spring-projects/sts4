*Required*. Specifies the [VM types](https://bosh.io/docs/terminology.html#vm-type) available to deployments. At least one should be specified.

Example:

	vm_types:
	- name: default
	  cloud_properties:
	    instance_type: m1.small

CPI Specific `cloud_properties`

- See [AWS CPI VM types cloud properties](https://bosh.io/docs/aws-cpi.html#resource-pools)
- See [Azure CPI VM types cloud properties](https://bosh.io/docs/azure-cpi.html#resource-pools)
- See [OpenStack CPI VM types cloud properties](https://bosh.io/docs/openstack-cpi.html#resource-pools)
- See [Softlayer CPI VM types cloud properties](https://bosh.io/docs/softlayer-cpi.html#resource-pools)
- See [Google Cloud Platform CPI VM types cloud properties](https://bosh.io/docs/google-cpi.html#resource-pools)
- See [vSphere CPI VM types cloud properties](https://bosh.io/docs/vsphere-cpi.html#resource-pools)
- See [vCloud CPI VM types cloud properties](https://bosh.io/docs/vcloud-cpi.html#resource-pools)
