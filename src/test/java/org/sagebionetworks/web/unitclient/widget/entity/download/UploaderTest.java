package org.sagebionetworks.web.unitclient.widget.entity.download;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.client.utils.FutureUtils.getDoneFuture;
import static org.sagebionetworks.web.client.utils.FutureUtils.getFailedFuture;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Folder;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.attachment.UploadResult;
import org.sagebionetworks.repo.model.attachment.UploadStatus;
import org.sagebionetworks.repo.model.file.ExternalGoogleCloudUploadDestination;
import org.sagebionetworks.repo.model.file.ExternalObjectStoreUploadDestination;
import org.sagebionetworks.repo.model.file.ExternalS3UploadDestination;
import org.sagebionetworks.repo.model.file.ExternalUploadDestination;
import org.sagebionetworks.repo.model.file.S3UploadDestination;
import org.sagebionetworks.repo.model.file.State;
import org.sagebionetworks.repo.model.file.UploadDaemonStatus;
import org.sagebionetworks.repo.model.file.UploadDestination;
import org.sagebionetworks.repo.model.file.UploadType;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.SynapseJavascriptFactory.OBJECT_TYPE;
import org.sagebionetworks.web.client.SynapseProperties;
import org.sagebionetworks.web.client.callback.MD5Callback;
import org.sagebionetworks.web.client.events.CancelHandler;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.UploadSuccessHandler;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.download.S3DirectUploader;
import org.sagebionetworks.web.client.widget.entity.download.Uploader;
import org.sagebionetworks.web.client.widget.entity.download.UploaderView;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import org.sagebionetworks.web.unitclient.widget.upload.MultipartUploaderStub;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;

@RunWith(MockitoJUnitRunner.class)
public class UploaderTest {
	@Mock
	S3DirectUploader mockS3DirectUploader;
	MultipartUploaderStub multipartUploader;
	@Mock
	UploaderView mockView;
	@Mock
	AuthenticationController mockAuthenticationController;
	@Mock
	SynapseClientAsync mockSynapseClient;
	@Mock
	SynapseJSNIUtils mockSynapseJsniUtils;
	@Mock
	GlobalApplicationState mockGlobalApplicationState;
	private static AdapterFactory adapterFactory = new AdapterFactoryImpl(); // alt: GwtAdapterFactory
	@Mock
	EventBus mockEventBus;

	Uploader uploader;
	@Mock
	GWTWrapper mockGwt;
	FileEntity testEntity;
	@Mock
	CancelHandler mockCancelHandler;
	@Mock
	UploadSuccessHandler mockUploadSuccessHandler;
	String parentEntityId;
	private Long storageLocationId;
	String md5 = "e10e3f4491440ce7b48edc97f03307bb";
	@Mock
	ExternalObjectStoreUploadDestination mockExternalObjectStoreUploadDestination;
	@Mock
	SynapseJavascriptClient mockSynapseJavascriptClient;
	@Mock
	JavaScriptObject mockFileList;
	@Mock
	JavaScriptObject mockDroppedFileList;
	@Mock
	SynapseProperties mockSynapseProperties;
	@Captor
	ArgumentCaptor<CallbackP<JavaScriptObject>> dragAndDropHandlerCaptor;
	@Mock
	Callback mockCallback;
	@Mock
	Folder mockFolder;
	@Captor
	ArgumentCaptor<Entity> entityCaptor;
	@Captor
	ArgumentCaptor<Callback> callbackCaptor;

	public static final String SUCCESS_FILE_HANDLE = "99999";

