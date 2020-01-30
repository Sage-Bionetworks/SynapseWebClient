package org.sagebionetworks.web.client.plotly;

public enum BarMode {
	GROUP("Grouped"), STACK("Stacked");

	private String displayName;

	BarMode(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}
}
