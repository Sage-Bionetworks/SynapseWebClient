package org.sagebionetworks.web.unitclient.widget.entity.browse;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.AutoGenFactory;
import org.sagebionetworks.repo.model.Folder;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.entity.browse.FilesBrowser;
import org.sagebionetworks.web.client.widget.entity.browse.FilesBrowserView;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class FilesBrowserTest {

	FilesBrowserView mockView;
	SynapseClientAsync mockSynapseClient;
	NodeModelCreator mockNodeModelCreator;
	AdapterFactory adapterFactory;
	AutoGenFactory autoGenFactory;
	GlobalApplicationState mockGlobalApplicationState;
	AuthenticationController mockAuthenticationController;
	FilesBrowser filesBrowser;
	
	String configuredEntityId = "syn123";
	
	@Before
	public void before() throws JSONObjectAdapterException {
		mockView = mock(FilesBrowserView.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockNodeModelCreator = mock(NodeModelCreator.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		adapterFactory = new AdapterFactoryImpl();
		autoGenFactory = new AutoGenFactory();
		filesBrowser = new FilesBrowser(mockView, mockSynapseClient,
				mockNodeModelCreator, adapterFactory, autoGenFactory,
				mockGlobalApplicationState, mockAuthenticationController);
		verify(mockView).setPresenter(filesBrowser);
		filesBrowser.configure(configuredEntityId);
		reset(mockView);
	}
	
	@Test
	public void testConfigure() {		
		String entityId = "syn123";
		filesBrowser.configure(entityId);
		verify(mockView).configure(entityId, false);
		reset(mockView);
		
		String title = "title";
		filesBrowser.configure(entityId, title);
		verify(mockView).configure(entityId, false, title);
		reset(mockView);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testCreateFolder() throws Exception {
		String newId = "syn456";
		AsyncMockStubber.callSuccessWith(newId).when(mockSynapseClient).createOrUpdateEntity(anyString(), anyString(), eq(true), any(AsyncCallback.class));
		
		filesBrowser.createFolder();
		verify(mockSynapseClient).createOrUpdateEntity(anyString(), anyString(), eq(true), any(AsyncCallback.class));
		verify(mockView).showFolderEditDialog(anyString());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testCreateFolderFail() throws Exception {
		AsyncMockStubber.callFailureWith(new Exception()).when(mockSynapseClient).createOrUpdateEntity(anyString(), anyString(), eq(true), any(AsyncCallback.class));
		
		filesBrowser.createFolder();
		
		verify(mockSynapseClient).createOrUpdateEntity(anyString(), anyString(), eq(true), any(AsyncCallback.class));
		verify(mockView).showErrorMessage(DisplayConstants.ERROR_FOLDER_CREATION_FAILED);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testDeleteFolder() throws Exception {
		String id = "syn456";
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseClient).deleteEntityById(anyString(), any(AsyncCallback.class));
		
		filesBrowser.deleteFolder(id);
		verify(mockSynapseClient).deleteEntityById(anyString(), any(AsyncCallback.class));
		verify(mockView).refreshTreeView(anyString());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testDeleteFolderFail() throws Exception {
		String id = "syn456";
		AsyncMockStubber.callFailureWith(new Exception()).when(mockSynapseClient).deleteEntityById(anyString(), any(AsyncCallback.class));
		
		filesBrowser.deleteFolder(id);
		verify(mockSynapseClient).deleteEntityById(anyString(), any(AsyncCallback.class));
		
		verify(mockView).showErrorMessage(DisplayConstants.ERROR_FOLDER_DELETE_FAILED);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testUpdateFolderName() throws Exception {
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseClient).updateEntity(anyString(), any(AsyncCallback.class));
		Folder f = new Folder();
		f.setName("raven");
		filesBrowser.updateFolderName(f);
		verify(mockSynapseClient).updateEntity(anyString(), any(AsyncCallback.class));
		verify(mockView).showInfo(anyString(), anyString());
		verify(mockView).refreshTreeView(configuredEntityId);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testUpdateFolderNameFail() throws Exception {
		AsyncMockStubber.callFailureWith(new Exception()).when(mockSynapseClient).updateEntity(anyString(), any(AsyncCallback.class));
		
		Folder f = new Folder();
		f.setName("raven");
		filesBrowser.updateFolderName(f);
		
		verify(mockSynapseClient).updateEntity(anyString(), any(AsyncCallback.class));
		verify(mockView).showErrorMessage(DisplayConstants.ERROR_FOLDER_RENAME_FAILED);
	}
}











