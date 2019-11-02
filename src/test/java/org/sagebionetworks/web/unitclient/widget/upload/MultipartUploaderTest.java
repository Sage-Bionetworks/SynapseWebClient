package org.sagebionetworks.web.unitclient.widget.upload;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.client.ContentTypeUtils.fixDefaultContentType;
import static org.sagebionetworks.web.client.widget.upload.MultipartUploaderImpl.EMPTY_FILE_ERROR_MESSAGE;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.ProgressCallback;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.callback.MD5Callback;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.upload.ByteRange;
import org.sagebionetworks.web.client.widget.upload.MultipartUploaderImpl;
import org.sagebionetworks.web.client.widget.upload.ProgressingFileUploadHandler;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import org.sagebionetworks.web.test.helper.CallbackMockStubber;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.logical.shared.HasAttachHandlers;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.xhr.client.XMLHttpRequest;


public class MultipartUploaderTest {

	@Mock
	ProgressingFileUploadHandler mockHandler;
	@Mock
	SynapseJSNIUtils synapseJsniUtils;
	@Mock
	GWTWrapper gwt;
	MultipartUploaderImpl uploader;
	String MD5;
	String partMd5;

	String[] fileNames;
	Long storageLocationId = 9090L;
	@Mock
	SynapseJavascriptClient mockJsClient;
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
	@Mock
	HasAttachHandlers mockView;
	@Captor
	ArgumentCaptor<ProgressCallback> progressCaptor;
	@Mock
	JavaScriptObject mockFileBlob;
	public static final String UPLOAD_ID = "39282";
	public static final String RESULT_FILE_HANDLE_ID = "999999";
	public static final double FILE_SIZE = 9281;
	public static final String FILE_NAME = "file.txt";
	public static final String CONTENT_TYPE = "text/plain";

	@Before
	public void before() throws Exception {
		MockitoAnnotations.initMocks(this);
		ChunkedFileToken token = new ChunkedFileToken();
		token.setFileName("testFile.txt");

		// direct upload
		// by default, do not support direct upload (direct upload tests will turn on)
		when(synapseJsniUtils.getContentType(any(JavaScriptObject.class), anyInt())).thenReturn("image/png");
		AsyncMockStubber.callSuccessWith(mockMultipartUploadStatus).when(mockJsClient).startMultipartUpload(any(MultipartUploadRequest.class), anyBoolean(), any(AsyncCallback.class));
		when(mockMultipartUploadStatus.getUploadId()).thenReturn(UPLOAD_ID);
		when(mockMultipartUploadStatus.getResultFileHandleId()).thenReturn(RESULT_FILE_HANDLE_ID);
		List<PartPresignedUrl> presignedUrlList = new ArrayList<PartPresignedUrl>();
		when(mockBatchPresignedUploadUrlResponse.getPartPresignedUrls()).thenReturn(presignedUrlList);
		presignedUrlList.add(mockPartPresignedUrl);
		when(mockPartPresignedUrl.getUploadPresignedUrl()).thenReturn("http://fakepresignedurl.uploader.test");
		AsyncMockStubber.callSuccessWith(mockBatchPresignedUploadUrlResponse).when(mockJsClient).getMultipartPresignedUrlBatch(any(BatchPresignedUploadUrlRequest.class), any(AsyncCallback.class));
		when(mockAddPartResponse.getAddPartState()).thenReturn(AddPartState.ADD_SUCCESS);
		AsyncMockStubber.callSuccessWith(mockAddPartResponse).when(mockJsClient).addPartToMultipartUpload(anyString(), anyInt(), anyString(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(mockMultipartUploadStatus).when(mockJsClient).completeMultipartUpload(anyString(), any(AsyncCallback.class));
		// Stub the generation of a MD5.
		MD5 = "some md5";
		doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				final Object[] args = invocation.getArguments();
				((MD5Callback) args[args.length - 1]).setMD5(MD5);
				return null;
			}
		}).when(synapseJsniUtils).getFileMd5(any(JavaScriptObject.class), any(MD5Callback.class));

