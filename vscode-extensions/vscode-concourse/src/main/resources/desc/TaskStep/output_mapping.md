*Optional.* A map from task output names to concrete names to register in the build plan. This allows a task with generic output names to be used multiple times in the same plan.

This is often used together with input_mapping. For example:

	plan:
	- get: diego-release
	- get: cf-release
	- get: ci-scripts
	- task: create-diego-release
	  file: ci-scripts/create-release.yml
	  input_mapping: {release-repo: diego-release}
	  output_mapping: {release-tarball: diego-release-tarball}
	- task: create-cf-release
	  file: ci-scripts/create-release.yml
	  input_mapping: {release-repo: cf-release}
	  output_mapping: {release-tarball: cf-release-tarball}