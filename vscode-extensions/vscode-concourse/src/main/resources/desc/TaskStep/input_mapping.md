*Optional.* A map from task input names to concrete names in the build plan. This allows a task with generic input names to be used multiple times in the same plan, mapping its inputs to specific resources within the plan.

For example:

	plan:
	- get: diego-release
	- get: cf-release
	- get: ci-scripts
	- task: audit-diego-release
	  file: ci-scripts/audit-release.yml
	  input_mapping: {release-repo: diego-release}
	- task: audit-cf-release
	  file: ci-scripts/audit-release.yml
	  input_mapping: {release-repo: cf-release}