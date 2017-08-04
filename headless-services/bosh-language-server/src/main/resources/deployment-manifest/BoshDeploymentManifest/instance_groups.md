*Required*. Specifies the mapping between release [jobs](https://bosh.io/docs/terminology.html#job) and instance groups.

Example:

	instance_groups:
	- name: redis-master
	  instances: 1
	  azs: [z1, z2]
	  jobs:
	  - name: redis-server
	    release: redis
	    properties:
	      port: 3606
	  vm_type: medium
	  vm_extensions: [public-lbs]
	  stemcell: default
	  persistent_disk_type: medium
	  networks:
	  - name: default
	
	- name: redis-slave
	  instances: 2
	  azs: [z1, z2]
	  jobs:
	  - name: redis-server
	    release: redis
	    properties: {}
	  vm_type: medium
	  stemcell: default
	  persistent_disk_type: medium
	  networks:
	  - name: default
