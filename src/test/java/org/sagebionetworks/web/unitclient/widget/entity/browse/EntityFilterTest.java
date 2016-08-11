package org.sagebionetworks.web.unitclient.widget.entity.browse;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.EntityType;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Folder;
import org.sagebionetworks.repo.model.Link;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFilter;

import com.amazonaws.services.autoscaling.model.Filter;

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
		String[] queryValues = filter.getEntityQueryValues();
		Set<String> queryValuesSet = new HashSet<String>();
		Collections.addAll(queryValuesSet, queryValues);
		
		assertTrue(queryValuesSet.contains(EntityType.project.name()));
		assertTrue(queryValuesSet.contains(EntityType.folder.name()));
		assertTrue(queryValuesSet.contains(EntityType.file.name()));
		assertTrue(queryValuesSet.contains(EntityType.link.name()));
		
		List<EntityHeader> filteredHeaders = filter.filterForBrowsing(headers);
		
		assertEquals(headers.size(), filteredHeaders.size());
	}
	
	@Test
	public void testFilterContainersOnly() {
		EntityFilter filter = EntityFilter.CONTAINER;
		String[] queryValues = filter.getEntityQueryValues();
		Set<String> queryValuesSet = new HashSet<String>();
		Collections.addAll(queryValuesSet, queryValues);
		
		assertTrue(queryValuesSet.contains(EntityType.project.name()));
		assertTrue(queryValuesSet.contains(EntityType.folder.name()));
		assertFalse(queryValuesSet.contains(EntityType.file.name()));
		assertFalse(queryValuesSet.contains(EntityType.link.name()));
		
		List<EntityHeader> filteredHeaders = filter.filterForBrowsing(headers);
		assertTrue(filteredHeaders.contains(projectHeader));
		assertTrue(filteredHeaders.contains(folderHeader));
		assertFalse(filteredHeaders.contains(fileHeader));
		assertFalse(filteredHeaders.contains(linkHeader));
	}
	
	@Test
	public void testFilterProjectsOnly() {
		EntityFilter filter = EntityFilter.PROJECT;
		String[] queryValues = filter.getEntityQueryValues();
		Set<String> queryValuesSet = new HashSet<String>();
		Collections.addAll(queryValuesSet, queryValues);
		
		assertTrue(queryValuesSet.contains(EntityType.project.name()));
		assertFalse(queryValuesSet.contains(EntityType.folder.name()));
		assertFalse(queryValuesSet.contains(EntityType.file.name()));
		assertFalse(queryValuesSet.contains(EntityType.link.name()));
		
		List<EntityHeader> filteredHeaders = filter.filterForBrowsing(headers);
		assertTrue(filteredHeaders.contains(projectHeader));
		assertFalse(filteredHeaders.contains(folderHeader));
		assertFalse(filteredHeaders.contains(fileHeader));
		assertFalse(filteredHeaders.contains(linkHeader));
	}
	
	@Test
	public void testFilterFolders() {
		EntityFilter filter = EntityFilter.FOLDER;
		String[] queryValues = filter.getEntityQueryValues();
		Set<String> queryValuesSet = new HashSet<String>();
		Collections.addAll(queryValuesSet, queryValues);
		
		assertTrue(queryValuesSet.contains(EntityType.project.name()));
		assertTrue(queryValuesSet.contains(EntityType.folder.name()));
		assertFalse(queryValuesSet.contains(EntityType.file.name()));
		assertFalse(queryValuesSet.contains(EntityType.link.name()));
		
		List<EntityHeader> filteredHeaders = filter.filterForBrowsing(headers);
		assertTrue(filteredHeaders.contains(projectHeader));
		assertTrue(filteredHeaders.contains(folderHeader));
		assertFalse(filteredHeaders.contains(fileHeader));
		assertFalse(filteredHeaders.contains(linkHeader));
	}

	@Test
	public void testFilterFiles() {
		EntityFilter filter = EntityFilter.FILE;
		String[] queryValues = filter.getEntityQueryValues();
		Set<String> queryValuesSet = new HashSet<String>();
		Collections.addAll(queryValuesSet, queryValues);
		
		assertTrue(queryValuesSet.contains(EntityType.project.name()));
		assertTrue(queryValuesSet.contains(EntityType.folder.name()));
		assertTrue(queryValuesSet.contains(EntityType.file.name()));
		assertFalse(queryValuesSet.contains(EntityType.link.name()));
		
		List<EntityHeader> filteredHeaders = filter.filterForBrowsing(headers);
		assertTrue(filteredHeaders.contains(projectHeader));
		assertTrue(filteredHeaders.contains(folderHeader));
		assertTrue(filteredHeaders.contains(fileHeader));
		assertFalse(filteredHeaders.contains(linkHeader));
	}
}
