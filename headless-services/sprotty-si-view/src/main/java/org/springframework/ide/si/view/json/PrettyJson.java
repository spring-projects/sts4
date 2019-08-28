package org.springframework.ide.si.view.json;

import com.google.gson.GsonBuilder;

public class PrettyJson {
	
	@Override
	public String toString() {
		return new GsonBuilder().setPrettyPrinting().create().toJson(this);
	}
	

}
