package org.sagebionetworks.web.unitclient.widget.entity.download;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.AutoGenFactory;
import org.sagebionetworks.repo.model.Data;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.repo.model.file.ChunkedFileToken;
import org.sagebionetworks.repo.model.file.State;
import org.sagebionetworks.repo.model.file.UploadDaemonStatus;
import org.sagebionetworks.repo.model.util.ContentTypeUtils;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.ClientLogger;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.ProgressCallback;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.callback.MD5Callback;
import org.sagebionetworks.web.client.events.CancelEvent;
import org.sagebionetworks.web.client.events.CancelHandler;
import org.sagebionetworks.web.client.events.WidgetDescriptorUpdatedEvent;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.JSONEntityFactory;
import org.sagebionetworks.web.client.transform.JSONEntityFactoryImpl;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.transform.NodeModelCreatorImpl;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.JiraURLHelper;
import org.sagebionetworks.web.client.widget.entity.download.Uploader;
import org.sagebionetworks.web.client.widget.entity.download.UploaderView;
import org.sagebionetworks.web.shared.EntityWrapper;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.xhr.client.XMLHttpRequest;


public class UploaderTest {
	
	UploaderView view;
	AuthenticationController authenticationController; 
	SynapseClientAsync synapseClient;
	JiraURLHelper jiraURLHelper;
	SynapseJSNIUtils synapseJsniUtils;
	ClientLogger mockLogger;
	
	// JSON utility components
	private static JSONObjectAdapter jsonObjectAdapter = new JSONObjectAdapterImpl();
	private static AdapterFactory adapterFactory = new AdapterFactoryImpl(); // alt: GwtAdapterFactory
	private static JSONEntityFactory jsonEntityFactory = new JSONEntityFactoryImpl(adapterFactory);
	private static NodeModelCreator nodeModelCreator = new NodeModelCreatorImpl(jsonEntityFactory, jsonObjectAdapter);
	

	AutoGenFactory autogenFactory;
	Uploader uploader;
	GWTWrapper gwt;
	FileEntity testEntity;
	CancelHandler cancelHandler;
	String parentEntityId;
	
