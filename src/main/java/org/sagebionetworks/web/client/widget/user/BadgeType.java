package org.sagebionetworks.web.client.widget.user;


public enum BadgeType {
	AVATAR("AVATAR"),
	SMALL("SMALL USER CARD"),
	MEDIUM("MEDIUM USER CARD"),
	LARGE("LARGE USER CARD");

	private String userCardType;

	private BadgeType(String userCardType) {
		this.userCardType = userCardType;
	}

	public String getUserCardType() {
		return userCardType;
	}
}