		partMd5 = "another md5";
		doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				final Object[] args = invocation.getArguments();
				((MD5Callback) args[args.length - 1]).setMD5(partMd5);
				return null;
			}
		}).when(synapseJsniUtils).getFilePartMd5(any(JavaScriptObject.class), anyInt(), anyLong(), any(MD5Callback.class));

		when(synapseJsniUtils.getFileSize(any(JavaScriptObject.class))).thenReturn(FILE_SIZE);
		// fire the timer
		CallbackMockStubber.invokeCallback().when(gwt).scheduleExecution(any(Callback.class), anyInt());
		when(gwt.createXMLHttpRequest()).thenReturn(null);

		String[] fileNames = {"newFile.txt"};
		when(synapseJsniUtils.getMultipleUploadFileNames(any(JavaScriptObject.class))).thenReturn(fileNames);

		String file1 = "file1.txt";
		fileNames = new String[] {file1};
		when(synapseJsniUtils.getMultipleUploadFileNames(any(JavaScriptObject.class))).thenReturn(fileNames);

		uploader = new MultipartUploaderImpl(gwt, synapseJsniUtils, mockJsClient, mockCookies);

		when(mockView.isAttached()).thenReturn(true);
	}

	private void setPartsState(String partsState) {
		when(mockMultipartUploadStatus.getPartsState()).thenReturn(partsState);
	}

	@Test
	public void testDirectUploadFolder() throws Exception {
		setPartsState("0");
		doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				final Object[] args = invocation.getArguments();
				((MD5Callback) args[args.length - 1]).setMD5(null);
				return null;
			}
		}).when(synapseJsniUtils).getFileMd5(any(JavaScriptObject.class), any(MD5Callback.class));

		uploader.uploadFile(FILE_NAME, CONTENT_TYPE, mockFileBlob, mockHandler, storageLocationId, mockView);
		verify(synapseJsniUtils).getFileMd5(any(JavaScriptObject.class), any(MD5Callback.class));
		verify(mockHandler).uploadFailed(DisplayConstants.MD5_CALCULATION_ERROR);
	}

	@Test
	public void testDirectUploadAllPartsExist() throws Exception {
		setPartsState("11");
		uploader.uploadFile(FILE_NAME, CONTENT_TYPE, mockFileBlob, mockHandler, storageLocationId, mockView);
		verify(mockJsClient).startMultipartUpload(any(MultipartUploadRequest.class), anyBoolean(), any(AsyncCallback.class));

		// never tries to get a presigned url, since all parts are uploaded.
		verify(mockJsClient, never()).getMultipartPresignedUrlBatch(any(BatchPresignedUploadUrlRequest.class), any(AsyncCallback.class));
		verify(synapseJsniUtils, never()).uploadFileChunk(anyString(), any(JavaScriptObject.class), anyLong(), anyLong(), anyString(), any(XMLHttpRequest.class), any(ProgressCallback.class));
		// combine parts
		verify(mockJsClient).completeMultipartUpload(anyString(), any(AsyncCallback.class));
		// the handler should get the id.
		verify(mockHandler).uploadSuccess(RESULT_FILE_HANDLE_ID);
	}

	@Test
	public void testDirectUploadEmptyFile() throws Exception {
		when(synapseJsniUtils.getFileSize(any(JavaScriptObject.class))).thenReturn(0.0);
		setPartsState("0");
		uploader.uploadFile(FILE_NAME, CONTENT_TYPE, mockFileBlob, mockHandler, storageLocationId, mockView);
		verify(mockJsClient, never()).startMultipartUpload(any(MultipartUploadRequest.class), anyBoolean(), any(AsyncCallback.class));
		verify(mockHandler).uploadFailed(EMPTY_FILE_ERROR_MESSAGE + FILE_NAME);
	}

	@Test
	public void testDirectUploadSinglePart() throws Exception {
		setPartsState("0");
		uploader.uploadFile(FILE_NAME, CONTENT_TYPE, mockFileBlob, mockHandler, storageLocationId, mockView);
		verify(mockJsClient).startMultipartUpload(any(MultipartUploadRequest.class), anyBoolean(), any(AsyncCallback.class));
		ArgumentCaptor<BatchPresignedUploadUrlRequest> captor = ArgumentCaptor.forClass(BatchPresignedUploadUrlRequest.class);
		verify(mockJsClient).getMultipartPresignedUrlBatch(captor.capture(), any(AsyncCallback.class));
		assertEquals(MultipartUploaderImpl.BINARY_CONTENT_TYPE, captor.getValue().getContentType());
		verify(synapseJsniUtils).uploadFileChunk(eq(MultipartUploaderImpl.BINARY_CONTENT_TYPE), any(JavaScriptObject.class), anyLong(), anyLong(), anyString(), any(XMLHttpRequest.class), any(ProgressCallback.class));
		// manually call the method that's invoked with a successful xhr put (upload)
		uploader.addCurrentPartToMultipartUpload();
		verify(mockJsClient).addPartToMultipartUpload(anyString(), anyInt(), anyString(), any(AsyncCallback.class));
		verify(mockJsClient).completeMultipartUpload(anyString(), any(AsyncCallback.class));
		// the handler should get the id.
		verify(mockHandler).uploadSuccess(RESULT_FILE_HANDLE_ID);
	}

	@Test
	public void testDirectUploadSingleSecondPart() throws Exception {
		setPartsState("10");
		uploader.uploadFile(FILE_NAME, CONTENT_TYPE, mockFileBlob, mockHandler, storageLocationId, mockView);
		verify(mockJsClient).startMultipartUpload(any(MultipartUploadRequest.class), anyBoolean(), any(AsyncCallback.class));
		verify(mockJsClient).getMultipartPresignedUrlBatch(any(BatchPresignedUploadUrlRequest.class), any(AsyncCallback.class));
		verify(synapseJsniUtils).uploadFileChunk(anyString(), any(JavaScriptObject.class), anyLong(), anyLong(), anyString(), any(XMLHttpRequest.class), any(ProgressCallback.class));
		// manually call the method that's invoked with a successful xhr put (upload)
		uploader.addCurrentPartToMultipartUpload();
		verify(mockJsClient).addPartToMultipartUpload(anyString(), anyInt(), anyString(), any(AsyncCallback.class));
		verify(mockJsClient).completeMultipartUpload(anyString(), any(AsyncCallback.class));
		// the handler should get the id.
		verify(mockHandler).uploadSuccess(RESULT_FILE_HANDLE_ID);
	}

	@Test
	public void testDirectUploadMultiPart() throws Exception {
		setPartsState("00");
		uploader.uploadFile(FILE_NAME, CONTENT_TYPE, mockFileBlob, mockHandler, storageLocationId, mockView);
		verify(mockJsClient).startMultipartUpload(any(MultipartUploadRequest.class), anyBoolean(), any(AsyncCallback.class));
		verify(mockJsClient).getMultipartPresignedUrlBatch(any(BatchPresignedUploadUrlRequest.class), any(AsyncCallback.class));
		verify(synapseJsniUtils).uploadFileChunk(anyString(), any(JavaScriptObject.class), anyLong(), anyLong(), anyString(), any(XMLHttpRequest.class), any(ProgressCallback.class));
		// manually call the method that's invoked with a successful xhr put (upload)
		uploader.addCurrentPartToMultipartUpload();
		verify(mockJsClient).addPartToMultipartUpload(anyString(), anyInt(), anyString(), any(AsyncCallback.class));
		// should not have completed, since there's another part

		// SWC-4262: check the file md5 once in the beginning
		verify(synapseJsniUtils).getFileMd5(any(JavaScriptObject.class), any(MD5Callback.class));
		verify(mockJsClient, never()).completeMultipartUpload(anyString(), any(AsyncCallback.class));

		uploader.addCurrentPartToMultipartUpload();
		verify(mockJsClient).completeMultipartUpload(anyString(), any(AsyncCallback.class));
		// SWC-4262: check the file md5 once in the beginning, and once on complete.
		verify(synapseJsniUtils, times(2)).getFileMd5(any(JavaScriptObject.class), any(MD5Callback.class));

		// the handler should get the id.
		verify(mockHandler).uploadSuccess(RESULT_FILE_HANDLE_ID);
	}


	@Test
	public void testDirectUploadMultiPartFileChanges() throws Exception {
		setPartsState("00");
		uploader.uploadFile(FILE_NAME, CONTENT_TYPE, mockFileBlob, mockHandler, storageLocationId, mockView);

		verify(mockJsClient).startMultipartUpload(any(MultipartUploadRequest.class), anyBoolean(), any(AsyncCallback.class));

		// manually call the method that's invoked with a successful xhr put (upload)
		uploader.addCurrentPartToMultipartUpload();

		// SWC-4262: check the file md5 once in the beginning
		verify(synapseJsniUtils).getFileMd5(any(JavaScriptObject.class), any(MD5Callback.class));
		verify(mockJsClient, never()).completeMultipartUpload(anyString(), any(AsyncCallback.class));

		doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				final Object[] args = invocation.getArguments();
				((MD5Callback) args[args.length - 1]).setMD5("different md5!");
				return null;
			}
		}).when(synapseJsniUtils).getFileMd5(any(JavaScriptObject.class), any(MD5Callback.class));

		uploader.addCurrentPartToMultipartUpload();
		verify(mockJsClient, never()).completeMultipartUpload(anyString(), any(AsyncCallback.class));

		// SWC-4262: check the file md5 once in the beginning, once on complete (to verify)
		verify(synapseJsniUtils, times(2)).getFileMd5(any(JavaScriptObject.class), any(MD5Callback.class));

		// SWC-4262: the md5 check on complete caused upload to start from the beginning (recreating the
		// request)
		verify(mockHandler).uploadFailed(anyString());
	}

	// failure cases


	@Test
	public void testNoLongerUploading() throws Exception {
		// if file input element is no longer on the page, quitely shut the upload down.
		when(mockView.isAttached()).thenReturn(false);
		setPartsState("10");
		uploader.uploadFile(FILE_NAME, CONTENT_TYPE, mockFileBlob, mockHandler, storageLocationId, mockView);
		verify(mockJsClient, never()).completeMultipartUpload(anyString(), any(AsyncCallback.class));
	}

	@Test
	public void testUploadCanceled() throws Exception {
		// single part
		setPartsState("0");
		uploader.uploadFile(FILE_NAME, CONTENT_TYPE, mockFileBlob, mockHandler, storageLocationId, mockView);

		// user cancels the upload
		uploader.cancelUpload();
		verify(synapseJsniUtils).uploadFileChunk(eq(MultipartUploaderImpl.BINARY_CONTENT_TYPE), any(JavaScriptObject.class), anyLong(), anyLong(), anyString(), any(XMLHttpRequest.class), progressCaptor.capture());
		ProgressCallback progressCallback = progressCaptor.getValue();
		progressCallback.updateProgress(0, 1);
		// never updated the handler because the upload has been canceled (can't verify the abort(), since
		// xhr is a js object).
		verifyZeroInteractions(mockHandler);
	}

	@Test
	public void testStartMultipartUploadFailure() throws Exception {
		String error = "failed";
		AsyncMockStubber.callFailureWith(new IllegalArgumentException(error)).when(mockJsClient).startMultipartUpload(any(MultipartUploadRequest.class), anyBoolean(), any(AsyncCallback.class));
		setPartsState("00");
		uploader.uploadFile(FILE_NAME, CONTENT_TYPE, mockFileBlob, mockHandler, storageLocationId, mockView);

		verify(mockHandler).uploadFailed(error);
	}

	@Test
	public void testStartMultipartUploadAttemptsExceeded() throws Exception {
		setPartsState("0");
		uploader.uploadFile(FILE_NAME, CONTENT_TYPE, mockFileBlob, mockHandler, storageLocationId, mockView);
		// simulate the single part fails to upload MAX_RETRY times
		for (int i = 0; i < 11; i++) {
			uploader.partFailure("part failed");
		}
		// should have retried 11 times. plus the initial attempt, so 12 calls to start the upload...
		verify(mockJsClient, times(12)).startMultipartUpload(any(MultipartUploadRequest.class), anyBoolean(), any(AsyncCallback.class));
		// close dialog, and retry once more
		when(mockView.isAttached()).thenReturn(false);
		reset(mockJsClient);
		uploader.partFailure("part failed");
		verifyZeroInteractions(mockJsClient);
	}

	@Test
	public void testCompleteMultipartUploadFailure() throws Exception {
		String error = "failed";
		AsyncMockStubber.callFailureWith(new IllegalArgumentException(error)).when(mockJsClient).completeMultipartUpload(anyString(), any(AsyncCallback.class));
		setPartsState("0");
		uploader.uploadFile(FILE_NAME, CONTENT_TYPE, mockFileBlob, mockHandler, storageLocationId, mockView);

		// manually call the method that's invoked with a successful xhr put (upload)
		uploader.addCurrentPartToMultipartUpload();
		// should have logged the error locally, and attempted to retry the upload from the beginning
		verify(synapseJsniUtils).consoleError(error);
		verify(mockJsClient, times(2)).startMultipartUpload(any(MultipartUploadRequest.class), anyBoolean(), any(AsyncCallback.class));
	}

	@Test
	public void testByteRange() {
		// test chunk sizes
		ByteRange range;

		// case when total file size is less than chunk size
		range = new ByteRange(1, PartUtils.MAX_PART_SIZE_BYTES - 1024, PartUtils.MAX_PART_SIZE_BYTES);
		assertEquals(0, range.getStart());
		assertEquals(PartUtils.MAX_PART_SIZE_BYTES - 1024 - 1, range.getEnd());

		// case when total file size is equal to chunk size
		range = new ByteRange(1, PartUtils.MAX_PART_SIZE_BYTES, PartUtils.MAX_PART_SIZE_BYTES);
		assertEquals(0, range.getStart());
		assertEquals(PartUtils.MAX_PART_SIZE_BYTES - 1, range.getEnd());

		// case when total file size is greater than chunk size
		range = new ByteRange(1, PartUtils.MAX_PART_SIZE_BYTES + 1024, PartUtils.MAX_PART_SIZE_BYTES);
		assertEquals(0, range.getStart());
		assertEquals(PartUtils.MAX_PART_SIZE_BYTES - 1, range.getEnd());
		// also verify second chunk has the expected range
		range = new ByteRange(2, PartUtils.MAX_PART_SIZE_BYTES + 1024, PartUtils.MAX_PART_SIZE_BYTES);
		assertEquals(PartUtils.MAX_PART_SIZE_BYTES, range.getStart());
		assertEquals(PartUtils.MAX_PART_SIZE_BYTES + 1024 - 1, range.getEnd());

		// verify byte range is valid in later chunk in large file
		range = new ByteRange(430, (long) (4 * ClientProperties.GB), PartUtils.MAX_PART_SIZE_BYTES);
		assertTrue(range.getStart() > -1);
		assertTrue(range.getEnd() > -1);
	}

	@Test
	public void testFixingDefaultContentType() throws RestServiceException {
		String inputFilename = "file.R";
		String inputContentType = "foo/bar";

		// if the content type coming from the browser field is set,
		// then this method should never override it
		assertEquals(inputContentType, fixDefaultContentType(inputContentType, inputFilename));

		// but if the field reports a null or empty content type, then this method should fix it
		inputContentType = "";
		assertEquals(ContentTypeUtils.PLAIN_TEXT, fixDefaultContentType(inputContentType, inputFilename));

		inputContentType = null;
		assertEquals(ContentTypeUtils.PLAIN_TEXT, fixDefaultContentType(inputContentType, inputFilename));

		// should fix tab delimited files too
		inputFilename = "file.tab";
		assertEquals(WebConstants.TEXT_TAB_SEPARATED_VALUES, fixDefaultContentType(inputContentType, inputFilename));

		inputFilename = "file.tsv";
		assertEquals(WebConstants.TEXT_TAB_SEPARATED_VALUES, fixDefaultContentType(inputContentType, inputFilename));

		// should fix CSV files as well
		inputFilename = "file.CSV";
		assertEquals(WebConstants.TEXT_COMMA_SEPARATED_VALUES, fixDefaultContentType(inputContentType, inputFilename));

		// should fix text files as well
		inputFilename = "file.TXT";
		assertEquals(ContentTypeUtils.PLAIN_TEXT, fixDefaultContentType(inputContentType, inputFilename));

		// should workflow files
		assertEquals(ContentTypeUtils.PLAIN_TEXT, fixDefaultContentType(inputContentType, "test.wDL"));
		assertEquals(ContentTypeUtils.PLAIN_TEXT, fixDefaultContentType(inputContentType, "test.Cwl"));

		// test default
		inputContentType = null;
		inputFilename = "";
		assertEquals(ContentTypeUtils.APPLICATION_OCTET_STREAM, fixDefaultContentType(inputContentType, inputFilename));
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
