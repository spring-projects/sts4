package com.example.configproperties;

import java.util.Date;

public class ConfigBundle {
	
	private final String bundleConfigString;
	private final int bundleConfigInt;
	private final Date bundleConfigDate;
	
	public ConfigBundle(String bundleConfigString, int bundleConfigInt, Date bundleConfigDate) {
		super();
		this.bundleConfigString = bundleConfigString;
		this.bundleConfigInt = bundleConfigInt;
		this.bundleConfigDate = bundleConfigDate;
	}

	public Date getBundleConfigDate() {
		return bundleConfigDate;
	}
	
	public int getBundleConfigInt() {
		return bundleConfigInt;
	}
	
	public String getBundleConfigString() {
		return bundleConfigString;
	}

}
