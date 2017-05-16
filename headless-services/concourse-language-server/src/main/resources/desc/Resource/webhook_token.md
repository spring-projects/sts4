*Optional.* If specified, web hooks can be sent to trigger an immediate *check* of the resource, specifying this value as a primitive form of authentication via query params.

After configuring this value, you would then configure your hook sender with the following painfully long path appended to your external URL:

	/api/v1/teams/TEAM_NAME/pipelines/PIPELINE_NAME/resources/RESOURCE_NAME/check/webhook?webhook_token=WEBHOOK_TOKEN

Note that the request payload sent to this API endpoint is entirely ignored. You should configure the resource as if you're not using web hooks, as the resource config is still the "source of truth."