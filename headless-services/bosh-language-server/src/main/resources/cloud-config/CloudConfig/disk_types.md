*Required*. Specifies the [disk types](https://bosh.io/docs/terminology.html#disk-types) available to deployments. At least one should be specified.

Example:

	disk_types:
	- name: default
	  disk_size: 2
	  cloud_properties:
	    type: gp2

CPI Specific `cloud_properties`

- See [AWS CPI disk type cloud properties](https://bosh.io/docs/aws-cpi.html#disk-pools)
- See [Azure CPI disk type cloud properties](https://bosh.io/docs/azure-cpi.html#disk-pools)
- See [OpenStack CPI disk type cloud properties](https://bosh.io/docs/openstack-cpi.html#disk-pools)
- See [Softlayer CPI disk type cloud properties](https://bosh.io/docs/softlayer-cpi.html#disk-pools)
- See [Google Cloud Platform CPI disk type cloud properties](https://bosh.io/docs/google-cpi.html#disk-pools)
- See [vSphere CPI disk type cloud properties](https://bosh.io/docs/vsphere-cpi.html#disk-pools)
- See [vCloud CPI disk type cloud properties](https://bosh.io/docs/vcloud-cpi.html#disk-pools)
