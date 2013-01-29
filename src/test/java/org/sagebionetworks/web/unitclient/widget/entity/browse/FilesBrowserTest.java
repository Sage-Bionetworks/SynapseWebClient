package org.sagebionetworks.web.unitclient.widget.entity.browse;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.sagebionetworks.repo.model.AutoGenFactory;
import org.sagebionetworks.repo.model.Data;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.Folder;
import org.sagebionetworks.repo.model.LocationData;
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
import org.sagebionetworks.web.shared.EntityWrapper;
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
		String newId = "syn456";
		AsyncMockStubber.callFailureWith(new Exception()).when(mockSynapseClient).createOrUpdateEntity(anyString(), anyString(), eq(true), any(AsyncCallback.class));
		
		filesBrowser.createFolder(name);
		
		verify(mockSynapseClient).createOrUpdateEntity(anyString(), anyString(), eq(true), any(AsyncCallback.class));
		verify(mockView).showErrorMessage(DisplayConstants.ERROR_FOLDER_CREATION_FAILED);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testCreateEntityForUpload() throws Exception {
		String newId = "syn456";
		AsyncMockStubber.callSuccessWith(newId).when(mockSynapseClient).createOrUpdateEntity(anyString(), anyString(), eq(true), any(AsyncCallback.class));
		AsyncCallback<Entity> callback = mock(AsyncCallback.class);
		
		filesBrowser.createEntityForUpload(callback);
		
		verify(mockSynapseClient).createOrUpdateEntity(anyString(), anyString(), eq(true), any(AsyncCallback.class));
		ArgumentCaptor<Entity> arg = ArgumentCaptor.forClass(Entity.class);
		verify(callback).onSuccess(arg.capture());
		Entity file = arg.getValue();
		assertEquals(newId, file.getId());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testCreateEntityForUploadFail() throws Exception {
		String newId = "syn456";
		Exception ex = new Exception();
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseClient).createOrUpdateEntity(anyString(), anyString(), eq(true), any(AsyncCallback.class));
		AsyncCallback<Entity> callback = mock(AsyncCallback.class);
		
		filesBrowser.createEntityForUpload(callback);
		
		verify(mockSynapseClient).createOrUpdateEntity(anyString(), anyString(), eq(true), any(AsyncCallback.class));
		verify(callback).onFailure(ex);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testRenameChildToFilename() throws Exception {
		String entityId = "syn456";		
		Data hasFile = new Data();
		hasFile.setId(entityId);
		hasFile.setName(entityId);
		List<LocationData> locations = new ArrayList<LocationData>();
		LocationData ld = new LocationData();
		String filename = "filename.txt";
		ld.setPath("http://someurl.com/" + filename);
		locations.add(ld);
		hasFile.setLocations(locations);
		String entityJson = hasFile.writeToJSONObject(adapterFactory.createNew()).toJSONString();
		EntityWrapper wrapper = new EntityWrapper(entityJson, Data.class.getName());
		
		AsyncMockStubber.callSuccessWith(wrapper).when(mockSynapseClient).getEntity(eq(entityId), any(AsyncCallback.class));					
		when(mockNodeModelCreator.createEntity(wrapper)).thenReturn(hasFile);
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseClient).updateEntity(anyString(), any(AsyncCallback.class));

		filesBrowser.renameChildToFilename(entityId);
		
		verify(mockSynapseClient).getEntity(eq(entityId), any(AsyncCallback.class));
		ArgumentCaptor<String> arg = ArgumentCaptor.forClass(String.class);
		verify(mockSynapseClient).updateEntity(arg.capture(), any(AsyncCallback.class));
		String updatedJson = arg.getValue();
		Data updated = new Data(adapterFactory.createNew(updatedJson));
		assertEquals(filename, updated.getName());
		verify(mockView).refreshTreeView(configuredEntityId);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testRenameChildToFilenameFail() throws Exception {
		String entityId = "syn456";		
		Data hasFile = new Data();
		hasFile.setId(entityId);
		hasFile.setName(entityId);
		List<LocationData> locations = new ArrayList<LocationData>();
		LocationData ld = new LocationData();
		String filename = "filename.txt";
		ld.setPath("http://someurl.com/" + filename);
		locations.add(ld);
		hasFile.setLocations(locations);
		String entityJson = hasFile.writeToJSONObject(adapterFactory.createNew()).toJSONString();
		EntityWrapper wrapper = new EntityWrapper(entityJson, Data.class.getName());
		
		AsyncMockStubber.callFailureWith(new Exception()).when(mockSynapseClient).getEntity(eq(entityId), any(AsyncCallback.class));					

		filesBrowser.renameChildToFilename(entityId);
		
		verify(mockView).refreshTreeView(configuredEntityId);
	}
}











