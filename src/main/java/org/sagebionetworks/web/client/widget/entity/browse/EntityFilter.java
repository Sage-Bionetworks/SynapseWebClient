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
	FOLDER(EntityType.folder),
	FILE(EntityType.file),
	ALL_BUT_LINK(EntityType.project, EntityType.folder, EntityType.file),
	TABLE(EntityType.table);
	
	// when browsing (in the entity tree browser), only these types should be shown.
	private Set<String> entityTypeClassNamesSet;
	private List<EntityType> entityTypes;
	
	private EntityFilter(EntityType... values) {
		entityTypes = new ArrayList<EntityType>();
		entityTypeClassNamesSet = new HashSet<String>(values.length);
		for (int i = 0; i < values.length; i++) {
			entityTypes.add(values[i]);
			entityTypeClassNamesSet.add(EntityTypeUtils.getEntityClassNameForEntityType(values[i].name()));
		}
	}
	
	public List<EntityType> getEntityQueryValues() {
		return entityTypes;
	}
	
	public List<EntityHeader> filterForBrowsing(List<EntityHeader> headers) {
		List<EntityHeader> returnHeaders = new ArrayList<EntityHeader>();
		for (EntityHeader entityHeader : headers) {
			if (entityTypeClassNamesSet.contains(entityHeader.getType())) {
				returnHeaders.add(entityHeader);
			}
		}
		return returnHeaders;
	}
}
