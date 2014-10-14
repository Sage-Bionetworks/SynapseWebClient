package org.sagebionetworks.web.unitclient.widget.entity.download;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import org.sagebionetworks.repo.model.file.ChunkRequest;
import org.sagebionetworks.repo.model.file.ChunkedFileToken;
import org.sagebionetworks.repo.model.file.State;
import org.sagebionetworks.repo.model.file.UploadDaemonStatus;
import org.sagebionetworks.repo.model.file.UploadDestination;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.ClientLogger;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.callback.MD5Callback;
import org.sagebionetworks.web.client.events.CancelEvent;
import org.sagebionetworks.web.client.events.CancelHandler;
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
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import org.sagebionetworks.web.unitclient.widget.upload.MultipartUploaderStub;

import com.google.gwt.user.client.rpc.AsyncCallback;


public class UploaderTest {
	
	MultipartUploaderStub multipartUploader;
	UploaderView view;
	AuthenticationController authenticationController; 
	SynapseClientAsync synapseClient;
	JiraURLHelper jiraURLHelper;
	SynapseJSNIUtils synapseJsniUtils;
	ClientLogger mockLogger;
	GlobalApplicationState mockGlobalApplicationState;
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
		multipartUploader = new MultipartUploaderStub();
		view = mock(UploaderView.class);
		authenticationController = mock(AuthenticationController.class); 
		synapseClient=mock(SynapseClientAsync.class);
		jiraURLHelper=mock(JiraURLHelper.class);
		synapseJsniUtils=mock(SynapseJSNIUtils.class);
		autogenFactory=mock(AutoGenFactory.class);
		gwt = mock(GWTWrapper.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
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
		
		when(synapseJsniUtils.getContentType(anyString(), anyInt())).thenReturn("image/png");
		AsyncMockStubber.callSuccessWith(tokenJson).when(synapseClient).getChunkedFileToken(anyString(), anyString(), anyString(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith("http://fakepresignedurl.uploader.test").when(synapseClient).getChunkedPresignedUrl(any(ChunkRequest.class), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(null).when(synapseClient).getUploadDestinations(anyString(), any(AsyncCallback.class));
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
				synapseJsniUtils,
				gwt, authenticationController, multipartUploader, mockGlobalApplicationState);
		uploader.addCancelHandler(cancelHandler);
		parentEntityId = "syn1234";
		uploader.asWidget(parentEntityId);
		
		// Simulate success.
		multipartUploader.setFileHandle("99999");
	}
	
	@Test
	public void testGetUploadActionUrlWithNull() {
		uploader.getOldUploadUrl();
		verify(gwt).getModuleBaseURL();
		
		//also check view reset
		verify(view).resetToInitialState();
	}
	
	@Test
	public void testGetUploadActionUrlWithFileEntity() {
		FileEntity fileEntity = new FileEntity();
		uploader.asWidget(fileEntity);
		uploader.getOldUploadUrl();
		verify(gwt).getModuleBaseURL();
	}
	
	@Test
	public void testGetUploadActionUrlWithData() {
		Data data = new Data();
		uploader.asWidget(data);
		uploader.getOldUploadUrl();
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
		uploader.addCancelHandler(cancelHandler);
		
		final String file1 = "file1.txt";
		String[] fileNames = {file1};
		when(synapseJsniUtils.getMultipleUploadFileNames(anyString())).thenReturn(fileNames);
		uploader.handleUploads();
		verify(synapseClient).setFileEntityFileHandle(anyString(),  anyString(),  anyString(),  any(AsyncCallback.class));
		verify(view).hideLoading();
	}
	
	@Test
	public void testDirectUploadTeamIconHappyCase() throws Exception {
		CallbackP callback = mock(CallbackP.class);
		uploader.asWidget(null,  null, callback, false);
		uploader.handleUploads();
		verify(callback).invoke(anyString());
	}
	
	private void verifyUploadError() {
		verify(view).showErrorMessage(anyString());
		verify(cancelHandler).onCancel(any(CancelEvent.class));
	}
	
	@Test
	public void testDirectUploadStep1Failure() throws Exception {
		AsyncMockStubber.callFailureWith(new IllegalArgumentException()).when(synapseClient).getFileEntityIdWithSameName(anyString(), anyString(), any(AsyncCallback.class));
		uploader.directUploadStep1("newFile.txt");
		verifyUploadError();
	}
	
	@Test
	public void testDirectUploadStep1SameNameFound() throws Exception {
		String duplicateNameEntityId = "syn007";
		AsyncMockStubber.callSuccessWith(duplicateNameEntityId).when(synapseClient).getFileEntityIdWithSameName(anyString(), anyString(), any(AsyncCallback.class));
		uploader.directUploadStep1("newFile.txt");
		verify(view).showConfirmDialog(anyString(), any(Callback.class), any(Callback.class));
	}
	
	@Test
	public void testDirectUploadStep1NoParentEntityId() throws Exception {
		uploader.asWidget(null, null, null, false);
		uploader.directUploadStep1("newFile.txt");
		verify(synapseClient, Mockito.never()).getFileEntityIdWithSameName(anyString(), anyString(), any(AsyncCallback.class));
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
	public void testDirectUploadFailure() throws Exception {
		multipartUploader.setError("Something went wrong");
		uploader.handleUploads();
		verifyUploadError();
	}
	
	
	@Test
	public void testMultipleFileUploads() throws Exception {
		final String file1 = "file1.txt";
		final String file2 = "file2.txt";
		final String file3 = "file3.txt";
		String[] fileNames = {file1, file2, file3};
		when(synapseJsniUtils.getMultipleUploadFileNames(anyString())).thenReturn(fileNames);
		
		uploader.handleUploads();
		
		verify(synapseClient).getFileEntityIdWithSameName(eq(file1), eq(parentEntityId), any(AsyncCallback.class));
		
		// triggers file2 to upload.
		verify(synapseClient).getFileEntityIdWithSameName(eq(file2), eq(parentEntityId), any(AsyncCallback.class));
		
		// triggers file3 to upload
		verify(synapseClient).getFileEntityIdWithSameName(eq(file3), eq(parentEntityId), any(AsyncCallback.class));
		
	}
	
	@Test
	public void testCalculatePercentOverAllFiles(){
		double tollerance = 0.01;;
		int numberOfFiles = 3;
		int currentIndex = 0;
		assertEquals(0.166, Uploader.calculatePercentOverAllFiles(numberOfFiles, currentIndex, 0.50), tollerance);
		currentIndex = 1;
		assertEquals(0.50, Uploader.calculatePercentOverAllFiles(numberOfFiles, currentIndex, 0.50), tollerance);
		currentIndex = 2;
		assertEquals(0.833, Uploader.calculatePercentOverAllFiles(numberOfFiles, currentIndex, 0.50), tollerance);
	}
}
