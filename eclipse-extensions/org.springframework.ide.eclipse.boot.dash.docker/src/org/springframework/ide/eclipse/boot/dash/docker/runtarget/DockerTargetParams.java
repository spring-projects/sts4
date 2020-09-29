package org.springframework.ide.eclipse.boot.dash.docker.runtarget;

public class DockerTargetParams {

	private final String uri;
	
	public DockerTargetParams(String uri) {
		super();
		this.uri = uri;
	}

	public String getUri() {
		return uri;
	}
	
	@Override
	public String toString() {
		return uri;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((uri == null) ? 0 : uri.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DockerTargetParams other = (DockerTargetParams) obj;
		if (uri == null) {
			if (other.uri != null)
				return false;
		} else if (!uri.equals(other.uri))
			return false;
		return true;
	}
	
	

}
