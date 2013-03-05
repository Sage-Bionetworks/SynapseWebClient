package org.sagebionetworks.web.unitclient.widget.entity.download;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.AccessControlList;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.AutoGenFactory;
import org.sagebionetworks.repo.model.Data;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.EntityTypeProvider;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.JSONEntityFactory;
import org.sagebionetworks.web.client.transform.JSONEntityFactoryImpl;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.transform.NodeModelCreatorImpl;
import org.sagebionetworks.web.client.widget.entity.JiraURLHelper;
import org.sagebionetworks.web.client.widget.entity.download.Uploader;
import org.sagebionetworks.web.client.widget.entity.download.UploaderView;
import org.sagebionetworks.web.shared.EntityWrapper;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class UploaderTest {
	
	UploaderView view;
	AuthenticationController authenticationController; 
	EntityTypeProvider entityTypeProvider;
	SynapseClientAsync synapseClient;
	JiraURLHelper jiraURLHelper;
	SynapseJSNIUtils synapseJsniUtils;
	// JSON utility components
	private static JSONObjectAdapter jsonObjectAdapter = new JSONObjectAdapterImpl();
	private static AdapterFactory adapterFactory = new AdapterFactoryImpl(); // alt: GwtAdapterFactory
	private static JSONEntityFactory jsonEntityFactory = new JSONEntityFactoryImpl(adapterFactory);
	private static NodeModelCreator nodeModelCreator = new NodeModelCreatorImpl(jsonEntityFactory, jsonObjectAdapter);

	AutoGenFactory autogenFactory;
	Uploader uploader;
	GWTWrapper gwt;
	FileEntity testEntity;
	
	@Before
	public void before() throws Exception {
		view = mock(UploaderView.class);
		authenticationController = mock(AuthenticationController.class); 
		entityTypeProvider=mock(EntityTypeProvider.class);
		synapseClient=mock(SynapseClientAsync.class);
		jiraURLHelper=mock(JiraURLHelper.class);
		synapseJsniUtils=mock(SynapseJSNIUtils.class);
		autogenFactory=mock(AutoGenFactory.class);
		gwt = mock(GWTWrapper.class);
		AsyncMockStubber.callSuccessWith("syn123").when(synapseClient).createOrUpdateEntity(anyString(), anyString(), anyBoolean(), any(AsyncCallback.class));
		testEntity = new FileEntity();
		testEntity.setName("test file");
		testEntity.setId("syn99");
		EntityWrapper expectedEntityWrapper = new EntityWrapper(
				testEntity.writeToJSONObject(adapterFactory.createNew()).toJSONString(),
				FileEntity.class.getName());
		when(autogenFactory.newInstance(anyString())).thenReturn(testEntity);
		UserSessionData sessionData = new UserSessionData();
		sessionData.setProfile(new UserProfile());
		when(authenticationController.getLoggedInUser()).thenReturn(sessionData);
		when(jiraURLHelper.createAccessRestrictionIssue(anyString(), anyString(), anyString())).thenReturn("http://fakeJiraRestrictionLink");
		AsyncMockStubber.callSuccessWith(expectedEntityWrapper).when(synapseClient).updateExternalFile(anyString(), anyString(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(expectedEntityWrapper).when(synapseClient).createAccessRequirement(any(EntityWrapper.class), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(expectedEntityWrapper).when(synapseClient).updateExternalLocationable(anyString(), anyString(), any(AsyncCallback.class));
		uploader=new Uploader(view,nodeModelCreator,authenticationController, entityTypeProvider, synapseClient, jiraURLHelper, jsonObjectAdapter, synapseJsniUtils, adapterFactory, autogenFactory, gwt);
	}
	
	@Test
	public void testGetUploadActionUrlWithNull() {
		String parentEntityId = "syn1234";
		uploader.asWidget(parentEntityId, null);
		
		uploader.getUploadActionUrl(true);
		verify(synapseJsniUtils).getBaseFileHandleUrl();
	}
	
	@Test
	public void testGetUploadActionUrlWithFileEntity() {
		FileEntity fileEntity = new FileEntity();
		uploader.asWidget(fileEntity, null);
		uploader.getUploadActionUrl(true);
		verify(synapseJsniUtils).getBaseFileHandleUrl();
	}
	
	@Test
	public void testGetUploadActionUrlWithData() {
		Data data = new Data();
		uploader.asWidget(data, null);
		uploader.getUploadActionUrl(true);
		verify(gwt).getModuleBaseURL();
	}
	
	@Test
	public void testSetExternalPath() throws Exception {
		//this is the full success test
		//if entity is null, it should call synapseClient.createOrUpdateEntity() to create the FileEntity.
		String parentEntityId = "syn1234";
		uploader.asWidget(parentEntityId, null);
		uploader.setExternalFilePath("http://fakepath.url/blah.xml", true);
		verify(synapseClient).createOrUpdateEntity(anyString(), anyString(), anyBoolean(), any(AsyncCallback.class));
		verify(synapseClient).updateExternalFile(anyString(), anyString(), any(AsyncCallback.class));
		verify(synapseClient).createAccessRequirement(any(EntityWrapper.class), any(AsyncCallback.class));
		verify(view).showInfo(anyString(), anyString());
		verify(view).openNewBrowserTab(anyString());
	}
	
	@Test
	public void testSetExternalPathFailedCreate() throws Exception {
		AsyncMockStubber.callFailureWith(new Exception("failed to create")).when(synapseClient).createOrUpdateEntity(anyString(), anyString(), anyBoolean(), any(AsyncCallback.class));

		String parentEntityId = "syn1234";
		uploader.asWidget(parentEntityId, null);
		uploader.setExternalFilePath("http://fakepath.url/blah.xml", true);
		
		verify(view).showErrorMessage(anyString());
	}
	
	@Test
	public void testSetExternalPathFailedUpdateFile() throws Exception {
		AsyncMockStubber.callFailureWith(new Exception("failed to update path")).when(synapseClient).updateExternalFile(anyString(), anyString(), any(AsyncCallback.class));

		String parentEntityId = "syn1234";
		uploader.asWidget(parentEntityId, null);
		uploader.setExternalFilePath("http://fakepath.url/blah.xml", true);
		
		verify(view).showErrorMessage(anyString());
	}

	@Test
	public void testSetExternalPathFailedCreateAccessRequirement() throws Exception {
		AsyncMockStubber.callFailureWith(new Exception("failed to update path")).when(synapseClient).createAccessRequirement(any(EntityWrapper.class), any(AsyncCallback.class));

		String parentEntityId = "syn1234";
		uploader.asWidget(parentEntityId, null);
		uploader.setExternalFilePath("http://fakepath.url/blah.xml", true);
		
		verify(view).showErrorMessage(anyString());
	}

	
	@Test
	public void testSetExternalFilePathNotAFileEntity() {
		//success setting external file path with a Locationable
		Data data = new Data();
		uploader.asWidget(data, null);
		uploader.setExternalFilePath("http://fakepath.url/blah.xml", true);
		verify(synapseClient).updateExternalLocationable(anyString(), anyString(), any(AsyncCallback.class));
		verify(synapseClient).createAccessRequirement(any(EntityWrapper.class), any(AsyncCallback.class));
		verify(view).showInfo(anyString(), anyString());
		verify(view).openNewBrowserTab(anyString());
	}
	
	@Test
	public void testSetExternalFileEntityPathWithFileEntity() throws Exception {
		uploader.asWidget(testEntity, null);
		uploader.setExternalFilePath("http://fakepath.url/blah.xml", true);
		verify(synapseClient).updateExternalFile(anyString(), anyString(), any(AsyncCallback.class));
		verify(synapseClient).createAccessRequirement(any(EntityWrapper.class), any(AsyncCallback.class));
		verify(view).showInfo(anyString(), anyString());
		verify(view).openNewBrowserTab(anyString());

	}

}
