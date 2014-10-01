package org.sagebionetworks.web.unitclient.widget.entity.download;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sagebionetworks.repo.model.file.ChunkRequest;
import org.sagebionetworks.repo.model.file.ChunkedFileToken;
import org.sagebionetworks.repo.model.file.State;
import org.sagebionetworks.repo.model.file.UploadDaemonStatus;
import org.sagebionetworks.repo.model.util.ContentTypeUtils;
import org.sagebionetworks.web.client.ClientLogger;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.ProgressCallback;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.callback.MD5Callback;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.download.FileUploadHandler;
import org.sagebionetworks.web.client.widget.entity.download.MultipartUploaderImpl;
import org.sagebionetworks.web.client.widget.entity.download.Uploader;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.xhr.client.XMLHttpRequest;


public class MultipartUploaderTest {
	
	FileUploadHandler mockHandler;
	SynapseClientAsync synapseClient;
	SynapseJSNIUtils synapseJsniUtils;
	ClientLogger mockLogger;
	GWTWrapper gwt;
	MultipartUploaderImpl uploader;
	String MD5;
	UploadDaemonStatus status;
	String[] fileNames;
	
	@Before
	public void before() throws Exception {
		mockHandler = mock(FileUploadHandler.class);
		synapseClient = mock(SynapseClientAsync.class);
		synapseJsniUtils =mock(SynapseJSNIUtils.class);
		gwt = mock(GWTWrapper.class);
		mockLogger = mock(ClientLogger.class);
		ChunkedFileToken token = new ChunkedFileToken();
		token.setFileName("testFile.txt");
		
		//direct upload
		//by default, do not support direct upload (direct upload tests will turn on)
		when(synapseJsniUtils.getContentType(anyString(), anyInt())).thenReturn("image/png");
		AsyncMockStubber.callSuccessWith(token).when(synapseClient).getChunkedFileToken(anyString(), anyString(), anyString(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith("http://fakepresignedurl.uploader.test").when(synapseClient).getChunkedPresignedUrl(any(ChunkRequest.class), any(AsyncCallback.class));
		status = new UploadDaemonStatus();
		status.setState(State.COMPLETED);
		status.setFileHandleId("fake handle");
		AsyncMockStubber.callSuccessWith(status).when(synapseClient).combineChunkedFileUpload(any(List.class), any(AsyncCallback.class));
		// Stub the generation of a MD5.
		MD5 = "some md5";
		doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
                final Object[] args = invocation.getArguments();
                ((MD5Callback) args[args.length - 1]).setMD5(MD5);
				return null;
			}
		}).when(synapseJsniUtils).getFileMd5(anyString(), anyInt(), any(MD5Callback.class));
		
		when(gwt.createXMLHttpRequest()).thenReturn(null);

		String[] fileNames = {"newFile.txt"};
		when(synapseJsniUtils.getMultipleUploadFileNames(anyString())).thenReturn(fileNames);
		
		String file1 = "file1.txt";
		fileNames = new String[]{file1};
		when(synapseJsniUtils.getMultipleUploadFileNames(anyString())).thenReturn(fileNames);
		
