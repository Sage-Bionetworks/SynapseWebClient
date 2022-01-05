package org.sagebionetworks.web.unitclient.widget.table.modal.fileview;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.shared.WebConstants.DATASET;
import static org.sagebionetworks.web.shared.WebConstants.FILE;
import static org.sagebionetworks.web.shared.WebConstants.FOLDER;
import static org.sagebionetworks.web.shared.WebConstants.TABLE;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.table.Dataset;
import org.sagebionetworks.repo.model.table.EntityView;
import org.sagebionetworks.repo.model.table.SubmissionView;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.repo.model.table.ViewType;
import org.sagebionetworks.repo.model.table.ViewTypeMask;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.widget.table.modal.fileview.TableType;
import org.sagebionetworks.web.shared.WebConstants;

public class TableTypeTest {

	@Mock
	TableEntity mockTableEntity;
	@Mock
	SubmissionView mockSubmissionView;
	@Mock
	Dataset mockDataset;
	@Mock
	EntityView mockEntityView;

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testIncludeFiles() {
		assertFalse(TableType.table.isIncludeFiles());
		assertFalse(TableType.project_view.isIncludeFiles());
		assertFalse(TableType.submission_view.isIncludeFiles());
		assertTrue(TableType.dataset.isIncludeFiles());
		assertTrue(TableType.file_view.isIncludeFiles());
		assertTrue((new TableType(EntityView.class, FILE)).isIncludeFiles());
	}

	@Test
	public void testIncludeFolders() {
		assertFalse(TableType.table.isIncludeFolders());
		assertFalse(TableType.project_view.isIncludeFolders());
		assertFalse(TableType.submission_view.isIncludeFolders());
		assertFalse(TableType.file_view.isIncludeFolders());
		assertFalse(TableType.dataset.isIncludeFolders());
		assertTrue((new TableType(EntityView.class, WebConstants.FOLDER)).isIncludeFolders());
	}

	@Test
	public void testIncludeTables() {
		assertFalse(TableType.table.isIncludeTables());
		assertFalse(TableType.project_view.isIncludeTables());
		assertFalse(TableType.submission_view.isIncludeTables());
		assertFalse(TableType.file_view.isIncludeTables());
		assertFalse(TableType.dataset.isIncludeTables());
		assertTrue((new TableType(EntityView.class, TABLE)).isIncludeTables());
	}

	@Test
	public void testIncludeDatasets() {
		assertFalse(TableType.table.isIncludeDatasets());
		assertFalse(TableType.project_view.isIncludeDatasets());
		assertFalse(TableType.submission_view.isIncludeDatasets());
		assertFalse(TableType.file_view.isIncludeDatasets());
		assertFalse(TableType.dataset.isIncludeDatasets());
		assertTrue((new TableType(EntityView.class, DATASET)).isIncludeDatasets());
	}

	@Test
	public void testGetTableTypeFromCheckboxes() {
		TableType noneInMask = TableType.getEntityViewTableType(false, false, false, false);
		assertFalse(noneInMask.isIncludeFiles());
		assertFalse(noneInMask.isIncludeFolders());
		assertFalse(noneInMask.isIncludeTables());
		assertFalse(noneInMask.isIncludeDatasets());

		TableType allInMask = TableType.getEntityViewTableType(true, true, true, true);
		assertTrue(allInMask.isIncludeFiles());
		assertTrue(allInMask.isIncludeFolders());
		assertTrue(allInMask.isIncludeTables());
		assertTrue(allInMask.isIncludeDatasets());
	}

	@Test
	public void testGetDisplayName() {
		assertEquals(TableType.table.getDisplayName(), DisplayConstants.TABLE);
		assertEquals(TableType.submission_view.getDisplayName(), DisplayConstants.SUBMISSION_VIEW);
		assertEquals(TableType.dataset.getDisplayName(), DisplayConstants.DATASET);
		assertEquals(TableType.file_view.getDisplayName(), DisplayConstants.FILE_VIEW);
		assertEquals(TableType.project_view.getDisplayName(), DisplayConstants.PROJECT_VIEW);
		assertEquals(new TableType(EntityView.class, FILE | FOLDER | TABLE).getDisplayName(), DisplayConstants.VIEW);
	}


	@Test
	public void testGetTableTypeFromEntity() {
		assertEquals(TableType.table, TableType.getTableType(mockTableEntity));

		assertEquals(TableType.submission_view, TableType.getTableType(mockSubmissionView));

		assertEquals(TableType.dataset, TableType.getTableType(mockDataset));

		// using old type
		when(mockEntityView.getViewTypeMask()).thenReturn(null);
		when(mockEntityView.getType()).thenReturn(ViewType.file);
		assertEquals(TableType.file_view, TableType.getTableType(mockEntityView));
		when(mockEntityView.getType()).thenReturn(ViewType.file_and_table);
		assertEquals(new TableType(EntityView.class, FILE | TABLE), TableType.getTableType(mockEntityView));
		when(mockEntityView.getType()).thenReturn(ViewType.project);
		assertEquals(TableType.project_view, TableType.getTableType(mockEntityView));

		// using new mask
		when(mockEntityView.getType()).thenReturn(null);
		when(mockEntityView.getViewTypeMask()).thenReturn(ViewTypeMask.File.getMask());
		assertEquals(TableType.file_view, TableType.getTableType(mockEntityView));
		when(mockEntityView.getViewTypeMask()).thenReturn(ViewTypeMask.Table.getMask());
		assertEquals(new TableType(EntityView.class, TABLE), TableType.getTableType(mockEntityView));
		when(mockEntityView.getViewTypeMask()).thenReturn(ViewTypeMask.File.getMask() | ViewTypeMask.Table.getMask());
		assertEquals(new TableType(EntityView.class, FILE | TABLE), TableType.getTableType(mockEntityView));
	}
}
