package org.sagebionetworks.web.unitclient.widget.table.v2.results.cell;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.Stubber;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.file.S3FileHandle;
import org.sagebionetworks.web.client.widget.asynch.AsynchTableFileHandleProvider;
import org.sagebionetworks.web.client.widget.asynch.TableFileHandleRequest;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.FileCellRendererImpl;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.FileCellRendererView;
import org.sagebionetworks.web.shared.table.CellAddress;

public class FileCellRendererImplTest {

	FileCellRendererView mockView;
	AsynchTableFileHandleProvider mockFileHandleProvider;
	FileCellRendererImpl renderer;
	String tableId;
	String columnId;
	Long rowId; 
	Long rowVersion;
	CellAddress address;
	String fileHandleId;
	FileHandle fileHandle;
	Throwable error;
	Stubber succesStubber;
	Stubber failureStubber;
	
	@Before
	public void before(){
		mockView = Mockito.mock(FileCellRendererView.class);
		mockFileHandleProvider = Mockito.mock(AsynchTableFileHandleProvider.class);
		renderer = new FileCellRendererImpl(mockView, mockFileHandleProvider);
		tableId = "syn123";
		columnId = "456";
		rowId = 15L;
		rowVersion = 2L;
		address = new CellAddress(tableId, columnId, rowId, rowVersion);
		renderer.setCellAddresss(address);
		fileHandleId = "999";
		fileHandle = new S3FileHandle();
		fileHandle.setId(fileHandleId);
		fileHandle.setFileName("filename.jpg");
		
		Mockito.doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation)
					throws Throwable {
				TableFileHandleRequest request =  (TableFileHandleRequest) invocation.getArguments()[0];
				request.getCallback().onSuccess(fileHandle);
				return null;
			}
		}).when(mockFileHandleProvider).requestFileHandle(any(TableFileHandleRequest.class));
	}
	
	@Test
	public void testCreateAnchorHref(){
		String expectedHref = "/Portal/filehandle?entityId=syn123&columnId=456&rowId=15&rowVersionNumber=2";
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
		final Throwable error = new Throwable("an error");
		Mockito.doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation)
					throws Throwable {
				TableFileHandleRequest request =  (TableFileHandleRequest) invocation.getArguments()[0];
				request.getCallback().onFailure(error);
				return null;
			}
		}).when(mockFileHandleProvider).requestFileHandle(any(TableFileHandleRequest.class));
		//attached.
		when(mockView.isAttached()).thenReturn(true);
		renderer.setValue(fileHandleId);
		// loading shown first
		verify(mockView).setLoadingVisible(true);
		// hide loading
		verify(mockView).setLoadingVisible(false);
		verify(mockView).setErrorText(FileCellRendererImpl.UNABLE_TO_LOAD_FILE_DATA);
	}
	
	@Test
	public void testSetValueFailureNotAttached(){
		// setup an error.
		final Throwable error = new Throwable("an error");
		Mockito.doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation)
					throws Throwable {
				TableFileHandleRequest request =  (TableFileHandleRequest) invocation.getArguments()[0];
				request.getCallback().onFailure(error);
				return null;
			}
		}).when(mockFileHandleProvider).requestFileHandle(any(TableFileHandleRequest.class));
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
