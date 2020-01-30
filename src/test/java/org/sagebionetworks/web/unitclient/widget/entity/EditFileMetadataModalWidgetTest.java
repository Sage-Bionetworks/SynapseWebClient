package org.sagebionetworks.web.unitclient.widget.entity;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.client.widget.entity.RenameEntityModalWidgetImpl.NAME_MUST_INCLUDE_AT_LEAST_ONE_CHARACTER;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.file.FileHandleAssociateType;
import org.sagebionetworks.repo.model.file.FileHandleAssociation;
import org.sagebionetworks.repo.model.file.FileHandleCopyRequest;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.EditFileMetadataModalView;
import org.sagebionetworks.web.client.widget.entity.EditFileMetadataModalWidgetImpl;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.user.client.rpc.AsyncCallback;

@RunWith(MockitoJUnitRunner.class)
public class EditFileMetadataModalWidgetTest {
	@Mock
	EditFileMetadataModalView mockView;
	@Mock
	SynapseClientAsync mockSynapseClient;
	@Mock
	SynapseJavascriptClient mockJsClient;
	@Mock
	Callback mockCallback;
	String parentId;
	EditFileMetadataModalWidgetImpl widget;
	@Mock
	FileEntity mockFileEntity;

	public static final String ENTITY_ID = "syn007";
	public static final String OLD_NAME = "Start Name";
	public static final String OLD_DATA_FILE_HANDLE_ID = "1";
	public static final String OLD_FILENAME = "temp.txt";
	public static final String OLD_CONTENT_TYPE = "txt/plain";

	public static final String NEW_NAME = "modified entity name";
	public static final String NEW_FILENAME = "modified file name";
	public static final String NEW_CONTENT_TYPE = "modified content type";
	@Mock
	FileHandle mockFileHandle;

	@Before
	public void before() {
		when(mockFileEntity.getName()).thenReturn(OLD_NAME);
		when(mockFileEntity.getDataFileHandleId()).thenReturn(OLD_DATA_FILE_HANDLE_ID);
		when(mockFileHandle.getContentType()).thenReturn(OLD_CONTENT_TYPE);
		when(mockFileHandle.getFileName()).thenReturn(OLD_FILENAME);
		when(mockFileHandle.getId()).thenReturn(OLD_DATA_FILE_HANDLE_ID);
		when(mockFileEntity.getId()).thenReturn(ENTITY_ID);

		widget = new EditFileMetadataModalWidgetImpl(mockView, mockSynapseClient, mockJsClient);
		when(mockView.getEntityName()).thenReturn(NEW_NAME);
		when(mockView.getFileName()).thenReturn(NEW_FILENAME);
		when(mockView.getContentType()).thenReturn(NEW_CONTENT_TYPE);
	}

	@Test
	public void testConfigure() {
		widget.configure(mockFileEntity, mockFileHandle, mockCallback);
		verify(mockView).clear();
		verify(mockView).show();
		verify(mockView).configure(OLD_NAME, OLD_FILENAME, OLD_CONTENT_TYPE);
	}

	@Test
	public void testNullName() {
		widget.configure(mockFileEntity, mockFileHandle, mockCallback);
		when(mockView.getEntityName()).thenReturn(null);
		widget.onPrimary();
		verify(mockView).showError(NAME_MUST_INCLUDE_AT_LEAST_ONE_CHARACTER);
		verifyZeroInteractions(mockSynapseClient);
		// should only be called on success
		verify(mockCallback, never()).invoke();
	}

	@Test
	public void testNullFileName() {
		widget.configure(mockFileEntity, mockFileHandle, mockCallback);
		when(mockView.getFileName()).thenReturn(null);
		widget.onPrimary();
		verify(mockView).showError(EditFileMetadataModalWidgetImpl.FILE_NAME_MUST_INCLUDE_AT_LEAST_ONE_CHARACTER);
		verifyZeroInteractions(mockSynapseClient);
		// should only be called on success
		verify(mockCallback, never()).invoke();
	}

	@Test
	public void testNullContentType() {
		widget.configure(mockFileEntity, mockFileHandle, mockCallback);
		when(mockView.getContentType()).thenReturn(null);
		widget.onPrimary();

		// ok to clear out content type
		verify(mockSynapseClient).updateFileEntity(any(FileEntity.class), any(FileHandleCopyRequest.class), any(AsyncCallback.class));

		// should only be called on success
		verify(mockCallback, never()).invoke();
	}


