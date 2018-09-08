package org.sagebionetworks.web.client.widget.user;

import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.IsWidget;

public interface UserBadgeView extends IsWidget, SynapseView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	void setPresenter(Presenter presenter);
	void setDisplayName(String displayName, String shortDisplayName);
	void showLoadError(String error);
	void showAnonymousUserPicture();
	void showCustomUserPicture(String url);
	void showDescription(String description);
	void setSize(BadgeSize size);
	void setDefaultPictureColor(String colorCss);
	void setDefaultPictureLetter(String letter);
	void setHref(String href);
	void setStyleNames(String style);
	void addUsernameLinkStyle(String style);
	void setHeight(String height);
	void setCustomClickHandler(ClickHandler clickHandler);
	void setUserId(String userId);
	void setOpenInNewWindow();
	void doNothingOnClick();
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void onImageLoadError();
	}

}
