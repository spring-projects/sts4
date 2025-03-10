package com.example.configproperties;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class MyController {
	
	private ConfigurationPropertiesExample configs;
	private ConfigurationPropertiesExampleWithNestedConfigs nestedConfigs;
	private ConfigurationPropertiesWithRecords recordConfigs;
	
	public MyController(
			ConfigurationPropertiesExample configs,
			ConfigurationPropertiesExampleWithNestedConfigs nestedConfigs,
			ConfigurationPropertiesWithRecords recordConfigs) {
		this.configs = configs;
		this.nestedConfigs = nestedConfigs;
		this.recordConfigs = recordConfigs;
	}
	
	@GetMapping("/configs")
	public String getExampleConfig() {
		return configs.getSimpleConfigProp();
	}
	
	@GetMapping("/bundleconfigs")
	public String getBundleConfig() {
		return nestedConfigs.getParentLevelConfig() + " / " + nestedConfigs.getBundle().getBundleConfigString();
	}
	
	@GetMapping("/recordconfigs")
	public String getRecordConfig() {
		return recordConfigs.name();
	}

}
