Use the `health-check-http-endpoint` attribute to customize the endpoint for the `http` health check type. If you do not provide a `health-check-http-endpoint` attribute, it uses endpoint `/`.

	---
	  ...
	  health-check-type: http
	  health-check-http-endpoint: /health
