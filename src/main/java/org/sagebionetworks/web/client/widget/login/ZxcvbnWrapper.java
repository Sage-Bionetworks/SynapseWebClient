package org.sagebionetworks.web.client.widget.login;

public interface ZxcvbnWrapper {
	void scorePassword(String password);
	String getFeedback();
	int getScore();
}
