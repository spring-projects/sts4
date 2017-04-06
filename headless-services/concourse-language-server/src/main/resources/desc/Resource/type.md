*Required.* The type of the resource. Each worker advertises a mapping of `resource-type -> container-image; type` corresponds to the key in the map.

To see what resource types your deployment supports, check the status of your worker pool via the `/api/v1/workers` API endpoint.