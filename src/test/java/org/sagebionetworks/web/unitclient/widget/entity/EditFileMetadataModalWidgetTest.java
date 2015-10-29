package org.sagebionetworks.web.unitclient.widget.entity;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.client.widget.entity.RenameEntityModalWidgetImpl.NAME_MUST_INCLUDE_AT_LEAST_ONE_CHARACTER;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.EditFileMetadataModalView;
import org.sagebionetworks.web.client.widget.entity.EditFileMetadataModalWidgetImpl;
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
	String startingFilename;
	String newName = "modified entity name";
	String newFileName = "modified file name";
			
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
		entity.setDataFileHandleId(dataFileHandleId);
		
		widget = new EditFileMetadataModalWidgetImpl(mockView, mockSynapseClient);
		when(mockView.getEntityName()).thenReturn(newName);
		when(mockView.getFileName()).thenReturn(newFileName);
	}
	
	@Test
	public void testConfigure(){
		widget.configure(entity, startingFilename, mockCallback);
		verify(mockView).clear();
		verify(mockView).show();
		verify(mockView).configure(startName, startingFilename);
	}
	
	@Test
	public void testNullName(){
		widget.configure(entity, startingFilename, mockCallback);
		when(mockView.getEntityName()).thenReturn(null);
		widget.onPrimary();
		verify(mockView).showError(NAME_MUST_INCLUDE_AT_LEAST_ONE_CHARACTER);
		verify(mockSynapseClient, never()).updateEntity(any(Entity.class), any(AsyncCallback.class));
		// should only be called on success
		verify(mockCallback, never()).invoke();
	}
	
	@Test
	public void testNullFileName(){
		widget.configure(entity, startingFilename, mockCallback);
		when(mockView.getFileName()).thenReturn(null);
		widget.onPrimary();
		verify(mockView).showError(EditFileMetadataModalWidgetImpl.FILE_NAME_MUST_INCLUDE_AT_LEAST_ONE_CHARACTER);
		verify(mockSynapseClient, never()).updateEntity(any(Entity.class), any(AsyncCallback.class));
		// should only be called on success
		verify(mockCallback, never()).invoke();
	}
	
	
	@Test
	public void testNoChange(){
		widget.configure(entity, startingFilename, mockCallback);
		when(mockView.getEntityName()).thenReturn(startName);
		when(mockView.getFileName()).thenReturn(startingFilename);
		
		// Calling save with no real change just closes the dialog.
		widget.onPrimary();
		verify(mockView, never()).setLoading(true);
		verify(mockView).hide();
		verify(mockSynapseClient, never()).updateEntity(any(Entity.class), any(AsyncCallback.class));
		// should only be called on success
		verify(mockCallback, never()).invoke();
	}
	
	@Test
	public void testHappyCase(){
		widget.configure(entity, startingFilename, mockCallback);
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseClient).updateEntity(any(Entity.class), any(AsyncCallback.class));
		// save button
		widget.onPrimary();
		verify(mockView).setLoading(true);
		verify(mockView).hide();
		verify(mockCallback).invoke();
		verify(mockSynapseClient).updateEntity(any(Entity.class), any(AsyncCallback.class));
		assertEquals(newFileName, entity.getFileNameOverride());
	}
	
	@Test
	public void testUpdateEntityNameNotFileName(){
		widget.configure(entity, startingFilename, mockCallback);
		when(mockView.getFileName()).thenReturn(startingFilename);
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseClient).updateEntity(any(Entity.class), any(AsyncCallback.class));
		// save button
		widget.onPrimary();
		verify(mockView).setLoading(true);
		verify(mockView).hide();
		verify(mockCallback).invoke();
		assertNull(entity.getFileNameOverride());
	}
	
	@Test
	public void testUpdateFailed(){
		Exception error = new Exception("an object already exists with that name");
		String newName = "a new name";
		widget.configure(entity, startingFilename, mockCallback);
		AsyncMockStubber.callFailureWith(error).when(mockSynapseClient).updateEntity(any(Entity.class), any(AsyncCallback.class));
		// save button
		widget.onPrimary();
		verify(mockView).setLoading(true);
		verify(mockView).showError(error.getMessage());
		verify(mockView).setLoading(false);
		verify(mockView, never()).hide();
		verify(mockCallback, never()).invoke();
	}

}
