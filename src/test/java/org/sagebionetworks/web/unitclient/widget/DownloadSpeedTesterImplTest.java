package org.sagebionetworks.web.unitclient.widget;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.file.FileHandleAssociation;
import org.sagebionetworks.repo.model.file.FileResult;
import org.sagebionetworks.repo.model.file.FileResultFailureCode;
import org.sagebionetworks.repo.model.file.S3FileHandle;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.RequestBuilderWrapper;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.cache.ClientCache;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.DownloadSpeedTesterImpl;
import org.sagebionetworks.web.client.widget.FileHandleWidget;
import org.sagebionetworks.web.client.widget.FileHandleWidgetView;
import org.sagebionetworks.web.client.widget.asynch.FileHandleAsyncHandler;
import org.sagebionetworks.web.client.widget.asynch.PresignedAndFileHandleURLAsyncHandler;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.FileCellRendererImpl;
import org.sagebionetworks.web.shared.exceptions.UnauthorizedException;
import org.sagebionetworks.web.shared.table.CellAddress;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import org.sagebionetworks.web.test.helper.RequestBuilderMockStubber;

import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class DownloadSpeedTesterImplTest {

	DownloadSpeedTesterImpl downloadSpeedTester;

	@Mock
	AuthenticationController mockAuthController;
	@Mock
	PresignedAndFileHandleURLAsyncHandler mockFileHandleAsyncHandler;
	@Mock
	RequestBuilderWrapper mockRequestBuilder;
	@Mock
	SynapseJavascriptClient mockJsClient;
	
	@Mock
	FileResult mockFileResult;
	@Mock
	S3FileHandle mockFileHandle;
	@Mock
	FileEntity mockFileEntity;
	@Mock
	Response mockResponse;
	@Mock
	AsyncCallback<Double> mockCallback;
	@Captor
	ArgumentCaptor<Throwable> throwableCaptor;
	@Mock
	ClientCache mockClientCache;
	public static final String FILE_HANDLE_ID = "876567";
	public static final Long FILE_CONTENT_SIZE = 1L;
	public static final String PRESIGNED_URL = "https://s3.presigned.url/test.zip";
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		when(mockAuthController.isLoggedIn()).thenReturn(true);
		
		when(mockResponse.getStatusCode()).thenReturn(Response.SC_OK);
		when(mockFileEntity.getDataFileHandleId()).thenReturn(FILE_HANDLE_ID);
		when(mockFileResult.getFileHandle()).thenReturn(mockFileHandle);
		when(mockFileResult.getPreSignedURL()).thenReturn(PRESIGNED_URL);
		when(mockFileHandle.getContentSize()).thenReturn(FILE_CONTENT_SIZE);
		
		AsyncMockStubber.callSuccessWith(mockFileEntity).when(mockJsClient).getEntity(anyString(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(mockFileResult).when(mockFileHandleAsyncHandler).getFileResult(any(FileHandleAssociation.class), any(AsyncCallback.class));
		RequestBuilderMockStubber.callOnResponseReceived(null, mockResponse).when(mockRequestBuilder).sendRequest(anyString(), any(RequestCallback.class));
		
		downloadSpeedTester = new DownloadSpeedTesterImpl(mockAuthController, mockFileHandleAsyncHandler, mockRequestBuilder, mockJsClient, mockClientCache);
		when(mockClientCache.get(DownloadSpeedTesterImpl.ESTIMATED_DOWNLOAD_SPEED_CACHE_KEY)).thenReturn(null);
		when(mockClientCache.contains(DownloadSpeedTesterImpl.ESTIMATED_DOWNLOAD_SPEED_CACHE_KEY)).thenReturn(false);
	}

	@Test
	public void testHappyCase() {
		downloadSpeedTester.testDownloadSpeed(mockCallback);
		
		verify(mockCallback).onSuccess(anyDouble());
	}
	
	@Test
	public void testNotLoggedIn() {
		when(mockAuthController.isLoggedIn()).thenReturn(false);
		
		downloadSpeedTester.testDownloadSpeed(mockCallback);
		
		verify(mockCallback).onFailure(throwableCaptor.capture());
		assertTrue(throwableCaptor.getValue() instanceof UnauthorizedException);
	}
	
	@Test
	public void testFailToGetEntity() {
		Exception error = new Exception("didn't work");
		AsyncMockStubber.callFailureWith(error).when(mockJsClient).getEntity(anyString(), any(AsyncCallback.class));

		downloadSpeedTester.testDownloadSpeed(mockCallback);
		
		verify(mockCallback).onFailure(error);
	}
	
	@Test
	public void testFailToGetFileResult() {
		when(mockFileResult.getPreSignedURL()).thenReturn(null);
		when(mockFileResult.getFileHandle()).thenReturn(null);
		when(mockFileResult.getFailureCode()).thenReturn(FileResultFailureCode.UNAUTHORIZED);
		
		downloadSpeedTester.testDownloadSpeed(mockCallback);
		
		verify(mockCallback).onFailure(throwableCaptor.capture());
		assertTrue(throwableCaptor.getValue().getMessage().contains(FileResultFailureCode.UNAUTHORIZED.toString()));
	}
	
	@Test
	public void testFailDownloadFile() {
		String statusText = "gateway timed out";
		when(mockResponse.getStatusCode()).thenReturn(Response.SC_GATEWAY_TIMEOUT);
		when(mockResponse.getStatusText()).thenReturn(statusText);
		
		downloadSpeedTester.testDownloadSpeed(mockCallback);
		
		verify(mockCallback).onFailure(throwableCaptor.capture());
		assertTrue(throwableCaptor.getValue().getMessage().contains(statusText));
	}
	
	@Test
	public void testCachedDownloadSpeed() {
		String cachedDownloadSpeedString = "188267.37037";
		when(mockClientCache.contains(DownloadSpeedTesterImpl.ESTIMATED_DOWNLOAD_SPEED_CACHE_KEY)).thenReturn(true);
		when(mockClientCache.get(DownloadSpeedTesterImpl.ESTIMATED_DOWNLOAD_SPEED_CACHE_KEY)).thenReturn(cachedDownloadSpeedString);
		
		downloadSpeedTester.testDownloadSpeed(mockCallback);
		
		verifyZeroInteractions(mockRequestBuilder, mockJsClient);
		verify(mockCallback).onSuccess(Double.valueOf(cachedDownloadSpeedString));
	}
	
	@Test
	public void testCachedInvalidValueDownloadSpeed() throws RequestException {
		String cachedDownloadSpeedString = Double.toString(Double.POSITIVE_INFINITY);
		when(mockClientCache.contains(DownloadSpeedTesterImpl.ESTIMATED_DOWNLOAD_SPEED_CACHE_KEY)).thenReturn(true);
		when(mockClientCache.get(DownloadSpeedTesterImpl.ESTIMATED_DOWNLOAD_SPEED_CACHE_KEY)).thenReturn(cachedDownloadSpeedString);
		
		downloadSpeedTester.testDownloadSpeed(mockCallback);
		
		//verify it runs the test
		verify(mockJsClient).getEntity(anyString(), any(AsyncCallback.class));
		verify(mockRequestBuilder).sendRequest(anyString(), any(RequestCallback.class));
		verify(mockCallback).onSuccess(anyDouble());
	}

}
