package org.sagebionetworks.web.unitclient.widget.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.matches;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.TermsOfUseAccessRequirement;
import org.sagebionetworks.repo.model.VersionInfo;
import org.sagebionetworks.repo.model.VersionableEntity;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.VersionHistoryWidget;
import org.sagebionetworks.web.client.widget.entity.VersionHistoryWidgetView;
import org.sagebionetworks.web.client.widget.entity.controller.PreflightController;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;

@RunWith(MockitoJUnitRunner.class)
public class VersionHistoryWidgetTest {
	public static final Long CURRENT_FILE_VERSION = 8888L;
	@Mock
	SynapseClientAsync mockSynapseClient;
	@Mock
	SynapseJavascriptClient mockJsClient;
	@Mock
	SynapseJavascriptClient mockSynapseJavascriptClient;
	@Mock
	GlobalApplicationState mockGlobalApplicationState;
	@Mock
	VersionHistoryWidgetView mockView;
	@Mock
	IconsImageBundle mockIconsImageBundle;
	@Mock
	PreflightController mockPreflightController;
	@Mock
	PlaceChanger mockPlaceChanger;
	VersionHistoryWidget versionHistoryWidget;
	VersionableEntity vb;
	String entityId = "syn123";
	EntityBundle bundle;
	AdapterFactory adapterFactory = new AdapterFactoryImpl();
	@Captor
	ArgumentCaptor<Place> placeCaptor;
	@Mock
	SynapseAlert mockSynAlert;

	@Before
	public void before() throws JSONObjectAdapterException {
		versionHistoryWidget = new VersionHistoryWidget(mockView, mockSynapseClient, mockJsClient, mockGlobalApplicationState, mockPreflightController, mockSynAlert);

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

		List<VersionInfo> versions = new ArrayList<VersionInfo>();
		VersionInfo v1 = new VersionInfo();
		v1.setVersionNumber(CURRENT_FILE_VERSION);
		versions.add(v1);
		VersionInfo v2 = new VersionInfo();
		v2.setVersionNumber(8889L);
		versions.add(v2);
		AsyncMockStubber.callSuccessWith(versions).when(mockJsClient).getEntityVersions(anyString(), anyInt(), anyInt(), any(AsyncCallback.class));
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		AsyncMockStubber.callSuccessWith(vb).when(mockJsClient).updateEntity(any(Entity.class), anyString(), anyBoolean(), any(AsyncCallback.class));
	}

	@Test
	public void testOnMore() throws Exception {
		// with null version
		versionHistoryWidget.setEntityBundle(bundle, null);

		verify(mockView).clearVersions();

		// verify current version is set when offset is 0
		assertEquals(CURRENT_FILE_VERSION, versionHistoryWidget.getVersionNumber());

		verify(mockJsClient).getEntityVersions(anyString(), eq(WebConstants.ZERO_OFFSET.intValue()), anyInt(), any(AsyncCallback.class));
	}

	@Test
	public void testLoadVersionsFail() throws Exception {
		AsyncMockStubber.callFailureWith(new IllegalArgumentException()).when(mockJsClient).getEntityVersions(anyString(), anyInt(), anyInt(), any(AsyncCallback.class));
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
		versionHistoryWidget.setEntityBundle(bundle, null);
	}

	@Test
	public void testUpdateVersionInfo() throws Exception {
		String testLabel = "testLabel";
		String testComment = "testComment";
		versionHistoryWidget.setEntityBundle(bundle, null);
		versionHistoryWidget.updateVersionInfo(testLabel, testComment);
		ArgumentCaptor<Entity> entityCaptor = ArgumentCaptor.forClass(Entity.class);
		verify(mockJsClient).updateEntity(entityCaptor.capture(), anyString(), anyBoolean(), (AsyncCallback<Entity>) any());
		VersionableEntity capturedEntity = (VersionableEntity) entityCaptor.getValue();
		assertEquals(testComment, capturedEntity.getVersionComment());
		assertEquals(testLabel, capturedEntity.getVersionLabel());
		verify(mockGlobalApplicationState).refreshPage();
	}

	@Test
	public void testUpdateVersionInfoNoOp() {
		String testLabel = "testLabel";
		String testComment = "testComment";
		vb.setVersionLabel(testLabel);
		vb.setVersionComment(testComment);
		versionHistoryWidget.setEntityBundle(bundle, null);
		versionHistoryWidget.updateVersionInfo(testLabel, testComment);
		verify(mockJsClient, never()).updateEntity(any(Entity.class), anyString(), anyBoolean(), (AsyncCallback<Entity>) any());
		verify(mockView).hideEditVersionInfo();
	}

