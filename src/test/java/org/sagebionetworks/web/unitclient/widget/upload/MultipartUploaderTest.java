package org.sagebionetworks.web.unitclient.widget.upload;

import static junit.framework.Assert.*;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sagebionetworks.repo.model.file.AddPartResponse;
import org.sagebionetworks.repo.model.file.AddPartState;
import org.sagebionetworks.repo.model.file.BatchPresignedUploadUrlRequest;
import org.sagebionetworks.repo.model.file.BatchPresignedUploadUrlResponse;
import org.sagebionetworks.repo.model.file.ChunkedFileToken;
import org.sagebionetworks.repo.model.file.MultipartUploadRequest;
import org.sagebionetworks.repo.model.file.MultipartUploadStatus;
import org.sagebionetworks.repo.model.file.PartPresignedUrl;
import org.sagebionetworks.repo.model.file.PartUtils;
import org.sagebionetworks.repo.model.util.ContentTypeUtils;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.MultipartFileUploadClientAsync;
import org.sagebionetworks.web.client.ProgressCallback;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.callback.MD5Callback;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.upload.ByteRange;
import org.sagebionetworks.web.client.widget.upload.MultipartUploaderImpl;
import org.sagebionetworks.web.client.widget.upload.ProgressingFileUploadHandler;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.xhr.client.XMLHttpRequest;


public class MultipartUploaderTest {
	
	ProgressingFileUploadHandler mockHandler;
	SynapseJSNIUtils synapseJsniUtils;
	GWTWrapper gwt;
	MultipartUploaderImpl uploader;
	String MD5;
	String partMd5;
	
	String[] fileNames;
	Long storageLocationId = 9090L;
	@Mock
	MultipartFileUploadClientAsync mockMultipartFileUploadClient;
	@Mock
	CookieProvider mockCookies;
	@Mock
	MultipartUploadStatus mockMultipartUploadStatus;
	@Mock
	BatchPresignedUploadUrlResponse mockBatchPresignedUploadUrlResponse;
	@Mock
	PartPresignedUrl mockPartPresignedUrl;
	@Mock
	AddPartResponse mockAddPartResponse;
	public static final String UPLOAD_ID = "39282";
	public static final String RESULT_FILE_HANDLE_ID = "999999";
	public static final double FILE_SIZE=9281;
	
	@Before
	public void before() throws Exception {
		MockitoAnnotations.initMocks(this);
		mockHandler = mock(ProgressingFileUploadHandler.class);
		synapseJsniUtils =mock(SynapseJSNIUtils.class);
		gwt = mock(GWTWrapper.class);
		ChunkedFileToken token = new ChunkedFileToken();
		token.setFileName("testFile.txt");
		
		//direct upload
		//by default, do not support direct upload (direct upload tests will turn on)
		when(synapseJsniUtils.getContentType(anyString(), anyInt())).thenReturn("image/png");
		AsyncMockStubber.callSuccessWith(mockMultipartUploadStatus).when(mockMultipartFileUploadClient).startMultipartUpload(any(MultipartUploadRequest.class), anyBoolean(), any(AsyncCallback.class));
		when(mockMultipartUploadStatus.getUploadId()).thenReturn(UPLOAD_ID);
		when(mockMultipartUploadStatus.getResultFileHandleId()).thenReturn(RESULT_FILE_HANDLE_ID);
		List<PartPresignedUrl> presignedUrlList = new ArrayList<PartPresignedUrl>();
		when(mockBatchPresignedUploadUrlResponse.getPartPresignedUrls()).thenReturn(presignedUrlList);
		presignedUrlList.add(mockPartPresignedUrl);
		when(mockPartPresignedUrl.getUploadPresignedUrl()).thenReturn("http://fakepresignedurl.uploader.test");
		AsyncMockStubber.callSuccessWith(mockBatchPresignedUploadUrlResponse).when(mockMultipartFileUploadClient).getMultipartPresignedUrlBatch(any(BatchPresignedUploadUrlRequest.class), any(AsyncCallback.class));
		when(mockAddPartResponse.getAddPartState()).thenReturn(AddPartState.ADD_SUCCESS);
		AsyncMockStubber.callSuccessWith(mockAddPartResponse).when(mockMultipartFileUploadClient).addPartToMultipartUpload(anyString(), anyInt(), anyString(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(mockMultipartUploadStatus).when(mockMultipartFileUploadClient).completeMultipartUpload(anyString(), any(AsyncCallback.class));
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
		
		partMd5 = "another md5";
		doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
                final Object[] args = invocation.getArguments();
                ((MD5Callback) args[args.length - 1]).setMD5(partMd5);
				return null;
			}
		}).when(synapseJsniUtils).getFilePartMd5(anyString(), anyInt(), anyLong(), anyInt(), any(MD5Callback.class));

