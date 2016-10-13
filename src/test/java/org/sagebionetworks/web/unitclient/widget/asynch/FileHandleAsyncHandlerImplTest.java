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
		
		//add one, simulate single file response
		fileHandleAsyncHandler.getFileHandle(mockFileAssociation, mockCallback);
		resultList.add(mockFileResult);
		
		fileHandleAsyncHandler.executeRequests();
		verify(mockSynapseClient).getFileHandleAndUrlBatch(any(BatchFileRequest.class), any(AsyncCallback.class));
		verify(mockCallback).onSuccess(mockFileResult);
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

}
