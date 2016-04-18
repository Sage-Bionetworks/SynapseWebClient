package org.sagebionetworks.web.client.widget.entity.browse;


public enum EntityFilter {
	ALL("project", "folder", "file", "link"),
	CONTAINER("project", "folder"),
	PROJECT("project"),
	FOLDER("folder"),
	FILE("file");
	
	private String[] entityQueryValues;
	private EntityFilter(String... values) {
		entityQueryValues = values;
	}
	
	String[] getEntityQueryValues() {
		return entityQueryValues;
	}
}
