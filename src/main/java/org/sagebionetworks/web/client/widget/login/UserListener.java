package org.sagebionetworks.web.client.widget.login;

import org.sagebionetworks.repo.model.UserSessionData;


public interface UserListener {
	
	public void userChanged(UserSessionData newUser);

}
