package org.sagebionetworks.web.client.widget.entity.dialog;

import org.sagebionetworks.web.client.widget.provenance.Dimension;

public class WidgetRegistration {
	private String className, friendlyName;
	private Dimension size;
	public WidgetRegistration(String className,	String friendlyName, Dimension size) {
		super();
		this.friendlyName = friendlyName;
		this.className = className;
		this.size = size;
	}
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	public Dimension getSize() {
		return size;
	}
	public void setSize(Dimension size) {
		this.size = size;
	}
	public String getFriendlyName() {
		return friendlyName;
	}
	public void setFriendlyName(String friendlyName) {
		this.friendlyName = friendlyName;
	}
}
