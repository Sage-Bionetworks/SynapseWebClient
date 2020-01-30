package org.sagebionetworks.web.unitclient.widget.table.v2.results.cell;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Stubber;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.file.FileHandleAssociation;
import org.sagebionetworks.repo.model.file.FileResult;
import org.sagebionetworks.repo.model.file.FileResultFailureCode;
import org.sagebionetworks.repo.model.file.S3FileHandle;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.asynch.FileHandleAsyncHandler;
import org.sagebionetworks.web.client.widget.table.modal.fileview.TableType;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.FileCellRenderer;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.FileCellRendererView;
import org.sagebionetworks.web.shared.table.CellAddress;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class FileCellRendererImplTest {

	FileCellRendererView mockView;
	@Mock
	FileHandleAsyncHandler mockFileHandleAsyncHandler;
	FileCellRenderer renderer;
	String tableId;
	ColumnModel column;
	Long rowId;
	Long rowVersion;
	CellAddress address;
	String fileHandleId;
	FileHandle fileHandle;
	Throwable error;
	Stubber succesStubber;
	Stubber failureStubber;
	TableType tableType;
	@Mock
	AuthenticationController mockAuthController;
	@Mock
	FileResult mockFileResult;

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		mockView = Mockito.mock(FileCellRendererView.class);
		renderer = new FileCellRenderer(mockView, mockAuthController, mockFileHandleAsyncHandler);
		tableId = "syn123";
		column = new ColumnModel();
		column.setId("456");
		rowId = 15L;
		rowVersion = 2L;
		tableType = TableType.table;
		address = new CellAddress(tableId, column, rowId, rowVersion, tableType);
		renderer.setCellAddresss(address);
		fileHandleId = "999";
		fileHandle = new S3FileHandle();
		fileHandle.setId(fileHandleId);
		fileHandle.setFileName("filename.jpg");

		when(mockFileResult.getFileHandle()).thenReturn(fileHandle);
		AsyncMockStubber.callSuccessWith(mockFileResult).when(mockFileHandleAsyncHandler).getFileResult(any(FileHandleAssociation.class), any(AsyncCallback.class));
	}

	@Test
	public void testCreateAnchorHref() {
		renderer.setValue(fileHandleId);
		String expectedHref = "/Portal/filehandleassociation?associatedObjectId=syn123&associatedObjectType=TableEntity&fileHandleId=" + fileHandleId;
		assertEquals(expectedHref, renderer.createAnchorHref());
	}

	@Test
	public void testCreateAnchorHrefView() {
		tableType = TableType.projects;
		address = new CellAddress(tableId, column, rowId, rowVersion, tableType);
		renderer.setCellAddresss(address);
		renderer.setValue(fileHandleId);
		String expectedHref = "/Portal/filehandleassociation?associatedObjectId=" + rowId + "&associatedObjectType=FileEntity&fileHandleId=" + fileHandleId;
		assertEquals(expectedHref, renderer.createAnchorHref());
	}

	@Test
	public void testCreateAnchorHrefFileViewMissingRowId() {
		tableType = TableType.files;
		rowId = null;
		address = new CellAddress(tableId, column, rowId, rowVersion, tableType);
		renderer.setCellAddresss(address);
		renderer.setValue(fileHandleId);
		verify(mockFileHandleAsyncHandler, never()).getFileResult(any(FileHandleAssociation.class), any(AsyncCallback.class));
		verify(mockView).setLoadingVisible(false);
		verify(mockView).setErrorText(FileCellRenderer.FILE_SYNAPSE_ID_UNAVAILABLE);
	}

	@Test
	public void testSetValueSuccessAttached() {
		when(mockView.isAttached()).thenReturn(true);
		renderer.setValue(fileHandleId);
		// loading shown first
		verify(mockView).setLoadingVisible(true);
		// hide loading
		verify(mockView).setLoadingVisible(false);
		verify(mockView).setAnchor(fileHandle.getFileName(), renderer.createAnchorHref());
	}

	@Test
	public void testSetValueSuccessUnauthorized() {
		when(mockView.isAttached()).thenReturn(true);
		when(mockFileResult.getFileHandle()).thenReturn(null);
		when(mockFileResult.getFailureCode()).thenReturn(FileResultFailureCode.UNAUTHORIZED);
		renderer.setValue(fileHandleId);
		// loading shown first
		verify(mockView).setLoadingVisible(true);
		// hide loading
		verify(mockView).setLoadingVisible(false);
		verify(mockView).setErrorText(FileCellRenderer.UNABLE_TO_LOAD_FILE_DATA + ": " + FileResultFailureCode.UNAUTHORIZED.toString());
	}

	@Test
	public void testSetValueSuccessNotAttached() {
		// If the widget is not attached then do not set the values
		when(mockView.isAttached()).thenReturn(false);
		renderer.setValue(fileHandleId);
		// loading shown first
		verify(mockView).setLoadingVisible(true);
		// hide loading
		verify(mockView, never()).setLoadingVisible(false);
		verify(mockView, never()).setAnchor(anyString(), anyString());
	}

	@Test
	public void testSetValueFailureAttached() {
		// setup an error.
		String errorMessage = "an error";
		final Throwable error = new Throwable(errorMessage);

		AsyncMockStubber.callFailureWith(error).when(mockFileHandleAsyncHandler).getFileResult(any(FileHandleAssociation.class), any(AsyncCallback.class));
		// attached.
		when(mockView.isAttached()).thenReturn(true);
		renderer.setValue(fileHandleId);
		// loading shown first
		verify(mockView).setLoadingVisible(true);
		// hide loading
		verify(mockView).setLoadingVisible(false);
		verify(mockView).setErrorText(FileCellRenderer.UNABLE_TO_LOAD_FILE_DATA + ": " + errorMessage);
	}

	@Test
	public void testSetValueFailureNotAttached() {
		// setup an error.
		final Throwable error = new Throwable("an error");
		AsyncMockStubber.callFailureWith(error).when(mockFileHandleAsyncHandler).getFileResult(any(FileHandleAssociation.class), any(AsyncCallback.class));
		// not attached.
		when(mockView.isAttached()).thenReturn(false);
		renderer.setValue(fileHandleId);
		// loading shown first
		verify(mockView).setLoadingVisible(true);
		// hide loading
		verify(mockView, never()).setLoadingVisible(false);
		verify(mockView, never()).setErrorText(anyString());
	}
}