	@Test
	public void testNoChange() {
		widget.configure(mockFileEntity, mockFileHandle, mockCallback);
		when(mockView.getEntityName()).thenReturn(OLD_NAME);
		when(mockView.getFileName()).thenReturn(OLD_FILENAME);
		when(mockView.getContentType()).thenReturn(OLD_CONTENT_TYPE);

		// Calling save with no real change just closes the dialog.
		widget.onPrimary();
		verify(mockView, never()).setLoading(true);
		verify(mockView).hide();
		verify(mockJsClient, never()).updateEntity(any(Entity.class), anyString(), anyBoolean(), any(AsyncCallback.class));
		// should only be called on success
		verify(mockCallback, never()).invoke();
	}

	@Test
	public void testUpdateEntity() {
		// only the entity name has changed
		when(mockView.getFileName()).thenReturn(OLD_FILENAME);
		when(mockView.getContentType()).thenReturn(OLD_CONTENT_TYPE);

		widget.configure(mockFileEntity, mockFileHandle, mockCallback);
		AsyncMockStubber.callSuccessWith(null).when(mockJsClient).updateEntity(any(Entity.class), anyString(), anyBoolean(), any(AsyncCallback.class));

		// save button
		widget.onPrimary();
		verify(mockView).setLoading(true);
		verify(mockView).hide();
		verify(mockCallback).invoke();
		verify(mockJsClient).updateEntity(any(Entity.class), anyString(), anyBoolean(), any(AsyncCallback.class));
		verify(mockFileEntity).setName(NEW_NAME);
	}

	@Test
	public void testUpdateFileEntityHandle() {
		// simulate user changed all three values
		widget.configure(mockFileEntity, mockFileHandle, mockCallback);
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseClient).updateFileEntity(any(FileEntity.class), any(FileHandleCopyRequest.class), any(AsyncCallback.class));
		// save button
		widget.onPrimary();
		verify(mockFileEntity).setFileNameOverride(null);
		verify(mockView).setLoading(true);
		verify(mockView).hide();
		verify(mockCallback).invoke();
		ArgumentCaptor<FileHandleCopyRequest> captor = ArgumentCaptor.forClass(FileHandleCopyRequest.class);
		verify(mockSynapseClient).updateFileEntity(any(FileEntity.class), captor.capture(), any(AsyncCallback.class));
		FileHandleCopyRequest fileHandleCopyRequest = captor.getValue();
		assertEquals(NEW_CONTENT_TYPE, fileHandleCopyRequest.getNewContentType());
		assertEquals(NEW_FILENAME, fileHandleCopyRequest.getNewFileName());
		FileHandleAssociation fha = fileHandleCopyRequest.getOriginalFile();
		assertEquals(ENTITY_ID, fha.getAssociateObjectId());
		assertEquals(FileHandleAssociateType.FileEntity, fha.getAssociateObjectType());
		assertEquals(OLD_DATA_FILE_HANDLE_ID, fha.getFileHandleId());
		verify(mockFileEntity).setName(NEW_NAME);
	}

	@Test
	public void testUpdateEntityFailed() {
		// only the entity name has changed
		when(mockView.getFileName()).thenReturn(OLD_FILENAME);
		when(mockView.getContentType()).thenReturn(OLD_CONTENT_TYPE);

		Exception error = new Exception("an object already exists with that name");
		widget.configure(mockFileEntity, mockFileHandle, mockCallback);
		AsyncMockStubber.callFailureWith(error).when(mockJsClient).updateEntity(any(Entity.class), anyString(), anyBoolean(), any(AsyncCallback.class));
		// save button
		widget.onPrimary();
		verify(mockView).setLoading(true);
		verify(mockView).showError(error.getMessage());
		verify(mockView).setLoading(false);
		verify(mockView, never()).hide();
		verify(mockCallback, never()).invoke();
	}

	@Test
	public void testUpdateFileEntityHandleFailed() {
		Exception error = new Exception("something wrong with file name, content type, or entity name");
		widget.configure(mockFileEntity, mockFileHandle, mockCallback);
		AsyncMockStubber.callFailureWith(error).when(mockSynapseClient).updateFileEntity(any(FileEntity.class), any(FileHandleCopyRequest.class), any(AsyncCallback.class));
		// save button
		widget.onPrimary();
		verify(mockView).setLoading(true);
		verify(mockView).showError(error.getMessage());
		verify(mockView).setLoading(false);
		verify(mockView, never()).hide();
		verify(mockCallback, never()).invoke();
	}

}
