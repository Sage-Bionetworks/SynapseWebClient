package org.sagebionetworks.web.unitclient.widget.entity;

import static org.junit.Assert.*;
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
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.AccessControlList;
import org.sagebionetworks.repo.model.Annotations;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.auth.UserEntityPermissions;
import org.sagebionetworks.repo.model.entity.query.EntityQueryResult;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.file.PreviewFileHandle;
import org.sagebionetworks.repo.model.file.S3FileHandle;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.cache.ClientCache;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.EntityBadge;
import org.sagebionetworks.web.client.widget.entity.EntityBadgeView;
import org.sagebionetworks.web.client.widget.entity.annotation.AnnotationTransformer;
import org.sagebionetworks.web.client.widget.entity.dialog.ANNOTATION_TYPE;
import org.sagebionetworks.web.client.widget.entity.dialog.Annotation;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import org.sagebionetworks.web.shared.EntityBundlePlus;
import org.sagebionetworks.web.shared.KeyValueDisplay;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class EntityBadgeTest {

	SynapseClientAsync mockSynapseClient;
	GlobalApplicationState mockGlobalApplicationState;
	PlaceChanger mockPlaceChanger;
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
	UserEntityPermissions mockPermissions;
	AccessControlList mockBenefactorAcl;
	@Mock
	GWTWrapper mockGWT;
	
	@Before
	public void before() throws JSONObjectAdapterException {
		MockitoAnnotations.initMocks(this);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockView = mock(EntityBadgeView.class);
		mockClientCache = mock(ClientCache.class);
		getInfoCallback = mock(AsyncCallback.class);
		mockPlaceChanger = mock(PlaceChanger.class);
		mockTransformer = mock(AnnotationTransformer.class);
		mockUserBadge = mock(UserBadge.class);
		mockPermissions = mock(UserEntityPermissions.class);
		when(mockPermissions.getCanPublicRead()).thenReturn(true);
		mockSynapseJSNIUtils = mock(SynapseJSNIUtils.class);
		mockBenefactorAcl = mock(AccessControlList.class);
		when(mockBenefactorAcl.getId()).thenReturn("not the current entity id");
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		widget = new EntityBadge(mockView, mockGlobalApplicationState, mockTransformer, mockUserBadge, mockSynapseJSNIUtils, mockSynapseClient, mockGWT);
		
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
		EntityBundle bundle = mock(EntityBundle.class);
		when(bundle.getEntity()).thenReturn(entity);
//		when(bundle.getAnnotations()).thenReturn(value);
		when(bundle.getPermissions()).thenReturn(mockPermissions);
		when(bundle.getBenefactorAcl()).thenReturn(mockBenefactorAcl);
		
		AsyncMockStubber.callSuccessWith(bundle).when(mockSynapseClient).getEntityBundle(anyString(), anyInt(), any(AsyncCallback.class));
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
	public void testEntityClickedCustomHandler() throws Exception {
		CallbackP<String> mockEntityClicked = mock(CallbackP.class);
		widget.setEntityClickedHandler(mockEntityClicked);
		String id = "syn77";
		EntityQueryResult header = new EntityQueryResult();
		header.setId(id);
		widget.entityClicked(header);
		verify(mockEntityClicked).invoke(id);
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
		widget.addAnnotations(keyValueDisplay, annotations);
		widget.addWikiStatus(keyValueDisplay, rootWikiKeyId);
		//verify nothing was added to keyValueDisplay
		assertTrue(map.isEmpty());
		assertTrue(order.isEmpty());
	}
	
	@Test
	public void testWikiStatus() throws Exception {
		rootWikiKeyId = "8888";
		annotationList.clear();
		widget.addWikiStatus(keyValueDisplay, rootWikiKeyId);
		assertEquals(1, map.size());
		assertEquals(1, order.size());
	}
	
	@Test
	public void testAddPublic() throws Exception {
		widget.addPublicPrivate(keyValueDisplay, mockPermissions);
		assertEquals(1, map.size());
		assertEquals(1, order.size());
		assertEquals("Public", map.keySet().iterator().next());
	}
	@Test
	public void testAddPrivate() throws Exception {
		when(mockPermissions.getCanPublicRead()).thenReturn(false);
		widget.addPublicPrivate(keyValueDisplay, mockPermissions);
		assertEquals(1, map.size());
		assertEquals(1, order.size());
		assertEquals("Private", map.keySet().iterator().next());
	}
	
	@Test
	public void testAddLocalSharingSettingsInherited() throws Exception {
		widget.addHasLocalSharingSettings(keyValueDisplay, entityId, mockBenefactorAcl);
		assertEquals(0, map.size());
		assertEquals(0, order.size());
	}
	@Test
	public void testAddLocalSharingSettingsHasLocal() throws Exception {
		when(mockBenefactorAcl.getId()).thenReturn(entityId);
		widget.addHasLocalSharingSettings(keyValueDisplay, entityId, mockBenefactorAcl);
		assertEquals(1, map.size());
		assertEquals(1, order.size());
		assertEquals(EntityBadge.HAS_LOCAL_SHARING_SETTINGS, map.keySet().iterator().next());
	}


	
	@Test
	public void testAddAnnotationsAndWikiStatus() throws Exception {
		rootWikiKeyId = "8888";
		widget.addAnnotations(keyValueDisplay, annotations);
		widget.addWikiStatus(keyValueDisplay, rootWikiKeyId);
		//in the @before we set up 3 annotation keys.  Plus the has a wiki note.
		assertEquals(4, map.size());
		assertEquals(4, order.size());
		assertTrue(map.containsKey("key1"));
	}
	
	@Test
	public void testAddContentSize() {
		
		List<FileHandle> fileHandles = new ArrayList<FileHandle>();
		FileHandle previewFileHandle = new PreviewFileHandle();
		fileHandles.add(previewFileHandle);
		widget.addContentSize(keyValueDisplay, fileHandles);
		assertEquals(0, map.size());
		assertEquals(0, order.size());
		assertFalse(map.containsKey("File Size"));
		
		FileHandle s3FileHandle = new S3FileHandle();
		s3FileHandle.setContentSize(500L);
		fileHandles.add(s3FileHandle);
		
		widget.addContentSize(keyValueDisplay, fileHandles);
		assertEquals(1, map.size());
		assertEquals(1, order.size());
		assertTrue(map.containsKey("File Size"));
	}
}
