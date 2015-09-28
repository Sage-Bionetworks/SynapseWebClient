package org.sagebionetworks.web.unitclient.widget.entity.file;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.file.S3FileHandle;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.entity.file.FileTitleBar;
import org.sagebionetworks.web.client.widget.entity.file.FileTitleBarView;

import junit.framework.Assert;

public class FileTitleBarTest {
		
	FileTitleBar fileTitleBar;
	FileTitleBarView mockView;
	AuthenticationController mockAuthController;
	SynapseClientAsync mockSynapseClient;
	EntityBundle mockBundle;
	GlobalApplicationState mockGlobalAppState;
	org.sagebionetworks.repo.model.FileEntity mockFileEntity;
	S3FileHandle handle;
	Long synStorageLocationId = 1L;
	@Before
	public void setup(){	
		mockView = mock(FileTitleBarView.class);
		mockAuthController = mock(AuthenticationController.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockGlobalAppState = mock(GlobalApplicationState.class);
		fileTitleBar = new FileTitleBar(mockView, mockAuthController, mockSynapseClient, mockGlobalAppState);
		mockBundle = mock(EntityBundle.class);
		mockFileEntity = mock(org.sagebionetworks.repo.model.FileEntity.class);
		Mockito.when(mockFileEntity.getId()).thenReturn("syn123");
		Mockito.when(mockFileEntity.getName()).thenReturn("syn123");
		Mockito.when(mockFileEntity.getDataFileHandleId()).thenReturn("syn123");
		Mockito.when(mockBundle.getEntity()).thenReturn(mockFileEntity);
		Mockito.when(mockGlobalAppState.getSynapseProperty("org.sagebionetworks.portal.synapse_storage_id"))
				.thenReturn(String.valueOf(synStorageLocationId));
		List<FileHandle> fileHandles = new LinkedList<FileHandle>();
		handle = new S3FileHandle();
		handle.setId("syn123");
		handle.setBucketName("testBucket");
		handle.setKey("testKey");
		handle.setStorageLocationId(synStorageLocationId);
		fileHandles.add(handle);
		Mockito.when(mockBundle.getFileHandles()).thenReturn(fileHandles);
		verify(mockView).setPresenter(fileTitleBar);
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
		fileTitleBar.setEntityBundle(mockBundle);
		fileTitleBar.setS3Description();
		verify(mockView).setFileLocation("| Synapse Storage");
	}

	@Test
	public void testSetS3DescriptionForExternalS3() {
		handle.setStorageLocationId(2L);
		fileTitleBar.setEntityBundle(mockBundle);
		fileTitleBar.setS3Description();
		verify(mockView).setFileLocation("| s3://" + handle.getBucketName() + "/" + handle.getKey() + "/" + "syn123");
	}
}
