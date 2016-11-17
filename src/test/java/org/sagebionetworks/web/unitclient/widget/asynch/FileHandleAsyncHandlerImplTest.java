package org.sagebionetworks.web.unitclient.widget.asynch;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.file.BatchFileRequest;
import org.sagebionetworks.repo.model.file.BatchFileResult;
import org.sagebionetworks.repo.model.file.FileHandleAssociation;
import org.sagebionetworks.repo.model.file.FileResult;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.asynch.FileHandleAsyncHandlerImpl;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class FileHandleAsyncHandlerImplTest {
	FileHandleAsyncHandlerImpl fileHandleAsyncHandler;
	@Mock
	SynapseClientAsync mockSynapseClient;
	@Mock
	GWTWrapper mockGwt;
	String fileHandleId = "123";
	@Mock
	AsyncCallback mockCallback;
	@Mock
	AsyncCallback mockCallback2;
	@Mock
	BatchFileResult mockResult;
	List<FileResult> resultList;
	@Mock
	FileResult mockFileResult;
	@Mock
	FileHandleAssociation mockFileAssociation;
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		fileHandleAsyncHandler = new FileHandleAsyncHandlerImpl(mockSynapseClient, mockGwt);
		resultList = new ArrayList<FileResult>();
		AsyncMockStubber.callSuccessWith(mockResult).when(mockSynapseClient).getFileHandleAndUrlBatch(any(BatchFileRequest.class), any(AsyncCallback.class));
		when(mockResult.getRequestedFiles()).thenReturn(resultList);
		when(mockFileResult.getFileHandleId()).thenReturn(fileHandleId);
		when(mockFileAssociation.getFileHandleId()).thenReturn(fileHandleId);
	}
	
	@Test
	public void testConstructor() {
		verify(mockGwt).scheduleFixedDelay(any(Callback.class), eq(FileHandleAsyncHandlerImpl.DELAY));
	}

	@Test
	public void testSuccess() {
		//verify no rpc if nothing has been requested.
		fileHandleAsyncHandler.executeRequests();
		verifyZeroInteractions(mockSynapseClient);
		
		//simulate single file response for multiple requests for that file
		fileHandleAsyncHandler.getFileHandle(mockFileAssociation, mockCallback);
		fileHandleAsyncHandler.getFileHandle(mockFileAssociation, mockCallback2);
		
		resultList.add(mockFileResult);
		
		fileHandleAsyncHandler.executeRequests();
		verify(mockSynapseClient).getFileHandleAndUrlBatch(any(BatchFileRequest.class), any(AsyncCallback.class));
		verify(mockCallback).onSuccess(mockFileResult);
		verify(mockCallback2).onSuccess(mockFileResult);
	}
	
	@Test
	public void testFailure() {
		//simulate exception response
		Exception ex = new Exception("problem loading batch");
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseClient).getFileHandleAndUrlBatch(any(BatchFileRequest.class), any(AsyncCallback.class));
		fileHandleAsyncHandler.getFileHandle(mockFileAssociation, mockCallback);
		fileHandleAsyncHandler.executeRequests();
		
		verify(mockCallback).onFailure(ex);
	}
	
	@Test
	public void testNotFound() {
		when(mockFileResult.getFileHandleId()).thenReturn("another id");
		//add one, simulate different file response
		fileHandleAsyncHandler.getFileHandle(mockFileAssociation, mockCallback);
		resultList.add(mockFileResult);
		
		fileHandleAsyncHandler.executeRequests();
		verify(mockSynapseClient).getFileHandleAndUrlBatch(any(BatchFileRequest.class), any(AsyncCallback.class));
		verify(mockCallback).onFailure(any(Throwable.class));
	}
	
	@Test
	public void testAutoExecute() {
		// verify that if we ask for enough requests, it will automatically execute the rpc.
		for (int i = 0; i < FileHandleAsyncHandlerImpl.LIMIT; i++) {
			FileHandleAssociation mockFha = Mockito.mock(FileHandleAssociation.class);
			when(mockFha.getFileHandleId()).thenReturn("file handle id = " + i);		
			fileHandleAsyncHandler.getFileHandle(mockFha, mockCallback);	
		}
		verifyZeroInteractions(mockSynapseClient);

		fileHandleAsyncHandler.getFileHandle(mockFileAssociation, mockCallback);
		verify(mockSynapseClient).getFileHandleAndUrlBatch(any(BatchFileRequest.class), any(AsyncCallback.class));
	}
}
