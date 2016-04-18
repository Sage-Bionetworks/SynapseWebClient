package org.sagebionetworks.web.client.widget.entity.browse;

import org.sagebionetworks.repo.model.util.ContentTypeUtils;
import org.sagebionetworks.web.client.DisplayUtils;

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
	
	boolean isImage(String contentType) {
		return DisplayUtils.isRecognizedImageContentType(contentType);
	}
	
	boolean isCode(String fileName) {
		return ContentTypeUtils.isRecognizedCodeFileName(fileName);
	}
	
	boolean isText(String contentType) {
		return DisplayUtils.isTextType(contentType);
	}
	
	String[] getEntityQueryValues() {
		return entityQueryValues;
	}
}
