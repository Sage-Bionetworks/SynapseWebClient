package org.sagebionetworks.web.unitclient.widget.entity.file;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.VersionInfo;
import org.sagebionetworks.repo.model.auth.UserEntityPermissions;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.repo.model.file.ExternalObjectStoreFileHandle;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.file.GoogleCloudFileHandle;
import org.sagebionetworks.repo.model.file.S3FileHandle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.SynapseProperties;
import org.sagebionetworks.web.client.events.DownloadListUpdatedEvent;
import org.sagebionetworks.web.client.widget.clienthelp.FileClientsHelp;
import org.sagebionetworks.web.client.widget.entity.EntityBadge;
import org.sagebionetworks.web.client.widget.entity.file.AddToDownloadList;
import org.sagebionetworks.web.client.widget.entity.file.FileDownloadMenuItem;
import org.sagebionetworks.web.client.widget.entity.file.FileTitleBar;
import org.sagebionetworks.web.client.widget.entity.file.FileTitleBarView;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

@RunWith(MockitoJUnitRunner.class)
public class FileTitleBarTest {
	public static final String ENTITY_ID = "syn123";
	FileTitleBar fileTitleBar;
	@Mock
	FileTitleBarView mockView;
	@Mock
	EntityBundle mockBundle;
	@Mock
	SynapseProperties mockSynapseProperties;
	@Mock
	org.sagebionetworks.repo.model.FileEntity mockFileEntity;
	S3FileHandle s3FileHandle;
	GoogleCloudFileHandle googleCloudFileHandle;
	Long synStorageLocationId = 1L;
	@Mock
	FileDownloadMenuItem mockFileDownloadButton;
	@Mock
	ExternalObjectStoreFileHandle mockExternalObjectStoreFileHandle;
	@Mock
	SynapseJSNIUtils mockSynapseJSNIUtils;
	@Mock
	SynapseJavascriptClient mockJsClient;
	@Mock
	FileClientsHelp mockFileClientsHelp;
	List<VersionInfo> versions;
	@Mock
	VersionInfo mockCurrentVersion;
	@Mock
	EventBus mockEventBus;
	@Mock
	UserEntityPermissions mockPermissions;

	public static final String DATA_FILE_HANDLE_ID = "872";
	public static final Long FILE_VERSION = 3L;
	public static final String FILE_NAME = "afile.txt";

	@Before
	public void setup() {
		fileTitleBar = new FileTitleBar(mockView, mockSynapseProperties, mockFileDownloadButton, mockJsClient, mockFileClientsHelp, mockEventBus, mockSynapseJSNIUtils);
		when(mockFileEntity.getId()).thenReturn(ENTITY_ID);
		when(mockFileEntity.getName()).thenReturn(FILE_NAME);
		when(mockFileEntity.getDataFileHandleId()).thenReturn(DATA_FILE_HANDLE_ID);
		when(mockFileEntity.getVersionNumber()).thenReturn(FILE_VERSION);
		when(mockBundle.getEntity()).thenReturn(mockFileEntity);
		when(mockBundle.getPermissions()).thenReturn(mockPermissions);
		when(mockPermissions.getCanDownload()).thenReturn(true);
		when(mockSynapseProperties.getSynapseProperty("org.sagebionetworks.portal.synapse_storage_id")).thenReturn(String.valueOf(synStorageLocationId));
		List<FileHandle> fileHandles = new LinkedList<FileHandle>();
		s3FileHandle = new S3FileHandle();
		s3FileHandle.setId(DATA_FILE_HANDLE_ID);
		s3FileHandle.setBucketName("testBucket");
		s3FileHandle.setKey("testKey");
		s3FileHandle.setStorageLocationId(synStorageLocationId);
		fileHandles.add(s3FileHandle);
		Mockito.when(mockBundle.getFileHandles()).thenReturn(fileHandles);
		verify(mockView).setFileDownloadMenuItem(any(Widget.class));
		versions = new ArrayList<>();
		AsyncMockStubber.callSuccessWith(versions).when(mockJsClient).getEntityVersions(anyString(), anyInt(), anyInt(), any());
		AsyncMockStubber.callSuccessWith(null).when(mockJsClient).addFileToDownloadList(anyString(), anyString(), any(AsyncCallback.class));
	}

	@Test
	public void testConstruction() {
		verify(mockView).setFileDownloadMenuItem(any(Widget.class));
		verify(mockView).setPresenter(fileTitleBar);
	}

	@Test
	public void testAsWidget() {
		fileTitleBar.asWidget();
	}

	@Test
	public void testSetS3DescriptionForSynapseStorage() {
		fileTitleBar.configure(mockBundle);
		verify(mockView).setFileLocation("| Synapse Storage");
	}

	@Test
	public void testSetS3DescriptionForExternalS3() {
		s3FileHandle.setStorageLocationId(2L);
		fileTitleBar.configure(mockBundle);
		verify(mockView).setFileLocation("| s3://" + s3FileHandle.getBucketName() + "/" + s3FileHandle.getKey());
	}

