*Require*. The Director creates compilation VMs for release compilation. The Director will compile each release on every necessary stemcell used in a deployment. A compilation definition allows to specify VM characteristics.

Example:

	compilation:
	  workers: 2
	  reuse_compilation_vms: true
	  az: z1
	  vm_type: default
	  network: private
