package org.sagebionetworks.web.client.widget.entity.browse;

import static org.sagebionetworks.repo.model.EntityType.entityview;
import static org.sagebionetworks.repo.model.EntityType.file;
import static org.sagebionetworks.repo.model.EntityType.folder;
import static org.sagebionetworks.repo.model.EntityType.link;
import static org.sagebionetworks.repo.model.EntityType.project;
import static org.sagebionetworks.repo.model.EntityType.table;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.EntityType;
import org.sagebionetworks.web.client.EntityTypeUtils;

public enum EntityFilter {
	ALL(project, folder, file, link), CONTAINER(project, folder), PROJECT(project), FOLDER(folder), FILE(file), ALL_BUT_LINK(project, folder, file), PROJECT_OR_TABLE(project, table, entityview);

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
