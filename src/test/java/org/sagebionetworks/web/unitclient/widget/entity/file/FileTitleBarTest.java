package org.sagebionetworks.web.unitclient.widget.entity.file;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.ACTAccessRequirement;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.Code;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.LocationData;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.web.client.EntityTypeProvider;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.entity.EntityEditor;
import org.sagebionetworks.web.client.widget.entity.file.FileTitleBar;
import org.sagebionetworks.web.client.widget.entity.file.FileTitleBarView;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class FileTitleBarTest {
		
	FileTitleBar fileTitleBar;
	FileTitleBarView mockView;
	AuthenticationController mockAuthController;
	EntityTypeProvider mockEntityTypeProvider;
	SynapseClientAsync mockSynapseClient;
	EntityEditor mockEntityEditor;
	@Before
	public void setup(){	
		mockView = mock(FileTitleBarView.class);
		mockAuthController = mock(AuthenticationController.class);
		mockEntityTypeProvider = mock(EntityTypeProvider.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockEntityEditor = mock(EntityEditor.class);
		fileTitleBar = new FileTitleBar(mockView, mockAuthController, mockEntityTypeProvider, mockSynapseClient, mockEntityEditor);
		
		verify(mockView).setPresenter(fileTitleBar);
	}
	
	@Test
	public void testAsWidget(){
		fileTitleBar.asWidget();
	}
	
	@Test
	public void testUpdateNodeStorageUsage() {
		//updating node storage usage is wired to synapse client getStorageUsage (and result is completely based on the return of that call)
		final Long testSize = 1234l;
		fileTitleBar.setEntityBundle(new EntityBundle(new Project(), null, null, null, null, null, null));
		AsyncMockStubber.callSuccessWith(testSize).when(mockSynapseClient).getStorageUsage(anyString(), any(AsyncCallback.class));
		fileTitleBar.updateNodeStorageUsage(new AsyncCallback<Long>() {
			@Override
			public void onSuccess(Long result) {
				Assert.assertEquals(testSize, result);
			}
			@Override
			public void onFailure(Throwable caught) {
				Assert.fail(caught.getMessage());
			}
		});
		
		verify(mockSynapseClient).getStorageUsage(anyString(), (AsyncCallback<Long>) any());
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

}
