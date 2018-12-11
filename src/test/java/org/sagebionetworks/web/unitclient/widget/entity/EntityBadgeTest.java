package org.sagebionetworks.web.unitclient.widget.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.gwtbootstrap3.client.ui.constants.IconType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.AccessControlList;
import org.sagebionetworks.repo.model.Annotations;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Link;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.ResourceAccess;
import org.sagebionetworks.repo.model.auth.UserEntityPermissions;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.file.S3FileHandle;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.SynapseProperties;
import org.sagebionetworks.web.client.cache.ClientCache;
import org.sagebionetworks.web.client.events.DownloadListUpdatedEvent;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.EntityBadge;
import org.sagebionetworks.web.client.widget.entity.EntityBadgeView;
import org.sagebionetworks.web.client.widget.entity.annotation.AnnotationTransformer;
import org.sagebionetworks.web.client.widget.entity.dialog.ANNOTATION_TYPE;
import org.sagebionetworks.web.client.widget.entity.dialog.Annotation;
import org.sagebionetworks.web.client.widget.entity.file.AddToDownloadList;
import org.sagebionetworks.web.client.widget.lazyload.LazyLoadHelper;
import org.sagebionetworks.web.shared.KeyValueDisplay;
import org.sagebionetworks.web.shared.PublicPrincipalIds;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class EntityBadgeTest {

	private static final String TRANSFORMED_FRIENDLY_VALUE = "friendly value";
	private static final String VALUE3 = "42";
	private static final String VALUE2 = "foo";
	private static final String KEY3 = "key3";
	private static final String KEY1 = "key1";
	private static final String KEY2 = "key2";
	private static final String USER_ID = "12430";
	GlobalApplicationState mockGlobalApplicationState;
	PlaceChanger mockPlaceChanger;
	AdapterFactory adapterFactory = new AdapterFactoryImpl();
	ClientCache mockClientCache;
	AsyncCallback<KeyValueDisplay<String>> getInfoCallback;
	EntityBadgeView mockView;
	String entityId = "syn123";
	String entityName = "An Entity";
	Long entityThreadCount;
	EntityBadge widget;
	AnnotationTransformer mockTransformer;
	String rootWikiKeyId;
	List<Annotation> annotationList;
	Annotations annotations;
	UserEntityPermissions mockPermissions;
	AccessControlList mockBenefactorAcl;
	@Mock
	LazyLoadHelper mockLazyLoadHelper;
	@Mock
	PublicPrincipalIds mockPublicPrincipalIds;
	@Mock
	ResourceAccess mockResourceAccess;
	@Mock
	SynapseJavascriptClient mockSynapseJavascriptClient;
	@Mock
	PopupUtilsView mockPopupUtils;
	@Mock
	SynapseProperties mockSynapseProperties;
	@Mock
	EventBus mockEventBus;
	@Mock
	SynapseJSNIUtils mockSynapseJSNIUtils;
	@Captor
	ArgumentCaptor<ClickHandler> clickHandlerCaptor;
	@Mock
	S3FileHandle mockDataFileHandle;
	@Mock
	AuthenticationController mockAuthController;
	Set<ResourceAccess> resourceAccessSet;
	@Before
	public void before() throws JSONObjectAdapterException {
		MockitoAnnotations.initMocks(this);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockView = mock(EntityBadgeView.class);
		mockClientCache = mock(ClientCache.class);
		getInfoCallback = mock(AsyncCallback.class);
		mockPlaceChanger = mock(PlaceChanger.class);
		mockTransformer = mock(AnnotationTransformer.class);
		mockPermissions = mock(UserEntityPermissions.class);
		when(mockPermissions.getCanPublicRead()).thenReturn(true);
		mockBenefactorAcl = mock(AccessControlList.class);
		when(mockBenefactorAcl.getId()).thenReturn("not the current entity id");
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		widget = new EntityBadge(mockView, mockGlobalApplicationState, mockTransformer,
				mockSynapseJavascriptClient,
				mockLazyLoadHelper,
				mockPopupUtils, mockSynapseProperties, 
				mockEventBus,
				mockAuthController,
				mockSynapseJSNIUtils);
		
		when(mockAuthController.isLoggedIn()).thenReturn(true);
		when(mockSynapseProperties.getPublicPrincipalIds()).thenReturn(mockPublicPrincipalIds);
		annotationList = new ArrayList<Annotation>();
		annotationList.add(new Annotation(ANNOTATION_TYPE.STRING, KEY1, Collections.EMPTY_LIST));
		annotationList.add(new Annotation(ANNOTATION_TYPE.STRING, KEY2, Collections.singletonList(VALUE2)));
		annotationList.add(new Annotation(ANNOTATION_TYPE.LONG, KEY3, Collections.singletonList(VALUE3)));
		when(mockTransformer.annotationsToList(any(Annotations.class))).thenReturn(annotationList);
		when(mockTransformer.getFriendlyValues(any(Annotation.class))).thenReturn(TRANSFORMED_FRIENDLY_VALUE);
		rootWikiKeyId = "123";
		when(mockView.isAttached()).thenReturn(true);
		entityThreadCount = 0L;
		resourceAccessSet = new HashSet<>();
		resourceAccessSet.add(mockResourceAccess);
		when(mockBenefactorAcl.getResourceAccess()).thenReturn(resourceAccessSet);
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseJavascriptClient).addFileToDownloadList(anyString(), anyString(), any(AsyncCallback.class));
		when(mockAuthController.getCurrentUserPrincipalId()).thenReturn(USER_ID);
	}
	
	private EntityBundle setupEntity(Entity entity) {
		EntityBundle bundle = mock(EntityBundle.class);
		when(bundle.getEntity()).thenReturn(entity);
//		when(bundle.getAnnotations()).thenReturn(value);
		when(bundle.getPermissions()).thenReturn(mockPermissions);
		when(bundle.getBenefactorAcl()).thenReturn(mockBenefactorAcl);
		when(bundle.getRootWikiId()).thenReturn(rootWikiKeyId);
		when(bundle.getThreadCount()).thenReturn(entityThreadCount);
		when(bundle.getFileHandles()).thenReturn(Collections.singletonList(mockDataFileHandle));
		AsyncMockStubber.callSuccessWith(bundle).when(mockSynapseJavascriptClient).getEntityBundle(anyString(), anyInt(), any(AsyncCallback.class));
		
		return bundle;
	}
	
	private EntityHeader configure() {
		EntityHeader header = new EntityHeader();
		header.setId(entityId);
		header.setName(entityName);
		widget.configure(header);
		return header;
	}
	
	@Test
	public void testConfigure() throws Exception {
		EntityHeader header = configure();
		verify(mockView).setEntity(header);
	}

	/**
	 * This tests the standard case when the badge is outside the viewport and scrolled into view.  
	 */
	@Test
	public void testCheckForInViewAndLoadData() {
		//set up entity
		when(mockPublicPrincipalIds.isPublic(anyLong())).thenReturn(true);
		String entityId = "syn12345";
		Project testProject = new Project();
		testProject.setId(entityId);
		entityThreadCount = 0L;
		setupEntity(testProject);
		
		//configure
		configure();
		
		ArgumentCaptor<Callback> captor = ArgumentCaptor.forClass(Callback.class);
		verify(mockLazyLoadHelper).configure(captor.capture(), eq(mockView));
		captor.getValue().invoke();
		
		verify(mockSynapseJavascriptClient).getEntityBundle(anyString(), anyInt(), any(AsyncCallback.class));
		verify(mockView).showPublicIcon();
		verify(mockView).setAnnotations(anyString());
		verify(mockView).showHasWikiIcon();
		verify(mockView, never()).showAddToDownloadList();
	}
	
	@Test
	public void testGetFileEntityBundle() {
		//verify download button is configured and shown
		String entityId = "syn12345";
		FileEntity testFile = new FileEntity();
		testFile.setId(entityId);
		testFile.setDataFileHandleId("123");
		when(mockPublicPrincipalIds.isPublic(anyLong())).thenReturn(true);
		entityThreadCount = 1L;
		setupEntity(testFile);
		configure();
		widget.getEntityBundle();
		
		verify(mockSynapseJavascriptClient).getEntityBundle(anyString(), anyInt(), any(AsyncCallback.class));
		verify(mockView).showPublicIcon();
		verify(mockView).setAnnotations(anyString());
		verify(mockView).showHasWikiIcon();
		verify(mockView).showDiscussionThreadIcon();
		verify(mockView).showAddToDownloadList();
	}
	
	@Test
	public void testCheckForInViewAndLoadDataFailure() {
		configure();
		//test failure response from getEntityBundle
		String errorMessage = "problem occurred while asking for entity bundle";
		Exception ex = new Exception(errorMessage);
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseJavascriptClient).getEntityBundle(anyString(), anyInt(), any(AsyncCallback.class));
		widget.getEntityBundle();
		
		verify(mockView).setError(errorMessage);
	}

	@Test
	public void testEntityClickedCustomHandler() throws Exception {
		configure();
		ClickHandler mockEntityClicked = mock(ClickHandler.class);
		widget.setClickHandler(mockEntityClicked);
		verify(mockView).addClickHandler(mockEntityClicked);
	}
	
	@Test
	public void testGetEntity() {
		EntityHeader header = new EntityHeader();
		header.setId("syn12345");
		widget.configure(header);
		assertTrue(header == widget.getHeader());
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
	public void testCanUnlink() {
		configure();
		EntityBundle bundle = setupEntity(new Link());
		when(mockPermissions.getCanDelete()).thenReturn(true);
		
		widget.setEntityBundle(bundle);
		verify(mockView).showUnlinkIcon();
	}

	@Test
	public void testUnlinkCannotDeletePermission() {
		configure();
		EntityBundle bundle = setupEntity(new Link());
		when(mockPermissions.getCanDelete()).thenReturn(false);
		
		widget.setEntityBundle(bundle);
		verify(mockView, never()).showUnlinkIcon();
	}
	
	@Test
	public void testUnlinkNotALink() {
		configure();
		EntityBundle bundle = setupEntity(new Project());
		when(mockPermissions.getCanDelete()).thenReturn(true);
		
		widget.setEntityBundle(bundle);
		verify(mockView, never()).showUnlinkIcon();
	}
	
	@Test
	public void testOnUnlink() {
		//simulate successful delete of Link entity
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseJavascriptClient).deleteEntityById(anyString(), any(AsyncCallback.class));
		configure();
		EntityBundle bundle = setupEntity(new Link());
		when(mockPermissions.getCanDelete()).thenReturn(true);
		widget.setEntityBundle(bundle);
		
		widget.onUnlink();
		verify(mockSynapseJavascriptClient).deleteEntityById(eq(entityId), any(AsyncCallback.class));
		verify(mockPopupUtils).showInfo(EntityBadge.LINK_SUCCESSFULLY_DELETED);
	}
	
	@Test
	public void testOnUnlinkFailure() {
		//simulate failure to delete of Link entity
		String errorMessage = "error occurred";
		Exception ex = new Exception(errorMessage);
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseJavascriptClient).deleteEntityById(anyString(), any(AsyncCallback.class));
		configure();
		EntityBundle bundle = setupEntity(new Link());
		when(mockPermissions.getCanDelete()).thenReturn(true);
		widget.setEntityBundle(bundle);
		
		widget.onUnlink();
		verify(mockSynapseJavascriptClient).deleteEntityById(eq(entityId), any(AsyncCallback.class));
		verify(mockPopupUtils).showErrorMessage(errorMessage);
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
		String result = widget.getContentSize(null);
		assertTrue("".equals(result));
		
		FileHandle s3FileHandle = new S3FileHandle();
		s3FileHandle.setContentSize(500L);
		
		result = widget.getContentSize(s3FileHandle);
		assertEquals(friendlySize, result);
	}
	
	@Test
	public void testContentMd5() {
		String result = widget.getContentMd5(null);
		assertTrue("".equals(result));
		
		FileHandle s3FileHandle = new S3FileHandle();
		String contentMd5 = "fghij";
		s3FileHandle.setContentMd5(contentMd5);
		
		result = widget.getContentMd5(s3FileHandle);
		assertEquals(contentMd5, result);
	}
	
	@Test
	public void testOnAddToDownloadList() {
		String fileHandleId = "9999";
		when(mockDataFileHandle.getId()).thenReturn(fileHandleId);
		FileEntity testFile = new FileEntity();
		testFile.setId(entityId);
		testFile.setDataFileHandleId(fileHandleId);
		setupEntity(testFile);
		EntityHeader header = configure();
		
		widget.getEntityBundle();
		
		widget.onAddToDownloadList();
		
		verify(mockSynapseJavascriptClient).addFileToDownloadList(eq(fileHandleId), eq(entityId), any(AsyncCallback.class));
		verify(mockPopupUtils).showInfo(header.getName() + EntityBadge.ADDED_TO_DOWNLOAD_LIST, "#!Profile:"+USER_ID+"/downloads", EntityBadge.VIEW_DOWNLOAD_LIST, IconType.CHECK_CIRCLE);
		verify(mockEventBus).fireEvent(any(DownloadListUpdatedEvent.class));
		verify(mockSynapseJSNIUtils).sendAnalyticsEvent(AddToDownloadList.DOWNLOAD_ACTION_EVENT_NAME, AddToDownloadList.FILES_ADDED_TO_DOWNLOAD_LIST_EVENT_NAME, Integer.toString(1));
	}
	
	@Test
	public void testOnAddToDownloadListError() {
		String errorMessage = "a simulated error";
		AsyncMockStubber.callFailureWith(new Exception(errorMessage)).when(mockSynapseJavascriptClient).addFileToDownloadList(anyString(), anyString(), any(AsyncCallback.class));
		String fileHandleId = "9999";
		when(mockDataFileHandle.getId()).thenReturn(fileHandleId);
		FileEntity testFile = new FileEntity();
		testFile.setId(entityId);
		testFile.setDataFileHandleId(fileHandleId);
		setupEntity(testFile);
		configure();
		widget.getEntityBundle();
		
		widget.onAddToDownloadList();
		
		verify(mockSynapseJavascriptClient).addFileToDownloadList(eq(fileHandleId), eq(entityId), any(AsyncCallback.class));
		verifyZeroInteractions(mockPopupUtils, mockEventBus);
		verify(mockView).setError(errorMessage);
	}
}