	@Before
	public void before() throws Exception {
		multipartUploader = new MultipartUploaderStub();
		testEntity = new FileEntity();
		testEntity.setName("test file");
		testEntity.setId("syn99");
		UserProfile profile = new UserProfile();
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		when(mockAuthenticationController.getCurrentUserProfile()).thenReturn(profile);
		when(mockSynapseJsniUtils.getContentType(any(JavaScriptObject.class), anyInt())).thenReturn("image/png");
		S3UploadDestination d = new S3UploadDestination();
		d.setUploadType(UploadType.S3);
		List<UploadDestination> destinations = new ArrayList<UploadDestination>();
		destinations.add(d);
		AsyncMockStubber.callSuccessWith(destinations).when(mockSynapseJavascriptClient).getUploadDestinations(anyString(), any(AsyncCallback.class));

		UploadDaemonStatus status = new UploadDaemonStatus();
		status.setState(State.COMPLETED);
		status.setFileHandleId("fake handle");
		String completedUploadDaemonStatusJson = status.writeToJSONObject(adapterFactory.createNew()).toJSONString();

		AsyncMockStubber.callSuccessWith("entityID").when(mockSynapseClient).setFileEntityFileHandle(anyString(), anyString(), anyString(), any(AsyncCallback.class));

		when(mockGwt.createXMLHttpRequest()).thenReturn(null);

		String[] fileNames = {"newFile.txt"};
		when(mockSynapseJsniUtils.getMultipleUploadFileNames(any(JavaScriptObject.class))).thenReturn(fileNames);
		AsyncMockStubber.callSuccessWith(testEntity).when(mockSynapseClient).updateExternalFile(anyString(), anyString(), anyString(), anyString(), anyLong(), anyString(), anyLong(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(testEntity).when(mockSynapseClient).createExternalFile(anyString(), anyString(), anyString(), anyString(), anyLong(), anyString(), anyLong(), any(AsyncCallback.class));
		// by default, there is no name conflict
		AsyncMockStubber.callFailureWith(new NotFoundException()).when(mockSynapseClient).getFileEntityIdWithSameName(anyString(), anyString(), any(AsyncCallback.class));
		uploader = new Uploader(mockView, mockSynapseClient, mockSynapseJsniUtils, mockGwt, mockAuthenticationController, multipartUploader, mockGlobalApplicationState, mockS3DirectUploader, mockSynapseJavascriptClient, mockSynapseProperties, mockEventBus);
		uploader.setCancelHandler(mockCancelHandler);
		uploader.setSuccessHandler(mockUploadSuccessHandler);
		parentEntityId = "syn1234";
		uploader.configure(null, parentEntityId, null, true);

		// Simulate success.
		multipartUploader.setFileHandle(SUCCESS_FILE_HANDLE);

		when(mockSynapseJsniUtils.getFileSize(any(JavaScriptObject.class))).thenReturn(1.0);
		when(mockSynapseJsniUtils.isFileAPISupported()).thenReturn(true);
		storageLocationId = 9090L;

		// Stub the generation of a MD5.
		doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				final Object[] args = invocation.getArguments();
				((MD5Callback) args[args.length - 1]).setMD5(md5);
				return null;
			}
		}).when(mockSynapseJsniUtils).getFileMd5(any(JavaScriptObject.class), any(MD5Callback.class));

