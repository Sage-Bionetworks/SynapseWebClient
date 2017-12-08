package org.sagebionetworks.web.unitclient.widget.entity.file;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.file.ExternalObjectStoreFileHandle;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.file.S3FileHandle;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.widget.entity.file.FileDownloadButton;
import org.sagebionetworks.web.client.widget.entity.file.FileTitleBar;
import org.sagebionetworks.web.client.widget.entity.file.FileTitleBarView;

import com.google.gwt.user.client.ui.Widget;

import junit.framework.Assert;

public class FileTitleBarTest {
		
	FileTitleBar fileTitleBar;
	@Mock
	FileTitleBarView mockView;
	@Mock
	EntityBundle mockBundle;
	@Mock
	GlobalApplicationState mockGlobalAppState;
	@Mock
	org.sagebionetworks.repo.model.FileEntity mockFileEntity;
	S3FileHandle handle;
	Long synStorageLocationId = 1L;
	@Mock
	FileDownloadButton mockFileDownloadButton;
	@Mock
	EntityUpdatedHandler mockEntityUpdatedHandler;
	@Mock
	ExternalObjectStoreFileHandle mockExternalObjectStoreFileHandle;
	public static final String DATA_FILE_HANDLE_ID = "872";
	@Before
	public void setup(){
		MockitoAnnotations.initMocks(this);
		fileTitleBar = new FileTitleBar(mockView, mockGlobalAppState, mockFileDownloadButton);
		Mockito.when(mockFileEntity.getId()).thenReturn("syn123");
		Mockito.when(mockFileEntity.getName()).thenReturn("syn123");
		Mockito.when(mockFileEntity.getDataFileHandleId()).thenReturn(DATA_FILE_HANDLE_ID);
		Mockito.when(mockBundle.getEntity()).thenReturn(mockFileEntity);
		Mockito.when(mockGlobalAppState.getSynapseProperty("org.sagebionetworks.portal.synapse_storage_id"))
				.thenReturn(String.valueOf(synStorageLocationId));
		List<FileHandle> fileHandles = new LinkedList<FileHandle>();
		handle = new S3FileHandle();
		handle.setId(DATA_FILE_HANDLE_ID);
		handle.setBucketName("testBucket");
		handle.setKey("testKey");
		handle.setStorageLocationId(synStorageLocationId);
		fileHandles.add(handle);
		Mockito.when(mockBundle.getFileHandles()).thenReturn(fileHandles);
		verify(mockView).setFileDownloadButton(any(Widget.class));
	}
	
	@Test
	public void testAsWidget(){
		fileTitleBar.asWidget();
	}
	
	@Test
	public void testIsDataNotInFile() {
		FileEntity fileEntity = new FileEntity();
		fileEntity.setDataFileHandleId(null);
		Assert.assertFalse(FileTitleBar.isDataPossiblyWithin(fileEntity));
	}
	
	@Test
	public void testIsDataInFile() {
		FileEntity fileEntity = new FileEntity();
		fileEntity.setDataFileHandleId("123");
		Assert.assertTrue(FileTitleBar.isDataPossiblyWithin(fileEntity));
	}

	@Test
	public void testSetS3DescriptionForSynapseStorage() {
		fileTitleBar.configure(mockBundle);
		verify(mockView).setFileLocation("| Synapse Storage");
	}

	@Test
	public void testSetS3DescriptionForExternalS3() {
		handle.setStorageLocationId(2L);
		fileTitleBar.configure(mockBundle);
		verify(mockView).setFileLocation("| s3://" + handle.getBucketName() + "/" + handle.getKey());
	}
	
	@Test
	public void testConfigure() {
		FileEntity fileEntity = new FileEntity();
		when(mockBundle.getEntity()).thenReturn(fileEntity);
		fileTitleBar.configure(mockBundle);
		verify(mockFileDownloadButton).configure(mockBundle);
	}
	
	@Test
	public void testSetEntityUpdateHandler() {
		fileTitleBar.setEntityUpdatedHandler(mockEntityUpdatedHandler);
		verify(mockFileDownloadButton).setEntityUpdatedHandler(mockEntityUpdatedHandler);
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
		
		Mockito.when(mockBundle.getFileHandles()).thenReturn(Collections.singletonList((FileHandle)mockExternalObjectStoreFileHandle));
		fileTitleBar.configure(mockBundle);
		verify(mockFileDownloadButton).configure(mockBundle);
		verify(mockView).setExternalObjectStoreUIVisible(true);
		verify(mockView).setExternalObjectStoreInfo(endpoint, bucket, fileKey);
	}
}
