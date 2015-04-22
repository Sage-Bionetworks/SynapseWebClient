package org.sagebionetworks.repo.model;

public class ProjectTooltipHeader {
	public ProjectHeader header;
	public UserProfile lastModifier;
	
	public ProjectTooltipHeader(ProjectHeader header,UserProfile lastModifier) {
		this.header = header;
		this.lastModifier = lastModifier;
	}
}
