*Optional.* A string specifying the rootfs of the container, as interpreted 
by your worker's Garden backend.

You should only use this if you cannot use `image_resource` for some reason, 
and you know what you're doing.

WARNING: This property has been renamed to `rootfs_uri` in Concourse 3.0.