package org.sagebionetworks.web.client;

import static org.junit.Assert.assertEquals;

import org.gwtbootstrap3.client.ui.constants.IconType;
import org.junit.Test;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.EntityType;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Folder;
import org.sagebionetworks.repo.model.Link;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.table.Dataset;
import org.sagebionetworks.repo.model.table.EntityView;
import org.sagebionetworks.repo.model.table.MaterializedView;
import org.sagebionetworks.repo.model.table.SubmissionView;
import org.sagebionetworks.repo.model.table.TableEntity;

public class EntityTypeUtilsTest {

	@Test
	public void testGetEntityClassNameForEntityType() {
		assertEquals(FileEntity.class.getName(), EntityTypeUtils.getEntityClassNameForEntityType(EntityType.file.name()));
		assertEquals(Folder.class.getName(), EntityTypeUtils.getEntityClassNameForEntityType(EntityType.folder.name()));
		assertEquals(Project.class.getName(), EntityTypeUtils.getEntityClassNameForEntityType(EntityType.project.name()));
		assertEquals(TableEntity.class.getName(), EntityTypeUtils.getEntityClassNameForEntityType(EntityType.table.name()));
		assertEquals(EntityView.class.getName(), EntityTypeUtils.getEntityClassNameForEntityType(EntityType.entityview.name()));
		assertEquals(Link.class.getName(), EntityTypeUtils.getEntityClassNameForEntityType(EntityType.link.name()));
		assertEquals(SubmissionView.class.getName(), EntityTypeUtils.getEntityClassNameForEntityType(EntityType.submissionview.name()));
		assertEquals(Dataset.class.getName(), EntityTypeUtils.getEntityClassNameForEntityType(EntityType.dataset.name()));
		assertEquals(MaterializedView.class.getName(), EntityTypeUtils.getEntityClassNameForEntityType(EntityType.materializedview.name()));
		assertEquals(FileEntity.class.getName(), EntityTypeUtils.getEntityClassNameForEntityType("default"));
	}

	@Test
	public void testGetEntityTypeForEntityClassName() {
		assertEquals(EntityType.file, EntityTypeUtils.getEntityTypeForEntityClassName(FileEntity.class.getName()));
		assertEquals(EntityType.folder, EntityTypeUtils.getEntityTypeForEntityClassName(Folder.class.getName()));
		assertEquals(EntityType.project, EntityTypeUtils.getEntityTypeForEntityClassName(Project.class.getName()));
		assertEquals(EntityType.table, EntityTypeUtils.getEntityTypeForEntityClassName(TableEntity.class.getName()));
		assertEquals(EntityType.entityview, EntityTypeUtils.getEntityTypeForEntityClassName(EntityView.class.getName()));
		assertEquals(EntityType.link, EntityTypeUtils.getEntityTypeForEntityClassName(Link.class.getName()));
		assertEquals(EntityType.submissionview, EntityTypeUtils.getEntityTypeForEntityClassName(SubmissionView.class.getName()));
		assertEquals(EntityType.dataset, EntityTypeUtils.getEntityTypeForEntityClassName(Dataset.class.getName()));
		assertEquals(EntityType.materializedview, EntityTypeUtils.getEntityTypeForEntityClassName(MaterializedView.class.getName()));
		assertEquals(EntityType.file, EntityTypeUtils.getEntityTypeForEntityClassName("default"));
	}

	@Test
	public void testGetIconTypeForEntityClassName() {
		assertEquals(IconType.FILE, EntityTypeUtils.getIconTypeForEntityClassName(FileEntity.class.getName()));
		assertEquals(IconType.FOLDER, EntityTypeUtils.getIconTypeForEntityClassName(Folder.class.getName()));
		assertEquals(IconType.LIST_ALT, EntityTypeUtils.getIconTypeForEntityClassName(Project.class.getName()));
		assertEquals(IconType.TABLE, EntityTypeUtils.getIconTypeForEntityClassName(TableEntity.class.getName()));
		assertEquals(IconType.TH_LIST, EntityTypeUtils.getIconTypeForEntityClassName(EntityView.class.getName()));
		assertEquals(IconType.LINK, EntityTypeUtils.getIconTypeForEntityClassName(Link.class.getName()));
		assertEquals(IconType.TH_LIST, EntityTypeUtils.getIconTypeForEntityClassName(MaterializedView.class.getName()));
		assertEquals(IconType.FILE, EntityTypeUtils.getIconTypeForEntityClassName("default"));
	}

	@Test
	public void testGetFriendlyTableTypeName() {
		assertEquals(EntityTypeUtils.TABLE_ENTITY_DISPLAY_NAME, EntityTypeUtils.getFriendlyTableTypeName(TableEntity.class.getName()));
		assertEquals(EntityTypeUtils.ENTITY_VIEW_DISPLAY_NAME, EntityTypeUtils.getFriendlyTableTypeName(EntityView.class.getName()));
		assertEquals(EntityTypeUtils.SUBMISSION_VIEW_DISPLAY_NAME, EntityTypeUtils.getFriendlyTableTypeName(SubmissionView.class.getName()));
		assertEquals(EntityTypeUtils.DATASET_DISPLAY_NAME, EntityTypeUtils.getFriendlyTableTypeName(Dataset.class.getName()));
		assertEquals(EntityTypeUtils.MATERIALIZED_VIEW_DISPLAY_NAME, EntityTypeUtils.getFriendlyTableTypeName(MaterializedView.class.getName()));

		assertEquals(EntityTypeUtils.UNKNOWN_TABLE_TYPE, EntityTypeUtils.getFriendlyTableTypeName("coffee table"));
	}

	@Test
	public void testGetEntityTypeWithHeader() {
		// EntityHeader.type field is the class name
		EntityHeader header = new EntityHeader();
		header.setType(FileEntity.class.getName());
		assertEquals(EntityType.file, EntityTypeUtils.getEntityType(header));

		header.setType(Folder.class.getName());
		assertEquals(EntityType.folder,EntityTypeUtils.getEntityType(header));

		header.setType(Project.class.getName());
		assertEquals(EntityType.project,EntityTypeUtils.getEntityType(header));

		header.setType(TableEntity.class.getName());
		assertEquals(EntityType.table, EntityTypeUtils.getEntityType(header));

		header.setType(EntityView.class.getName());
		assertEquals(EntityType.entityview,EntityTypeUtils.getEntityType(header));

		header.setType(MaterializedView.class.getName());
		assertEquals(EntityType.materializedview,EntityTypeUtils.getEntityType(header));

		header.setType(Link.class.getName());
		assertEquals(EntityType.link, EntityTypeUtils.getEntityType(header));

		header.setType(SubmissionView.class.getName());
		assertEquals(EntityType.submissionview, EntityTypeUtils.getEntityType(header));

		header.setType(Dataset.class.getName());
		assertEquals(EntityType.dataset, EntityTypeUtils.getEntityType(header));

		// Default cases
		header.setType(null);
		assertEquals(EntityType.file, EntityTypeUtils.getEntityType(header));

		header.setType("");
		assertEquals(EntityType.file, EntityTypeUtils.getEntityType(header));
	}
}
