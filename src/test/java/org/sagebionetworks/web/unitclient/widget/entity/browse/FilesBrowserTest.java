package org.sagebionetworks.web.unitclient.widget.entity.browse;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.sagebionetworks.repo.model.AutoGenFactory;
import org.sagebionetworks.repo.model.Folder;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.SynapseClientAsync;
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
	
	FilesBrowser filesBrowser;
	
	String configuredEntityId = "syn123";
	
	@Before
	public void before() throws JSONObjectAdapterException {
		mockView = mock(FilesBrowserView.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockNodeModelCreator = mock(NodeModelCreator.class);
		adapterFactory = new AdapterFactoryImpl();
		autoGenFactory = new AutoGenFactory();
		filesBrowser = new FilesBrowser(mockView, mockSynapseClient, mockNodeModelCreator, adapterFactory, autoGenFactory);
		verify(mockView).setPresenter(filesBrowser);
		filesBrowser.configure(configuredEntityId);
		reset(mockView);
	}
	
	@Test
	public void testConfigure() {		
		String entityId = "syn123";
		filesBrowser.configure(entityId);
		verify(mockView).configure(entityId);
		reset(mockView);
		
		String title = "title";
		filesBrowser.configure(entityId, title);
		verify(mockView).configure(entityId, title);
		reset(mockView);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testCreateFolder() throws Exception {
		String name = "folder name";
		String newId = "syn456";
		AsyncMockStubber.callSuccessWith(newId).when(mockSynapseClient).createOrUpdateEntity(anyString(), anyString(), eq(true), any(AsyncCallback.class));
		
		filesBrowser.createFolder(name);
		
		ArgumentCaptor<String> arg = ArgumentCaptor.forClass(String.class);
		verify(mockSynapseClient).createOrUpdateEntity(arg.capture(), anyString(), eq(true), any(AsyncCallback.class));
		JSONObjectAdapter entityJson = new JSONObjectAdapterImpl(arg.getValue());
		Folder folder = new Folder(entityJson);
		assertEquals(name, folder.getName());
		verify(mockView).showInfo(anyString(), anyString());
		verify(mockView).refreshTreeView(configuredEntityId);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testCreateFolderFail() throws Exception {
		String name = "folder name";
		AsyncMockStubber.callFailureWith(new Exception()).when(mockSynapseClient).createOrUpdateEntity(anyString(), anyString(), eq(true), any(AsyncCallback.class));
		
		filesBrowser.createFolder(name);
		
		verify(mockSynapseClient).createOrUpdateEntity(anyString(), anyString(), eq(true), any(AsyncCallback.class));
		verify(mockView).showErrorMessage(DisplayConstants.ERROR_FOLDER_CREATION_FAILED);
	}
}











