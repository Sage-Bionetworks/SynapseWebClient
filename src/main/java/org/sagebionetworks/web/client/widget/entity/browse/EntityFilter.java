package org.sagebionetworks.web.client.widget.entity.browse;


public enum EntityFilter {
	ALL("project", "folder", "file", "link"),
	CONTAINER("project", "folder"),
	PROJECT("project"),
	FOLDER("project", "folder"),
	FILE("project", "folder", "file");
	
	// when browsing (in the entity tree browser), only these types should be shown.
	private String[] entityQueryValues;
	
	private EntityFilter(String... values) {
		entityQueryValues = values;
	}
	
	String[] getEntityQueryValues() {
		return entityQueryValues;
	}
}
