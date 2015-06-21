package org.sagebionetworks.web.unitclient.widget.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.Annotations;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.entity.query.EntityQueryResult;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.cache.ClientCache;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.widget.entity.EntityBadge;
import org.sagebionetworks.web.client.widget.entity.EntityBadgeView;
import org.sagebionetworks.web.client.widget.entity.EntityIconsCache;
import org.sagebionetworks.web.client.widget.entity.annotation.AnnotationTransformer;
import org.sagebionetworks.web.client.widget.entity.dialog.ANNOTATION_TYPE;
import org.sagebionetworks.web.client.widget.entity.dialog.Annotation;
import org.sagebionetworks.web.client.widget.provenance.ProvUtils;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import org.sagebionetworks.web.shared.EntityBundlePlus;
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
	String rootWikiKeyId;
	KeyValueDisplay<String> keyValueDisplay;
	Map<String,String> map;
	List<String> order;
	List<Annotation> annotationList;
	Annotations annotations;
	UserBadge mockUserBadge;
	SynapseJSNIUtils mockSynapseJSNIUtils; 
	
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
		mockUserBadge = mock(UserBadge.class);
		mockSynapseJSNIUtils = mock(SynapseJSNIUtils.class);
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		widget = new EntityBadge(mockView, mockEntityIconsCache, mockSynapseClient, mockGlobalApplicationState, mockTransformer, mockUserBadge, mockSynapseJSNIUtils);
		
		annotationList = new ArrayList<Annotation>();
		annotationList.add(new Annotation(ANNOTATION_TYPE.STRING, "key1", Collections.EMPTY_LIST));
		annotationList.add(new Annotation(ANNOTATION_TYPE.STRING, "key2", Collections.singletonList("foo")));
		annotationList.add(new Annotation(ANNOTATION_TYPE.LONG, "key3", Collections.singletonList("42")));
		when(mockTransformer.annotationsToList(any(Annotations.class))).thenReturn(annotationList);
		when(mockTransformer.getFriendlyValues(any(Annotation.class))).thenReturn("friendly value");
		rootWikiKeyId = "123";
		map = new HashMap<String, String>();
		order = new ArrayList<String>();
		keyValueDisplay = new KeyValueDisplay<String>(map, order);
	}
	
	private void setupEntity(Entity entity) throws JSONObjectAdapterException {
		UserProfile userProfile =  new UserProfile();
		userProfile.setOwnerId("4444");
		userProfile.setUserName("Bilbo");
		
		EntityBundle bundle = mock(EntityBundle.class);
		when(bundle.getEntity()).thenReturn(entity);
//		when(bundle.getAnnotations()).thenReturn(value);
		EntityBundlePlus entityBundlePlus = new EntityBundlePlus();
		entityBundlePlus.setEntityBundle(bundle);
		entityBundlePlus.setProfile(userProfile);
		
		AsyncMockStubber.callSuccessWith(entityBundlePlus).when(mockSynapseClient).getEntityInfo(anyString(), any(AsyncCallback.class));
	}
	
	@Test
	public void testConfigure() throws Exception {
		EntityQueryResult header = new EntityQueryResult();
		header.setId("syn008");
		widget.configure(header);
		verify(mockView).setEntity(header);
		
		//in this case, "modified by" and "modified on" are not set.
		verify(mockView).setModifiedByWidgetVisible(false);
		verify(mockView).setModifiedOn("");
	}
	
	@Test
	public void testConfigureWithModificationData() throws Exception {
		EntityQueryResult header = new EntityQueryResult();
		header.setId("syn008");
		Long modifiedByPrincipalId = 12345L;
		Date modifiedOn = new Date();
		String smallDateString="10/02/2000 01:26:45PM";
		when(mockSynapseJSNIUtils.convertDateToSmallString(any(Date.class))).thenReturn(smallDateString);
		header.setModifiedByPrincipalId(modifiedByPrincipalId);
		header.setModifiedOn(modifiedOn);
		widget.configure(header);
		verify(mockView).setEntity(header);
		
		//in this case, "modified by" and "modified on" are not set.
		verify(mockUserBadge).configure(modifiedByPrincipalId.toString());
		verify(mockView).setModifiedByWidgetVisible(true);
		verify(mockSynapseJSNIUtils).convertDateToSmallString(modifiedOn);
		verify(mockView).setModifiedOn(smallDateString);
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
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseClient).getEntityInfo(anyString(), any(AsyncCallback.class));
		widget.getInfo(entityId, getInfoCallback);
		//exception should be passed back to callback
		verify(getInfoCallback).onFailure(eq(ex));
	}
	
	@Test
	public void testEntityClicked() throws Exception {
		//check the passthrough
		EntityQueryResult header = new EntityQueryResult();
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
		EntityQueryResult header = new EntityQueryResult();
		header.setId("syn12345");
		widget.configure(header);
		assertTrue(header == widget.getHeader());
	}
	
	@Test
	public void testSetClickHandler() {
		ClickHandler mockClickHandler = mock(ClickHandler.class);
		widget.setClickHandler(mockClickHandler);
		verify(mockView).setClickHandler(mockClickHandler);
		verify(mockUserBadge).setCustomClickHandler(mockClickHandler);
	}


	@Test
	public void testAddAnnotationsAndWikiStatusEmpty() throws Exception {
		rootWikiKeyId = null;
		annotationList.clear();
		widget.addAnnotationsAndWikiStatus(keyValueDisplay, annotations, rootWikiKeyId);
		//verify nothing was added to keyValueDisplay
		assertTrue(map.isEmpty());
		assertTrue(order.isEmpty());
	}
	
	@Test
	public void testWikiStatus() throws Exception {
		rootWikiKeyId = "8888";
		annotationList.clear();
		widget.addAnnotationsAndWikiStatus(keyValueDisplay, annotations, rootWikiKeyId);
		assertEquals(1, map.size());
		assertEquals(1, order.size());
	}
	
	@Test
	public void testAddAnnotationsAndWikiStatus() throws Exception {
		rootWikiKeyId = "8888";
		widget.addAnnotationsAndWikiStatus(keyValueDisplay, annotations, rootWikiKeyId);
		//in the @before we set up 3 annotation keys.  Plus the has a wiki note.
		assertEquals(4, map.size());
		assertEquals(4, order.size());
		assertTrue(map.containsKey("key1"));
	}
}
