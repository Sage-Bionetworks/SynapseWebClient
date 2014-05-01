package org.sagebionetworks.web.client.widget.sharing;

import org.sagebionetworks.web.shared.users.PermissionLevel;

public class PermissionLevelSelect {
	private String display;
	private PermissionLevel level;
	public PermissionLevelSelect(String display, PermissionLevel level) {
		super();
		this.display = display;
		this.level = level;
	}
	public String getDisplay() {
		return display;
	}
	public void setDisplay(String display) {
		this.display = display;
	}
	public PermissionLevel getLevel() {
		return level;
	}
	public void setLevel(PermissionLevel level) {
		this.level = level;
	}		
	
	public String toString() {
		return display;
	}
}