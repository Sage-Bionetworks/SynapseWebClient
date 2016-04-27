package org.sagebionetworks.web.client.widget.entity.browse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.EntityType;
import org.sagebionetworks.web.client.EntityTypeUtils;

public enum EntityFilter {
	ALL(EntityType.project, EntityType.folder, EntityType.file, EntityType.link),
	CONTAINER(EntityType.project, EntityType.folder),
	PROJECT(EntityType.project),
	FOLDER(EntityType.project, EntityType.folder),
	FILE(EntityType.project, EntityType.folder, EntityType.file);
	
	// when browsing (in the entity tree browser), only these types should be shown.
	private String[] entityQueryValues;
	private Set<String> entityTypeClassNamesSet;
	
	private EntityFilter(EntityType... values) {
		entityQueryValues = new String[values.length];
		entityTypeClassNamesSet = new HashSet<String>(entityQueryValues.length);
		for (int i = 0; i < values.length; i++) {
			entityQueryValues[i] = values[i].name();
			entityTypeClassNamesSet.add(EntityTypeUtils.getEntityClassNameForEntityType(entityQueryValues[i]));
		}
	}
	
	String[] getEntityQueryValues() {
		return entityQueryValues;
	}
	
	List<EntityHeader> filterForBrowsing(List<EntityHeader> headers) {
		List<EntityHeader> returnHeaders = new ArrayList<EntityHeader>();
		for (EntityHeader entityHeader : headers) {
			if (entityTypeClassNamesSet.contains(entityHeader.getType())) {
				returnHeaders.add(entityHeader);
			}
		}
		return returnHeaders;
	}
}