		uploader = new MultipartUploaderImpl(gwt, synapseClient, synapseJsniUtils, mockLogger);
		
	}
	
	@Test
	public void testNothingSelected(){
		when(synapseJsniUtils.getMultipleUploadFileNames(anyString())).thenReturn(null);
		uploader.uploadSelectedFile("123", mockHandler);
		verify(mockHandler).uploadFailed(MultipartUploaderImpl.PLEASE_SELECT_A_FILE);
	}
	
	@Test
	public void testDirectUploadHappyCase() throws Exception {
		uploader.uploadSelectedFile("123", mockHandler);
		// jump to step three
		uploader.directUploadStep4(null, 1);
		verify(synapseClient).getChunkedFileToken(anyString(), anyString(), anyString(), any(AsyncCallback.class));
		verify(synapseClient).getChunkedPresignedUrl(any(ChunkRequest.class), any(AsyncCallback.class));
		verify(synapseJsniUtils).uploadFileChunk(anyString(), anyInt(), anyString(), anyLong(), anyLong(), anyString(), any(XMLHttpRequest.class), any(ProgressCallback.class));
		verify(synapseClient).combineChunkedFileUpload(any(List.class), any(AsyncCallback.class));
		// the handler should get the id.
		verify(mockHandler).setFileHandleId(status.getFileHandleId());
	}

	
	@Test
	public void testDirectUploadStep3Failure() throws Exception {
		String error = "an error";
		AsyncMockStubber.callFailureWith(new IllegalArgumentException(error)).when(synapseClient).getChunkedFileToken(anyString(), anyString(), anyString(), any(AsyncCallback.class));
		uploader.uploadSelectedFile("123", mockHandler);
		verify(mockHandler).uploadFailed(error);
	}

	@Test
	public void testDirectUploadStep4Failure() throws Exception {
		when(synapseJsniUtils.isDirectUploadSupported()).thenReturn(true);
		AsyncMockStubber.callFailureWith(new IllegalArgumentException()).when(synapseClient).getChunkedPresignedUrl(any(ChunkRequest.class), any(AsyncCallback.class));
		uploader.uploadSelectedFile("123", mockHandler);
		uploader.directUploadStep3(0, 0, 1, 12345, new ArrayList<ChunkRequest>());
		executeScheduledCallback();
		//should have called twice
		verify(synapseClient, Mockito.times(2)).getChunkedPresignedUrl(any(ChunkRequest.class), any(AsyncCallback.class));
	}

	/**
	 * Verifies that gwt.scheduleExecution was called, and invokes the callback that it was given
	 */
	private void executeScheduledCallback() {
		ArgumentCaptor<Callback> captor = ArgumentCaptor.forClass(Callback.class);
		verify(gwt).scheduleExecution(captor.capture(), anyInt());
		Callback callback = captor.getValue();
		callback.invoke();
	}
	
//	@Test
//	public void testDirectUploadStep4FailureFinalAttempt() throws Exception {
//		when(synapseJsniUtils.isDirectUploadSupported()).thenReturn(true);
//		AsyncMockStubber.callFailureWith(new IllegalArgumentException()).when(synapseClient).getChunkedPresignedUrl(anyString(), any(AsyncCallback.class));
//		int attempt = Uploader.MAX_RETRY;
//		uploader.directUploadStep4("", 0, attempt, 1, 12345, new ArrayList<String>());
//		verifyUploadError();
//	}
	
//	@Test
//	public void testDirectUploadStep5Failure() throws Exception {
//		when(synapseJsniUtils.isDirectUploadSupported()).thenReturn(true);
//		AsyncMockStubber.callFailureWith(new IllegalArgumentException()).when(synapseClient).combineChunkedFileUpload(any(List.class), any(AsyncCallback.class));
//		uploader.handleUploads();
//		//kick off what would happen after a successful upload
//		uploader.directUploadStep5(null, 1);
//		verifyUploadError();
//	}
//	
//	@Test
//	public void testDirectUploadStep5CompleteUploadFailure() throws Exception {
//		when(synapseJsniUtils.isDirectUploadSupported()).thenReturn(true);
//		AsyncMockStubber.callFailureWith(new IllegalArgumentException()).when(synapseClient).setFileEntityFileHandle(anyString(), anyString(), anyString(), any(AsyncCallback.class));
//		uploader.handleUploads();
//		//kick off what would happen after a successful upload
//		uploader.directUploadStep5(null,1);
//		verifyUploadError();
//	}
//
//	@Test
//	public void testDirectUploadStep5Retry() throws Exception {
//		//returned a failed status every time, and verify that we will eventually see an upload error (once the MAX_RETRY limit has been surpassed)
//		UploadDaemonStatus status = new UploadDaemonStatus();
//		status.setState(State.FAILED);
//		status.setFileHandleId("fake handle");
//		String failedUploadDaemonStatusJson = status.writeToJSONObject(adapterFactory.createNew()).toJSONString();
//		AsyncMockStubber.callSuccessWith(failedUploadDaemonStatusJson).when(synapseClient).combineChunkedFileUpload(any(List.class), any(AsyncCallback.class));
//		
//		when(synapseJsniUtils.isDirectUploadSupported()).thenReturn(true);
//		uploader.handleUploads();
//		uploader.directUploadStep5(null,1);
//		verifyUploadError();
//	}
	
	@Test
	public void testByteRange() {
		//test chunk sizes
		MultipartUploaderImpl.ByteRange range;
		//case when total file size is less than chunk size
		range = uploader.getByteRange(1, Uploader.BYTES_PER_CHUNK - 1024);
		assertEquals(0, range.getStart());
		assertEquals(Uploader.BYTES_PER_CHUNK - 1024 - 1, range.getEnd());
		
		//case when total file size is equal to chunk size
		range = uploader.getByteRange(1, Uploader.BYTES_PER_CHUNK);
		assertEquals(0, range.getStart());
		assertEquals(Uploader.BYTES_PER_CHUNK-1, range.getEnd());

		//case when total file size is greater than chunk size
		range = uploader.getByteRange(1, Uploader.BYTES_PER_CHUNK + 1024); 
		assertEquals(0, range.getStart());
		assertEquals(Uploader.BYTES_PER_CHUNK-1, range.getEnd());
		//also verify second chunk has the expected range
		range = uploader.getByteRange(2, Uploader.BYTES_PER_CHUNK + 1024);
		assertEquals(Uploader.BYTES_PER_CHUNK, range.getStart());
		assertEquals(Uploader.BYTES_PER_CHUNK+1024-1, range.getEnd());
		
		//verify byte range is valid in later chunk in large file
		range = uploader.getByteRange(430, (long)(4 * ClientProperties.GB));
		assertTrue(range.getStart() > -1);
		assertTrue(range.getEnd() > -1);
	}
	
