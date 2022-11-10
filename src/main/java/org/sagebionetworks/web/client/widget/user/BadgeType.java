package org.sagebionetworks.web.client.widget.user;

public enum BadgeType {
  AVATAR("AVATAR"),
  SMALL_CARD("SMALL USER CARD"),
  MEDIUM_CARD("MEDIUM USER CARD"),
  LARGE_CARD("LARGE USER CARD");

  private String userCardType;

  private BadgeType(String userCardType) {
    this.userCardType = userCardType;
  }

  public String getUserCardType() {
    return userCardType;
  }
}
