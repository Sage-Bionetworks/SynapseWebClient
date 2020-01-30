package org.sagebionetworks.web.client.plotly;

public enum GraphType {
	BAR("Bar"), SCATTER("Line");

	private String displayName;

	GraphType(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}
}
