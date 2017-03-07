package org.sagebionetworks.web.shared;

import com.google.gwt.user.client.rpc.IsSerializable;


public class ProjectDisplayBundle implements IsSerializable {
	
	private boolean wiki;
	private boolean files;
	private boolean tables;
	private boolean challenge;
	private boolean discussion;
	private boolean docker;
	
	public ProjectDisplayBundle() {}
	
	public ProjectDisplayBundle(boolean wiki, boolean files, boolean tables, boolean challenge, boolean discussion, boolean docker) {
		this.wiki = wiki;
		this.files = files;
		this.tables = tables;
		this.challenge = challenge;
		this.discussion = discussion;
		this.docker = docker;
	}
	
	public boolean wikiHasContent() {
		return wiki;
	}
	public boolean filesHasContent() {
		return files;
	}
	public boolean tablesHasContent() {
		return tables;
	}
	public boolean challengeHasContent() {
		return challenge;
	}
	public boolean discussionHasContent() {
		return discussion;
	}
	public boolean dockerHasContent() {
		return docker;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
//		result = prime * result + ((wiki == false) ? 0 : wiki.hashCode());
//		result = prime * result
//				+ ((userProfile == null) ? 0 : userProfile.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProjectDisplayBundle other = (ProjectDisplayBundle) obj;
		return !((this.wiki ^ other.wiki) || (this.files ^ other.files) || (this.tables ^ other.tables) 
				|| (this.challenge ^ other.challenge) || (this.discussion ^ other.discussion) || (this.docker ^ other.docker));
	}
	
}
