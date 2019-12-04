package org.sagebionetworks.web.client.widget.profile;

import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.ui.IsWidget;

public interface UserProfileEditorWidgetView extends IsWidget {

	public interface Presenter {
	}

	void setUsername(String userName);

	void setFirstName(String firstName);

	String getFirstName();

	String getLastName();

	String getUsername();

	void setLastName(String lastName);

	void setBio(String summary);

	String getBio();

	void showUsernameError(String error);

	void hideUsernameError();

	void addImageWidget(IsWidget image);

	void addFileInputWidget(IsWidget fileInputWidget);

	public void setPresenter(Presenter presenter);

	String getLink();

	void showLinkError(String string);

	void hideLinkError();

	String getCurrentPosition();

	void setCurrentPosition(String position);

	void setCurrentAffiliation(String company);

	String getCurrentAffiliation();

	void setIndustry(String industry);

	String getIndustry();

	void setLocation(String location);

	String getLocation();

	void setLink(String url);

	void addKeyDownHandlerToFields(KeyDownHandler keyDownHandler);

}
