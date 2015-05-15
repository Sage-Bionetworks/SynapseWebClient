package org.sagebionetworks.web.unitclient.widget.upload;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.sagebionetworks.web.client.widget.upload.MultipartUploaderImpl.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
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
import org.sagebionetworks.web.client.widget.upload.FileMetadata;
import org.sagebionetworks.web.client.widget.upload.ProgressingFileUploadHandler;
import org.sagebionetworks.web.client.widget.upload.MultipartUploaderImpl;
import org.sagebionetworks.web.client.widget.upload.FileUpload;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.exceptions.CombineFileChunksException;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.xhr.client.XMLHttpRequest;


public class MultipartUploaderTest {
	
	ProgressingFileUploadHandler mockHandler;
	SynapseClientAsync synapseClient;
	SynapseJSNIUtils synapseJsniUtils;
	ClientLogger mockLogger;
	GWTWrapper gwt;
	MultipartUploaderImpl uploader;
	String MD5;
	UploadDaemonStatus status;
	String[] fileNames;
	Long storageLocationId = 9090L;
	
	@Before
	public void before() throws Exception {
		mockHandler = mock(ProgressingFileUploadHandler.class);
		synapseClient = mock(SynapseClientAsync.class);
		synapseJsniUtils =mock(SynapseJSNIUtils.class);
		gwt = mock(GWTWrapper.class);
		mockLogger = mock(ClientLogger.class);
		ChunkedFileToken token = new ChunkedFileToken();
		token.setFileName("testFile.txt");
		
		//direct upload
		//by default, do not support direct upload (direct upload tests will turn on)
		when(synapseJsniUtils.getContentType(anyString(), anyInt())).thenReturn("image/png");
		AsyncMockStubber.callSuccessWith(token).when(synapseClient).getChunkedFileToken(anyString(), anyString(), anyString(), any(Long.class), any(AsyncCallback.class));
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
		
		// fire the timer 
		doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				final Object[] args = invocation.getArguments();
				Callback callback = (Callback) args[0];
				callback.invoke();
				return null;
			}
		}).when(gwt).scheduleExecution(any(Callback.class), anyInt());
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
		uploader.uploadSelectedFile("123", mockHandler, storageLocationId);
		verify(mockHandler).uploadFailed(MultipartUploaderImpl.PLEASE_SELECT_A_FILE);
	}
	
	@Test
	public void testDirectUploadHappyCase() throws Exception {
		uploader.uploadSelectedFile("123", mockHandler, storageLocationId);
		// jump to step three
		uploader.attemptCombineChunks(null, 1);
		verify(synapseClient).getChunkedFileToken(anyString(), anyString(), anyString(), any(Long.class), any(AsyncCallback.class));
		verify(synapseClient).getChunkedPresignedUrl(any(ChunkRequest.class), any(AsyncCallback.class));
		verify(synapseJsniUtils).uploadFileChunk(anyString(), anyInt(), anyString(), anyLong(), anyLong(), anyString(), any(XMLHttpRequest.class), any(ProgressCallback.class));
		verify(synapseClient).combineChunkedFileUpload(any(List.class), any(AsyncCallback.class));
		// the handler should get the id.
		FileUpload uploadedFile = new FileUpload(null, status.getFileHandleId());
		verify(mockHandler).uploadSuccess(any(FileUpload.class));
	}

	
	@Test
	public void testDirectUploadStep3Failure() throws Exception {
		String error = "an error";
		AsyncMockStubber.callFailureWith(new IllegalArgumentException(error)).when(synapseClient).getChunkedFileToken(anyString(), anyString(), anyString(), any(Long.class), any(AsyncCallback.class));
		uploader.uploadSelectedFile("123", mockHandler, storageLocationId);
		verify(mockHandler).uploadFailed(error);
	}

	@Test
	public void testDirectUploadStep4Failure() throws Exception {
		String error = "failed";
		AsyncMockStubber.callFailureWith(new IllegalArgumentException(error)).when(synapseClient).getChunkedPresignedUrl(any(ChunkRequest.class), any(AsyncCallback.class));
		uploader.uploadSelectedFile("123", mockHandler, storageLocationId);
		// It should try until the max retries are exceeded
		verify(synapseClient, Mockito.times(MAX_RETRY)).getChunkedPresignedUrl(any(ChunkRequest.class), any(AsyncCallback.class));
		// Things should end with an error.s
		verify(mockHandler).uploadFailed(EXCEEDED_THE_MAXIMUM_UPLOAD_A_SINGLE_FILE_CHUNK+error);
	}
	
	@Test
	public void testDirectUploadStep5Failure() throws Exception {
		String error = "failed";
		AsyncMockStubber.callFailureWith(new IllegalArgumentException(error)).when(synapseClient).combineChunkedFileUpload(any(List.class), any(AsyncCallback.class));
		uploader.uploadSelectedFile("123", mockHandler, storageLocationId);
		// jump to the end
		uploader.attemptCombineChunks(null, 1);
		verify(synapseClient, Mockito.times(MAX_RETRY)).combineChunkedFileUpload(any(List.class), any(AsyncCallback.class));
		// Things should end with an error.s
		verify(mockHandler).uploadFailed(EXCEEDED_THE_MAXIMUM_COMBINE_ALL_OF_THE_PARTS+error);
	}


	@Test
	public void testDirectUploadStep5Retry() throws Exception {
		//returned a failed status every time, and verify that we will eventually see an upload error (once the MAX_RETRY limit has been surpassed)
		UploadDaemonStatus status = new UploadDaemonStatus();
		status.setState(State.FAILED);
		status.setFileHandleId("fake handle");
		status.setErrorMessage("an error");
		AsyncMockStubber.callSuccessWith(status).when(synapseClient).combineChunkedFileUpload(any(List.class), any(AsyncCallback.class));
		uploader.uploadSelectedFile("123", mockHandler, storageLocationId);
		// jump to the end
		uploader.attemptCombineChunks(null, 1);
		verify(synapseClient, Mockito.times(MAX_RETRY)).combineChunkedFileUpload(any(List.class), any(AsyncCallback.class));
		// Things should end with an error.s
		verify(mockHandler).uploadFailed(EXCEEDED_THE_MAXIMUM_COMBINE_ALL_OF_THE_PARTS+status.getErrorMessage());
	}
	
	@Test
	public void testByteRange() {
		//test chunk sizes
		MultipartUploaderImpl.ByteRange range;
		//case when total file size is less than chunk size
		range = uploader.getByteRange(1, BYTES_PER_CHUNK - 1024);
		assertEquals(0, range.getStart());
		assertEquals(BYTES_PER_CHUNK - 1024 - 1, range.getEnd());
		
		//case when total file size is equal to chunk size
		range = uploader.getByteRange(1, BYTES_PER_CHUNK);
		assertEquals(0, range.getStart());
		assertEquals(BYTES_PER_CHUNK-1, range.getEnd());

		//case when total file size is greater than chunk size
		range = uploader.getByteRange(1, BYTES_PER_CHUNK + 1024); 
		assertEquals(0, range.getStart());
		assertEquals(BYTES_PER_CHUNK-1, range.getEnd());
		//also verify second chunk has the expected range
		range = uploader.getByteRange(2, BYTES_PER_CHUNK + 1024);
		assertEquals(BYTES_PER_CHUNK, range.getStart());
		assertEquals(BYTES_PER_CHUNK+1024-1, range.getEnd());
		
		//verify byte range is valid in later chunk in large file
		range = uploader.getByteRange(430, (long)(4 * ClientProperties.GB));
		assertTrue(range.getStart() > -1);
		assertTrue(range.getEnd() > -1);
	}
	
	@Test
	public void testChunkUploadSuccessWithMoreChunksToUpload() throws RestServiceException {
		//verify that request json is added to the list, and it calls step 2 (upload the next chunk) since there are more chunks to upload.
		List<ChunkRequest> requestList = new ArrayList<ChunkRequest>();
		ChunkRequest request = new ChunkRequest();
		uploader.uploadSelectedFile("123", mockHandler, storageLocationId);
		uploader.chunkUploadSuccess(request, 1, 2, 1024, requestList);
		assertTrue(requestList.size() == 1);
		//and it should try to get the url for the next chunk
		verify(synapseClient, times(2)).getChunkedPresignedUrl(any(ChunkRequest.class), any(AsyncCallback.class));
	}
	
	@Test
	public void testChunkUploadSuccessWithFinalChunk() throws RestServiceException {
		//verify that request json is added to the list, and it calls step 3 since the current chunk number is equal to the total chunk count
		List<ChunkRequest> requestList = new ArrayList<ChunkRequest>();
		ChunkRequest request = new ChunkRequest();
		uploader.uploadSelectedFile("123", mockHandler, storageLocationId);
		uploader.chunkUploadSuccess(request, 2, 2, 1024, requestList);
		assertTrue(requestList.size() == 1);
		//and it should try to get the url for the next chunk
		verify(synapseClient).combineChunkedFileUpload(any(List.class), any(AsyncCallback.class));
	}
	
	@Test
	public void testChunkUploadFailureFirstAttempt() throws RestServiceException, InterruptedException {
		List<ChunkRequest> requestList = new ArrayList<ChunkRequest>();
		int attempt = 1;
		uploader.uploadSelectedFile("123", mockHandler, storageLocationId);
		uploader.chunkUploadFailure(2, attempt, 2, 1024, requestList, "");
		verify(synapseClient, times(2)).getChunkedPresignedUrl(any(ChunkRequest.class), any(AsyncCallback.class));
	}
	
	@Test
	public void testChunkUploadFailureFinalAttempt() throws RestServiceException {
		List<ChunkRequest> requestList = new ArrayList<ChunkRequest>();
		int attempt = MAX_RETRY;
		uploader.uploadSelectedFile("123", mockHandler, storageLocationId);
		uploader.chunkUploadFailure(2, attempt, 2, 1024, requestList, "");
		verify(mockHandler).uploadFailed(EXCEEDED_THE_MAXIMUM_UPLOAD_A_SINGLE_FILE_CHUNK);
		verify(mockLogger).errorToRepositoryServices(anyString(), any(CombineFileChunksException.class));
	}
	
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
		
		// should fix CSV files as well
		inputFilename = "file.CSV";
		assertEquals(WebConstants.TEXT_COMMA_SEPARATED_VALUES, uploader.fixDefaultContentType(inputContentType, inputFilename));
		
		// should fix text files as well
		inputFilename = "file.TXT";
		assertEquals(ContentTypeUtils.PLAIN_TEXT, uploader.fixDefaultContentType(inputContentType, inputFilename));
	}
	
	@Test
	public void testChunkCount() {
		//see SWC-1436
		assertEquals(2L, uploader.getChunkCount(8404992L));
	}
	
	@Test
	public void testGetSelectedFileMetadataNull(){
		String inputId = "123";
		FileUpload uploadedFile = new FileUpload(null,  inputId);
		when(synapseJsniUtils.getMultipleUploadFileNames(anyString())).thenReturn(null);
		assertEquals(null, uploader.getSelectedFileMetadata());
	}
	
}
