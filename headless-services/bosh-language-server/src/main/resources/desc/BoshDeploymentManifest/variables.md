*Optional*. Describes variables.

Example:

	variables:
	- name: admin_password
	  type: password
	- name: default_ca
	  type: certificate
	  options:
	    is_ca: true
	    common_name: some-ca
	- name: director_ssl
	  type: certificate
	  options:
	    ca: default_ca
	    common_name: cc.cf.internal
	    alternative_names: [cc.cf.internal]
	    
See (CLI Variable Interpolation)[https://bosh.io/docs/cli-int.html] for more details about variables.