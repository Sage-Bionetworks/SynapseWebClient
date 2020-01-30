package org.sagebionetworks.web.unitclient.widget;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.file.FileHandleAssociation;
import org.sagebionetworks.repo.model.file.FileResult;
import org.sagebionetworks.repo.model.file.FileResultFailureCode;
import org.sagebionetworks.repo.model.file.S3FileHandle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.FileHandleWidget;
import org.sagebionetworks.web.client.widget.FileHandleWidgetView;
import org.sagebionetworks.web.client.widget.asynch.FileHandleAsyncHandler;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.FileCellRenderer;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class FileHandleWidgetTest {

	@Mock
	FileHandleAsyncHandler mockFileHandleAsyncHandler;
	@Mock
	FileResult mockFileResult;
	@Mock
	AuthenticationController mockAuthController;
	@Mock
	S3FileHandle mockFileHandle;
	@Mock
	FileHandleWidgetView mockView;
	@Mock
	SynapseJSNIUtils mockJsniUtils;
	@Mock
	FileHandleAssociation mockFHA;
	FileHandleWidget widget;
	public static final String FILE_HANDLE_ID = "876567";
	public static final String FILE_NAME = "test.png";

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		when(mockFileResult.getFileHandle()).thenReturn(mockFileHandle);
		AsyncMockStubber.callSuccessWith(mockFileResult).when(mockFileHandleAsyncHandler).getFileResult(any(FileHandleAssociation.class), any(AsyncCallback.class));
		widget = new FileHandleWidget(mockView, mockAuthController, mockFileHandleAsyncHandler, mockJsniUtils);
		when(mockFHA.getFileHandleId()).thenReturn(FILE_HANDLE_ID);
		when(mockFileHandle.getFileName()).thenReturn(FILE_NAME);
	}

	@Test
	public void testSetValueAttached() {
		when(mockView.isAttached()).thenReturn(true);
		widget.configure(mockFHA);
		assertEquals(FILE_HANDLE_ID, widget.getFileHandleId());
		InOrder order = Mockito.inOrder(mockView);
		// loading shown first
		order.verify(mockView).setLoadingVisible(true);
		order.verify(mockView).setLoadingVisible(false);
		verify(mockView).setAnchor(eq(FILE_NAME), anyString());
	}

	@Test
	public void testSetValueSuccessUnauthorized() {
		when(mockView.isAttached()).thenReturn(true);
		when(mockFileResult.getFileHandle()).thenReturn(null);
		when(mockFileResult.getFailureCode()).thenReturn(FileResultFailureCode.UNAUTHORIZED);
		widget.configure(mockFHA);
		InOrder order = Mockito.inOrder(mockView);
		order.verify(mockView).setLoadingVisible(true);
		order.verify(mockView).setLoadingVisible(false);
		verify(mockView).setErrorText(FileCellRenderer.UNABLE_TO_LOAD_FILE_DATA + ": " + FileResultFailureCode.UNAUTHORIZED.toString());
	}

	@Test
	public void testSetValueSuccessNotAttached() {
		// If the widget is not attached then do not set the values
		when(mockView.isAttached()).thenReturn(false);
		widget.configure(mockFHA);
		verify(mockView).setLoadingVisible(true);
		verify(mockView, never()).setLoadingVisible(false);
		verify(mockView, never()).setAnchor(anyString(), anyString());
	}

	@Test
	public void testSetValueFailureAttached() {
		// setup an error.
		String errorMessage = "an error";
		final Throwable error = new Throwable(errorMessage);
		AsyncMockStubber.callFailureWith(error).when(mockFileHandleAsyncHandler).getFileResult(any(FileHandleAssociation.class), any(AsyncCallback.class));
		when(mockView.isAttached()).thenReturn(true);
		widget.configure(mockFHA);
		InOrder order = Mockito.inOrder(mockView);
		order.verify(mockView).setLoadingVisible(true);
		order.verify(mockView).setLoadingVisible(false);
		verify(mockView).setErrorText(FileCellRenderer.UNABLE_TO_LOAD_FILE_DATA + ": " + errorMessage);
	}

	@Test
	public void testSetRawFileHandleId() {
		String fileName = "different name";
		String rawFileHandleId = "876";
		widget.configure(fileName, rawFileHandleId);
		verify(mockView).setLoadingVisible(false);
		assertEquals(rawFileHandleId, widget.getFileHandleId());
		verify(mockView).setAnchor(eq(fileName), anyString());
	}

}
