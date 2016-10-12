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
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.Stubber;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.file.FileHandleAssociation;
import org.sagebionetworks.repo.model.file.FileResult;
import org.sagebionetworks.repo.model.file.S3FileHandle;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.asynch.FileHandleAsyncHandler;
import org.sagebionetworks.web.client.widget.asynch.TableFileHandleRequest;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.FileCellRendererImpl;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.FileCellRendererView;
import org.sagebionetworks.web.shared.table.CellAddress;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class FileCellRendererImplTest {

	FileCellRendererView mockView;
	@Mock
	FileHandleAsyncHandler mockFileHandleAsyncHandler;
	FileCellRendererImpl renderer;
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
	@Mock
	AuthenticationController mockAuthController;
	String xsrfToken = "98208";
	boolean isView;
	@Mock
	FileResult mockFileResult;
	@Before
	public void before(){
		MockitoAnnotations.initMocks(this);
		mockView = Mockito.mock(FileCellRendererView.class);
		renderer = new FileCellRendererImpl(mockView, mockAuthController, mockFileHandleAsyncHandler);
		tableId = "syn123";
		column = new ColumnModel();
		column.setId("456");
		rowId = 15L;
		rowVersion = 2L;
		isView = false;
		address = new CellAddress(tableId, column, rowId, rowVersion, isView);
		renderer.setCellAddresss(address);
		fileHandleId = "999";
		fileHandle = new S3FileHandle();
		fileHandle.setId(fileHandleId);
		fileHandle.setFileName("filename.jpg");
		
		when(mockFileResult.getFileHandle()).thenReturn(fileHandle);
		AsyncMockStubber.callSuccessWith(mockFileResult).when(mockFileHandleAsyncHandler).getFileHandle(any(FileHandleAssociation.class), any(AsyncCallback.class));
		when(mockAuthController.getCurrentXsrfToken()).thenReturn(xsrfToken);
	}
	
	@Test
	public void testCreateAnchorHref(){
		renderer.setValue(fileHandleId);
		String expectedHref = "/Portal/filehandleassociation?associatedObjectId=syn123&associatedObjectType=TableEntity&fileHandleId=999&xsrfToken=98208";
		assertEquals(expectedHref, renderer.createAnchorHref());
	}
	
	@Test
	public void testCreateAnchorHrefView(){
		isView = true;
		address = new CellAddress(tableId, column, rowId, rowVersion, isView);
		renderer.setCellAddresss(address);
		renderer.setValue(fileHandleId);
		String objectId = "syn" + rowId;
		String expectedHref = "/Portal/filehandleassociation?associatedObjectId="+objectId+"&associatedObjectType=FileEntity&fileHandleId=999&xsrfToken=98208";
		assertEquals(expectedHref, renderer.createAnchorHref());
	}
	
	@Test
	public void testSetValueSuccessAttached(){
		when(mockView.isAttached()).thenReturn(true);
		renderer.setValue(fileHandleId);
		// loading shown first
		verify(mockView).setLoadingVisible(true);
		// hide loading
		verify(mockView).setLoadingVisible(false);
		verify(mockView).setAnchor(fileHandle.getFileName(), renderer.createAnchorHref());
	}

	@Test
	public void testSetValueSuccessNotAttached(){
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
	public void testSetValueFailureAttached(){
		// setup an error.
		String errorMessage = "an error";
		final Throwable error = new Throwable(errorMessage);
		
		AsyncMockStubber.callFailureWith(error).when(mockFileHandleAsyncHandler).getFileHandle(any(FileHandleAssociation.class), any(AsyncCallback.class));
		//attached.
		when(mockView.isAttached()).thenReturn(true);
		renderer.setValue(fileHandleId);
		// loading shown first
		verify(mockView).setLoadingVisible(true);
		// hide loading
		verify(mockView).setLoadingVisible(false);
		verify(mockView).setErrorText(FileCellRendererImpl.UNABLE_TO_LOAD_FILE_DATA + ": " + errorMessage);
	}
	
	@Test
	public void testSetValueFailureNotAttached(){
		// setup an error.
		final Throwable error = new Throwable("an error");
		AsyncMockStubber.callFailureWith(error).when(mockFileHandleAsyncHandler).getFileHandle(any(FileHandleAssociation.class), any(AsyncCallback.class));
		//not attached.
		when(mockView.isAttached()).thenReturn(false);
		renderer.setValue(fileHandleId);
		// loading shown first
		verify(mockView).setLoadingVisible(true);
		// hide loading
		verify(mockView, never()).setLoadingVisible(false);
		verify(mockView, never()).setErrorText(anyString());
	}
}
