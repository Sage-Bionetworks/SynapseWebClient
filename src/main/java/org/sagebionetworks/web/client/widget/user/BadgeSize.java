package org.sagebionetworks.web.client.widget.user;


public enum BadgeSize {
	DEFAULT("SMALL USER CARD"), MEDIUM("MEDIUM USER CARD"), LARGE("LARGE USER CARD");

	String reactClientSize;

	private BadgeSize(String reactClientSize) {
		this.reactClientSize = reactClientSize;
	}

	public String getReactClientSize() {
		return reactClientSize;
	}
}
