package org.sagebionetworks.web.client.widget.profile;

import com.google.gwt.user.client.ui.IsWidget;

public interface UserProfileEditorWidgetView extends IsWidget {

	void setUsername(String userName);

	void setFirstName(String firstName);

	String getFirstName();

	String getLastName();

	String getUsername();

	void setLastName(String lastName);

	void setBio(String summary);

	String getBio();

}
