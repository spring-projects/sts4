*Required*. Specifies the AZs available to deployments. At least one should be specified.

See [first class AZs](https://bosh.io/docs/azs.html) for more details.

Example:

	azs:
	- name: z1
	  cloud_properties:
	    availability_zone: us-east-1c
	- name: z2
	  cloud_properties:
	    availability_zone: us-east-1d

CPI Specific `cloud_properties`.

- See [AWS CPI AZ cloud properties](https://bosh.io/docs/aws-cpi.html#azs).
- See [Azure CPI AZ cloud properties](https://bosh.io/docs/azure-cpi.html#azs").
- See [OpenStack CPI AZ cloud properties](https://bosh.io/docks/openstack-cpi.html#azs).
- See [Softlayer CPI AZ cloud properties](https://bosh.io/docks/softlayer-cpi.html#azs).
- See [Google Cloud Platform CPI AZ cloud properties](https://bosh.io/docks/google-cpi.html#azs).
- See [vSphere CPI AZ cloud properties](https://bosh.io/docks/vsphere-cpi.html#azs).
- See [See vCloud CPI AZ cloud properties](https://bosh.io/docks/vcloud-cpi.html#azs).