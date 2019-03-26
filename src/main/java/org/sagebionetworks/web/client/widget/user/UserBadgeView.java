package org.sagebionetworks.web.client.widget.user;

import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.utils.Callback;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.IsWidget;

public interface UserBadgeView extends IsWidget, SynapseView {
	void configure(UserProfile profile);
	void showLoadError(String error);
	void setSize(BadgeSize size);
	void addStyleName(String styles);
	void setHeight(String height);
	void setCustomClickHandler(ClickHandler clickHandler);
	void doNothingOnClick();
	void setTextHidden(boolean isTextHidden);
	void setTooltipHidden(boolean isTooltipHidden);
	void setOpenInNewWindow();
	void addContextCommand(String commandName, Callback callback); 
}