		when(synapseJsniUtils.getFileSize(anyString(), anyInt())).thenReturn(FILE_SIZE);
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
		
		uploader = new MultipartUploaderImpl(gwt, synapseJsniUtils, mockMultipartFileUploadClient, mockCookies);
		
		when(synapseJsniUtils.isElementExists(anyString())).thenReturn(true);
	}
	
	private void setPartsState(String partsState) {
		when(mockMultipartUploadStatus.getPartsState()).thenReturn(partsState);
	}
	
	@Test
	public void testNothingSelected(){
		when(synapseJsniUtils.getMultipleUploadFileNames(anyString())).thenReturn(null);
		uploader.uploadSelectedFile("123", mockHandler, storageLocationId);
		verify(mockHandler).uploadFailed(MultipartUploaderImpl.PLEASE_SELECT_A_FILE);
	}
	
	@Test
	public void testDirectUploadAllPartsExist() throws Exception {
		setPartsState("11");
		uploader.uploadSelectedFile("123", mockHandler, storageLocationId);
		verify(mockMultipartFileUploadClient).startMultipartUpload(any(MultipartUploadRequest.class), anyBoolean(), any(AsyncCallback.class));
		
		//never tries to get a presigned url, since all parts are uploaded.
		verify(mockMultipartFileUploadClient, never()).getMultipartPresignedUrlBatch(any(BatchPresignedUploadUrlRequest.class), any(AsyncCallback.class));
		verify(synapseJsniUtils, never()).uploadFileChunk(anyString(), anyInt(), anyString(), anyLong(), anyLong(), anyString(), any(XMLHttpRequest.class), any(ProgressCallback.class));
		//combine parts
		verify(mockMultipartFileUploadClient).completeMultipartUpload(anyString(), any(AsyncCallback.class));
		// the handler should get the id.
		verify(mockHandler).uploadSuccess(RESULT_FILE_HANDLE_ID);
	}
	
	@Test
	public void testDirectUploadSinglePart() throws Exception {
		setPartsState("0");
		uploader.uploadSelectedFile("123", mockHandler, storageLocationId);
		verify(mockMultipartFileUploadClient).startMultipartUpload(any(MultipartUploadRequest.class), anyBoolean(), any(AsyncCallback.class));
		verify(mockMultipartFileUploadClient).getMultipartPresignedUrlBatch(any(BatchPresignedUploadUrlRequest.class), any(AsyncCallback.class));
		verify(synapseJsniUtils).uploadFileChunk(eq(MultipartUploaderImpl.BINARY_CONTENT_TYPE), anyInt(), anyString(), anyLong(), anyLong(), anyString(), any(XMLHttpRequest.class), any(ProgressCallback.class));
		//manually call the method that's invoked with a successful xhr put (upload)
		uploader.addCurrentPartToMultipartUpload();
		verify(mockMultipartFileUploadClient).addPartToMultipartUpload(anyString(), anyInt(), anyString(), any(AsyncCallback.class));
		verify(mockMultipartFileUploadClient).completeMultipartUpload(anyString(), any(AsyncCallback.class));
		// the handler should get the id.
		verify(mockHandler).uploadSuccess(RESULT_FILE_HANDLE_ID);
	}
	

	@Test
	public void testDirectUploadSingleSecondPart() throws Exception {
		setPartsState("10");
		uploader.uploadSelectedFile("123", mockHandler, storageLocationId);
		verify(mockMultipartFileUploadClient).startMultipartUpload(any(MultipartUploadRequest.class), anyBoolean(), any(AsyncCallback.class));
		verify(mockMultipartFileUploadClient).getMultipartPresignedUrlBatch(any(BatchPresignedUploadUrlRequest.class), any(AsyncCallback.class));
		verify(synapseJsniUtils).uploadFileChunk(anyString(), anyInt(), anyString(), anyLong(), anyLong(), anyString(), any(XMLHttpRequest.class), any(ProgressCallback.class));
		//manually call the method that's invoked with a successful xhr put (upload)
		uploader.addCurrentPartToMultipartUpload();
		verify(mockMultipartFileUploadClient).addPartToMultipartUpload(anyString(), anyInt(), anyString(), any(AsyncCallback.class));
		verify(mockMultipartFileUploadClient).completeMultipartUpload(anyString(), any(AsyncCallback.class));
		// the handler should get the id.
		verify(mockHandler).uploadSuccess(RESULT_FILE_HANDLE_ID);
	}

	@Test
	public void testDirectUploadMultiPart() throws Exception {
		setPartsState("00");
		uploader.uploadSelectedFile("123", mockHandler, storageLocationId);
		verify(mockMultipartFileUploadClient).startMultipartUpload(any(MultipartUploadRequest.class), anyBoolean(), any(AsyncCallback.class));
		verify(mockMultipartFileUploadClient).getMultipartPresignedUrlBatch(any(BatchPresignedUploadUrlRequest.class), any(AsyncCallback.class));
		verify(synapseJsniUtils).uploadFileChunk(anyString(), anyInt(), anyString(), anyLong(), anyLong(), anyString(), any(XMLHttpRequest.class), any(ProgressCallback.class));
		//manually call the method that's invoked with a successful xhr put (upload)
		uploader.addCurrentPartToMultipartUpload();
		verify(mockMultipartFileUploadClient).addPartToMultipartUpload(anyString(), anyInt(), anyString(), any(AsyncCallback.class));
		//should not have completed, since there's another part
		verify(mockMultipartFileUploadClient, never()).completeMultipartUpload(anyString(), any(AsyncCallback.class));
		
		uploader.addCurrentPartToMultipartUpload();
		verify(mockMultipartFileUploadClient).completeMultipartUpload(anyString(), any(AsyncCallback.class));
		// the handler should get the id.
		verify(mockHandler).uploadSuccess(RESULT_FILE_HANDLE_ID);
	}
	
	//failure cases
	
	
	@Test
	public void testNoLongerUploading() throws Exception {
		//if file input element is no longer on the page, quitely shut the upload down.
		when(synapseJsniUtils.isElementExists(anyString())).thenReturn(false);
		setPartsState("10");
		uploader.uploadSelectedFile("123", mockHandler, storageLocationId);
		verifyZeroInteractions(mockMultipartFileUploadClient);
		verifyZeroInteractions(mockHandler);
	}

	@Test
	public void testStartMultipartUploadFailure() throws Exception {
		String error = "failed";
		AsyncMockStubber.callFailureWith(new IllegalArgumentException(error)).when(mockMultipartFileUploadClient).startMultipartUpload(any(MultipartUploadRequest.class), anyBoolean(), any(AsyncCallback.class));
		setPartsState("00");
		uploader.uploadSelectedFile("123", mockHandler, storageLocationId);

		verify(mockHandler).uploadFailed(error);
	}
	
	@Test
	public void testStartMultipartUploadAttemptsExceeded() throws Exception {
		setPartsState("0");
		uploader.uploadSelectedFile("123", mockHandler, storageLocationId);
		//simulate the single part fails to upload MAX_RETRY times
		for (int i = 0; i < 11; i++) {
			uploader.partFailure("part failed");
		}
		//should have retried 11 times.  plus the initial attempt, so 12 calls to start the upload...
		verify(mockMultipartFileUploadClient, times(12)).startMultipartUpload(any(MultipartUploadRequest.class), anyBoolean(), any(AsyncCallback.class));
		//close dialog, and retry once more
		when(synapseJsniUtils.isElementExists(anyString())).thenReturn(false);
		reset(mockMultipartFileUploadClient);
		uploader.partFailure("part failed");
		verifyZeroInteractions(mockMultipartFileUploadClient);
	}
	
	@Test
	public void testCompleteMultipartUploadFailure() throws Exception {
		String error = "failed";
		AsyncMockStubber.callFailureWith(new IllegalArgumentException(error)).when(mockMultipartFileUploadClient).completeMultipartUpload(anyString(), any(AsyncCallback.class));
		setPartsState("0");
		uploader.uploadSelectedFile("123", mockHandler, storageLocationId);
		
		//manually call the method that's invoked with a successful xhr put (upload)
		uploader.addCurrentPartToMultipartUpload();
		//should have logged the error locally, and attempted to retry the upload from the beginning
		verify(synapseJsniUtils).consoleError(error);
		verify(mockMultipartFileUploadClient, times(2)).startMultipartUpload(any(MultipartUploadRequest.class), anyBoolean(), any(AsyncCallback.class));
	}

	@Test
	public void testByteRange() {
		//test chunk sizes
		ByteRange range;
		
		//case when total file size is less than chunk size
		range = new ByteRange(1, PartUtils.MAX_PART_SIZE_BYTES - 1024, PartUtils.MAX_PART_SIZE_BYTES);
		assertEquals(0, range.getStart());
		assertEquals(PartUtils.MAX_PART_SIZE_BYTES - 1024 - 1, range.getEnd());
		
		//case when total file size is equal to chunk size
		range = new ByteRange(1, PartUtils.MAX_PART_SIZE_BYTES, PartUtils.MAX_PART_SIZE_BYTES);
		assertEquals(0, range.getStart());
		assertEquals(PartUtils.MAX_PART_SIZE_BYTES-1, range.getEnd());

		//case when total file size is greater than chunk size
		range = new ByteRange(1, PartUtils.MAX_PART_SIZE_BYTES + 1024, PartUtils.MAX_PART_SIZE_BYTES); 
		assertEquals(0, range.getStart());
		assertEquals(PartUtils.MAX_PART_SIZE_BYTES-1, range.getEnd());
		//also verify second chunk has the expected range
		range = new ByteRange(2, PartUtils.MAX_PART_SIZE_BYTES + 1024, PartUtils.MAX_PART_SIZE_BYTES);
		assertEquals(PartUtils.MAX_PART_SIZE_BYTES, range.getStart());
		assertEquals(PartUtils.MAX_PART_SIZE_BYTES+1024-1, range.getEnd());
		
		//verify byte range is valid in later chunk in large file
		range = new ByteRange(430, (long)(4 * ClientProperties.GB), PartUtils.MAX_PART_SIZE_BYTES);
		assertTrue(range.getStart() > -1);
		assertTrue(range.getEnd() > -1);
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
	public void testCompletedPartCount() {
		assertEquals(0, uploader.getCompletedPartCount(""));
		assertEquals(0, uploader.getCompletedPartCount("0"));
		assertEquals(0, uploader.getCompletedPartCount("0000"));
		assertEquals(2, uploader.getCompletedPartCount("0101"));
		assertEquals(4, uploader.getCompletedPartCount("1111"));
	}
}
