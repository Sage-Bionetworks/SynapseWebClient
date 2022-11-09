package org.sagebionetworks.web.client.widget.user;

public enum AvatarSize {
  SMALL("SMALL"),
  MEDIUM("MEDIUM"),
  LARGE("LARGE");

  private String avatarSize;

  private AvatarSize(String avatarSize) {
    this.avatarSize = avatarSize;
  }

  public String getAvatarSize() {
    return avatarSize;
  }
}
