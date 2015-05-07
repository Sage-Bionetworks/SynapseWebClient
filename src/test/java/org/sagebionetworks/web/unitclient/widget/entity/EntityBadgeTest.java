package org.sagebionetworks.web.unitclient.widget.entity;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.cache.ClientCache;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.widget.entity.EntityBadge;
import org.sagebionetworks.web.client.widget.entity.EntityBadgeView;
import org.sagebionetworks.web.client.widget.entity.EntityIconsCache;
import org.sagebionetworks.web.client.widget.entity.annotation.AnnotationTransformer;
import org.sagebionetworks.web.shared.KeyValueDisplay;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class EntityBadgeTest {

	SynapseClientAsync mockSynapseClient;
	GlobalApplicationState mockGlobalApplicationState;
	PlaceChanger mockPlaceChanger;
	EntityIconsCache mockEntityIconsCache;
	AdapterFactory adapterFactory = new AdapterFactoryImpl();
	ClientCache mockClientCache;
	AsyncCallback<KeyValueDisplay<String>> getInfoCallback;
	EntityBadgeView mockView;
	String entityId = "syn123";
	EntityBadge widget;
	AnnotationTransformer mockTransformer;

	@Before
	public void before() throws JSONObjectAdapterException {
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockView = mock(EntityBadgeView.class);
		mockClientCache = mock(ClientCache.class);
		mockEntityIconsCache = mock(EntityIconsCache.class);
		getInfoCallback = mock(AsyncCallback.class);
		mockPlaceChanger = mock(PlaceChanger.class);
		mockTransformer = mock(AnnotationTransformer.class);
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		widget = new EntityBadge(mockView, mockEntityIconsCache, mockSynapseClient, adapterFactory, mockGlobalApplicationState, mockClientCache, mockTransformer);
		
		//set up user profile
		UserProfile userProfile =  new UserProfile();
		userProfile.setOwnerId("4444");
		userProfile.setUserName("Bilbo");
		AsyncMockStubber.callSuccessWith(userProfile).when(mockSynapseClient).getUserProfile(anyString(), any(AsyncCallback.class));
	}
	
	private void setupEntity(Entity entity) throws JSONObjectAdapterException {
		EntityBundle bundle = mock(EntityBundle.class);
		when(bundle.getEntity()).thenReturn(entity);
//		when(bundle.getAnnotations()).thenReturn(value);
		AsyncMockStubber.callSuccessWith(bundle).when(mockSynapseClient).getEntityBundle(anyString(), anyInt(), any(AsyncCallback.class));
	}
	
	@Test
	public void testConfigure() throws Exception {
		//check the passthrough (view shows information from entity header
		EntityHeader header = new EntityHeader();
		header.setId("syn008");
		widget.configure(header);
		verify(mockView).setEntity(header);
	}

	@Test
	public void testGetIconForType() throws Exception {
		//check the passthrough
		ImageResource testResource = mock(ImageResource.class);
		when(mockEntityIconsCache.getIconForType(anyString())).thenReturn(testResource);
		ImageResource returnedResource = widget.getIconForType("water");
		assertEquals(testResource, returnedResource);
	}
	
	@Test
	public void testGetInfoHappyCase() throws Exception {
		String entityId = "syn12345";
		Project testProject = new Project();
		testProject.setModifiedBy("4444");
		//note: can't test modified on because it format it using the gwt DateUtils (calls GWT.create())
		testProject.setId(entityId);
		setupEntity(testProject);
		widget.getInfo(entityId, getInfoCallback);
		verify(getInfoCallback).onSuccess(any(KeyValueDisplay.class));
	}

	@Test
	public void testGetInfoFailure() throws Exception {
		//failure to get entity
		Exception ex = new Exception("unhandled");
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseClient).getEntityBundle(anyString(), anyInt(), any(AsyncCallback.class));
		widget.getInfo(entityId, getInfoCallback);
		//exception should be passed back to callback
		verify(getInfoCallback).onFailure(eq(ex));
	}

	@Test
	public void testGetInfoProfileFailure() throws Exception {
		String entityId = "syn12345";
		Project testProject = new Project();
		testProject.setModifiedBy("4444");
		testProject.setId(entityId);
		setupEntity(testProject);
		Exception ex = new Exception("unhandled get profile error");
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseClient).getUserProfile(anyString(), any(AsyncCallback.class));
		
		widget.getInfo(entityId, getInfoCallback);
		verify(getInfoCallback).onFailure(eq(ex));
	}
	
	@Test
	public void testEntityClicked() throws Exception {
		//check the passthrough
		EntityHeader header = new EntityHeader();
		header.setId("syn93847");
		widget.entityClicked(header);
		verify(mockPlaceChanger).goTo(any(Synapse.class));
	}
	
	@Test
	public void testShowTypeIcon() throws Exception {
		EntityHeader header = new EntityHeader();
		header.setId("syn93847");
		widget.hideLoadingIcon();
		verify(mockView).hideLoadingIcon();
	}
	
	@Test
	public void testShowLoadingIcon() throws Exception {
		EntityHeader header = new EntityHeader();
		header.setId("syn93847");
		widget.showLoadingIcon();
		verify(mockView).showLoadingIcon();
	}
	
	@Test
	public void testGetEntity() {
		EntityHeader header = new EntityHeader();
		header.setId("syn12345");
		widget.configure(header);
		assertTrue(header == widget.getHeader());
	}
	
	@Test
	public void testSetClickHandler() {
		ClickHandler mockClickHandler = mock(ClickHandler.class);
		widget.setClickHandler(mockClickHandler);
		verify(mockView).setClickHandler(any(ClickHandler.class));
	}

}
