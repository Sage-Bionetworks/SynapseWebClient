package org.sagebionetworks.web.unitclient.widget.entity.download;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sagebionetworks.repo.model.Annotations;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.repo.model.attachment.UploadResult;
import org.sagebionetworks.repo.model.attachment.UploadStatus;
import org.sagebionetworks.repo.model.file.ChunkRequest;
import org.sagebionetworks.repo.model.file.ChunkedFileToken;
import org.sagebionetworks.repo.model.file.ExternalS3UploadDestination;
import org.sagebionetworks.repo.model.file.ExternalUploadDestination;
import org.sagebionetworks.repo.model.file.S3UploadDestination;
import org.sagebionetworks.repo.model.file.State;
import org.sagebionetworks.repo.model.file.UploadDaemonStatus;
import org.sagebionetworks.repo.model.file.UploadDestination;
import org.sagebionetworks.repo.model.file.UploadType;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.ClientLogger;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.callback.MD5Callback;
import org.sagebionetworks.web.client.events.CancelEvent;
import org.sagebionetworks.web.client.events.CancelHandler;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.JiraURLHelper;
import org.sagebionetworks.web.client.widget.entity.download.Uploader;
import org.sagebionetworks.web.client.widget.entity.download.UploaderView;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
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
	private static AdapterFactory adapterFactory = new AdapterFactoryImpl(); // alt: GwtAdapterFactory
	
	Uploader uploader;
	GWTWrapper gwt;
	FileEntity testEntity;
	CancelHandler cancelHandler;
	String parentEntityId;
	private Long storageLocationId;
	String md5 = "e10e3f4491440ce7b48edc97f03307bb";
	@Before
	public void before() throws Exception {
		multipartUploader = new MultipartUploaderStub();
		view = mock(UploaderView.class);
		authenticationController = mock(AuthenticationController.class); 
		synapseClient=mock(SynapseClientAsync.class);
		jiraURLHelper=mock(JiraURLHelper.class);
		synapseJsniUtils=mock(SynapseJSNIUtils.class);
		gwt = mock(GWTWrapper.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockLogger = mock(ClientLogger.class);
		AsyncMockStubber.callSuccessWith("syn123").when(synapseClient).createOrUpdateEntity(any(Entity.class), any(Annotations.class), anyBoolean(), any(AsyncCallback.class));
		testEntity = new FileEntity();
		testEntity.setName("test file");
		testEntity.setId("syn99");
		ChunkedFileToken token = new ChunkedFileToken();
		token.setFileName("testFile.txt");
		String tokenJson = token.writeToJSONObject(adapterFactory.createNew()).toJSONString();
		UserSessionData sessionData = new UserSessionData();
		sessionData.setProfile(new UserProfile());
		when(authenticationController.isLoggedIn()).thenReturn(true);
		when(authenticationController.getCurrentUserSessionData()).thenReturn(sessionData);
		
		when(synapseJsniUtils.getContentType(anyString(), anyInt())).thenReturn("image/png");
		AsyncMockStubber.callSuccessWith(tokenJson).when(synapseClient).getChunkedFileToken(anyString(), anyString(), anyString(), anyLong(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith("http://fakepresignedurl.uploader.test").when(synapseClient).getChunkedPresignedUrl(any(ChunkRequest.class), any(AsyncCallback.class));
		
		S3UploadDestination d = new S3UploadDestination();
		d.setUploadType(UploadType.S3);
		List<UploadDestination> destinations = new ArrayList<UploadDestination>();
		destinations.add(d);
		AsyncMockStubber.callSuccessWith(destinations).when(synapseClient).getUploadDestinations(anyString(), any(AsyncCallback.class));
		
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
		AsyncMockStubber.callSuccessWith(testEntity).when(synapseClient).updateExternalFile(anyString(), anyString(), anyLong(), anyString(), anyLong(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(testEntity).when(synapseClient).createLockAccessRequirement(anyString(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(testEntity).when(synapseClient).createExternalFile(anyString(), anyString(), anyString(), anyLong(), anyString(), anyLong(), any(AsyncCallback.class));
		//by default, there is no name conflict
		AsyncMockStubber.callFailureWith(new NotFoundException()).when(synapseClient).getFileEntityIdWithSameName(anyString(), anyString(), any(AsyncCallback.class));
		uploader = new Uploader(view,
				synapseClient,
				synapseJsniUtils,
				gwt, authenticationController, multipartUploader, mockGlobalApplicationState, mockLogger);
		uploader.addCancelHandler(cancelHandler);
		parentEntityId = "syn1234";
		uploader.asWidget(parentEntityId);
		
		// Simulate success.
		multipartUploader.setFileHandle("99999");
		
		when(synapseJsniUtils.getFileSize(anyString(), anyInt())).thenReturn(1.0);
		when(synapseJsniUtils.isFileAPISupported()).thenReturn(true);
		storageLocationId = 9090L;
		
		// Stub the generation of a MD5.
		doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
                final Object[] args = invocation.getArguments();
                ((MD5Callback) args[args.length - 1]).setMD5(md5);
				return null;
			}
		}).when(synapseJsniUtils).getFileMd5(anyString(), anyInt(), any(MD5Callback.class));
	}
	
	@Test
	public void testSetNewExternalPath() throws Exception {
		//this is the full success test
		//if entity is null, it should call synapseClient.createExternalFile() to create the FileEntity and associate the path.
		uploader.setExternalFilePath("http://fakepath.url/blah.xml", "", storageLocationId);
		verify(synapseClient).createExternalFile(anyString(), anyString(), anyString(), eq((Long)null), eq((String)null), eq(storageLocationId), any(AsyncCallback.class));
		verify(view).showInfo(anyString(), anyString());
	}
	
	@Test
	public void testSetExternalPathFailedCreate() throws Exception {
		AsyncMockStubber.callFailureWith(new Exception("failed to create")).when(synapseClient).createExternalFile(anyString(), anyString(),anyString(), eq((Long)null), eq((String)null), anyLong(), any(AsyncCallback.class));
		uploader.setExternalFilePath("http://fakepath.url/blah.xml", "", storageLocationId);
		verify(view).showErrorMessage(anyString());
	}
	
	@Test
	public void testSetExternalPathFailedUpdateFile() throws Exception {
		AsyncMockStubber.callFailureWith(new Exception("failed to update path")).when(synapseClient).createExternalFile(anyString(), anyString(),anyString(), eq((Long)null), eq((String)null), anyLong(), any(AsyncCallback.class));
		uploader.setExternalFilePath("http://fakepath.url/blah.xml", "", storageLocationId);
		verify(view).showErrorMessage(anyString());
	}
	
	@Test
	public void testSetExternalFileEntityPathWithFileEntity() throws Exception {
		uploader.asWidget(testEntity);
		uploader.setExternalFilePath("http://fakepath.url/blah.xml", "", storageLocationId);
		verify(synapseClient).updateExternalFile(anyString(), anyString(),eq((Long)null), eq((String)null), eq(storageLocationId), any(AsyncCallback.class));
		verify(view).showInfo(anyString(), anyString());
	}

	

	@Test
	public void testSetNewSftpExternalPath() throws Exception {
		//this is the full success test
		//if entity is null, it should call synapseClient.createExternalFile() to create the FileEntity and associate the path.
		uploader.setFileNames(new String[] {"test.txt"});
		uploader.setSftpExternalFilePath("http://fakepath.url/blah.xml", storageLocationId);
		verify(synapseClient).createExternalFile(anyString(), anyString(), anyString(), anyLong(), eq(md5), eq(storageLocationId), any(AsyncCallback.class));
		verify(view).showInfo(anyString(), anyString());
	}
	
	@Test
	public void testSetSftpExternalPathFailedCreate() throws Exception {
		uploader.setFileNames(new String[] {"test.txt"});
		AsyncMockStubber.callFailureWith(new Exception("failed to create")).when(synapseClient).createExternalFile(anyString(), anyString(),anyString(), anyLong(), eq(md5), anyLong(), any(AsyncCallback.class));
		uploader.setSftpExternalFilePath("http://fakepath.url/blah.xml", storageLocationId);
		verify(view).showErrorMessage(anyString());
	}
	
	@Test
	public void testSetSftpExternalPathFailedUpdateFile() throws Exception {
		uploader.setFileNames(new String[] {"test.txt"});
		AsyncMockStubber.callFailureWith(new Exception("failed to update path")).when(synapseClient).createExternalFile(anyString(), anyString(),anyString(), anyLong(), eq(md5), anyLong(), any(AsyncCallback.class));
		uploader.setSftpExternalFilePath("http://fakepath.url/blah.xml", storageLocationId);
		verify(view).showErrorMessage(anyString());
	}
	
	@Test
	public void testSetSftpExternalFileEntityPathWithFileEntity() throws Exception {
		uploader.asWidget(testEntity);
		uploader.setSftpExternalFilePath("http://fakepath.url/blah.xml", storageLocationId);
		verify(synapseClient).updateExternalFile(anyString(), anyString(),anyLong(), eq(md5), eq(storageLocationId), any(AsyncCallback.class));
		verify(view).showInfo(anyString(), anyString());
	}
	
	@Test
	public void testDirectUploadHappyCase() throws Exception {
		uploader.addCancelHandler(cancelHandler);
		verify(view).showUploadingToSynapseStorage();
		verify(view).enableMultipleFileUploads(true);
		final String file1 = "file1.txt";
		String[] fileNames = {file1};
		when(synapseJsniUtils.getMultipleUploadFileNames(anyString())).thenReturn(fileNames);
		uploader.handleUploads();
		verify(synapseClient).setFileEntityFileHandle(anyString(),  anyString(),  anyString(),  any(AsyncCallback.class));
		verify(view).hideLoading();
		assertEquals(UploadType.S3, uploader.getCurrentUploadType());
	}
	
	@Test
	public void testUpdateS3UploadBannerViewNull() throws Exception {
		reset(view);
		uploader.updateS3UploadBannerView(null);
		verify(view).showUploadingToSynapseStorage();
	}
	@Test
	public void testUpdateS3UploadBannerViewEmpty() throws Exception {
		reset(view);
		uploader.updateS3UploadBannerView("");
		verify(view).showUploadingToSynapseStorage();
	}
	@Test
	public void testUpdateS3UploadBannerViewSet() throws Exception {
		reset(view);
		String banner = "this is my test banner";
		uploader.updateS3UploadBannerView(banner);
		verify(view).showUploadingBanner(banner);
	}
	
	@Test
	public void testDirectUploadNoFilesSelected() throws Exception {
		uploader.setFileNames(null);
		when(synapseJsniUtils.getMultipleUploadFileNames(anyString())).thenReturn(null);
		uploader.handleUploads();
		verify(view).hideLoading();
		verify(view).showErrorMessage(DisplayConstants.NO_FILES_SELECTED_FOR_UPLOAD_MESSAGE);
		verify(view).enableUpload();
		
		assertEquals(UploadType.S3, uploader.getCurrentUploadType());
	}
	
	@Test
	public void testDirectUploadTeamIconHappyCase() throws Exception {
		CallbackP callback = mock(CallbackP.class);
		uploader.asWidget(null,  null, callback, false);
		uploader.handleUploads();
		verify(callback).invoke(anyString());
	}
	
	private void verifyUploadError() {
		verify(view).showErrorMessage(anyString(), anyString());
		verify(cancelHandler).onCancel(any(CancelEvent.class));
		verify(mockLogger).errorToRepositoryServices(anyString(), any(Throwable.class));
	}
	
	@Test
	public void testDirectUploadStep1Failure() throws Exception {
		Callback mockCallback = mock(Callback.class);
		AsyncMockStubber.callFailureWith(new IllegalArgumentException()).when(synapseClient).getFileEntityIdWithSameName(anyString(), anyString(), any(AsyncCallback.class));
		uploader.checkForExistingFileName("newFile.txt", mockCallback);
		verifyUploadError();
		verifyZeroInteractions(mockCallback);
	}
	
	@Test
	public void testDirectUploadStep1SameNameFound() throws Exception {
		Callback mockCallback = mock(Callback.class);
		String duplicateNameEntityId = "syn007";
		AsyncMockStubber.callSuccessWith(duplicateNameEntityId).when(synapseClient).getFileEntityIdWithSameName(anyString(), anyString(), any(AsyncCallback.class));
		uploader.checkForExistingFileName("newFile.txt", mockCallback);
		verify(view).showConfirmDialog(anyString(), any(Callback.class), any(Callback.class));
		verifyZeroInteractions(mockCallback);
	}
	
	@Test
	public void testDirectUploadStep1NoParentEntityId() throws Exception {
		Callback mockCallback = mock(Callback.class);
		uploader.asWidget(null, null, null, false);
		uploader.checkForExistingFileName("newFile.txt", mockCallback);
		verify(synapseClient, Mockito.never()).getFileEntityIdWithSameName(anyString(), anyString(), any(AsyncCallback.class));
		verify(mockCallback).invoke();
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
	
	@Test
	public void testUploadToInvalidExternalHTTPS() {
		ExternalUploadDestination d = new ExternalUploadDestination();
		d.setUploadType(UploadType.HTTPS);
		List<UploadDestination> destinations = new ArrayList<UploadDestination>();
		destinations.add(d);
		AsyncMockStubber.callSuccessWith(destinations).when(synapseClient).getUploadDestinations(anyString(), any(AsyncCallback.class));
		uploader.queryForUploadDestination();
		assertNull(uploader.getStorageLocationId());
		verifyUploadError();
	}

	@Test
	public void testUploadToInvalidExternalS3() {
		ExternalUploadDestination d = new ExternalUploadDestination();
		d.setUploadType(UploadType.S3);
		List<UploadDestination> destinations = new ArrayList<UploadDestination>();
		destinations.add(d);
		AsyncMockStubber.callSuccessWith(destinations).when(synapseClient).getUploadDestinations(anyString(), any(AsyncCallback.class));
		uploader.queryForUploadDestination();
		assertNull(uploader.getStorageLocationId());
		verifyUploadError();
	}

	@Test
	public void testUploadToValidExternalS3() {
		ExternalS3UploadDestination d = new ExternalS3UploadDestination();
		d.setUploadType(UploadType.S3);
		d.setStorageLocationId(storageLocationId);
		List<UploadDestination> destinations = new ArrayList<UploadDestination>();
		destinations.add(d);
		AsyncMockStubber.callSuccessWith(destinations).when(synapseClient).getUploadDestinations(anyString(), any(AsyncCallback.class));
		uploader.queryForUploadDestination();
		assertEquals(uploader.getStorageLocationId(), storageLocationId);
	}

	
	@Test
	public void testInvalidUploadDestination() {
		//add an invalid upload destination
		List<UploadDestination> destinations = new ArrayList<UploadDestination>();
		destinations.add(mock(UploadDestination.class));
		AsyncMockStubber.callSuccessWith(destinations).when(synapseClient).getUploadDestinations(anyString(), any(AsyncCallback.class));
		uploader.queryForUploadDestination();
		assertNull(uploader.getStorageLocationId());
		verifyUploadError();
	}
	
	@Test
	public void testUploadToExternal() {
		String sftpProxy = "http://mytestproxy.com/sftp";
		when(mockGlobalApplicationState.getSynapseProperty(WebConstants.SFTP_PROXY_ENDPOINT)).thenReturn(sftpProxy);
		String url = "sftp://ok.net";
		when(gwt.encodeQueryString(anyString())).thenReturn(url);

		ExternalUploadDestination d = new ExternalUploadDestination();
		d.setUploadType(UploadType.SFTP);
		d.setUrl(url);
		d.setStorageLocationId(storageLocationId);
		List<UploadDestination> destinations = new ArrayList<UploadDestination>();
		destinations.add(d);
		AsyncMockStubber.callSuccessWith(destinations).when(synapseClient).getUploadDestinations(anyString(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith("ok.net").when(synapseClient).getHost(anyString(), any(AsyncCallback.class));
		
		uploader.queryForUploadDestination();
		assertEquals(uploader.getStorageLocationId(), storageLocationId);
		assertEquals(UploadType.SFTP, uploader.getCurrentUploadType());
		verify(view).showUploadingToExternalStorage(anyString(), anyString());
		verify(view).enableMultipleFileUploads(false);
		
		uploader.setFileNames(new String[] {"test.txt"});
		uploader.uploadToSftpProxy(url);
		verify(synapseClient).getFileEntityIdWithSameName(anyString(), anyString(), any(AsyncCallback.class));
		//capture the value sent to the form to submit
		ArgumentCaptor<String> c = ArgumentCaptor.forClass(String.class);
		verify(view).submitForm(c.capture());
		String target = c.getValue();
		//should point to the sftp proxy, and contain the original url
		assertTrue(target.startsWith(sftpProxy));
		assertTrue(target.contains("?url=" + url));
	}

	@Test
	public void testQueryForUploadDestinationsWithUploadToS3() {
		S3UploadDestination d = new S3UploadDestination();
		d.setUploadType(UploadType.S3);
		d.setStorageLocationId(storageLocationId);
		List<UploadDestination> destinations = new ArrayList<UploadDestination>();
		destinations.add(d);
		AsyncMockStubber.callSuccessWith(destinations).when(synapseClient).getUploadDestinations(anyString(), any(AsyncCallback.class));
		uploader.queryForUploadDestination();
		assertEquals(uploader.getStorageLocationId(), storageLocationId);
	}

	@Test
	public void testQueryForUploadDestinationsWithoutParentEntityId() {
		//Configure the uploader without a parent entity id, but with an existing file entity.
		//This is the case when updating a file entity (create a new version).
		String entityId = "syn123";
		FileEntity fileEntity = new FileEntity();
		fileEntity.setId(entityId);
		
		Mockito.reset(synapseClient);
		uploader.asWidget(fileEntity);
		assertNull(uploader.getStorageLocationId());
		ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
		verify(synapseClient).getUploadDestinations(stringCaptor.capture(), any(AsyncCallback.class));
		assertEquals(entityId, stringCaptor.getValue());
	}

	@Test
	public void testQueryForUploadDestinationsWithNullEntity() {
		Mockito.reset(synapseClient);
		uploader.asWidget((FileEntity)null);
		assertNull(uploader.getStorageLocationId());
	}

	@Test
	public void testUploadNoCredentials() {
		uploader.setCurrentUploadType(UploadType.S3);
		uploader.handleUploads();
		
		verify(view, Mockito.never()).showErrorMessage(DisplayConstants.CREDENTIALS_REQUIRED_MESSAGE);
		reset(view);
		uploader.setCurrentUploadType(UploadType.SFTP);
		uploader.handleUploads();
		verify(view).showErrorMessage(DisplayConstants.CREDENTIALS_REQUIRED_MESSAGE);
		verify(view).hideLoading();
		verify(view).enableUpload();
	}
	
	@Test
	public void testUploadCredentials() {
		when(view.getExternalUsername()).thenReturn("alfred");
		when(view.getExternalPassword()).thenReturn("12345");
		
		uploader.setCurrentUploadType(UploadType.SFTP);
		uploader.handleUploads();
		verify(view, Mockito.never()).showErrorMessage(DisplayConstants.CREDENTIALS_REQUIRED_MESSAGE);
	}
	
	@Test
	public void testGetSftpProxyLink() throws UnsupportedEncodingException {
		String sftpProxy = "http://mytestproxy.com/sftp";
		String filename = "override that.txt";
		//test with existing query param
		when(mockGlobalApplicationState.getSynapseProperty(WebConstants.SFTP_PROXY_ENDPOINT)).thenReturn(sftpProxy +"?gwt.codesvr=localhost:9999");
		//and the sftp link contains characters that should be escaped
		String sftpLink = "sftp://this/and/that.txt?foo=bar";
		String encodedUrl = URLEncoder.encode(sftpLink, "UTF-8");
		String encodedFilename = URLEncoder.encode(filename, "UTF-8");
		when(gwt.encodeQueryString(sftpLink)).thenReturn(encodedUrl);
		when(gwt.encodeQueryString(filename)).thenReturn(encodedFilename);
		String sftpProxyLink = Uploader.getSftpProxyLink(filename, sftpLink, mockGlobalApplicationState, gwt);
		//verify that the sftp link was encoded
		assertTrue(sftpProxyLink.contains(encodedUrl));
		assertTrue(sftpProxyLink.contains(encodedFilename));
		//and that it did not add another '?' for the url param
		assertTrue(sftpProxyLink.contains("&url="));
	}
	
	@Test
	public void testHandleSftpSubmitResult() throws RestServiceException {
		uploader.setCurrentUploadType(UploadType.SFTP);
		uploader.setFileNames(new String[] {"test.txt"});
		UploadResult r = new UploadResult();
		r.setUploadStatus(UploadStatus.SUCCESS);
		String newUrl = "sftp://bar/test.txt";
		r.setMessage(newUrl);
		uploader.handleSubmitResult(r);
		//should try to create a new external file
		verify(synapseClient).createExternalFile(anyString(), anyString(), anyString(), anyLong(), anyString(), anyLong(), any(AsyncCallback.class));
	}
	
	@Test
	public void testHandleS3SubmitResult() throws RestServiceException {
		uploader.setCurrentUploadType(UploadType.S3);
		uploader.setFileNames(new String[] {"test.txt"});
		UploadResult r = new UploadResult();
		r.setUploadStatus(UploadStatus.SUCCESS);
		String fileHandleId = "1234";
		r.setMessage(fileHandleId);
		uploader.handleSubmitResult(r);
		verify(synapseClient).setFileEntityFileHandle(anyString(),  anyString(),  anyString(),  any(AsyncCallback.class));
	}
	
	@Test
	public void testHandleSubmitResultFailure() throws RestServiceException {
		uploader.setCurrentUploadType(UploadType.S3);
		uploader.setFileNames(new String[] {"test.txt"});
		UploadResult r = new UploadResult();
		r.setUploadStatus(UploadStatus.FAILED);
		r.setMessage("error occurred");
		uploader.handleSubmitResult(r);
		verifyUploadError();
	}
	
	@Test
	public void testUploadFiles() {
		uploader.uploadFiles();
		verify(view).triggerUpload();
	}
	
	@Test
	public void testFileSupported() {
		when(synapseJsniUtils.isFileAPISupported()).thenReturn(true);
		assertTrue(uploader.checkFileAPISupported());
	}
	
	@Test
	public void testFileNotSupported() {
		when(synapseJsniUtils.isFileAPISupported()).thenReturn(false);
		assertFalse(uploader.checkFileAPISupported());
		verifyUploadError();
	}
	
	@Test
	public void testClearState() {
		uploader.setCurrentExternalUploadUrl("sftp://an.sftp.site/");
		uploader.setCurrentUploadType(UploadType.SFTP);
		
		uploader.clearState();
		verify(view).clear();
		assertNull(uploader.getCurrentExternalUploadUrl());
		assertNull(uploader.getCurrentUploadType());
	}
	
	@Test
	public void testIsJschAuthorizationError() {
		assertFalse(uploader.isJschAuthorizationError(""));
		assertFalse(uploader.isJschAuthorizationError(null));
		assertFalse(uploader.isJschAuthorizationError("Bad request."));
		assertTrue(uploader.isJschAuthorizationError("com.jcraft.jsch.JSchException: Auth fail"));
		assertTrue(uploader.isJschAuthorizationError("com.JCRAFT.jsch.jschexception: Auth FAIL"));
	}
	
	@Test
	public void testGetSelectedFilesText() {
		String fileName = "single file.txt";
		when(synapseJsniUtils.getMultipleUploadFileNames(anyString())).thenReturn(new String[]{fileName});
		assertEquals(fileName, uploader.getSelectedFilesText());
	}
	
	@Test
	public void testGetSelectedFilesTextNoFiles() {
		when(synapseJsniUtils.getMultipleUploadFileNames(anyString())).thenReturn(null);
		assert(uploader.getSelectedFilesText().isEmpty());
	}
	
	@Test
	public void testGetSelectedFilesTextMultipleFiles() {
		when(synapseJsniUtils.getMultipleUploadFileNames(anyString())).thenReturn(new String[]{"file1", "file2"});
		assertEquals("2 files", uploader.getSelectedFilesText());
	}

}
