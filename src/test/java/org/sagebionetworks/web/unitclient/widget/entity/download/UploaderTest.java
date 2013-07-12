package org.sagebionetworks.web.unitclient.widget.entity.download;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
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
import org.sagebionetworks.web.client.EntityTypeProvider;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.ProgressCallback;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.events.CancelEvent;
import org.sagebionetworks.web.client.events.CancelHandler;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.JSONEntityFactory;
import org.sagebionetworks.web.client.transform.JSONEntityFactoryImpl;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.transform.NodeModelCreatorImpl;
import org.sagebionetworks.web.client.widget.entity.JiraURLHelper;
import org.sagebionetworks.web.client.widget.entity.download.Uploader;
import org.sagebionetworks.web.client.widget.entity.download.UploaderView;
import org.sagebionetworks.web.shared.EntityWrapper;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.xhr.client.XMLHttpRequest;


public class UploaderTest {
	
	UploaderView view;
	AuthenticationController authenticationController; 
	EntityTypeProvider entityTypeProvider;
	SynapseClientAsync synapseClient;
	JiraURLHelper jiraURLHelper;
	SynapseJSNIUtils synapseJsniUtils;
	
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
	
	@Before
	public void before() throws Exception {
		view = mock(UploaderView.class);
		authenticationController = mock(AuthenticationController.class); 
		entityTypeProvider=mock(EntityTypeProvider.class);
		synapseClient=mock(SynapseClientAsync.class);
		jiraURLHelper=mock(JiraURLHelper.class);
		synapseJsniUtils=mock(SynapseJSNIUtils.class);
		autogenFactory=mock(AutoGenFactory.class);
		gwt = mock(GWTWrapper.class);
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
		when(synapseJsniUtils.getContentType(anyString())).thenReturn("image/png");
		AsyncMockStubber.callSuccessWith(tokenJson).when(synapseClient).getChunkedFileToken(anyString(), anyString(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith("http://fakepresignedurl.uploader.test").when(synapseClient).getChunkedPresignedUrl(anyString(), any(AsyncCallback.class));
		UploadDaemonStatus status = new UploadDaemonStatus();
		status.setState(State.COMPLETED);
		status.setFileHandleId("fake handle");
		String completedUploadDaemonStatusJson = status.writeToJSONObject(adapterFactory.createNew()).toJSONString();
		AsyncMockStubber.callSuccessWith(completedUploadDaemonStatusJson).when(synapseClient).combineChunkedFileUpload(any(List.class), any(AsyncCallback.class));
		
		AsyncMockStubber.callSuccessWith("entityID").when(synapseClient).completeUpload(anyString(),  anyString(),  anyString(),  anyBoolean(),  any(AsyncCallback.class));
		
		when(gwt.createXMLHttpRequest()).thenReturn(null);
		cancelHandler = mock(CancelHandler.class);
		
		when(jiraURLHelper.createAccessRestrictionIssue(anyString(), anyString(), anyString())).thenReturn("http://fakeJiraRestrictionLink");
		AsyncMockStubber.callSuccessWith(expectedEntityWrapper).when(synapseClient).updateExternalFile(anyString(), anyString(),anyString(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(expectedEntityWrapper).when(synapseClient).createLockAccessRequirement(anyString(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(expectedEntityWrapper).when(synapseClient).updateExternalLocationable(anyString(), anyString(), anyString(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(expectedEntityWrapper).when(synapseClient).createExternalFile(anyString(), anyString(), anyString(), any(AsyncCallback.class));
		uploader = new Uploader(view, nodeModelCreator,
				authenticationController, entityTypeProvider, synapseClient,
				jiraURLHelper, jsonObjectAdapter, synapseJsniUtils,
				adapterFactory, autogenFactory, gwt);
		uploader.addCancelHandler(cancelHandler);
	}
	
	@Test
	public void testGetUploadActionUrlWithNull() {
		String parentEntityId = "syn1234";
		uploader.asWidget(parentEntityId, null);
		
		uploader.getDefaultUploadActionUrl(true);
		verify(synapseJsniUtils).getBaseFileHandleUrl();
	}
	
	@Test
	public void testGetUploadActionUrlWithFileEntity() {
		FileEntity fileEntity = new FileEntity();
		uploader.asWidget(fileEntity, null);
		uploader.getDefaultUploadActionUrl(true);
		verify(synapseJsniUtils).getBaseFileHandleUrl();
	}
	
	@Test
	public void testGetUploadActionUrlWithData() {
		Data data = new Data();
		uploader.asWidget(data, null);
		uploader.getDefaultUploadActionUrl(true);
		verify(gwt).getModuleBaseURL();
	}
	
	@Test
	public void testSetNewExternalPath() throws Exception {
		//this is the full success test
		//if entity is null, it should call synapseClient.createExternalFile() to create the FileEntity and associate the path.
		String parentEntityId = "syn1234";
		uploader.asWidget(parentEntityId, null);
		uploader.setExternalFilePath("http://fakepath.url/blah.xml", "", true);
		verify(synapseClient).createExternalFile(anyString(), anyString(), anyString(), any(AsyncCallback.class));
		verify(synapseClient).createLockAccessRequirement(anyString(), any(AsyncCallback.class));
		verify(view).showInfo(anyString(), anyString());
	}
	
	@Test
	public void testSetExternalPathFailedCreate() throws Exception {
		AsyncMockStubber.callFailureWith(new Exception("failed to create")).when(synapseClient).createExternalFile(anyString(), anyString(),anyString(), any(AsyncCallback.class));

		String parentEntityId = "syn1234";
		uploader.asWidget(parentEntityId, null);
		uploader.setExternalFilePath("http://fakepath.url/blah.xml", "", true);
		
		verify(view).showErrorMessage(anyString());
	}
	
	@Test
	public void testSetExternalPathFailedUpdateFile() throws Exception {
		AsyncMockStubber.callFailureWith(new Exception("failed to update path")).when(synapseClient).createExternalFile(anyString(), anyString(),anyString(), any(AsyncCallback.class));

		String parentEntityId = "syn1234";
		uploader.asWidget(parentEntityId, null);
		uploader.setExternalFilePath("http://fakepath.url/blah.xml", "", true);
		
		verify(view).showErrorMessage(anyString());
	}

	@Test
	public void testSetExternalPathFailedCreateAccessRequirement() throws Exception {
		AsyncMockStubber.callFailureWith(new Exception("failed to update path")).when(synapseClient).createLockAccessRequirement(anyString(), any(AsyncCallback.class));

		String parentEntityId = "syn1234";
		uploader.asWidget(parentEntityId, null);
		uploader.setExternalFilePath("http://fakepath.url/blah.xml", "", true);
		
		verify(view).showErrorMessage(anyString());
	}

	
	@Test
	public void testSetExternalFilePathNotAFileEntity() {
		//success setting external file path with a Locationable
		Data data = new Data();
		uploader.asWidget(data, null);
		uploader.setExternalFilePath("http://fakepath.url/blah.xml", "", true);
		verify(synapseClient).updateExternalLocationable(anyString(), anyString(), anyString(), any(AsyncCallback.class));
		verify(synapseClient).createLockAccessRequirement(anyString(), any(AsyncCallback.class));
		verify(view).showInfo(anyString(), anyString());
	}
	
	@Test
	public void testSetExternalFileEntityPathWithFileEntity() throws Exception {
		uploader.asWidget(testEntity, null);
		uploader.setExternalFilePath("http://fakepath.url/blah.xml", "", true);
		verify(synapseClient).updateExternalFile(anyString(), anyString(),anyString(), any(AsyncCallback.class));
		verify(synapseClient).createLockAccessRequirement(anyString(), any(AsyncCallback.class));
		verify(view).showInfo(anyString(), anyString());
	}

	@Test
	public void testDirectUploadHappyCase() throws Exception {
		when(synapseJsniUtils.isDirectUploadSupported()).thenReturn(true);
		uploader.handleUpload("newFile.txt");
		verify(synapseClient).getChunkedFileToken(anyString(), anyString(), any(AsyncCallback.class));
		verify(synapseClient).getChunkedPresignedUrl(anyString(), any(AsyncCallback.class));
		verify(synapseJsniUtils).uploadFileChunk(anyString(), anyString(), anyInt(), anyInt(), anyString(), any(XMLHttpRequest.class), any(ProgressCallback.class));
		//kick off what would happen after a successful upload
		uploader.directUploadStep3(false, null);
		verify(synapseClient).combineChunkedFileUpload(any(List.class), any(AsyncCallback.class));
		verify(synapseClient).completeUpload(anyString(),  anyString(),  anyString(),  anyBoolean(),  any(AsyncCallback.class));
		verify(view).hideLoading();
	}
	
	private void verifyUploadError() {
		verify(view).showErrorMessage(anyString());
		verify(cancelHandler).onCancel(any(CancelEvent.class));
	}
	
	@Test
	public void testDirectUploadStep1Failure() throws Exception {
		when(synapseJsniUtils.isDirectUploadSupported()).thenReturn(true);
		AsyncMockStubber.callFailureWith(new IllegalArgumentException()).when(synapseClient).getChunkedFileToken(anyString(), anyString(), any(AsyncCallback.class));
		uploader.handleUpload("newFile.txt");
		verifyUploadError();
	}

	@Test
	public void testDirectUploadStep2Failure() throws Exception {
		when(synapseJsniUtils.isDirectUploadSupported()).thenReturn(true);
		AsyncMockStubber.callFailureWith(new IllegalArgumentException()).when(synapseClient).getChunkedPresignedUrl(anyString(), any(AsyncCallback.class));
		uploader.handleUpload("newFile.txt");
		verifyUploadError();
	}

	@Test
	public void testDirectUploadStep3Failure() throws Exception {
		when(synapseJsniUtils.isDirectUploadSupported()).thenReturn(true);
		AsyncMockStubber.callFailureWith(new IllegalArgumentException()).when(synapseClient).combineChunkedFileUpload(any(List.class), any(AsyncCallback.class));
		uploader.handleUpload("newFile.txt");
		//kick off what would happen after a successful upload
		uploader.directUploadStep3(false, null);
		verifyUploadError();
	}
	
	@Test
	public void testDirectUploadStep3CompleteUploadFailure() throws Exception {
		when(synapseJsniUtils.isDirectUploadSupported()).thenReturn(true);
		AsyncMockStubber.callFailureWith(new IllegalArgumentException()).when(synapseClient).completeUpload(anyString(), anyString(), anyString(), anyBoolean(), any(AsyncCallback.class));
		uploader.handleUpload("newFile.txt");
		//kick off what would happen after a successful upload
		uploader.directUploadStep3(false, null);
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
		uploader.chunkUploadSuccess("new request json", "content type",2, 2, 1024, requestList);
		assertTrue(requestList.size() == 1);
		//and it should try to get the url for the next chunk
		verify(synapseClient).combineChunkedFileUpload(any(List.class), any(AsyncCallback.class));
	}
	
	@Test
	public void testChunkUploadFailureFirstAttempt() throws RestServiceException {
		List<String> requestList = new ArrayList<String>();
		int attempt = 1;
		uploader.chunkUploadFailure("content type",2, attempt, 2, 1024, requestList);
		//and it should retry to get the url for this chunk
		verify(synapseClient).getChunkedPresignedUrl(anyString(), any(AsyncCallback.class));
	}
	
	@Test
	public void testChunkUploadFailureFinalAttempt() throws RestServiceException {
		List<String> requestList = new ArrayList<String>();
		int attempt = Uploader.MAX_RETRY;
		uploader.chunkUploadFailure("content type",2, attempt, 2, 1024, requestList);
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
}