//	@Test
//	public void testChunkUploadSuccessWithMoreChunksToUpload() throws RestServiceException {
//		//verify that request json is added to the list, and it calls step 2 (upload the next chunk) since there are more chunks to upload.
//		List<String> requestList = new ArrayList<String>();
//		uploader.chunkUploadSuccess("new request json", "content type",1, 2, 1024, requestList);
//		assertTrue(requestList.size() == 1);
//		//and it should try to get the url for the next chunk
//		verify(synapseClient).getChunkedPresignedUrl(anyString(), any(AsyncCallback.class));
//	}
	
//	@Test
//	public void testChunkUploadSuccessWithFinalChunk() throws RestServiceException {
//		//verify that request json is added to the list, and it calls step 3 since the current chunk number is equal to the total chunk count
//		List<String> requestList = new ArrayList<String>();
//		String[] fileNames = {"arbitraryFileName"};
//		uploader.setFileNames(fileNames);
//		uploader.chunkUploadSuccess("new request json", "content type", 2, 2, 1024, requestList);
//		assertTrue(requestList.size() == 1);
//		//and it should try to get the url for the next chunk
//		verify(synapseClient).combineChunkedFileUpload(any(List.class), any(AsyncCallback.class));
//	}
	
//	@Test
//	public void testChunkUploadFailureFirstAttempt() throws RestServiceException, InterruptedException {
//		List<String> requestList = new ArrayList<String>();
//		int attempt = 1;
//		uploader.chunkUploadFailure("content type",2, attempt, 2, 1024, requestList, "");
//		executeScheduledCallback();
//		verify(synapseClient).getChunkedPresignedUrl(anyString(), any(AsyncCallback.class));
//	}
	
//	@Test
//	public void testChunkUploadFailureFinalAttempt() throws RestServiceException {
//		List<String> requestList = new ArrayList<String>();
//		int attempt = Uploader.MAX_RETRY;
//		uploader.chunkUploadFailure("content type",2, attempt, 2, 1024, requestList, "");
//		verifyUploadError();
//	}
	
	@Test
	public void testFixingDefaultContentType() throws RestServiceException {
		String inputFilename = "file.R";
		String inputContentType = "foo/bar";
		
		//if the content type coming from the browser field is set, 
		//then this method should never override it
		assertEquals(inputContentType, uploader.fixDefaultContentType(inputContentType, inputFilename));
		
		//but if the field reports a null or empty content type, then this method should fix it
		inputContentType = "";
		assertEquals(ContentTypeUtils.PLAIN_TEXT, uploader.fixDefaultContentType(inputContentType, inputFilename));
		
		inputContentType = null;
		assertEquals(ContentTypeUtils.PLAIN_TEXT, uploader.fixDefaultContentType(inputContentType, inputFilename));
		
		//should fix tab delimited files too
		inputFilename = "file.tab";
		assertEquals(WebConstants.TEXT_TAB_SEPARATED_VALUES, uploader.fixDefaultContentType(inputContentType, inputFilename));
		
		inputFilename = "file.tsv";
		assertEquals(WebConstants.TEXT_TAB_SEPARATED_VALUES, uploader.fixDefaultContentType(inputContentType, inputFilename));
	}
	
	@Test
	public void testChunkCount() {
		//see SWC-1436
		assertEquals(2L, uploader.getChunkCount(8404992L));
	}
	
}
