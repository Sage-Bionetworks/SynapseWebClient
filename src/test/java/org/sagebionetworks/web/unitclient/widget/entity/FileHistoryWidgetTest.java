package org.sagebionetworks.web.unitclient.widget.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.matches;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.Data;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.TermsOfUseAccessRequirement;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.repo.model.VersionInfo;
import org.sagebionetworks.repo.model.Versionable;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.EntitySchemaCache;
import org.sagebionetworks.web.client.EntityTypeProvider;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.FileHistoryWidget;
import org.sagebionetworks.web.client.widget.entity.FileHistoryWidgetView;
import org.sagebionetworks.web.client.widget.entity.JiraURLHelper;
import org.sagebionetworks.web.client.widget.entity.controller.PreflightController;
import org.sagebionetworks.web.client.widget.pagination.DetailedPaginationWidget;
import org.sagebionetworks.web.shared.EntityWrapper;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class FileHistoryWidgetTest {

	public static final Long CURRENT_FILE_VERSION = 8888L;
	SynapseClientAsync mockSynapseClient;
	AuthenticationController mockAuthenticationController;
	NodeModelCreator mockNodeModelCreator;
	GlobalApplicationState mockGlobalApplicationState;
	FileHistoryWidgetView mockView;
	EntitySchemaCache mockSchemaCache;
	EntityTypeProvider mockEntityTypeProvider;
	IconsImageBundle mockIconsImageBundle;
	JiraURLHelper mockJiraURLHelper;
	PreflightController mockPreflightController;
	FileHistoryWidget fileHistoryWidget;
	Versionable vb;
	String entityId = "syn123";
	EntityBundle bundle;
	AdapterFactory adapterFactory = new AdapterFactoryImpl();
	DetailedPaginationWidget mockPaginationWidget;
	public static final Long DEFAULT_MOCK_VERSION_COUNT = 2L;
	@Before
	public void before() throws JSONObjectAdapterException {
		mockAuthenticationController = mock(AuthenticationController.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockNodeModelCreator = mock(NodeModelCreator.class);
		mockPaginationWidget = mock(DetailedPaginationWidget.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockView = mock(FileHistoryWidgetView.class);
		mockSchemaCache = mock(EntitySchemaCache.class);
		mockEntityTypeProvider = mock(EntityTypeProvider.class);
		mockIconsImageBundle = mock(IconsImageBundle.class);
		mockJiraURLHelper = mock(JiraURLHelper.class);
		mockPreflightController = mock(PreflightController.class);
		UserSessionData usd = new UserSessionData();
		UserProfile up = new UserProfile();
		up.setOwnerId("101");
		usd.setProfile(up);
		
		when(mockAuthenticationController.getCurrentUserSessionData()).thenReturn(usd);
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);


		fileHistoryWidget = new FileHistoryWidget(mockView, mockNodeModelCreator, mockSynapseClient, mockGlobalApplicationState, mockAuthenticationController, mockPaginationWidget, mockPreflightController);

		vb = new FileEntity();
		vb.setId(entityId);
		vb.setVersionNumber(new Long(1));
		vb.setVersionLabel("");
		vb.setVersionComment("");
		bundle = mock(EntityBundle.class, RETURNS_DEEP_STUBS);
		when(bundle.getPermissions().getCanCertifiedUserEdit()).thenReturn(true);
		when(bundle.getEntity()).thenReturn(vb);

		List<AccessRequirement> accessRequirements = new ArrayList<AccessRequirement>();
		TermsOfUseAccessRequirement accessRequirement = new TermsOfUseAccessRequirement();
		accessRequirement.setId(101L);
		accessRequirement.setTermsOfUse("terms of use");
		accessRequirements.add(accessRequirement);
		when(bundle.getAccessRequirements()).thenReturn(accessRequirements);
		when(bundle.getUnmetAccessRequirements()).thenReturn(accessRequirements);
				
		AsyncMockStubber.callSuccessWith(new EntityWrapper()).when(mockSynapseClient).getEntity(anyString(), any(AsyncCallback.class));
		
		PaginatedResults<VersionInfo> mockPagedResults = mock(PaginatedResults.class);
		when(mockPagedResults.getTotalNumberOfResults()).thenReturn(DEFAULT_MOCK_VERSION_COUNT);
		List<VersionInfo> versions = new ArrayList<VersionInfo>();
		VersionInfo v1 = new VersionInfo();
		v1.setVersionNumber(CURRENT_FILE_VERSION);
		versions.add(v1);
		VersionInfo v2 = new VersionInfo();
		v2.setVersionNumber(8889L);
		versions.add(v2);
		when(mockPagedResults.getResults()).thenReturn(versions);
		when(mockNodeModelCreator.createPaginatedResults(anyString(), any(Class.class))).thenReturn(mockPagedResults);
		
	}

	@Test
	public void testOnPageChange() throws Exception {
		AsyncMockStubber
				.callSuccessWith("")
				.when(mockSynapseClient)
				.getEntityVersions(anyString(), anyInt(), anyInt(),
						any(AsyncCallback.class));
		//with null version
		fileHistoryWidget.setEntityBundle(bundle, null);
				
		verify(mockView).clearVersions();
		verify(mockPaginationWidget).configure(FileHistoryWidget.VERSION_LIMIT.longValue(), 0L, DEFAULT_MOCK_VERSION_COUNT, fileHistoryWidget);
		
		//verify current version is set when offset is 0
		assertEquals(CURRENT_FILE_VERSION, fileHistoryWidget.getVersionNumber());
	}

	@Test
	public void testLoadVersionsFail() throws Exception {
		AsyncMockStubber
				.callFailureWith(new IllegalArgumentException())
				.when(mockSynapseClient)
				.getEntityVersions(anyString(), anyInt(), anyInt(),
						any(AsyncCallback.class));
		AsyncCallback<PaginatedResults<VersionInfo>> callback = new AsyncCallback<PaginatedResults<VersionInfo>>() {
			@Override
			public void onFailure(Throwable caught) {
				assertTrue(caught instanceof IllegalArgumentException);
			}

			@Override
			public void onSuccess(PaginatedResults<VersionInfo> result) {
				fail("Called onSuccess on a failure");
			}
		};
		fileHistoryWidget.setEntityBundle(bundle, null);
	}

	@Test
	public void testUpdateVersionLabel() throws Exception {
		String testLabel = "testLabel";
		fileHistoryWidget.setEntityBundle(bundle, null);
		fileHistoryWidget.updateVersionLabel(testLabel);
		ArgumentCaptor<Entity> entityCaptor = ArgumentCaptor.forClass(Entity.class);
		verify(mockSynapseClient).updateEntity(entityCaptor.capture(), (AsyncCallback<Entity>) any());
		Versionable capturedEntity = (Versionable)entityCaptor.getValue();
		assertEquals(testLabel, capturedEntity.getVersionLabel());
	}
	
	@Test
	public void testUpdateVersionComment() throws Exception {
		String testComment = "testComment";
		fileHistoryWidget.setEntityBundle(bundle, null);
		fileHistoryWidget.updateVersionComment(testComment);
		ArgumentCaptor<Entity> entityCaptor = ArgumentCaptor.forClass(Entity.class);
		verify(mockSynapseClient).updateEntity(entityCaptor.capture(), (AsyncCallback<Entity>) any());
		Versionable capturedEntity = (Versionable)entityCaptor.getValue();
		assertEquals(testComment, capturedEntity.getVersionComment());
	}

	@Test
	public void testDeleteVersion() throws Exception {
		fileHistoryWidget.setEntityBundle(bundle, 20L);
		fileHistoryWidget.deleteVersion(vb.getVersionNumber());
		verify(mockSynapseClient).deleteEntityVersionById(matches(vb.getId()), eq(vb.getVersionNumber()), (AsyncCallback<Void>) any());
	}
	
	@Test
	public void testOnEdit() {
		fileHistoryWidget.setEntityBundle(bundle, null);
		String oldValue = "an old comment";
		vb.setVersionComment(oldValue);
		AsyncMockStubber.callWithInvoke().when(mockPreflightController).checkUploadToEntity(any(EntityBundle.class), any(Callback.class));
		fileHistoryWidget.onEditVersionCommentClicked();
		verify(mockView).showEditVersionComment(oldValue);
	}
	
	@Test
	public void testOnEditFailedPreflight() {
		fileHistoryWidget.setEntityBundle(bundle, null);
		AsyncMockStubber.callNoInvovke().when(mockPreflightController).checkUploadToEntity(any(EntityBundle.class), any(Callback.class));
		fileHistoryWidget.onEditVersionCommentClicked();
		verify(mockView, never()).showEditVersionComment(anyString());
	}
	

	@Test
	public void testOnEditLabel() {
		fileHistoryWidget.setEntityBundle(bundle, null);
		String oldValue = "an old label";
		vb.setVersionLabel(oldValue);
		AsyncMockStubber.callWithInvoke().when(mockPreflightController).checkUploadToEntity(any(EntityBundle.class), any(Callback.class));
		fileHistoryWidget.onEditVersionLabelClicked();
		verify(mockView).showEditVersionLabel(oldValue);
	}
	
	@Test
	public void testOnEditLabelFailedPreflight() {
		fileHistoryWidget.setEntityBundle(bundle, null);
		AsyncMockStubber.callNoInvovke().when(mockPreflightController).checkUploadToEntity(any(EntityBundle.class), any(Callback.class));
		fileHistoryWidget.onEditVersionCommentClicked();
		verify(mockView, never()).showEditVersionComment(anyString());
	}
	
	@Test
	public void testSetEntityBundleCanEditCurrent() {
		when(bundle.getPermissions().getCanCertifiedUserEdit()).thenReturn(true);
		fileHistoryWidget.setEntityBundle(bundle, null);
		
		//auto expand version history = false
		verify(mockView).setEntityBundle(vb, false);
		verify(mockView).setEditVersionCommentButtonVisible(true);
		verify(mockView).setEditVersionLabelButtonVisible(true);
	}
	
	@Test
	public void testSetEntityBundleCanEditPrevious() {
		when(bundle.getPermissions().getCanCertifiedUserEdit()).thenReturn(true);
		fileHistoryWidget.setEntityBundle(bundle, 24L);

		//auto expand version history = true
		verify(mockView).setEntityBundle(vb, true);
		verify(mockView).setEditVersionCommentButtonVisible(false);
		verify(mockView).setEditVersionLabelButtonVisible(false);
	}
	
	@Test
	public void testSetEntityBundleNoEditCurrent() {
		when(bundle.getPermissions().getCanCertifiedUserEdit()).thenReturn(false);
		fileHistoryWidget.setEntityBundle(bundle, null);
		
		//auto expand version history = false
		verify(mockView).setEntityBundle(vb, false);
		verify(mockView).setEditVersionCommentButtonVisible(false);
		verify(mockView).setEditVersionLabelButtonVisible(false);
	}
	
	@Test
	public void testSetEntityBundleNoEditPrevious() {
		when(bundle.getPermissions().getCanCertifiedUserEdit()).thenReturn(false);
		fileHistoryWidget.setEntityBundle(bundle, 24L);

		//auto expand version history = false
		verify(mockView).setEntityBundle(vb, true);
		verify(mockView).setEditVersionCommentButtonVisible(false);
		verify(mockView).setEditVersionLabelButtonVisible(false);

	}
	
	
	
}
