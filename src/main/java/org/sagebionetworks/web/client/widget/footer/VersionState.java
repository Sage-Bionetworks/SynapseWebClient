package org.sagebionetworks.web.client.widget.footer;

public class VersionState {
	private String version;
	private boolean isVersionChange;

	public VersionState(String version, boolean isVersionChange) {
		super();
		this.version = version;
		this.isVersionChange = isVersionChange;
	}

	public String getVersion() {
		return version;
	}

	public boolean isVersionChange() {
		return isVersionChange;
	}

	public void setVersionChange(boolean isVersionChange) {
		this.isVersionChange = isVersionChange;
	}

	public void setVersion(String version) {
		this.version = version;
	}
}
