package org.sagebionetworks.web.unitclient.widget.table.modal.fileview;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.table.EntityView;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.repo.model.table.ViewType;
import org.sagebionetworks.repo.model.table.ViewTypeMask;
import org.sagebionetworks.web.client.widget.table.modal.fileview.TableType;

public class TableTypeTest {

	@Mock
	TableEntity mockTableEntity;
	@Mock
	EntityView mockEntityView;

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testIncludeFiles() {
		assertFalse(TableType.table.isIncludeFiles());
		assertFalse(TableType.projects.isIncludeFiles());
		assertFalse(TableType.tables.isIncludeFiles());
		assertFalse(TableType.folders.isIncludeFiles());
		assertFalse(TableType.folders_tables.isIncludeFiles());
		assertTrue(TableType.files.isIncludeFiles());
		assertTrue(TableType.files_tables.isIncludeFiles());
		assertTrue(TableType.files_folders.isIncludeFiles());
		assertTrue(TableType.files_folders_tables.isIncludeFiles());
	}

	@Test
	public void testIncludeFolders() {
		assertFalse(TableType.table.isIncludeFolders());
		assertFalse(TableType.projects.isIncludeFolders());
		assertFalse(TableType.tables.isIncludeFolders());
		assertTrue(TableType.folders.isIncludeFolders());
		assertTrue(TableType.folders_tables.isIncludeFolders());
		assertFalse(TableType.files.isIncludeFolders());
		assertFalse(TableType.files_tables.isIncludeFolders());
		assertTrue(TableType.files_folders.isIncludeFolders());
		assertTrue(TableType.files_folders_tables.isIncludeFolders());
	}

	@Test
	public void testIncludeTables() {
		assertFalse(TableType.table.isIncludeTables());
		assertFalse(TableType.projects.isIncludeTables());
		assertTrue(TableType.tables.isIncludeTables());
		assertFalse(TableType.folders.isIncludeTables());
		assertTrue(TableType.folders_tables.isIncludeTables());
		assertFalse(TableType.files.isIncludeTables());
		assertTrue(TableType.files_tables.isIncludeTables());
		assertFalse(TableType.files_folders.isIncludeTables());
		assertTrue(TableType.files_folders_tables.isIncludeTables());
	}

	@Test
	public void testGetTableTypeFromCheckboxes() {
		assertEquals(TableType.tables, TableType.getTableType(false, false, true));
		assertEquals(TableType.folders, TableType.getTableType(false, true, false));
		assertEquals(TableType.folders_tables, TableType.getTableType(false, true, true));
		assertEquals(TableType.files, TableType.getTableType(true, false, false));
		assertEquals(TableType.files_tables, TableType.getTableType(true, false, true));
		assertEquals(TableType.files_folders, TableType.getTableType(true, true, false));
		assertEquals(TableType.files_folders_tables, TableType.getTableType(true, true, true));
		assertEquals(TableType.table, TableType.getTableType((Long) null));
	}

	@Test
	public void testGetTableTypeFromEntity() {
		assertEquals(TableType.table, TableType.getTableType(mockTableEntity));

		// using old type
		when(mockEntityView.getViewTypeMask()).thenReturn(null);
		when(mockEntityView.getType()).thenReturn(ViewType.file);
		assertEquals(TableType.files, TableType.getTableType(mockEntityView));
		when(mockEntityView.getType()).thenReturn(ViewType.file_and_table);
		assertEquals(TableType.files_tables, TableType.getTableType(mockEntityView));
		when(mockEntityView.getType()).thenReturn(ViewType.project);
		assertEquals(TableType.projects, TableType.getTableType(mockEntityView));

		// using new mask
		when(mockEntityView.getType()).thenReturn(null);
		when(mockEntityView.getViewTypeMask()).thenReturn(ViewTypeMask.File.getMask());
		assertEquals(TableType.files, TableType.getTableType(mockEntityView));
		when(mockEntityView.getViewTypeMask()).thenReturn(ViewTypeMask.Table.getMask());
		assertEquals(TableType.tables, TableType.getTableType(mockEntityView));
		when(mockEntityView.getViewTypeMask()).thenReturn(ViewTypeMask.File.getMask() | ViewTypeMask.Table.getMask());
		assertEquals(TableType.files_tables, TableType.getTableType(mockEntityView));
	}
}
