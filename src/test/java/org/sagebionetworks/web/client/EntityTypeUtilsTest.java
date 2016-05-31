package org.sagebionetworks.web.client;

import static org.junit.Assert.*;

import org.gwtbootstrap3.client.ui.constants.IconType;
import org.junit.Test;
import org.sagebionetworks.repo.model.EntityType;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Folder;
import org.sagebionetworks.repo.model.Link;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.table.FileView;
import org.sagebionetworks.repo.model.table.TableEntity;

public class EntityTypeUtilsTest {

	@Test
	public void testGetEntityClassNameForEntityType() {
		assertEquals(FileEntity.class.getName(), EntityTypeUtils.getEntityClassNameForEntityType(EntityType.file.name()));
		assertEquals(Folder.class.getName(), EntityTypeUtils.getEntityClassNameForEntityType(EntityType.folder.name()));
		assertEquals(Project.class.getName(), EntityTypeUtils.getEntityClassNameForEntityType(EntityType.project.name()));
		assertEquals(TableEntity.class.getName(), EntityTypeUtils.getEntityClassNameForEntityType(EntityType.table.name()));
		assertEquals(FileView.class.getName(), EntityTypeUtils.getEntityClassNameForEntityType(EntityType.fileview.name()));
		assertEquals(Link.class.getName(), EntityTypeUtils.getEntityClassNameForEntityType(EntityType.link.name()));
		
		assertEquals(FileEntity.class.getName(), EntityTypeUtils.getEntityClassNameForEntityType("default"));
	}

	@Test
	public void testGetEntityTypeForEntityClassName() {
		assertEquals(EntityType.file, EntityTypeUtils.getEntityTypeForEntityClassName(FileEntity.class.getName()));
		assertEquals(EntityType.folder, EntityTypeUtils.getEntityTypeForEntityClassName(Folder.class.getName()));
		assertEquals(EntityType.project, EntityTypeUtils.getEntityTypeForEntityClassName(Project.class.getName()));
		assertEquals(EntityType.table, EntityTypeUtils.getEntityTypeForEntityClassName(TableEntity.class.getName()));
		assertEquals(EntityType.fileview, EntityTypeUtils.getEntityTypeForEntityClassName(FileView.class.getName()));
		assertEquals(EntityType.link, EntityTypeUtils.getEntityTypeForEntityClassName(Link.class.getName()));
		
		assertEquals(EntityType.file, EntityTypeUtils.getEntityTypeForEntityClassName("default"));
	}
	
	@Test
	public void testGetIconTypeForEntityClassName() {
		assertEquals(IconType.FILE_O, EntityTypeUtils.getIconTypeForEntityClassName(FileEntity.class.getName()));
		assertEquals(IconType.FOLDER, EntityTypeUtils.getIconTypeForEntityClassName(Folder.class.getName()));
		assertEquals(IconType.LIST_ALT, EntityTypeUtils.getIconTypeForEntityClassName(Project.class.getName()));
		assertEquals(IconType.TABLE, EntityTypeUtils.getIconTypeForEntityClassName(TableEntity.class.getName()));
		assertEquals(IconType.FILES_O, EntityTypeUtils.getIconTypeForEntityClassName(FileView.class.getName()));
		assertEquals(IconType.LINK, EntityTypeUtils.getIconTypeForEntityClassName(Link.class.getName()));
		
		assertEquals(IconType.FILE_O, EntityTypeUtils.getIconTypeForEntityClassName("default"));
	}

}
