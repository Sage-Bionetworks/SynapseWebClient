package org.sagebionetworks.web.unitclient.widget.entity;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.client.widget.entity.RenameEntityModalWidgetImpl.NAME_MUST_INCLUDE_AT_LEAST_ONE_CHARACTER;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.VersionInfo;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.file.S3FileHandle;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.EditFileMetadataModalView;
import org.sagebionetworks.web.client.widget.entity.EditFileMetadataModalWidgetImpl;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class EditFileMetadataModalWidgetTest {

	EditFileMetadataModalView mockView;
	SynapseClientAsync mockSynapseClient;
	Callback mockCallback;
	String startName;
	String parentId;
	EditFileMetadataModalWidgetImpl widget;
	FileEntity entity;
	List<FileHandle> entityFileHandles;
	Long versionNumber;
	String startingFilename, startingContentType;
	String newName = "modified entity name";
	String newFileName = "modified file name";
	String newContentType = "application/zip";
			
	@Before
	public void before(){
		mockView = mock(EditFileMetadataModalView.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockCallback = mock(Callback.class);
		entity = new FileEntity();
		startName = "Start Name";
		entity.setName(startName);
		String dataFileHandleId = "1";
		startingFilename = "temp.txt";
		startingContentType = "text/plain";
		versionNumber = 3L;
		entity.setVersionNumber(versionNumber);
		entity.setDataFileHandleId(dataFileHandleId);
		entityFileHandles = new ArrayList<FileHandle>();
		S3FileHandle mockHandle = mock(S3FileHandle.class);
		when(mockHandle.getId()).thenReturn(dataFileHandleId);
		when(mockHandle.getFileName()).thenReturn(startingFilename);
		when(mockHandle.getContentType()).thenReturn(startingContentType);
		
		entityFileHandles.add(mockHandle);
		widget = new EditFileMetadataModalWidgetImpl(mockView, mockSynapseClient);
		PaginatedResults<VersionInfo> results = new PaginatedResults<VersionInfo>();
		VersionInfo versionInfo = new VersionInfo();
		versionInfo.setVersionNumber(versionNumber);
		results.setResults(Collections.singletonList(versionInfo));
		//by default, set up so that versionNumber 3 is the current version
		AsyncMockStubber.callSuccessWith(results).when(mockSynapseClient).getEntityVersions(anyString(), anyInt(), anyInt(), any(AsyncCallback.class));
		
		when(mockView.getEntityName()).thenReturn(newName);
		when(mockView.getFileName()).thenReturn(newFileName);
		when(mockView.getContentType()).thenReturn(newContentType);
	}
	
	@Test
	public void testConfigure(){
		widget.configure(entity, entityFileHandles, mockCallback);
		verify(mockView).clear();
		verify(mockView).show();
		verify(mockView).configure(startName, startingFilename, startingContentType);
	}
	
	@Test
	public void testNullName(){
		widget.configure(entity, entityFileHandles, mockCallback);
		when(mockView.getEntityName()).thenReturn(null);
		widget.onPrimary();
		verify(mockView).showError(NAME_MUST_INCLUDE_AT_LEAST_ONE_CHARACTER);
		verify(mockSynapseClient, never()).updateFileEntity(any(FileEntity.class), anyString(), anyString(), anyString(), any(AsyncCallback.class));
		// should only be called on success
		verify(mockCallback, never()).invoke();
	}
	
	@Test
	public void testNullFileName(){
		widget.configure(entity, entityFileHandles, mockCallback);
		when(mockView.getFileName()).thenReturn(null);
		widget.onPrimary();
		verify(mockView).showError(EditFileMetadataModalWidgetImpl.FILE_NAME_MUST_INCLUDE_AT_LEAST_ONE_CHARACTER);
		verify(mockSynapseClient, never()).updateFileEntity(any(FileEntity.class), anyString(), anyString(), anyString(), any(AsyncCallback.class));
		// should only be called on success
		verify(mockCallback, never()).invoke();
	}
	
	@Test
	public void testNullContentType(){
		widget.configure(entity, entityFileHandles, mockCallback);
		when(mockView.getContentType()).thenReturn(null);
		widget.onPrimary();
		verify(mockView).showError(EditFileMetadataModalWidgetImpl.FILE_CONTENT_TYPE_MUST_INCLUDE_AT_LEAST_ONE_CHARACTER);
		verify(mockSynapseClient, never()).updateFileEntity(any(FileEntity.class), anyString(), anyString(), anyString(), any(AsyncCallback.class));
		// should only be called on success
		verify(mockCallback, never()).invoke();
	}

	
	@Test
	public void testNoChange(){
		widget.configure(entity, entityFileHandles, mockCallback);
		when(mockView.getEntityName()).thenReturn(startName);
		when(mockView.getFileName()).thenReturn(startingFilename);
		when(mockView.getContentType()).thenReturn(startingContentType);
		
		// Calling save with no real change just closes the dialog.
		widget.onPrimary();
		verify(mockView, never()).setLoading(true);
		verify(mockView).hide();
		verify(mockSynapseClient, never()).updateFileEntity(any(FileEntity.class), anyString(), anyString(), anyString(), any(AsyncCallback.class));
		// should only be called on success
		verify(mockCallback, never()).invoke();
	}
	
	@Test
	public void testHappyCase(){
		widget.configure(entity, entityFileHandles, mockCallback);
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseClient).updateFileEntity(any(FileEntity.class), anyString(), anyString(), anyString(), any(AsyncCallback.class));
		// save button
		widget.onPrimary();
		verify(mockView).setLoading(true);
		verify(mockView).hide();
		verify(mockCallback).invoke();
	}
	
	@Test
	public void testUpdateFailed(){
		Exception error = new Exception("an object already exists with that name");
		String newName = "a new name";
		widget.configure(entity, entityFileHandles, mockCallback);
		AsyncMockStubber.callFailureWith(error).when(mockSynapseClient).updateFileEntity(any(FileEntity.class), anyString(), anyString(), anyString(), any(AsyncCallback.class));
		// save button
		widget.onPrimary();
		verify(mockView).setLoading(true);
		verify(mockView).showError(error.getMessage());
		verify(mockView).setLoading(false);
		verify(mockView, never()).hide();
		verify(mockCallback, never()).invoke();
	}

	@Test
	public void testEditOldVersion() {
		//set the current version to a different number than the file entity version being edited.
		PaginatedResults<VersionInfo> results = new PaginatedResults<VersionInfo>();
		VersionInfo versionInfo = new VersionInfo();
		versionInfo.setVersionNumber(45L);
		results.setResults(Collections.singletonList(versionInfo));
		AsyncMockStubber.callSuccessWith(results).when(mockSynapseClient).getEntityVersions(anyString(), anyInt(), anyInt(), any(AsyncCallback.class));
		
		widget.configure(entity, entityFileHandles, mockCallback);
		verify(mockView).showErrorPopup(EditFileMetadataModalWidgetImpl.CURRENT_VERSION_ONLY_MESSAGE);
	}
}
