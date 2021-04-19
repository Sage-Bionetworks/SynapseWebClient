package org.sagebionetworks.web.client.widget.user;


public enum BadgeSize {
	SMALL("SMALL USER CARD", "LARGE"),
	MEDIUM("MEDIUM USER CARD", "LARGE"),
	LARGE("LARGE USER CARD", "LARGE"),
	SMALL_AVATAR("AVATAR", "SMALL"),
	LARGE_AVATAR("AVATAR", "LARGE");

	private String reactClientSize;
	private String avatarSize;

	private BadgeSize(String reactClientSize, String avatarSize) {
		this.reactClientSize = reactClientSize;
		this.avatarSize = avatarSize;
	}

	public String getReactClientSize() {
		return reactClientSize;
	}
	public String getAvatarSize() {
		return avatarSize;
	}
}
