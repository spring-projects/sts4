*Optional.* A string specifying the rootfs of the container, as interpreted 
by your worker's Garden backend.

You should only use this if you cannot use `image_resource` for some reason, 
and you know what you're doing.

Note: Prior to Concourse 3.0 this property was called `image`.