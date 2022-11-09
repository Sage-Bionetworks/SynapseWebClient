package org.sagebionetworks.web.client.widget.user;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.IsWidget;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.utils.Callback;

public interface UserBadgeView extends IsWidget, SynapseView {
  void configure(
    UserProfile profile,
    String pictureURL,
    Boolean isCertified,
    Boolean isValidated
  );

  void showLoadError(String error);

  void setBadgeType(BadgeType badgeType);

  void setShowAvatar(boolean showAvatar);

  void setAvatarSize(AvatarSize avatarSize);

  void addStyleName(String styles);

  void setHeight(String height);

  void setCustomClickHandler(ClickHandler clickHandler);

  void doNothingOnClick();

  void setShowCardOnHover(boolean isTooltipHidden);

  void setOpenInNewWindow();

  void addContextCommand(String commandName, Callback callback);
}
