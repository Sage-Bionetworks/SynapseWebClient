package org.sagebionetworks.web.unitclient.widget.entity.browse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.EntityType;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Folder;
import org.sagebionetworks.repo.model.Link;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFilter;

public class EntityFilterTest {
	List<EntityHeader> headers;
	EntityHeader projectHeader, folderHeader, fileHeader, linkHeader;

	@Before
	public void setUp() {
		headers = new ArrayList<EntityHeader>();
		projectHeader = new EntityHeader();
		projectHeader.setType(Project.class.getName());
		headers.add(projectHeader);
		folderHeader = new EntityHeader();
		folderHeader.setType(Folder.class.getName());
		headers.add(folderHeader);
		fileHeader = new EntityHeader();
		fileHeader.setType(FileEntity.class.getName());
		headers.add(fileHeader);
		linkHeader = new EntityHeader();
		linkHeader.setType(Link.class.getName());
		headers.add(linkHeader);
	}

	@Test
	public void testFilterAllAllowed() {
		EntityFilter filter = EntityFilter.ALL;
		List<EntityType> queryValues = filter.getEntityQueryValues();

		assertTrue(queryValues.contains(EntityType.project));
		assertTrue(queryValues.contains(EntityType.folder));
		assertTrue(queryValues.contains(EntityType.file));
		assertTrue(queryValues.contains(EntityType.link));

		List<EntityHeader> filteredHeaders = filter.filterForBrowsing(headers);

		assertEquals(headers.size(), filteredHeaders.size());
	}

	@Test
	public void testFilterContainersOnly() {
		EntityFilter filter = EntityFilter.CONTAINER;
		List<EntityType> queryValues = filter.getEntityQueryValues();

		assertTrue(queryValues.contains(EntityType.project));
		assertTrue(queryValues.contains(EntityType.folder));
		assertFalse(queryValues.contains(EntityType.file));
		assertFalse(queryValues.contains(EntityType.link));

		List<EntityHeader> filteredHeaders = filter.filterForBrowsing(headers);
		assertTrue(filteredHeaders.contains(projectHeader));
		assertTrue(filteredHeaders.contains(folderHeader));
		assertFalse(filteredHeaders.contains(fileHeader));
		assertFalse(filteredHeaders.contains(linkHeader));
	}

	@Test
	public void testFilterProjectsOnly() {
		EntityFilter filter = EntityFilter.PROJECT;
		List<EntityType> queryValues = filter.getEntityQueryValues();

		assertTrue(queryValues.contains(EntityType.project));
		assertFalse(queryValues.contains(EntityType.folder));
		assertFalse(queryValues.contains(EntityType.file));
		assertFalse(queryValues.contains(EntityType.link));

		List<EntityHeader> filteredHeaders = filter.filterForBrowsing(headers);
		assertTrue(filteredHeaders.contains(projectHeader));
		assertFalse(filteredHeaders.contains(folderHeader));
		assertFalse(filteredHeaders.contains(fileHeader));
		assertFalse(filteredHeaders.contains(linkHeader));
	}

	@Test
	public void testFilterFolders() {
		EntityFilter filter = EntityFilter.CONTAINER;
		List<EntityType> queryValues = filter.getEntityQueryValues();

		assertTrue(queryValues.contains(EntityType.project));
		assertTrue(queryValues.contains(EntityType.folder));
		assertFalse(queryValues.contains(EntityType.file));
		assertFalse(queryValues.contains(EntityType.link));

		List<EntityHeader> filteredHeaders = filter.filterForBrowsing(headers);
		assertTrue(filteredHeaders.contains(projectHeader));
		assertTrue(filteredHeaders.contains(folderHeader));
		assertFalse(filteredHeaders.contains(fileHeader));
		assertFalse(filteredHeaders.contains(linkHeader));
	}

	@Test
	public void testFilterFiles() {
		EntityFilter filter = EntityFilter.ALL_BUT_LINK;
		List<EntityType> queryValues = filter.getEntityQueryValues();

		assertTrue(queryValues.contains(EntityType.project));
		assertTrue(queryValues.contains(EntityType.folder));
		assertTrue(queryValues.contains(EntityType.file));
		assertFalse(queryValues.contains(EntityType.link));

		List<EntityHeader> filteredHeaders = filter.filterForBrowsing(headers);
		assertTrue(filteredHeaders.contains(projectHeader));
		assertTrue(filteredHeaders.contains(folderHeader));
		assertTrue(filteredHeaders.contains(fileHeader));
		assertFalse(filteredHeaders.contains(linkHeader));
	}
}
