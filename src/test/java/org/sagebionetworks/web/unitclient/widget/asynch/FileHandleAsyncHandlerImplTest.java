package org.sagebionetworks.web.unitclient.widget.asynch;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.file.BatchFileRequest;
import org.sagebionetworks.repo.model.file.BatchFileResult;
import org.sagebionetworks.repo.model.file.FileHandleAssociation;
import org.sagebionetworks.repo.model.file.FileResult;
import org.sagebionetworks.repo.model.file.FileResultFailureCode;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.cache.ClientCache;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.asynch.FileHandleAsyncHandlerImpl;
import org.sagebionetworks.web.shared.exceptions.ForbiddenException;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class FileHandleAsyncHandlerImplTest {
	FileHandleAsyncHandlerImpl fileHandleAsyncHandler;
	@Mock
	SynapseJavascriptClient mockSynapseJavascriptClient;
	@Mock
	GWTWrapper mockGwt;
	@Mock
	ClientCache mockClientCache;
	@Mock
	AdapterFactory mockAdapterFactory;
	@Mock
	JSONObjectAdapter mockJSONObjectAdapter;
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
	@Captor
	ArgumentCaptor<Throwable> throwableCaptor;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		fileHandleAsyncHandler = new FileHandleAsyncHandlerImpl(mockSynapseJavascriptClient, mockGwt, mockClientCache, mockAdapterFactory);
		resultList = new ArrayList<FileResult>();
		AsyncMockStubber.callSuccessWith(mockResult).when(mockSynapseJavascriptClient).getFileHandleAndUrlBatch(any(BatchFileRequest.class), any(AsyncCallback.class));
		when(mockResult.getRequestedFiles()).thenReturn(resultList);
		when(mockFileResult.getFileHandleId()).thenReturn(fileHandleId);
		when(mockFileAssociation.getFileHandleId()).thenReturn(fileHandleId);
		when(mockAdapterFactory.createNew()).thenReturn(mockJSONObjectAdapter);
	}

	@Test
	public void testConstructor() {
		verify(mockGwt).scheduleFixedDelay(any(Callback.class), anyInt());
	}

	@Test
	public void testSuccess() {
		// verify no rpc if nothing has been requested.
		fileHandleAsyncHandler.executeRequests();
		verifyZeroInteractions(mockSynapseJavascriptClient);

		// simulate single file response for multiple requests for that file
		fileHandleAsyncHandler.getFileResult(mockFileAssociation, mockCallback);
		fileHandleAsyncHandler.getFileResult(mockFileAssociation, mockCallback2);

		resultList.add(mockFileResult);

		fileHandleAsyncHandler.executeRequests();
		verify(mockSynapseJavascriptClient).getFileHandleAndUrlBatch(any(BatchFileRequest.class), any(AsyncCallback.class));
		verify(mockCallback).onSuccess(mockFileResult);
		verify(mockCallback2).onSuccess(mockFileResult);
	}

	@Test
	public void testSuccessFileHandleNotFound() {
		// simulate single file response for multiple requests for that file
		fileHandleAsyncHandler.getFileResult(mockFileAssociation, mockCallback);
		when(mockFileResult.getFailureCode()).thenReturn(FileResultFailureCode.NOT_FOUND);

		resultList.add(mockFileResult);

		fileHandleAsyncHandler.executeRequests();

		verify(mockSynapseJavascriptClient).getFileHandleAndUrlBatch(any(BatchFileRequest.class), any(AsyncCallback.class));
		verify(mockCallback).onFailure(throwableCaptor.capture());
		Throwable th = throwableCaptor.getValue();
		assertTrue(th instanceof NotFoundException);
	}

	@Test
	public void testSuccessFileHandleUnauthorized() {
		// simulate single file response for multiple requests for that file
		fileHandleAsyncHandler.getFileResult(mockFileAssociation, mockCallback);
		when(mockFileResult.getFailureCode()).thenReturn(FileResultFailureCode.UNAUTHORIZED);

		resultList.add(mockFileResult);

		fileHandleAsyncHandler.executeRequests();

		verify(mockSynapseJavascriptClient).getFileHandleAndUrlBatch(any(BatchFileRequest.class), any(AsyncCallback.class));
		verify(mockCallback).onFailure(throwableCaptor.capture());
		Throwable th = throwableCaptor.getValue();
		assertTrue(th instanceof ForbiddenException);
	}

	@Test
	public void testFailure() {
		// simulate exception response
		Exception ex = new Exception("problem loading batch");
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseJavascriptClient).getFileHandleAndUrlBatch(any(BatchFileRequest.class), any(AsyncCallback.class));
		fileHandleAsyncHandler.getFileResult(mockFileAssociation, mockCallback);
		fileHandleAsyncHandler.executeRequests();

		verify(mockCallback).onFailure(ex);
	}

	@Test
	public void testNotFound() {
		when(mockFileResult.getFileHandleId()).thenReturn("another id");
		// add one, simulate different file response
		fileHandleAsyncHandler.getFileResult(mockFileAssociation, mockCallback);
		resultList.add(mockFileResult);

		fileHandleAsyncHandler.executeRequests();
		verify(mockSynapseJavascriptClient).getFileHandleAndUrlBatch(any(BatchFileRequest.class), any(AsyncCallback.class));
		verify(mockCallback).onFailure(any(Throwable.class));
	}

	@Test
	public void testAutoExecute() {
		// verify that if we ask for enough requests, it will automatically execute the rpc.
		for (int i = 0; i < FileHandleAsyncHandlerImpl.LIMIT; i++) {
			FileHandleAssociation mockFha = Mockito.mock(FileHandleAssociation.class);
			when(mockFha.getFileHandleId()).thenReturn("file handle id = " + i);
			fileHandleAsyncHandler.getFileResult(mockFha, mockCallback);
		}
		verifyZeroInteractions(mockSynapseJavascriptClient);

		fileHandleAsyncHandler.getFileResult(mockFileAssociation, mockCallback);
		verify(mockSynapseJavascriptClient).getFileHandleAndUrlBatch(any(BatchFileRequest.class), any(AsyncCallback.class));
	}
}
