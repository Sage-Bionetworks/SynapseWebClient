package org.sagebionetworks.web.unitclient.widget.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.AccessControlList;
import org.sagebionetworks.repo.model.Annotations;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.Project;
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
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.EntityBadge;
import org.sagebionetworks.web.client.widget.entity.EntityBadgeView;
import org.sagebionetworks.web.client.widget.entity.annotation.AnnotationTransformer;
import org.sagebionetworks.web.client.widget.entity.dialog.ANNOTATION_TYPE;
import org.sagebionetworks.web.client.widget.entity.dialog.Annotation;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import org.sagebionetworks.web.shared.KeyValueDisplay;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class EntityBadgeTest {

	private static final String TRANSFORMED_FRIENDLY_VALUE = "friendly value";
	private static final String VALUE3 = "42";
	private static final String VALUE2 = "foo";
	private static final String KEY3 = "key3";
	private static final String KEY1 = "key1";
	private static final String KEY2 = "key2";
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
		annotationList.add(new Annotation(ANNOTATION_TYPE.STRING, KEY1, Collections.EMPTY_LIST));
		annotationList.add(new Annotation(ANNOTATION_TYPE.STRING, KEY2, Collections.singletonList(VALUE2)));
		annotationList.add(new Annotation(ANNOTATION_TYPE.LONG, KEY3, Collections.singletonList(VALUE3)));
		when(mockTransformer.annotationsToList(any(Annotations.class))).thenReturn(annotationList);
		when(mockTransformer.getFriendlyValues(any(Annotation.class))).thenReturn(TRANSFORMED_FRIENDLY_VALUE);
		rootWikiKeyId = "123";
		when(mockView.isAttached()).thenReturn(true);
	}
	
	private EntityBundle setupEntity(Entity entity) {
		EntityBundle bundle = mock(EntityBundle.class);
		when(bundle.getEntity()).thenReturn(entity);
//		when(bundle.getAnnotations()).thenReturn(value);
		when(bundle.getPermissions()).thenReturn(mockPermissions);
		when(bundle.getBenefactorAcl()).thenReturn(mockBenefactorAcl);
		when(bundle.getRootWikiId()).thenReturn(rootWikiKeyId);
		AsyncMockStubber.callSuccessWith(bundle).when(mockSynapseClient).getEntityBundle(anyString(), anyInt(), any(AsyncCallback.class));
		return bundle;
	}
	
	private EntityQueryResult configure() {
		EntityQueryResult header = new EntityQueryResult();
		header.setId(entityId);
		widget.configure(header);
		return header;
	}
	
	@Test
	public void testConfigure() throws Exception {
		EntityQueryResult header = configure();
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

	/**
	 * This tests the standard case when the badge is outside the viewport and scrolled into view.  
	 * View is initially not ready (not attached) so it schedules a deferred callback.
	 * The view is ready when the deferred call is invoked, but the widget is not in the viewport, so it schedules execution to check again later.
	 * The widget is then in view, so it asks for the entity bundle, and successfully configures the view based on the response.
	 */
	@Test
	public void testCheckForInViewAndLoadData() {
		//set up entity
		String entityId = "syn12345";
		Project testProject = new Project();
		testProject.setModifiedBy("4444");
		//note: can't test modified on because it format it using the gwt DateUtils (calls GWT.create())
		testProject.setId(entityId);
		setupEntity(testProject);
		
		//simulate the view is not yet attached, or in viewport
		when(mockView.isAttached()).thenReturn(false);
		when(mockView.isInViewport()).thenReturn(false);
		
		configure();
		
		ArgumentCaptor<Callback> captor = ArgumentCaptor.forClass(Callback.class);
		verify(mockGWT).scheduleDeferred(captor.capture());
		Callback callback = captor.getValue();
		
		//simulate the view is now attached when callback is invoked, but still not in viewport
		when(mockView.isAttached()).thenReturn(true);
		callback.invoke();
		
		verify(mockGWT).scheduleExecution(captor.capture(), eq(EntityBadge.DELAY_UNTIL_IN_VIEW));
		callback = captor.getValue();
		
		//simulate the view is now attached and in the viewport, should ask for entity bundle
		when(mockView.isInViewport()).thenReturn(true);
		callback.invoke();
		
		verify(mockSynapseClient).getEntityBundle(anyString(), anyInt(), any(AsyncCallback.class));
		verify(mockView).showPublicIcon();
		verify(mockView).showAnnotationsIcon();
		verify(mockView).setAnnotations(anyString());
		verify(mockView).setAnnotations(anyString());
		verify(mockView).showHasWikiIcon();
		
	}
	
	@Test
	public void testCheckForInViewAndLoadDataFailure() {
		configure();
		//test failure response from getEntityBundle
		String errorMessage = "problem occurred while asking for entity bundle";
		Exception ex = new Exception(errorMessage);
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseClient).getEntityBundle(anyString(), anyInt(), any(AsyncCallback.class));
		widget.getEntityBundle();

		verify(mockView).showErrorIcon();
		verify(mockView).setError(errorMessage);
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
	public void testAnnotationsEmpty() throws Exception {
		annotationList.clear();
		String result = widget.getAnnotationsHTML(annotationList);
		assertTrue("".equals(result));
	}
	
	@Test
	public void testAnnotations() throws Exception {
		String result = widget.getAnnotationsHTML(annotationList);
		assertTrue(result.contains(KEY1));
		assertTrue(result.contains(KEY2));
		assertTrue(result.contains(KEY3));
		assertTrue(result.contains(TRANSFORMED_FRIENDLY_VALUE));
	}

	@Test
	public void testNoWiki() throws Exception {
		configure();
		EntityBundle bundle = setupEntity(new Project());
		when(bundle.getRootWikiId()).thenReturn(null);
		widget.setEntityBundle(bundle);
		verify(mockView, never()).showHasWikiIcon();
	}
	
	@Test
	public void testPrivate() throws Exception {
		configure();
		when(mockPermissions.getCanPublicRead()).thenReturn(false);
		EntityBundle bundle = setupEntity(new Project());
		
		widget.setEntityBundle(bundle);
		verify(mockView).showPrivateIcon();
	}
	
	@Test
	public void testLocalSharingSettings() throws Exception {
		configure();
		EntityBundle bundle = setupEntity(new Project());
		when(mockBenefactorAcl.getId()).thenReturn(entityId);
		
		widget.setEntityBundle(bundle);
		verify(mockView).showSharingSetIcon();
	}
	
	
	@Test
	public void testContentSize() {
		String friendlySize = "44MB";
		when(mockView.getFriendlySize(anyLong(), anyBoolean())).thenReturn(friendlySize);
		List<FileHandle> fileHandles = new ArrayList<FileHandle>();
		FileHandle previewFileHandle = new PreviewFileHandle();
		fileHandles.add(previewFileHandle);
		String result = widget.getContentSize(fileHandles);
		assertTrue("".equals(result));
		
		FileHandle s3FileHandle = new S3FileHandle();
		s3FileHandle.setContentSize(500L);
		fileHandles.add(s3FileHandle);
		
		result = widget.getContentSize(fileHandles);
		assertEquals(friendlySize, result);
	}
	
	@Test
	public void testContentMd5() {
		List<FileHandle> fileHandles = new ArrayList<FileHandle>();
		FileHandle previewFileHandle = new PreviewFileHandle();
		String previewContentMd5 = "abcde";
		previewFileHandle.setContentMd5(previewContentMd5);
		fileHandles.add(previewFileHandle);
		String result = widget.getContentMd5(fileHandles);
		assertTrue("".equals(result));
		
		FileHandle s3FileHandle = new S3FileHandle();
		String contentMd5 = "fghij";
		s3FileHandle.setContentMd5(contentMd5);
		fileHandles.add(s3FileHandle);
		
		result = widget.getContentMd5(fileHandles);
		assertEquals(contentMd5, result);
	}
}
