package org.sagebionetworks.web.unitclient.widget.table.modal.upload;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.ColumnType;
import org.sagebionetworks.repo.model.table.Row;
import org.sagebionetworks.repo.model.table.UploadToTablePreviewResult;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadPreviewView;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadPreviewWidgetImpl;


public class UploadPreviewWidgetImplTest {

	UploadPreviewView mockView;
	UploadPreviewWidgetImpl widget;

	@Before
	public void before() {
		mockView = Mockito.mock(UploadPreviewView.class);
		widget = new UploadPreviewWidgetImpl(mockView);
	}

	@Test
	public void testTruncateString() {
		assertEquals(null, UploadPreviewWidgetImpl.truncateValues(null));
		assertEquals("small", UploadPreviewWidgetImpl.truncateValues("small"));
		assertEquals("not so sm...", UploadPreviewWidgetImpl.truncateValues("not so small"));
	}

	@Test
	public void testConfigureNullPreivew() {
		UploadToTablePreviewResult preview = null;
		widget.configure(preview);
		verify(mockView).setPreviewMessage("");
		verify(mockView).setEmptyMessageVisible(true);
		verify(mockView).showEmptyPreviewMessage(UploadPreviewWidgetImpl.NO_DATA_WAS_FOUND_IN_THE_FILE);
		verify(mockView, never()).setHeaders(any(List.class));
		verify(mockView, never()).addRow(any(List.class));
	}

	@Test
	public void testConfigureNullColumnName() {
		ColumnModel cm = new ColumnModel();
		cm.setName(null);
		cm.setColumnType(ColumnType.STRING);
		UploadToTablePreviewResult preview = new UploadToTablePreviewResult();
		preview.setRowsScanned(3L);
		preview.setSuggestedColumns(Arrays.asList(cm));
		preview.setSampleRows(null);
		widget.configure(preview);
		verify(mockView).setPreviewMessage(UploadPreviewWidgetImpl.PREVIEW_MESSAGE_PREFIX + preview.getRowsScanned() + UploadPreviewWidgetImpl.PREVIEW_MESSAGE_SUFFIX);
		verify(mockView).setHeaders(Arrays.asList("BLANK (STRING)"));
		verify(mockView, never()).addRow(any(List.class));
	}

	@Test
	public void testConfigureWithColumnName() {
		ColumnModel cm = new ColumnModel();
		cm.setName("Some Column");
		cm.setColumnType(ColumnType.BOOLEAN);
		UploadToTablePreviewResult preview = new UploadToTablePreviewResult();
		preview.setRowsScanned(3L);
		preview.setSuggestedColumns(Arrays.asList(cm));
		preview.setSampleRows(null);
		widget.configure(preview);
		verify(mockView).setPreviewMessage(UploadPreviewWidgetImpl.PREVIEW_MESSAGE_PREFIX + preview.getRowsScanned() + UploadPreviewWidgetImpl.PREVIEW_MESSAGE_SUFFIX);
		verify(mockView).setHeaders(Arrays.asList("Some Column (BOOLEAN)"));
		verify(mockView, never()).addRow(any(List.class));
	}

	@Test
	public void testConfigureNullNoValues() {
		ColumnModel cm = new ColumnModel();
		cm.setName("Some Column");
		cm.setColumnType(ColumnType.BOOLEAN);
		UploadToTablePreviewResult preview = new UploadToTablePreviewResult();
		preview.setRowsScanned(3L);
		preview.setSuggestedColumns(Arrays.asList(cm));
		preview.setSampleRows(null);
		widget.configure(preview);
		verify(mockView).showEmptyPreviewMessage(UploadPreviewWidgetImpl.NO_ROWS_WERE_FOUND_IN_THE_FILE);
		verify(mockView).setEmptyMessageVisible(true);
		verify(mockView, never()).addRow(any(List.class));
	}

	@Test
	public void testConfigureNullEmptyRow() {
		ColumnModel cm = new ColumnModel();
		cm.setName("Some Column");
		cm.setColumnType(ColumnType.BOOLEAN);
		UploadToTablePreviewResult preview = new UploadToTablePreviewResult();
		preview.setRowsScanned(3L);
		preview.setSuggestedColumns(Arrays.asList(cm));
		Row row = new Row();
		row.setValues(null);
		preview.setSampleRows(Arrays.asList(row));
		widget.configure(preview);
		verify(mockView).showEmptyPreviewMessage(UploadPreviewWidgetImpl.NO_ROWS_WERE_FOUND_IN_THE_FILE);
		verify(mockView).setEmptyMessageVisible(true);
		verify(mockView, never()).addRow(any(List.class));
	}

	@Test
	public void testConfigureWithSchemaAndRow() {
		ColumnModel cm = new ColumnModel();
		cm.setName("Some Column");
		cm.setColumnType(ColumnType.BOOLEAN);
		UploadToTablePreviewResult preview = new UploadToTablePreviewResult();
		preview.setRowsScanned(3L);
		preview.setSuggestedColumns(Arrays.asList(cm));
		Row row = new Row();
		row.setValues(Arrays.asList("true"));
		preview.setSampleRows(Arrays.asList(row));
		widget.configure(preview);
		verify(mockView, never()).showEmptyPreviewMessage(anyString());
		verify(mockView).setEmptyMessageVisible(false);
		verify(mockView).addRow(Arrays.asList("true"));
	}

}
