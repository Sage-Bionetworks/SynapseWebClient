package org.sagebionetworks.web.client.widget.entity.registration;


public class WidgetRegistration {
	private String className, friendlyName;

	public WidgetRegistration(String className, String friendlyName) {
		super();
		this.friendlyName = friendlyName;
		this.className = className;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getFriendlyName() {
		return friendlyName;
	}

	public void setFriendlyName(String friendlyName) {
		this.friendlyName = friendlyName;
	}
}