	@Test
	public void testSetGoogleCloudDescriptionForExternalGoogleCloud() {
		googleCloudFileHandle = new GoogleCloudFileHandle();
		googleCloudFileHandle.setId(DATA_FILE_HANDLE_ID);
		googleCloudFileHandle.setBucketName("testBucket");
		googleCloudFileHandle.setKey("testKey");
		googleCloudFileHandle.setStorageLocationId(synStorageLocationId);
		googleCloudFileHandle.setStorageLocationId(2L);
		when(mockBundle.getFileHandles()).thenReturn(Collections.singletonList(googleCloudFileHandle));
		fileTitleBar.configure(mockBundle);
		verify(mockView).setFileLocation("| gs://" + googleCloudFileHandle.getBucketName() + "/" + googleCloudFileHandle.getKey());
	}

	@Test
	public void testConfigure() {
		fileTitleBar.configure(mockBundle);
		verify(mockFileDownloadButton).configure(mockBundle);
		verify(mockView).setVersion(FILE_VERSION);
		verify(mockView).setCanDownload(true); // set up in the Before
	}

	@Test
	public void testCannotDownload() {
		when(mockPermissions.getCanDownload()).thenReturn(false);

		fileTitleBar.configure(mockBundle);

		verify(mockView).setCanDownload(false);
	}

	@Test
	public void testAddToDownloadList() {
		fileTitleBar.configure(mockBundle);
		fileTitleBar.onAddToDownloadList();

		verify(mockJsClient).addFileToDownloadList(eq(DATA_FILE_HANDLE_ID), eq(ENTITY_ID), any(AsyncCallback.class));
		verify(mockView).showAddedToDownloadListAlert(FILE_NAME + EntityBadge.ADDED_TO_DOWNLOAD_LIST);
		verify(mockEventBus).fireEvent(any(DownloadListUpdatedEvent.class));
		verify(mockSynapseJSNIUtils).sendAnalyticsEvent(AddToDownloadList.DOWNLOAD_ACTION_EVENT_NAME, AddToDownloadList.FILES_ADDED_TO_DOWNLOAD_LIST_EVENT_NAME, Integer.toString(1));
	}

	@Test
	public void testAddToDownloadListFailure() {
		String errorMessage = "unable to add";
		AsyncMockStubber.callFailureWith(new Exception(errorMessage)).when(mockJsClient).addFileToDownloadList(anyString(), anyString(), any(AsyncCallback.class));

		fileTitleBar.configure(mockBundle);
		fileTitleBar.onAddToDownloadList();

		verify(mockJsClient).addFileToDownloadList(eq(DATA_FILE_HANDLE_ID), eq(ENTITY_ID), any(AsyncCallback.class));
		verify(mockView).showErrorMessage(errorMessage);
	}

	@Test
	public void testExternalObjectStoreFileHandle() {
		String md5 = "878ac";
		String endpoint = "https://test.test";
		String bucket = "mybucket";
		String fileKey = "567898765sdfgfd/test.txt";
		when(mockExternalObjectStoreFileHandle.getId()).thenReturn(DATA_FILE_HANDLE_ID);
		when(mockExternalObjectStoreFileHandle.getContentMd5()).thenReturn(md5);
		when(mockExternalObjectStoreFileHandle.getContentSize()).thenReturn(null);

		when(mockExternalObjectStoreFileHandle.getEndpointUrl()).thenReturn(endpoint);
		when(mockExternalObjectStoreFileHandle.getBucket()).thenReturn(bucket);
		when(mockExternalObjectStoreFileHandle.getFileKey()).thenReturn(fileKey);

		Mockito.when(mockBundle.getFileHandles()).thenReturn(Collections.singletonList((FileHandle) mockExternalObjectStoreFileHandle));
		fileTitleBar.configure(mockBundle);
		verify(mockFileDownloadButton).configure(mockBundle);
		verify(mockView).setExternalObjectStoreUIVisible(true);
		verify(mockView).setExternalObjectStoreInfo(endpoint, bucket, fileKey);
	}

	@Test
	public void testGetEntityVersionsShowingCurrentVersion() {
		VersionInfo currentVersion = new VersionInfo();
		currentVersion.setVersionNumber(FILE_VERSION);
		versions.add(currentVersion);

		fileTitleBar.configure(mockBundle);

		verify(mockJsClient).getEntityVersions(anyString(), anyInt(), anyInt(), any());
		verify(mockView, times(2)).setVersionUIVisible(false);
		verify(mockView, never()).setVersionUIVisible(true);
	}

	@Test
	public void testGetEntityVersionsShowingOldVersion() {
		VersionInfo currentVersion = new VersionInfo();
		currentVersion.setVersionNumber(8L);
		versions.add(currentVersion);

		fileTitleBar.configure(mockBundle);

		verify(mockJsClient).getEntityVersions(anyString(), anyInt(), anyInt(), any());
		verify(mockView).setVersionUIVisible(true);
	}

	@Test
	public void testGetEntityVersionsFailure() {
		String error = "unable to get versions";
		AsyncMockStubber.callFailureWith(new Exception(error)).when(mockJsClient).getEntityVersions(anyString(), anyInt(), anyInt(), any());

		fileTitleBar.configure(mockBundle);

		verify(mockJsClient).getEntityVersions(anyString(), anyInt(), anyInt(), any());
		verify(mockView).setVersionUIVisible(false);
		verify(mockView, never()).setVersionUIVisible(true);
		verify(mockView).showErrorMessage(error);
	}

	@Test
	public void testOnProgrammaticDownloadOptions() {
		fileTitleBar.configure(mockBundle);

		fileTitleBar.onProgrammaticDownloadOptions();

		verify(mockFileClientsHelp).configureAndShow(ENTITY_ID, FILE_VERSION);
	}
}
