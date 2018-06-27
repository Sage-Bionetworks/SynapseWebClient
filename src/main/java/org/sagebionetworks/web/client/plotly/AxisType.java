package org.sagebionetworks.web.client.plotly;

public enum AxisType {
	AUTO ("Auto"),
	CATEGORY("Category"),
	LINEAR("Linear");
	private String displayName;
	AxisType(String displayName) {
		this.displayName = displayName;
	}
	
	public String getDisplayName() {
		return displayName;
	}
}