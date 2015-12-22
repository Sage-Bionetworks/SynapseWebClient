package org.sagebionetworks.web.server.servlet;

import org.sagebionetworks.repo.model.UserBundle;
import org.sagebionetworks.repo.model.UserSessionData;

public class UserLoginBundle {

	private UserSessionData userSessionData;
	private UserBundle userBundle;
	
	public UserLoginBundle(UserSessionData userSessionData, UserBundle userBundle) {
		this.userSessionData = userSessionData;
		this.userBundle = userBundle;
	}
	
	public UserSessionData getUserSessionData() {
		return userSessionData;
	}
	
	public void setUserSessionData(UserSessionData userSessionData) {
		this.userSessionData = userSessionData;
	}
	
	public UserBundle getUserBundle() {
		return userBundle;
	}
	
	public void setUserBundle(UserBundle userBundle) {
		this.userBundle = userBundle;
	}
}