	@Test
	public void testUpdateVersionInfoFailure() {
		String errorMessage = "error";
		Exception ex = new Exception(errorMessage);
		String testLabel = "testLabel";
		String testComment = "testComment";
		versionHistoryWidget.setEntityBundle(bundle, null);
		AsyncMockStubber.callFailureWith(ex).when(mockJsClient).updateEntity(any(Entity.class), anyString(), anyBoolean(), any(AsyncCallback.class));
		versionHistoryWidget.updateVersionInfo(testLabel, testComment);
		verify(mockJsClient).updateEntity(any(Entity.class), anyString(), anyBoolean(), (AsyncCallback<Entity>) any());
		verify(mockSynAlert).handleException(ex);
	}

	@Test
	public void testDeleteVersion() {
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseClient).deleteEntityVersionById(anyString(), anyLong(), any(AsyncCallback.class));
		versionHistoryWidget.setEntityBundle(bundle, 20L);
		verify(mockJsClient).getEntityVersions(anyString(), eq(WebConstants.ZERO_OFFSET.intValue()), anyInt(), any(AsyncCallback.class));

		versionHistoryWidget.deleteVersion(vb.getVersionNumber());

		verify(mockSynapseClient).deleteEntityVersionById(matches(vb.getId()), eq(vb.getVersionNumber()), (AsyncCallback<Void>) any());
		// deleting a different version, verify file history widget is refreshed
		verify(mockJsClient, times(2)).getEntityVersions(anyString(), eq(WebConstants.ZERO_OFFSET.intValue()), anyInt(), any(AsyncCallback.class));
	}

	@Test
	public void testDeleteCurrentlyViewedVersion() {
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseClient).deleteEntityVersionById(anyString(), anyLong(), any(AsyncCallback.class));
		versionHistoryWidget.setEntityBundle(bundle, vb.getVersionNumber());
		verify(mockJsClient).getEntityVersions(anyString(), eq(WebConstants.ZERO_OFFSET.intValue()), anyInt(), any(AsyncCallback.class));

		versionHistoryWidget.deleteVersion(vb.getVersionNumber());

		verify(mockSynapseClient).deleteEntityVersionById(matches(vb.getId()), eq(vb.getVersionNumber()), (AsyncCallback<Void>) any());
		// deleting a different version, verify file history widget is not simply refreshed (still called
		// only once during setEntityBundle())
		verify(mockJsClient).getEntityVersions(anyString(), eq(WebConstants.ZERO_OFFSET.intValue()), anyInt(), any(AsyncCallback.class));
		verify(mockPlaceChanger).goTo(placeCaptor.capture());
		// verify going to the current version after delete
		Synapse newPlace = (Synapse) placeCaptor.getValue();
		assertEquals(bundle.getEntity().getId(), newPlace.getEntityId());
		assertNull(newPlace.getVersionNumber());
	}

	@Test
	public void testDeleteVersionFailure() {
		Exception ex = new Exception("error occurred");
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseClient).deleteEntityVersionById(anyString(), anyLong(), any(AsyncCallback.class));
		versionHistoryWidget.setEntityBundle(bundle, 20L);

		versionHistoryWidget.deleteVersion(vb.getVersionNumber());

		verify(mockSynapseClient).deleteEntityVersionById(matches(vb.getId()), eq(vb.getVersionNumber()), (AsyncCallback<Void>) any());
		verify(mockSynAlert).handleException(ex);
	}

	@Test
	public void testOnEdit() {
		versionHistoryWidget.setEntityBundle(bundle, null);
		String oldComment = "an old comment";
		vb.setVersionComment(oldComment);
		String oldLabel = "an old label";
		vb.setVersionLabel(oldLabel);
		AsyncMockStubber.callWithInvoke().when(mockPreflightController).checkUploadToEntity(any(EntityBundle.class), any(Callback.class));
		versionHistoryWidget.onEditVersionInfoClicked();
		verify(mockView).showEditVersionInfo(oldLabel, oldComment);
	}

	@Test
	public void testOnEditFailedPreflight() {
		versionHistoryWidget.setEntityBundle(bundle, null);
		AsyncMockStubber.callNoInvovke().when(mockPreflightController).checkUploadToEntity(any(EntityBundle.class), any(Callback.class));
		versionHistoryWidget.onEditVersionInfoClicked();
		verify(mockView, never()).showEditVersionInfo(anyString(), anyString());
	}

	@Test
	public void testOnEditLabelFailedPreflight() {
		versionHistoryWidget.setEntityBundle(bundle, null);
		AsyncMockStubber.callNoInvovke().when(mockPreflightController).checkUploadToEntity(any(EntityBundle.class), any(Callback.class));
		versionHistoryWidget.onEditVersionInfoClicked();
		verify(mockView, never()).showEditVersionInfo(anyString(), anyString());
	}

	@Test
	public void testSetEntityBundleCanEditCurrent() {
		when(bundle.getPermissions().getCanCertifiedUserEdit()).thenReturn(true);
		versionHistoryWidget.setEntityBundle(bundle, null);

		// auto expand version history = false
		verify(mockView).setEntityBundle(vb, false);
		verify(mockView).setEditVersionInfoButtonVisible(true);
	}

	@Test
	public void testSetEntityBundleCanEditPrevious() {
		// showing a previous version
		// TODO: fix
		when(bundle.getPermissions().getCanCertifiedUserEdit()).thenReturn(true);
		versionHistoryWidget.setEntityBundle(bundle, 24L);

		// auto expand version history = true
		verify(mockView).setEntityBundle(vb, true);
		verify(mockView).setEditVersionInfoButtonVisible(false);
	}

	@Test
	public void testSetEntityBundleNoEditCurrent() {
		when(bundle.getPermissions().getCanCertifiedUserEdit()).thenReturn(false);
		versionHistoryWidget.setEntityBundle(bundle, null);

		// auto expand version history = false
		verify(mockView).setEntityBundle(vb, false);
		verify(mockView).setEditVersionInfoButtonVisible(false);
	}

	@Test
	public void testSetEntityBundleNoEditPrevious() {
		when(bundle.getPermissions().getCanCertifiedUserEdit()).thenReturn(false);
		versionHistoryWidget.setEntityBundle(bundle, 24L);

		// auto expand version history = false
		verify(mockView).setEntityBundle(vb, true);
		verify(mockView).setEditVersionInfoButtonVisible(false);
	}

	private void setupVersionResults(int count, int startVersionNumber) {
		List<VersionInfo> versions = new ArrayList<VersionInfo>();
		for (int i = 0; i < count; i++) {
			VersionInfo v = new VersionInfo();
			v.setVersionNumber(new Long(i + startVersionNumber));
			versions.add(v);
		}
		AsyncMockStubber.callSuccessWith(versions).when(mockJsClient).getEntityVersions(anyString(), anyInt(), anyInt(), any(AsyncCallback.class));
	}

	@Test
	public void testGetMore() {
		// simulate service initially returns a full page.
		setupVersionResults(VersionHistoryWidget.VERSION_LIMIT, 0);
		boolean canEdit = true;
		when(bundle.getPermissions().getCanCertifiedUserEdit()).thenReturn(canEdit);
		Long currentVersion = 0L;
		versionHistoryWidget.setEntityBundle(bundle, currentVersion);

		verify(mockJsClient).getEntityVersions(eq(entityId), eq(0), eq(VersionHistoryWidget.VERSION_LIMIT), any(AsyncCallback.class));
		verify(mockView).clearVersions();
		// version 0 is selected, so we should be able to edit
		verify(mockView).setEntityBundle(vb, false);
		verify(mockView).setEditVersionInfoButtonVisible(true);
		verify(mockView).setMoreButtonVisible(true);
		// verify full page is added (one of which is selected)
		boolean isVersionSelected = false;
		verify(mockView, times(VersionHistoryWidget.VERSION_LIMIT - 1)).addVersion(eq(entityId), any(VersionInfo.class), eq(canEdit), eq(isVersionSelected));
		isVersionSelected = true;
		verify(mockView).addVersion(eq(entityId), any(VersionInfo.class), eq(canEdit), eq(isVersionSelected));

		// now get the second page (verify new offset).
		// second page contains 2 versions only.
		setupVersionResults(2, VersionHistoryWidget.VERSION_LIMIT);
		reset(mockView);

		versionHistoryWidget.onMore();

		// add the 2 remaining results
		currentVersion = null; // current version is not on the current page
		verify(mockView, never()).clearVersions();
		verify(mockJsClient).getEntityVersions(eq(entityId), eq(VersionHistoryWidget.VERSION_LIMIT), eq(VersionHistoryWidget.VERSION_LIMIT), any(AsyncCallback.class));
		isVersionSelected = false;
		verify(mockView, times(2)).addVersion(eq(entityId), any(VersionInfo.class), eq(canEdit), eq(isVersionSelected));
		verify(mockView).setMoreButtonVisible(false);
	}

	@Test
	public void testTableOnMore() throws Exception {
		vb = new TableEntity();
		vb.setId(entityId);
		vb.setVersionNumber(CURRENT_FILE_VERSION);
		vb.setVersionLabel("");
		vb.setVersionComment("");
		when(bundle.getEntity()).thenReturn(vb);

		// with null version
		versionHistoryWidget.setEntityBundle(bundle, null);

		verify(mockView).clearVersions();

		// verify current version is not set when offset is 0, because (unlike Files) the first row of the
		// version history does not represent the latest version for Tables
		assertNull(versionHistoryWidget.getVersionNumber());
		verify(mockJsClient).getEntityVersions(anyString(), eq(WebConstants.ZERO_OFFSET.intValue()), anyInt(), any(AsyncCallback.class));
	}
}
