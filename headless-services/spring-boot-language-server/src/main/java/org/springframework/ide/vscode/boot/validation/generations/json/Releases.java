package org.springframework.ide.vscode.boot.validation.generations.json;

import java.util.Arrays;
import java.util.List;

import com.google.common.collect.ImmutableList;

public class Releases {

	private Release[] releases;

	public List<Release> getReleases() {
		return releases != null ? Arrays.asList(releases) : ImmutableList.of();
	}

}
