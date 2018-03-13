Splitting up your pipeline into sections.

A pipeline may optionally contain a section called `groups`. As more resources and jobs are added to a pipeline it can become difficult to navigate. Pipeline groups allow you to group jobs together under a header and have them show on different tabs in the user interface. Groups have no functional effect on your pipeline.

A simple grouping for the pipeline above may look like:

	groups:
	- name: tests
	  jobs:
	  - controller-mysql
	  - controller-postgres
	  - worker
	  - integration
	- name: deploy
	  jobs:
	  - deploy

This would display two tabs at the top of the home page: "tests" and "deploy". Once you have added groups to your pipeline then all jobs must be in a group otherwise they will not be visible.

For a real world example of how groups can be used to simplify navigation and provide logical grouping, see the groups used at the top of the page in the [Concourse pipeline](https://ci.concourse-ci.org/).