		when(mockSynapseJsniUtils.getFileList(anyString())).thenReturn(mockFileList);
	}

	@Test
	public void testSetNewExternalPath() throws Exception {
		// this is the full success test
		// if entity is null, it should call synapseClient.createExternalFile() to create the FileEntity and
		// associate the path.
		uploader.setExternalFilePath("http://fakepath.url/blah.xml", "", storageLocationId);
		verify(mockSynapseClient).createExternalFile(anyString(), anyString(), anyString(), anyString(), eq((Long) null), eq((String) null), eq(storageLocationId), any(AsyncCallback.class));
		verify(mockView).showInfo(anyString());
		verify(mockUploadSuccessHandler).onSuccessfulUpload();
	}

	@Test
	public void testSetExternalPathFailedCreate() throws Exception {
		AsyncMockStubber.callFailureWith(new Exception("failed to create")).when(mockSynapseClient).createExternalFile(anyString(), anyString(), anyString(), anyString(), eq((Long) null), eq((String) null), anyLong(), any(AsyncCallback.class));
		uploader.setExternalFilePath("http://fakepath.url/blah.xml", "", storageLocationId);
		verify(mockView).showErrorMessage(anyString());
	}

	@Test
	public void testSetExternalPathFailedUpdateFile() throws Exception {
		AsyncMockStubber.callFailureWith(new Exception("failed to update path")).when(mockSynapseClient).createExternalFile(anyString(), anyString(), anyString(), anyString(), eq((Long) null), eq((String) null), anyLong(), any(AsyncCallback.class));
		uploader.setExternalFilePath("http://fakepath.url/blah.xml", "", storageLocationId);
		verify(mockView).showErrorMessage(anyString());
	}

	@Test
	public void testSetNewExternalPathEncoding() throws Exception {
		String url = "http://fakepath.url/a b/c d/blah.xml";
		String encodedUrl = "http://fakepath.url/a%20b/c%20d/blah.xml";
		when(mockGwt.encode(anyString())).thenReturn(encodedUrl);
		uploader.setExternalFilePath(url, "", storageLocationId);
		verify(mockSynapseClient).createExternalFile(anyString(), eq(encodedUrl), anyString(), anyString(), eq((Long) null), eq((String) null), eq(storageLocationId), any(AsyncCallback.class));
		verify(mockView).showInfo(anyString());
	}

	@Test
	public void testSetExternalFileEntityPathWithFileEntity() throws Exception {
		uploader.configure(testEntity, null, null, true);
		uploader.setExternalFilePath("http://fakepath.url/blah.xml", "", storageLocationId);
		verify(mockSynapseClient).updateExternalFile(anyString(), anyString(), anyString(), anyString(), eq((Long) null), eq((String) null), eq(storageLocationId), any(AsyncCallback.class));
		verify(mockView).showInfo(anyString());
	}

	@Test
	public void testSetNewSftpExternalPath() throws Exception {
		// this is the full success test
		// if entity is null, it should call synapseClient.createExternalFile() to create the FileEntity and
		// associate the path.
		uploader.setFileNames(new String[] {"test.txt"});
		uploader.setSftpExternalFilePath("http://fakepath.url/blah.xml", storageLocationId);
		verify(mockSynapseClient).createExternalFile(anyString(), anyString(), anyString(), anyString(), anyLong(), eq(md5), eq(storageLocationId), any(AsyncCallback.class));
		verify(mockView).showInfo(anyString());
	}

	@Test
	public void testSetSftpExternalPathToFolder() throws Exception {
		// attempt to uplaod a Folder to sftp external link
		uploader.setFileNames(new String[] {"testfolder"});
		doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				final Object[] args = invocation.getArguments();
				((MD5Callback) args[args.length - 1]).setMD5(null);
				return null;
			}
		}).when(mockSynapseJsniUtils).getFileMd5(any(JavaScriptObject.class), any(MD5Callback.class));

		uploader.setSftpExternalFilePath("http://fakepath.url/blah.xml", storageLocationId);
		verify(mockSynapseClient, never()).createExternalFile(anyString(), anyString(), anyString(), anyString(), anyLong(), eq(md5), eq(storageLocationId), any(AsyncCallback.class));
		verify(mockView).showErrorMessage(DisplayConstants.MD5_CALCULATION_ERROR);
	}

	@Test
	public void testSetSftpExternalPathFailedCreate() throws Exception {
		uploader.setFileNames(new String[] {"test.txt"});
		AsyncMockStubber.callFailureWith(new Exception("failed to create")).when(mockSynapseClient).createExternalFile(anyString(), anyString(), anyString(), anyString(), anyLong(), eq(md5), anyLong(), any(AsyncCallback.class));
		uploader.setSftpExternalFilePath("http://fakepath.url/blah.xml", storageLocationId);
		verify(mockView).showErrorMessage(anyString());
	}

	@Test
	public void testSetSftpExternalPathFailedUpdateFile() throws Exception {
		uploader.setFileNames(new String[] {"test.txt"});
		AsyncMockStubber.callFailureWith(new Exception("failed to update path")).when(mockSynapseClient).createExternalFile(anyString(), anyString(), anyString(), anyString(), anyLong(), eq(md5), anyLong(), any(AsyncCallback.class));
		uploader.setSftpExternalFilePath("http://fakepath.url/blah.xml", storageLocationId);
		verify(mockView).showErrorMessage(anyString());
	}

	@Test
	public void testSetSftpExternalFileEntityPathWithFileEntity() throws Exception {
		String fileName = "test.txt";
		uploader.configure(testEntity, null, null, true);
		uploader.setFileNames(new String[] {fileName});
		uploader.setSftpExternalFilePath("http://fakepath.url/blah.xml", storageLocationId);
		verify(mockSynapseClient).updateExternalFile(anyString(), anyString(), eq(fileName), anyString(), anyLong(), eq(md5), eq(storageLocationId), any(AsyncCallback.class));
		verify(mockView).showInfo(anyString());
	}

	@Test
	public void testDirectUploadHappyCase() throws Exception {
		verify(mockView).showUploadingToSynapseStorage();
		verify(mockView).enableMultipleFileUploads(true);
		final String file1 = "file1.txt";
		String[] fileNames = {file1};
		when(mockSynapseJsniUtils.getMultipleUploadFileNames(mockFileList)).thenReturn(fileNames);
		AsyncMockStubber.callSuccessWith(testEntity).when(mockSynapseJavascriptClient).getEntity(anyString(), any(OBJECT_TYPE.class), any(AsyncCallback.class));
		uploader.handleUploads();
		verify(mockSynapseClient).setFileEntityFileHandle(anyString(), anyString(), anyString(), any(AsyncCallback.class));
		verify(mockView).hideLoading();
		assertEquals(UploadType.S3, uploader.getCurrentUploadType());
		// verify upload success
		verify(mockView).showInfo(DisplayConstants.TEXT_UPLOAD_SUCCESS);
		verify(mockView).clear();
		verify(mockGlobalApplicationState).clearDropZoneHandler();
		verify(mockView, times(2)).resetToInitialState();
		verify(mockUploadSuccessHandler).onSuccessfulUpload();
		verify(mockEventBus).fireEvent(any(EntityUpdatedEvent.class));
	}

	@Test
	public void testUpdateS3UploadBannerViewNull() throws Exception {
		reset(mockView);
		uploader.updateUploadBannerView(null);
		verify(mockView).showUploadingToSynapseStorage();
	}

	@Test
	public void testUpdateS3UploadBannerViewEmpty() throws Exception {
		reset(mockView);
		uploader.updateUploadBannerView("");
		verify(mockView).showUploadingToSynapseStorage();
	}

	@Test
	public void testUpdateS3UploadBannerViewSet() throws Exception {
		reset(mockView);
		String banner = "this is my test banner";
		uploader.updateUploadBannerView(banner);
		verify(mockView).showUploadingBanner(banner);
	}

	@Test
	public void testDirectUploadNoFilesSelected() throws Exception {
		uploader.setFileNames(null);
		when(mockSynapseJsniUtils.getMultipleUploadFileNames(mockFileList)).thenReturn(null);
		uploader.handleUploads();
		verify(mockView).hideLoading();
		verify(mockView).showErrorMessage(DisplayConstants.NO_FILES_SELECTED_FOR_UPLOAD_MESSAGE);
		verify(mockView).enableUpload();

		assertEquals(UploadType.S3, uploader.getCurrentUploadType());
	}

	@Test
	public void testDirectUploadTeamIconHappyCase() throws Exception {
		CallbackP callback = mock(CallbackP.class);
		uploader.configure(null, null, callback, false);
		uploader.handleUploads();
		verify(callback).invoke(anyString());
	}

	private void verifyUploadError() {
		verify(mockView).showErrorMessage(anyString(), anyString());
		verify(mockCancelHandler).onCancel();
	}

	@Test
	public void testCancelClicked() {
		assertFalse(multipartUploader.isCanceled());

		uploader.cancelClicked();

		verify(mockView).clear();
		verify(mockGlobalApplicationState).clearDropZoneHandler();
		verify(mockCancelHandler).onCancel();
		assertTrue(multipartUploader.isCanceled());
	}

	@Test
	public void testDirectUploadStep1Failure() throws Exception {
		AsyncMockStubber.callFailureWith(new IllegalArgumentException()).when(mockSynapseClient).getFileEntityIdWithSameName(anyString(), anyString(), any(AsyncCallback.class));
		uploader.checkForExistingFileName("newFile.txt", mockCallback);
		verifyUploadError();
		verifyZeroInteractions(mockCallback);
	}

	@Test
	public void testDirectUploadStep1SameNameFound() throws Exception {
		String duplicateNameEntityId = "syn007";
		AsyncMockStubber.callSuccessWith(duplicateNameEntityId).when(mockSynapseClient).getFileEntityIdWithSameName(anyString(), anyString(), any(AsyncCallback.class));
		uploader.checkForExistingFileName("newFile.txt", mockCallback);
		verify(mockView).showConfirmDialog(anyString(), any(Callback.class), any(Callback.class));
		verifyZeroInteractions(mockCallback);
	}

	@Test
	public void testDirectUploadSameNameFoundMultipleFiles() throws Exception {
		String file1 = "file1.txt";
		String file2 = "file2.txt";
		String[] fileNames = {file1, file2};
		when(mockSynapseJsniUtils.getMultipleUploadFileNames(any(JavaScriptObject.class))).thenReturn(fileNames);
		String duplicateNameEntityId = "syn128";
		AsyncMockStubber.callSuccessWith(duplicateNameEntityId).when(mockSynapseClient).getFileEntityIdWithSameName(eq(file1), anyString(), any(AsyncCallback.class));
		AsyncMockStubber.callFailureWith(new NotFoundException()).when(mockSynapseClient).getFileEntityIdWithSameName(eq(file2), anyString(), any(AsyncCallback.class));
		uploader.handleUploads();
		// capture the confirm callback, to simulate that user approves
		verify(mockView).showConfirmDialog(anyString(), callbackCaptor.capture(), any(Callback.class));
		callbackCaptor.getValue().invoke();

		// SWC-4274: Verify the 2 rpcs. The first adds a new file version to duplicateNameEntityId, and the
		// second creates a new file entity.
		verify(mockSynapseClient).setFileEntityFileHandle(anyString(), eq(duplicateNameEntityId), eq(parentEntityId), any(AsyncCallback.class));
		verify(mockSynapseClient).setFileEntityFileHandle(anyString(), eq(null), eq(parentEntityId), any(AsyncCallback.class));
	}



	@Test
	public void testDirectUploadStep1NoParentEntityId() throws Exception {
		uploader.configure(null, null, null, false);
		uploader.checkForExistingFileName("newFile.txt", mockCallback);
		verify(mockSynapseClient, Mockito.never()).getFileEntityIdWithSameName(anyString(), anyString(), any(AsyncCallback.class));
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
		when(mockSynapseJsniUtils.getMultipleUploadFileNames(any(JavaScriptObject.class))).thenReturn(fileNames);

		uploader.handleUploads();

		verify(mockSynapseClient).getFileEntityIdWithSameName(eq(file1), eq(parentEntityId), any(AsyncCallback.class));

		// triggers file2 to upload.
		verify(mockSynapseClient).getFileEntityIdWithSameName(eq(file2), eq(parentEntityId), any(AsyncCallback.class));

		// triggers file3 to upload
		verify(mockSynapseClient).getFileEntityIdWithSameName(eq(file3), eq(parentEntityId), any(AsyncCallback.class));
	}

	@Test
	public void testCalculatePercentOverAllFiles() {
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
		AsyncMockStubber.callSuccessWith(destinations).when(mockSynapseJavascriptClient).getUploadDestinations(anyString(), any(AsyncCallback.class));
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
		AsyncMockStubber.callSuccessWith(destinations).when(mockSynapseJavascriptClient).getUploadDestinations(anyString(), any(AsyncCallback.class));
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
		AsyncMockStubber.callSuccessWith(destinations).when(mockSynapseJavascriptClient).getUploadDestinations(anyString(), any(AsyncCallback.class));
		uploader.queryForUploadDestination();
		assertEquals(uploader.getStorageLocationId(), storageLocationId);
	}

	@Test
	public void testUploadToValidExternalGoogleCloud() {
		ExternalGoogleCloudUploadDestination d = new ExternalGoogleCloudUploadDestination();
		d.setUploadType(UploadType.GOOGLECLOUDSTORAGE);
		d.setStorageLocationId(storageLocationId);
		List<UploadDestination> destinations = new ArrayList<>();
		destinations.add(d);
		AsyncMockStubber.callSuccessWith(destinations).when(mockSynapseJavascriptClient).getUploadDestinations(anyString(), any(AsyncCallback.class));
		uploader.queryForUploadDestination();
		assertEquals(uploader.getStorageLocationId(), storageLocationId);
	}

	@Test
	public void testInvalidUploadDestination() {
		// add an invalid upload destination
		List<UploadDestination> destinations = new ArrayList<UploadDestination>();
		destinations.add(mock(UploadDestination.class));
		AsyncMockStubber.callSuccessWith(destinations).when(mockSynapseJavascriptClient).getUploadDestinations(anyString(), any(AsyncCallback.class));
		uploader.queryForUploadDestination();
		assertNull(uploader.getStorageLocationId());
		verifyUploadError();
	}

	@Test
	public void testUploadToExternal() {
		String sftpProxy = "http://mytestproxy.com/sftp";
		when(mockSynapseProperties.getSynapseProperty(WebConstants.SFTP_PROXY_ENDPOINT)).thenReturn(sftpProxy);
		String url = "sftp://ok.net";
		when(mockGwt.encodeQueryString(anyString())).thenReturn(url);

		ExternalUploadDestination d = new ExternalUploadDestination();
		d.setUploadType(UploadType.SFTP);
		d.setUrl(url);
		d.setStorageLocationId(storageLocationId);
		List<UploadDestination> destinations = new ArrayList<UploadDestination>();
		destinations.add(d);
		AsyncMockStubber.callSuccessWith(destinations).when(mockSynapseJavascriptClient).getUploadDestinations(anyString(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith("ok.net").when(mockSynapseClient).getHost(anyString(), any(AsyncCallback.class));

		uploader.queryForUploadDestination();
		assertEquals(uploader.getStorageLocationId(), storageLocationId);
		assertEquals(UploadType.SFTP, uploader.getCurrentUploadType());
		verify(mockView).showUploadingToExternalStorage(anyString(), anyString());
		verify(mockView).enableMultipleFileUploads(false);

		uploader.setFileNames(new String[] {"test.txt"});
		uploader.uploadToSftpProxy(url);
		verify(mockSynapseClient).getFileEntityIdWithSameName(anyString(), anyString(), any(AsyncCallback.class));
		// capture the value sent to the form to submit
		ArgumentCaptor<String> c = ArgumentCaptor.forClass(String.class);
		verify(mockView).submitForm(c.capture());
		String target = c.getValue();
		// should point to the sftp proxy, and contain the original url
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
		AsyncMockStubber.callSuccessWith(destinations).when(mockSynapseJavascriptClient).getUploadDestinations(anyString(), any(AsyncCallback.class));
		uploader.queryForUploadDestination();
		assertEquals(uploader.getStorageLocationId(), storageLocationId);
	}

	@Test
	public void testQueryForUploadDestinationsWithUploadToExternalObjectStore() {
		String banner = "banner";
		String endpoint = "endpointUrl";
		String bucket = "mr.h";
		String keyPrefixUUID = "keyPrefixUUID";
		UploadType uploadType = UploadType.S3;
		String fileName = "f.txt";
		when(mockExternalObjectStoreUploadDestination.getBanner()).thenReturn(banner);
		when(mockExternalObjectStoreUploadDestination.getStorageLocationId()).thenReturn(storageLocationId);
		when(mockExternalObjectStoreUploadDestination.getUploadType()).thenReturn(uploadType);
		when(mockExternalObjectStoreUploadDestination.getEndpointUrl()).thenReturn(endpoint);
		when(mockExternalObjectStoreUploadDestination.getBucket()).thenReturn(bucket);
		when(mockExternalObjectStoreUploadDestination.getKeyPrefixUUID()).thenReturn(keyPrefixUUID);

		AsyncMockStubber.callSuccessWith(Collections.singletonList(mockExternalObjectStoreUploadDestination)).when(mockSynapseJavascriptClient).getUploadDestinations(anyString(), any(AsyncCallback.class));
		uploader.queryForUploadDestination();
		assertEquals(uploader.getStorageLocationId(), storageLocationId);
		assertEquals(uploader.getCurrentUploadType(), uploadType);
		verify(mockView).showUploadingToS3DirectStorage(endpoint, banner);

		String accessKey = "abc";
		String secretKey = "123";
		when(mockView.getS3DirectAccessKey()).thenReturn(accessKey);
		when(mockView.getS3DirectSecretKey()).thenReturn(secretKey);

		uploader.directUploadStep2(fileName);

		verify(mockS3DirectUploader).configure(accessKey, secretKey, bucket, endpoint);
		verify(mockS3DirectUploader).uploadFile(anyString(), anyString(), any(JavaScriptObject.class), eq(uploader), eq(keyPrefixUUID), eq(storageLocationId), eq(mockView));
	}

	@Test
	public void testDragAndDrop() throws RestServiceException {
		// widget configured in @Before
		verify(mockGlobalApplicationState).setDropZoneHandler(dragAndDropHandlerCaptor.capture());
		verify(mockSynapseClient, never()).setFileEntityFileHandle(anyString(), anyString(), anyString(), any(AsyncCallback.class));

		// simulate drop
		String fileName = "single file.txt";
		when(mockSynapseJsniUtils.getMultipleUploadFileNames(any(JavaScriptObject.class))).thenReturn(new String[] {fileName});
		dragAndDropHandlerCaptor.getValue().invoke(mockDroppedFileList);

		verify(mockSynapseClient).setFileEntityFileHandle(anyString(), anyString(), anyString(), any(AsyncCallback.class));
	}

	@Test
	public void testQueryForUploadDestinationsWithoutParentEntityId() {
		// Configure the uploader without a parent entity id, but with an existing file entity.
		// This is the case when updating a file entity (create a new version).
		String entityId = "syn123";
		FileEntity fileEntity = new FileEntity();
		fileEntity.setId(entityId);

		Mockito.reset(mockSynapseJavascriptClient);
		uploader.configure(fileEntity, null, null, true);

		assertNull(uploader.getStorageLocationId());
		ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
		verify(mockSynapseJavascriptClient).getUploadDestinations(stringCaptor.capture(), any(AsyncCallback.class));
		assertEquals(entityId, stringCaptor.getValue());
	}

	@Test
	public void testQueryForUploadDestinationsWithNullEntity() {
		Mockito.reset(mockSynapseClient);
		uploader.configure((FileEntity) null, null, null, true);
		assertNull(uploader.getStorageLocationId());
	}

	@Test
	public void testUploadNoCredentials() {
		uploader.setCurrentUploadType(UploadType.S3);
		uploader.handleUploads();

		verify(mockView, Mockito.never()).showErrorMessage(DisplayConstants.CREDENTIALS_REQUIRED_MESSAGE);
		reset(mockView);
		uploader.setCurrentUploadType(UploadType.SFTP);
		uploader.handleUploads();
		verify(mockView).showErrorMessage(DisplayConstants.CREDENTIALS_REQUIRED_MESSAGE);
		verify(mockView).hideLoading();
		verify(mockView).enableUpload();
	}

	@Test
	public void testUploadCredentials() {
		when(mockView.getExternalUsername()).thenReturn("alfred");
		when(mockView.getExternalPassword()).thenReturn("12345");

		uploader.setCurrentUploadType(UploadType.SFTP);
		uploader.handleUploads();
		verify(mockView, Mockito.never()).showErrorMessage(DisplayConstants.CREDENTIALS_REQUIRED_MESSAGE);
	}

	@Test
	public void testGetSftpProxyLink() throws UnsupportedEncodingException {
		String sftpProxy = "http://mytestproxy.com/sftp";
		String filename = "override that.txt";
		// test with existing query param
		when(mockSynapseProperties.getSynapseProperty(WebConstants.SFTP_PROXY_ENDPOINT)).thenReturn(sftpProxy + "?gwt.codesvr=localhost:9999");
		// and the sftp link contains characters that should be escaped
		String sftpLink = "sftp://this/and/that.txt?foo=bar";
		String encodedUrl = URLEncoder.encode(sftpLink, "UTF-8");
		String encodedFilename = URLEncoder.encode(filename, "UTF-8");
		when(mockGwt.encodeQueryString(sftpLink)).thenReturn(encodedUrl);
		when(mockGwt.encodeQueryString(filename)).thenReturn(encodedFilename);
		String sftpProxyLink = Uploader.getSftpProxyLink(filename, sftpLink, mockSynapseProperties, mockGwt);
		// verify that the sftp link was encoded
		assertTrue(sftpProxyLink.contains(encodedUrl));
		assertTrue(sftpProxyLink.contains(encodedFilename));
		// and that it did not add another '?' for the url param
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
		// should try to create a new external file
		verify(mockSynapseClient).createExternalFile(anyString(), anyString(), anyString(), anyString(), anyLong(), anyString(), anyLong(), any(AsyncCallback.class));
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
		verify(mockSynapseClient).setFileEntityFileHandle(anyString(), anyString(), anyString(), any(AsyncCallback.class));
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
		verify(mockView).triggerUpload();
	}

	@Test
	public void testFileSupported() {
		when(mockSynapseJsniUtils.isFileAPISupported()).thenReturn(true);
		assertTrue(uploader.checkFileAPISupported());
	}

	@Test
	public void testFileNotSupported() {
		when(mockSynapseJsniUtils.isFileAPISupported()).thenReturn(false);
		assertFalse(uploader.checkFileAPISupported());
		verifyUploadError();
	}

	@Test
	public void testClearState() {
		uploader.setCurrentExternalUploadUrl("sftp://an.sftp.site/");
		uploader.setCurrentUploadType(UploadType.SFTP);

		uploader.clearState();
		verify(mockView).clear();
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
		when(mockSynapseJsniUtils.getMultipleUploadFileNames(any(JavaScriptObject.class))).thenReturn(new String[] {fileName});
		assertEquals(fileName, uploader.getSelectedFilesText());
	}

	@Test
	public void testGetSelectedFilesTextNoFiles() {
		when(mockSynapseJsniUtils.getMultipleUploadFileNames(any(JavaScriptObject.class))).thenReturn(null);
		assert (uploader.getSelectedFilesText().isEmpty());
	}

	@Test
	public void testGetSelectedFilesTextMultipleFiles() {
		when(mockSynapseJsniUtils.getMultipleUploadFileNames(any(JavaScriptObject.class))).thenReturn(new String[] {"file1", "file2"});
		assertEquals("2 files", uploader.getSelectedFilesText());
	}

	@Test
	public void testUploadFailed() {
		String message = "meow there's an error.";
		uploader.uploadFailed(message);
		verify(mockSynapseJavascriptClient).logError(any(Exception.class));
	}

	@Test
	public void testUploadIllegalName() {
		String[] files = {"test'.txt"};
		uploader.setFileNames(files);
		uploader.uploadBasedOnConfiguration();
		verify(mockView).showErrorMessage(DisplayConstants.ERROR_UPLOAD_TITLE, WebConstants.INVALID_ENTITY_NAME_MESSAGE);
	}

	@Test
	public void testUploadIllegalNameTrailingWhitespace() {
		String[] files = {"test.txt   "};
		uploader.setFileNames(files);
		uploader.uploadBasedOnConfiguration();
		verify(mockView).showErrorMessage(DisplayConstants.ERROR_UPLOAD_TITLE, WebConstants.INVALID_ENTITY_NAME_MESSAGE);
	}

	@Test
	public void testMkdirs() {
		// upload f1/f1a/test.txt. simulate first folder exists, and second folder must first be created.
		String folder1SynId = "syn232";
		AsyncMockStubber.callMixedWith(folder1SynId, new NotFoundException()).when(mockSynapseJavascriptClient).lookupChild(anyString(), anyString(), any(AsyncCallback.class));
		String folder1 = "f1";
		String folder2NewSynId = "syn500";
		String folder2 = "f1a";
		String relativePath = folder1 + "/" + folder2 + "/test.txt";
		when(mockSynapseJsniUtils.getWebkitRelativePath(any(), anyInt())).thenReturn(relativePath);
		when(mockFolder.getId()).thenReturn(folder2NewSynId);
		when(mockSynapseJavascriptClient.createEntity(any(Entity.class))).thenReturn(getDoneFuture(mockFolder));
		String[] files = {"test.txt"};
		uploader.setFileNames(files);

		uploader.uploadBasedOnConfiguration();

		// verify code tried to find the first folder name
		verify(mockSynapseJavascriptClient).lookupChild(eq(folder1), eq(parentEntityId), any(AsyncCallback.class));
		verify(mockSynapseJavascriptClient).lookupChild(eq(folder2), eq(folder1SynId), any(AsyncCallback.class));
		verify(mockSynapseJavascriptClient).createEntity(entityCaptor.capture());
		Folder newFolder = (Folder) entityCaptor.getValue();
		assertEquals(folder2, newFolder.getName());
		assertEquals(folder1SynId, newFolder.getParentId());
		verify(mockSynapseClient).getFileEntityIdWithSameName(anyString(), anyString(), any(AsyncCallback.class));
	}


	@Test
	public void testMkdirsFailureToLookupChild() {
		AsyncMockStubber.callFailureWith(new Exception("error")).when(mockSynapseJavascriptClient).lookupChild(anyString(), anyString(), any(AsyncCallback.class));
		String folder1 = "f1";
		String folder2 = "f1a";
		String relativePath = folder1 + "/" + folder2 + "/test.txt";
		when(mockSynapseJsniUtils.getWebkitRelativePath(any(), anyInt())).thenReturn(relativePath);
		String[] files = {"test.txt"};
		uploader.setFileNames(files);

		uploader.uploadBasedOnConfiguration();

		verify(mockSynapseJavascriptClient).lookupChild(eq(folder1), eq(parentEntityId), any(AsyncCallback.class));
		verifyUploadError();
	}

	@Test
	public void testMkdirsFailureToCreateFolder() {
		AsyncMockStubber.callFailureWith(new NotFoundException()).when(mockSynapseJavascriptClient).lookupChild(anyString(), anyString(), any(AsyncCallback.class));
		when(mockSynapseJavascriptClient.createEntity(any(Entity.class))).thenReturn(getFailedFuture(new Exception()));
		String folder1 = "f1";
		String folder2 = "f1a";
		String relativePath = folder1 + "/" + folder2 + "/test.txt";
		when(mockSynapseJsniUtils.getWebkitRelativePath(any(), anyInt())).thenReturn(relativePath);
		String[] files = {"test.txt"};
		uploader.setFileNames(files);

		uploader.uploadBasedOnConfiguration();

		verify(mockSynapseJavascriptClient).lookupChild(eq(folder1), eq(parentEntityId), any(AsyncCallback.class));
		verify(mockSynapseJavascriptClient).createEntity(entityCaptor.capture());
		assertEquals(folder1, entityCaptor.getValue().getName());
		verifyUploadError();
	}

}
