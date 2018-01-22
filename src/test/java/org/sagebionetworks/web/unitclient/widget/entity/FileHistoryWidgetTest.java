package org.sagebionetworks.web.unitclient.widget.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.matches;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.TermsOfUseAccessRequirement;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.repo.model.VersionInfo;
import org.sagebionetworks.repo.model.VersionableEntity;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.EntitySchemaCache;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.FileHistoryWidget;
import org.sagebionetworks.web.client.widget.entity.FileHistoryWidgetView;
import org.sagebionetworks.web.client.widget.entity.JiraURLHelper;
import org.sagebionetworks.web.client.widget.entity.controller.PreflightController;
import org.sagebionetworks.web.client.widget.pagination.countbased.BasicPaginationWidget;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class FileHistoryWidgetTest {

	public static final Long CURRENT_FILE_VERSION = 8888L;
	@Mock
	SynapseClientAsync mockSynapseClient;
	@Mock
	SynapseJavascriptClient mockSynapseJavascriptClient;
	@Mock
	AuthenticationController mockAuthenticationController;
	@Mock
	GlobalApplicationState mockGlobalApplicationState;
	@Mock
	FileHistoryWidgetView mockView;
	@Mock
	EntitySchemaCache mockSchemaCache;
	@Mock
	IconsImageBundle mockIconsImageBundle;
	@Mock
	JiraURLHelper mockJiraURLHelper;
	@Mock
	PreflightController mockPreflightController;
	@Mock
	PlaceChanger mockPlaceChanger;
	FileHistoryWidget fileHistoryWidget;
	VersionableEntity vb;
	String entityId = "syn123";
	EntityBundle bundle;
	AdapterFactory adapterFactory = new AdapterFactoryImpl();
	@Mock
	BasicPaginationWidget mockPaginationWidget;
	@Captor
	ArgumentCaptor<Place> placeCaptor;
	public static final Long DEFAULT_MOCK_VERSION_COUNT = 2L;
	boolean isOnCurrentVersion;
	@Before
	public void before() throws JSONObjectAdapterException {
		MockitoAnnotations.initMocks(this);
		UserSessionData usd = new UserSessionData();
		UserProfile up = new UserProfile();
		up.setOwnerId("101");
		usd.setProfile(up);
		
		when(mockAuthenticationController.getCurrentUserSessionData()).thenReturn(usd);
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);

		fileHistoryWidget = new FileHistoryWidget(mockView, mockSynapseClient, mockGlobalApplicationState, mockAuthenticationController, mockPaginationWidget, mockPreflightController);

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
				
		AsyncMockStubber.callSuccessWith(vb).when(mockSynapseJavascriptClient).getEntity(anyString(), any(AsyncCallback.class));
		
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
		AsyncMockStubber.callSuccessWith(mockPagedResults).when(mockSynapseClient).getEntityVersions(anyString(), anyInt(), anyInt(),any(AsyncCallback.class));
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		isOnCurrentVersion = true;
	}

	@Test
	public void testOnPageChange() throws Exception {
		//with null version
		fileHistoryWidget.setEntityBundle(bundle, null, isOnCurrentVersion);
				
		verify(mockView).clearVersions();
		verify(mockPaginationWidget).configure(FileHistoryWidget.VERSION_LIMIT.longValue(), 0L, DEFAULT_MOCK_VERSION_COUNT, fileHistoryWidget);
		
		//verify current version is set when offset is 0
		assertEquals(CURRENT_FILE_VERSION, fileHistoryWidget.getVersionNumber());
		
		verify(mockSynapseClient).getEntityVersions(anyString(), eq(WebConstants.ZERO_OFFSET.intValue()), anyInt(), any(AsyncCallback.class));
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
		fileHistoryWidget.setEntityBundle(bundle, null, isOnCurrentVersion);
	}

	@Test
	public void testUpdateVersionInfo() throws Exception {
		String testLabel = "testLabel";
		String testComment = "testComment";
		fileHistoryWidget.setEntityBundle(bundle, null, isOnCurrentVersion);
		fileHistoryWidget.updateVersionInfo(testLabel, testComment);
		ArgumentCaptor<Entity> entityCaptor = ArgumentCaptor.forClass(Entity.class);
		verify(mockSynapseClient).updateEntity(entityCaptor.capture(), (AsyncCallback<Entity>) any());
		VersionableEntity capturedEntity = (VersionableEntity)entityCaptor.getValue();
		assertEquals(testComment, capturedEntity.getVersionComment());
		assertEquals(testLabel, capturedEntity.getVersionLabel());
	}
	
	@Test
	public void testUpdateVersionInfoNoOp() {
		String testLabel = "testLabel";
		String testComment = "testComment";
		vb.setVersionLabel(testLabel);
		vb.setVersionComment(testComment);
		fileHistoryWidget.setEntityBundle(bundle, null, isOnCurrentVersion);
		fileHistoryWidget.updateVersionInfo(testLabel, testComment);
		verify(mockSynapseClient, never()).updateEntity(any(Entity.class), (AsyncCallback<Entity>) any());
		verify(mockView).hideEditVersionInfo();
	}
	
	@Test
	public void testUpdateVersionInfoFailure() {
		String errorMessage = "error";
		Exception ex = new Exception(errorMessage);
		String testLabel = "testLabel";
		String testComment = "testComment";
		fileHistoryWidget.setEntityBundle(bundle, null, isOnCurrentVersion);
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseClient).updateEntity(any(Entity.class), any(AsyncCallback.class));
		fileHistoryWidget.updateVersionInfo(testLabel, testComment);
		verify(mockSynapseClient).updateEntity(any(Entity.class), (AsyncCallback<Entity>) any());
		verify(mockView).showEditVersionInfoError(anyString());
	}

	@Test
	public void testDeleteVersion() {
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseClient).deleteEntityVersionById(anyString(), anyLong(), any(AsyncCallback.class));
		fileHistoryWidget.setEntityBundle(bundle, 20L, isOnCurrentVersion);
		verify(mockSynapseClient).getEntityVersions(anyString(), eq(WebConstants.ZERO_OFFSET.intValue()), anyInt(), any(AsyncCallback.class));
		
		fileHistoryWidget.deleteVersion(vb.getVersionNumber());
		
		verify(mockSynapseClient).deleteEntityVersionById(matches(vb.getId()), eq(vb.getVersionNumber()), (AsyncCallback<Void>) any());
		//deleting a different version, verify file history widget is refreshed
		verify(mockSynapseClient, times(2)).getEntityVersions(anyString(), eq(WebConstants.ZERO_OFFSET.intValue()), anyInt(), any(AsyncCallback.class));
	}
	
	@Test
	public void testDeleteCurrentlyViewedVersion() {
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseClient).deleteEntityVersionById(anyString(), anyLong(), any(AsyncCallback.class));
		fileHistoryWidget.setEntityBundle(bundle, vb.getVersionNumber(), isOnCurrentVersion);
		verify(mockSynapseClient).getEntityVersions(anyString(), eq(WebConstants.ZERO_OFFSET.intValue()), anyInt(), any(AsyncCallback.class));
		
		fileHistoryWidget.deleteVersion(vb.getVersionNumber());
		
		verify(mockSynapseClient).deleteEntityVersionById(matches(vb.getId()), eq(vb.getVersionNumber()), (AsyncCallback<Void>) any());
		//deleting a different version, verify file history widget is not simply refreshed (still called only once during setEntityBundle())
		verify(mockSynapseClient).getEntityVersions(anyString(), eq(WebConstants.ZERO_OFFSET.intValue()), anyInt(), any(AsyncCallback.class));
		verify(mockPlaceChanger).goTo(placeCaptor.capture());
		//verify going to the current version after delete
		Synapse newPlace = (Synapse)placeCaptor.getValue();
		assertEquals(bundle.getEntity().getId(), newPlace.getEntityId());
		assertNull(newPlace.getVersionNumber());
	}
	
	@Test
	public void testDeleteVersionFailure() {
		Exception ex = new Exception("error occurred");
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseClient).deleteEntityVersionById(anyString(), anyLong(), any(AsyncCallback.class));
		fileHistoryWidget.setEntityBundle(bundle, 20L, isOnCurrentVersion);
		
		fileHistoryWidget.deleteVersion(vb.getVersionNumber());
		
		verify(mockSynapseClient).deleteEntityVersionById(matches(vb.getId()), eq(vb.getVersionNumber()), (AsyncCallback<Void>) any());
		verify(mockView).showErrorMessage(anyString());
	}
	
	@Test
	public void testOnEdit() {
		fileHistoryWidget.setEntityBundle(bundle, null, isOnCurrentVersion);
		String oldComment = "an old comment";
		vb.setVersionComment(oldComment);
		String oldLabel = "an old label";
		vb.setVersionLabel(oldLabel);
		AsyncMockStubber.callWithInvoke().when(mockPreflightController).checkUploadToEntity(any(EntityBundle.class), any(Callback.class));
		fileHistoryWidget.onEditVersionInfoClicked();
		verify(mockView).showEditVersionInfo(oldLabel, oldComment);
	}
	
	@Test
	public void testOnEditFailedPreflight() {
		fileHistoryWidget.setEntityBundle(bundle, null, isOnCurrentVersion);
		AsyncMockStubber.callNoInvovke().when(mockPreflightController).checkUploadToEntity(any(EntityBundle.class), any(Callback.class));
		fileHistoryWidget.onEditVersionInfoClicked();
		verify(mockView, never()).showEditVersionInfo(anyString(), anyString());
	}
	
	@Test
	public void testOnEditLabelFailedPreflight() {
		fileHistoryWidget.setEntityBundle(bundle, null, isOnCurrentVersion);
		AsyncMockStubber.callNoInvovke().when(mockPreflightController).checkUploadToEntity(any(EntityBundle.class), any(Callback.class));
		fileHistoryWidget.onEditVersionInfoClicked();
		verify(mockView, never()).showEditVersionInfo(anyString(), anyString());
	}
	
	@Test
	public void testSetEntityBundleCanEditCurrent() {
		when(bundle.getPermissions().getCanCertifiedUserEdit()).thenReturn(true);
		fileHistoryWidget.setEntityBundle(bundle, null, isOnCurrentVersion);
		
		//auto expand version history = false
		verify(mockView).setEntityBundle(vb, false);
		verify(mockView).setEditVersionInfoButtonVisible(true);
	}
	
	@Test
	public void testSetEntityBundleCanEditPrevious() {
		//showing a previous version
		isOnCurrentVersion = false;
		when(bundle.getPermissions().getCanCertifiedUserEdit()).thenReturn(true);
		fileHistoryWidget.setEntityBundle(bundle, 24L, isOnCurrentVersion);

		//auto expand version history = true
		verify(mockView).setEntityBundle(vb, true);
		verify(mockView).setEditVersionInfoButtonVisible(false);
	}
	
	@Test
	public void testSetEntityBundleNoEditCurrent() {
		when(bundle.getPermissions().getCanCertifiedUserEdit()).thenReturn(false);
		fileHistoryWidget.setEntityBundle(bundle, null, isOnCurrentVersion);
		
		//auto expand version history = false
		verify(mockView).setEntityBundle(vb, false);
		verify(mockView).setEditVersionInfoButtonVisible(false);
	}
	
	@Test
	public void testSetEntityBundleNoEditPrevious() {
		//showing a previous version
		isOnCurrentVersion = false;
		when(bundle.getPermissions().getCanCertifiedUserEdit()).thenReturn(false);
		fileHistoryWidget.setEntityBundle(bundle, 24L, isOnCurrentVersion);

		//auto expand version history = false
		verify(mockView).setEntityBundle(vb, true);
		verify(mockView).setEditVersionInfoButtonVisible(false);
	}
	
	
	
}