	@Before
	public void before() throws Exception {
		view = mock(UploaderView.class);
		authenticationController = mock(AuthenticationController.class); 
		synapseClient=mock(SynapseClientAsync.class);
		jiraURLHelper=mock(JiraURLHelper.class);
		synapseJsniUtils=mock(SynapseJSNIUtils.class);
		autogenFactory=mock(AutoGenFactory.class);
		gwt = mock(GWTWrapper.class);
		mockLogger = mock(ClientLogger.class);
		AsyncMockStubber.callSuccessWith("syn123").when(synapseClient).createOrUpdateEntity(anyString(), anyString(), anyBoolean(), any(AsyncCallback.class));
		testEntity = new FileEntity();
		testEntity.setName("test file");
		testEntity.setId("syn99");
		EntityWrapper expectedEntityWrapper = new EntityWrapper(
				testEntity.writeToJSONObject(adapterFactory.createNew()).toJSONString(),
				FileEntity.class.getName());
		ChunkedFileToken token = new ChunkedFileToken();
		token.setFileName("testFile.txt");
		String tokenJson = token.writeToJSONObject(adapterFactory.createNew()).toJSONString();
		when(autogenFactory.newInstance(anyString())).thenReturn(testEntity);
		UserSessionData sessionData = new UserSessionData();
		sessionData.setProfile(new UserProfile());
		when(authenticationController.isLoggedIn()).thenReturn(true);
		when(authenticationController.getCurrentUserSessionData()).thenReturn(sessionData);
		
		//direct upload
		//by default, do not support direct upload (direct upload tests will turn on)
		when(synapseJsniUtils.isDirectUploadSupported()).thenReturn(false);
		when(synapseJsniUtils.getContentType(anyString(), anyInt())).thenReturn("image/png");
		AsyncMockStubber.callSuccessWith(tokenJson).when(synapseClient).getChunkedFileToken(anyString(), anyString(), anyString(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith("http://fakepresignedurl.uploader.test").when(synapseClient).getChunkedPresignedUrl(anyString(), any(AsyncCallback.class));
		UploadDaemonStatus status = new UploadDaemonStatus();
		status.setState(State.COMPLETED);
		status.setFileHandleId("fake handle");
		String completedUploadDaemonStatusJson = status.writeToJSONObject(adapterFactory.createNew()).toJSONString();
		AsyncMockStubber.callSuccessWith(completedUploadDaemonStatusJson).when(synapseClient).combineChunkedFileUpload(any(List.class), any(AsyncCallback.class));
		
		AsyncMockStubber.callSuccessWith("entityID").when(synapseClient).setFileEntityFileHandle(anyString(),  anyString(),  anyString(),  any(AsyncCallback.class));
		
		when(gwt.createXMLHttpRequest()).thenReturn(null);
		cancelHandler = mock(CancelHandler.class);
		
		String[] fileNames = {"newFile.txt"};
		when(synapseJsniUtils.getMultipleUploadFileNames(anyString())).thenReturn(fileNames);
		
		when(jiraURLHelper.createAccessRestrictionIssue(anyString(), anyString(), anyString())).thenReturn("http://fakeJiraRestrictionLink");
		AsyncMockStubber.callSuccessWith(expectedEntityWrapper).when(synapseClient).updateExternalFile(anyString(), anyString(),anyString(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(expectedEntityWrapper).when(synapseClient).createLockAccessRequirement(anyString(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(expectedEntityWrapper).when(synapseClient).updateExternalLocationable(anyString(), anyString(), anyString(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(expectedEntityWrapper).when(synapseClient).createExternalFile(anyString(), anyString(), anyString(), any(AsyncCallback.class));
		//by default, there is no name conflict
		AsyncMockStubber.callFailureWith(new NotFoundException()).when(synapseClient).getFileEntityIdWithSameName(anyString(), anyString(), any(AsyncCallback.class));
		uploader = new Uploader(view, nodeModelCreator,
				synapseClient,
				jsonObjectAdapter, synapseJsniUtils,
				gwt, authenticationController,
				mockLogger);
		uploader.addCancelHandler(cancelHandler);
		parentEntityId = "syn1234";
		uploader.asWidget(parentEntityId);
	}
	
	@Test
	public void testGetUploadActionUrlWithNull() {
		uploader.getDefaultUploadActionUrl();
		verify(gwt).getModuleBaseURL();
		
		//also check view reset
		verify(view).resetToInitialState();
	}
	
	@Test
	public void testGetUploadActionUrlWithFileEntity() {
		FileEntity fileEntity = new FileEntity();
		uploader.asWidget(fileEntity);
		uploader.getDefaultUploadActionUrl();
		verify(gwt).getModuleBaseURL();
	}
	
	@Test
	public void testGetUploadActionUrlWithData() {
		Data data = new Data();
		uploader.asWidget(data);
		uploader.getDefaultUploadActionUrl();
		verify(gwt).getModuleBaseURL();
	}
	
	@Test
	public void testSetNewExternalPath() throws Exception {
		//this is the full success test
		//if entity is null, it should call synapseClient.createExternalFile() to create the FileEntity and associate the path.
		uploader.setExternalFilePath("http://fakepath.url/blah.xml", "");
		verify(synapseClient).createExternalFile(anyString(), anyString(), anyString(), any(AsyncCallback.class));
		verify(view).showInfo(anyString(), anyString());
	}
	
	@Test
	public void testSetExternalPathFailedCreate() throws Exception {
		AsyncMockStubber.callFailureWith(new Exception("failed to create")).when(synapseClient).createExternalFile(anyString(), anyString(),anyString(), any(AsyncCallback.class));
		uploader.setExternalFilePath("http://fakepath.url/blah.xml", "");
		
		verify(view).showErrorMessage(anyString());
	}
	
	@Test
	public void testSetExternalPathFailedUpdateFile() throws Exception {
		AsyncMockStubber.callFailureWith(new Exception("failed to update path")).when(synapseClient).createExternalFile(anyString(), anyString(),anyString(), any(AsyncCallback.class));
		uploader.setExternalFilePath("http://fakepath.url/blah.xml", "");
		
		verify(view).showErrorMessage(anyString());
	}

	@Test
	public void testSetExternalFilePathNotAFileEntity() {
		//success setting external file path with a Locationable
		Data data = new Data();
		uploader.asWidget(data);
		uploader.setExternalFilePath("http://fakepath.url/blah.xml", "");
		verify(synapseClient).updateExternalLocationable(anyString(), anyString(), anyString(), any(AsyncCallback.class));
		verify(view).showInfo(anyString(), anyString());
	}
	
	@Test
	public void testSetExternalFileEntityPathWithFileEntity() throws Exception {
		uploader.asWidget(testEntity);
		uploader.setExternalFilePath("http://fakepath.url/blah.xml", "");
		verify(synapseClient).updateExternalFile(anyString(), anyString(),anyString(), any(AsyncCallback.class));
		verify(view).showInfo(anyString(), anyString());
	}

	@Test
	public void testDirectUploadHappyCase() throws Exception {
		uploader.setDirectUploadSupported(true);
		uploader.addCancelHandler(cancelHandler);
		
		final String file1 = "file1.txt";
		String[] fileNames = {file1};
		when(synapseJsniUtils.getMultipleUploadFileNames(anyString())).thenReturn(fileNames);
		uploader.handleUploads();
		verify(synapseJsniUtils).getFileMd5(anyString(), anyInt(), any(MD5Callback.class));
		
		uploader.directUploadStep3("newFile.txt", "plain/text", "6771718afc12275aa4e58b9bf3a49afe");
		verify(synapseClient).getChunkedFileToken(anyString(), anyString(), anyString(), any(AsyncCallback.class));
		verify(synapseClient).getChunkedPresignedUrl(anyString(), any(AsyncCallback.class));
		verify(synapseJsniUtils).uploadFileChunk(anyString(), anyInt(), anyString(), anyLong(), anyLong(), anyString(), any(XMLHttpRequest.class), any(ProgressCallback.class));
		//kick off what would happen after a successful upload
		uploader.directUploadStep5(null, 1);
		verify(synapseClient).combineChunkedFileUpload(any(List.class), any(AsyncCallback.class));
		verify(synapseClient).setFileEntityFileHandle(anyString(),  anyString(),  anyString(),  any(AsyncCallback.class));
		verify(view).hideLoading();
	}
	
	@Test
	public void testDirectUploadTeamIconHappyCase() throws Exception {
		when(synapseJsniUtils.isDirectUploadSupported()).thenReturn(true);
		CallbackP callback = mock(CallbackP.class);
		uploader.asWidget(null,  null, callback, false);
		uploader.handleUploads();
		uploader.directUploadStep3("newFile.txt", "plain/text", "6771718afc12275aa4e58b9bf3a49afe");
		uploader.directUploadStep5( null, 1);
		verify(callback).invoke(anyString());
	}
	
	private void verifyUploadError() {
		verify(view).showErrorMessage(anyString());
		verify(cancelHandler).onCancel(any(CancelEvent.class));
		verify(synapseJsniUtils).consoleError(anyString());
		verify(mockLogger).errorToRepositoryServices(anyString());
	}
	
	@Test
	public void testDirectUploadStep1Failure() throws Exception {
		when(synapseJsniUtils.isDirectUploadSupported()).thenReturn(true);
		AsyncMockStubber.callFailureWith(new IllegalArgumentException()).when(synapseClient).getFileEntityIdWithSameName(anyString(), anyString(), any(AsyncCallback.class));
		uploader.directUploadStep1("newFile.txt");
		verifyUploadError();
	}
	
	@Test
	public void testDirectUploadStep1SameNameFound() throws Exception {
		when(synapseJsniUtils.isDirectUploadSupported()).thenReturn(true);
		String duplicateNameEntityId = "syn007";
		AsyncMockStubber.callSuccessWith(duplicateNameEntityId).when(synapseClient).getFileEntityIdWithSameName(anyString(), anyString(), any(AsyncCallback.class));
		uploader.directUploadStep1("newFile.txt");
		verify(view).showConfirmDialog(anyString(), any(Callback.class), any(Callback.class));
	}
	
	@Test
	public void testDirectUploadStep1NoParentEntityId() throws Exception {
		when(synapseJsniUtils.isDirectUploadSupported()).thenReturn(true);
		uploader.asWidget(null, null, null, false);
		uploader.directUploadStep1("newFile.txt");
		verify(synapseClient, Mockito.never()).getFileEntityIdWithSameName(anyString(), anyString(), any(AsyncCallback.class));
		//should jump to step 2
		verify(synapseJsniUtils).getFileMd5(anyString(), anyInt(), any(MD5Callback.class));
	}
	
	@Test
	public void testDirectUploadStep3Failure() throws Exception {
		when(synapseJsniUtils.isDirectUploadSupported()).thenReturn(true);
		AsyncMockStubber.callFailureWith(new IllegalArgumentException()).when(synapseClient).getChunkedFileToken(anyString(), anyString(), anyString(), any(AsyncCallback.class));
		uploader.directUploadStep3("newFile.txt", "", "");
		verifyUploadError();
	}

	@Test
	public void testDirectUploadStep4Failure() throws Exception {
		when(synapseJsniUtils.isDirectUploadSupported()).thenReturn(true);
		AsyncMockStubber.callFailureWith(new IllegalArgumentException()).when(synapseClient).getChunkedPresignedUrl(anyString(), any(AsyncCallback.class));
		uploader.directUploadStep4("", 0, 0, 1, 12345, new ArrayList<String>());
		executeScheduledCallback();
		//should have called twice
		verify(synapseClient, Mockito.times(2)).getChunkedPresignedUrl(anyString(), any(AsyncCallback.class));
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
	
	@Test
	public void testDirectUploadStep4FailureFinalAttempt() throws Exception {
		when(synapseJsniUtils.isDirectUploadSupported()).thenReturn(true);
		AsyncMockStubber.callFailureWith(new IllegalArgumentException()).when(synapseClient).getChunkedPresignedUrl(anyString(), any(AsyncCallback.class));
		int attempt = Uploader.MAX_RETRY;
		uploader.directUploadStep4("", 0, attempt, 1, 12345, new ArrayList<String>());
		verifyUploadError();
	}
	
	@Test
	public void testDirectUploadStep5Failure() throws Exception {
		when(synapseJsniUtils.isDirectUploadSupported()).thenReturn(true);
		AsyncMockStubber.callFailureWith(new IllegalArgumentException()).when(synapseClient).combineChunkedFileUpload(any(List.class), any(AsyncCallback.class));
		uploader.handleUploads();
		//kick off what would happen after a successful upload
		uploader.directUploadStep5(null, 1);
		verifyUploadError();
	}
	
	@Test
	public void testDirectUploadStep5CompleteUploadFailure() throws Exception {
		when(synapseJsniUtils.isDirectUploadSupported()).thenReturn(true);
		AsyncMockStubber.callFailureWith(new IllegalArgumentException()).when(synapseClient).setFileEntityFileHandle(anyString(), anyString(), anyString(), any(AsyncCallback.class));
		uploader.handleUploads();
		//kick off what would happen after a successful upload
		uploader.directUploadStep5(null,1);
		verifyUploadError();
	}

	@Test
	public void testDirectUploadStep5Retry() throws Exception {
		//returned a failed status every time, and verify that we will eventually see an upload error (once the MAX_RETRY limit has been surpassed)
		UploadDaemonStatus status = new UploadDaemonStatus();
		status.setState(State.FAILED);
		status.setFileHandleId("fake handle");
		String failedUploadDaemonStatusJson = status.writeToJSONObject(adapterFactory.createNew()).toJSONString();
		AsyncMockStubber.callSuccessWith(failedUploadDaemonStatusJson).when(synapseClient).combineChunkedFileUpload(any(List.class), any(AsyncCallback.class));
		
		when(synapseJsniUtils.isDirectUploadSupported()).thenReturn(true);
		uploader.handleUploads();
		uploader.directUploadStep5(null,1);
		verifyUploadError();
	}
	
	@Test
	public void testByteRange() {
		//test chunk sizes
		Uploader.ByteRange range;
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
	
	@Test
	public void testChunkUploadSuccessWithMoreChunksToUpload() throws RestServiceException {
		//verify that request json is added to the list, and it calls step 2 (upload the next chunk) since there are more chunks to upload.
		List<String> requestList = new ArrayList<String>();
		uploader.chunkUploadSuccess("new request json", "content type",1, 2, 1024, requestList);
		assertTrue(requestList.size() == 1);
		//and it should try to get the url for the next chunk
		verify(synapseClient).getChunkedPresignedUrl(anyString(), any(AsyncCallback.class));
	}
	
	@Test
	public void testChunkUploadSuccessWithFinalChunk() throws RestServiceException {
		//verify that request json is added to the list, and it calls step 3 since the current chunk number is equal to the total chunk count
		List<String> requestList = new ArrayList<String>();
		String[] fileNames = {"arbitraryFileName"};
		uploader.setFileNames(fileNames);
		uploader.chunkUploadSuccess("new request json", "content type", 2, 2, 1024, requestList);
		assertTrue(requestList.size() == 1);
		//and it should try to get the url for the next chunk
		verify(synapseClient).combineChunkedFileUpload(any(List.class), any(AsyncCallback.class));
	}
	
	@Test
	public void testChunkUploadFailureFirstAttempt() throws RestServiceException, InterruptedException {
		List<String> requestList = new ArrayList<String>();
		int attempt = 1;
		uploader.chunkUploadFailure("content type",2, attempt, 2, 1024, requestList, "");
		executeScheduledCallback();
		verify(synapseClient).getChunkedPresignedUrl(anyString(), any(AsyncCallback.class));
	}
	
	@Test
	public void testChunkUploadFailureFinalAttempt() throws RestServiceException {
		List<String> requestList = new ArrayList<String>();
		int attempt = Uploader.MAX_RETRY;
		uploader.chunkUploadFailure("content type",2, attempt, 2, 1024, requestList, "");
		verifyUploadError();
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
	}
	
	@Test
	public void testChunkCount() {
		//see SWC-1436
		assertEquals(2L, uploader.getChunkCount(8404992L));
	}
	
	@Test
	public void testMultipleFileUploads() throws Exception {
		when(synapseJsniUtils.isDirectUploadSupported()).thenReturn(true);
		uploader.setDirectUploadSupported(true);
		
		final String file1 = "file1.txt";
		final String file2 = "file2.txt";
		final String file3 = "file3.txt";
		String[] fileNames = {file1, file2, file3};
		when(synapseJsniUtils.getMultipleUploadFileNames(anyString())).thenReturn(fileNames);
		
		uploader.handleUploads();
		
		verify(synapseClient).getFileEntityIdWithSameName(eq(file1), eq(parentEntityId), any(AsyncCallback.class));
		verify(synapseJsniUtils).getFileMd5(anyString(), anyInt(), any(MD5Callback.class));
		
		uploader.directUploadStep3(file1, "plain/text", "6771718afc12275aa4e58b9bf3a49afe");
		verify(synapseClient).getChunkedFileToken(anyString(), anyString(), anyString(), any(AsyncCallback.class));
		verify(synapseClient).getChunkedPresignedUrl(anyString(), any(AsyncCallback.class));
		
		uploader.directUploadStep5(null, 1);
		
		// triggers file2 to upload.
		verify(synapseClient).getFileEntityIdWithSameName(eq(file2), eq(parentEntityId), any(AsyncCallback.class));
		
		uploader.directUploadStep3(file2, "plain/text", "6771718afc12275aa4e58b9bf3a49afe");
		uploader.directUploadStep5(null, 1);
		
		// triggers file3 to upload
		verify(synapseClient).getFileEntityIdWithSameName(eq(file3), eq(parentEntityId), any(AsyncCallback.class));
		
		uploader.directUploadStep3(file3, "plain/text", "6771718afc12275aa4e58b9bf3a49afe");
		uploader.directUploadStep5(null, 1);
	}
}
