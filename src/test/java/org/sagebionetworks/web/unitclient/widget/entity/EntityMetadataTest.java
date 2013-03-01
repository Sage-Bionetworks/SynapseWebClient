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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.Data;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.TermsOfUseAccessRequirement;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.repo.model.VersionInfo;
import org.sagebionetworks.repo.model.Versionable;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.EntitySchemaCache;
import org.sagebionetworks.web.client.EntityTypeProvider;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.utils.APPROVAL_TYPE;
import org.sagebionetworks.web.client.utils.RESTRICTION_LEVEL;
import org.sagebionetworks.web.client.widget.entity.EntityMetadata;
import org.sagebionetworks.web.client.widget.entity.EntityMetadataView;
import org.sagebionetworks.web.client.widget.entity.JiraURLHelper;
import org.sagebionetworks.web.shared.EntityWrapper;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gdata.data.Kind.Adaptable;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sun.grizzly.tcp.Adapter;

public class EntityMetadataTest {

	SynapseClientAsync mockSynapseClient;
	AuthenticationController mockAuthenticationController;
	NodeModelCreator mockNodeModelCreator;
	GlobalApplicationState mockGlobalApplicationState;
	EntityMetadataView mockView;
	EntitySchemaCache mockSchemaCache;
	JSONObjectAdapter jsonObjectAdapter;
	EntityTypeProvider mockEntityTypeProvider;
	IconsImageBundle mockIconsImageBundle;
	EventBus mockEventBus;
	JiraURLHelper mockJiraURLHelper;
	EntityMetadata entityMetadata;
	Versionable vb;
	String entityId = "syn123";
	EntityBundle bundle;

	@Before
	public void before() throws JSONObjectAdapterException {
		mockAuthenticationController = mock(AuthenticationController.class);
		UserSessionData usd = new UserSessionData();
		UserProfile up = new UserProfile();
		up.setOwnerId("101");
		usd.setProfile(up);
		when(mockAuthenticationController.getLoggedInUser()).thenReturn(usd);

		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockNodeModelCreator = mock(NodeModelCreator.class);
		
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockView = mock(EntityMetadataView.class);
		mockSchemaCache = mock(EntitySchemaCache.class);
		jsonObjectAdapter = new JSONObjectAdapterImpl();
		mockEntityTypeProvider = mock(EntityTypeProvider.class);
		mockIconsImageBundle = mock(IconsImageBundle.class);
		mockEventBus = mock(EventBus.class);
		mockJiraURLHelper = mock(JiraURLHelper.class);

		entityMetadata = new EntityMetadata(mockView, mockSynapseClient, mockNodeModelCreator, mockAuthenticationController, jsonObjectAdapter, mockGlobalApplicationState, mockEntityTypeProvider, mockJiraURLHelper, mockEventBus);

		vb = new Data();
		vb.setId(entityId);
		vb.setVersionNumber(new Long(1));
		vb.setVersionLabel("");
		vb.setVersionComment("");
		bundle = mock(EntityBundle.class, RETURNS_DEEP_STUBS);
		when(bundle.getPermissions().getCanEdit()).thenReturn(true);
		when(bundle.getEntity()).thenReturn(vb);

		List<AccessRequirement> accessRequirements = new ArrayList<AccessRequirement>();
		TermsOfUseAccessRequirement accessRequirement = new TermsOfUseAccessRequirement();
		accessRequirement.setId(101L);
		accessRequirement.setTermsOfUse("terms of use");
		accessRequirements.add(accessRequirement);
		when(bundle.getAccessRequirements()).thenReturn(accessRequirements);
		when(bundle.getUnmetAccessRequirements()).thenReturn(accessRequirements);
				
		entityMetadata.setEntityBundle(bundle, false);

	}

	@Test
	public void testLoadVersions() throws Exception {
		AsyncMockStubber
				.callSuccessWith("")
				.when(mockSynapseClient)
				.getEntityVersions(anyString(), anyInt(), anyInt(),
						any(AsyncCallback.class));
		AsyncCallback<PaginatedResults<VersionInfo>> callback = new AsyncCallback<PaginatedResults<VersionInfo>>() {
			@Override
			public void onFailure(Throwable caught) {
				fail("unexpected failure in test: " + caught.getMessage());
			}

			@Override
			public void onSuccess(PaginatedResults<VersionInfo> result) {
				assertEquals(null, result);
			}
		};

		entityMetadata.loadVersions("synEMPTY", 0, 1, callback);
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

		entityMetadata.loadVersions("synEMPTY", 0, 1, callback);
	}

	@Test
	public void testUpdateVersionInfo() throws Exception {

		String testComment = "testComment";
		String testLabel = "testLabel";

		entityMetadata.editCurrentVersionInfo(vb.getId(), testLabel, testComment);
		ArgumentCaptor<String> json = ArgumentCaptor.forClass(String.class);
		verify(mockSynapseClient).updateEntity(json.capture(), (AsyncCallback<EntityWrapper>) any());
		JSONObjectAdapter joa = new JSONObjectAdapterImpl();
		joa = joa.createNew(json.getValue());
		Versionable out = new Data();
		out.initializeFromJSONObject(joa);
		assertEquals(new Long(1), out.getVersionNumber());
		assertEquals(testLabel, out.getVersionLabel());
		assertEquals(testComment, out.getVersionComment());
	}

	@Test
	public void testPromoteVersion() throws Exception {
		entityMetadata.promoteVersion(vb.getId(), vb.getVersionNumber());
		verify(mockSynapseClient).promoteEntityVersion(matches(vb.getId()), eq(vb.getVersionNumber()), (AsyncCallback<String>) any());
	}

	@Test
	public void testDeleteVersion() throws Exception {
		entityMetadata.deleteVersion(vb.getId(), vb.getVersionNumber());
		verify(mockSynapseClient).deleteEntityVersionById(matches(vb.getId()), eq(vb.getVersionNumber()), (AsyncCallback<Void>) any());
	}
	
	@Test
	public void testGetJiraFlagUrl() {
		String flagURLString = "flagURLString";
		when(mockJiraURLHelper.createFlagIssue(any(String.class),any(String.class),any(String.class))).thenReturn(flagURLString);
		assertEquals(flagURLString, entityMetadata.getJiraFlagUrl());
	}
	
	@Test
	public void testGetJiraRestrictionUrl() {
		String restrictionURLString = "restrictionURLString";
		when(mockJiraURLHelper.createAccessRestrictionIssue(any(String.class),any(String.class),any(String.class))).thenReturn(restrictionURLString);
		assertEquals(restrictionURLString, entityMetadata.getJiraRestrictionUrl());
	}
	
	@Test
	public void testGetJiraRequestAccessUrl() {
		String requestAccessURLString = "requestAccessURLString";
		when(mockJiraURLHelper.createRequestAccessIssue(any(String.class),any(String.class),any(String.class),any(String.class),any(String.class))).thenReturn(requestAccessURLString);
		assertEquals(requestAccessURLString, entityMetadata.getJiraRequestAccessUrl());
	}
	
	@Test
	public void testGetRestrictionLevel() {
		assertEquals(RESTRICTION_LEVEL.RESTRICTED, entityMetadata.getRestrictionLevel());
	}
	
	@Test
	public void testGetApprovalType() {
		assertEquals(APPROVAL_TYPE.USER_AGREEMENT, entityMetadata.getApprovalType());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testSetIsFavorite() throws Exception {
		PaginatedResults<EntityHeader> favorites = new PaginatedResults<EntityHeader>();
		List<EntityHeader> results = new ArrayList<EntityHeader>();
		favorites.setResults(results);
		EntityHeader added = new EntityHeader();
		String getFavoritesJson = favorites.writeToJSONObject(jsonObjectAdapter.createNew()).toJSONString();
		String addedJson = added.writeToJSONObject(jsonObjectAdapter.createNew()).toJSONString();
		AsyncMockStubber.callSuccessWith(getFavoritesJson).when(mockSynapseClient).getFavorites(anyInt(), anyInt(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(addedJson).when(mockSynapseClient).addFavorite(anyString(), any(AsyncCallback.class));
		Mockito.<PaginatedResults<?>>when(mockNodeModelCreator.createPaginatedResults(anyString(), eq(EntityHeader.class))).thenReturn(favorites);
				
		entityMetadata.setIsFavorite(true);
				
		verify(mockSynapseClient).addFavorite(eq(entityId), any(AsyncCallback.class));
		verify(mockSynapseClient).getFavorites(anyInt(), anyInt(), any(AsyncCallback.class));
		verify(mockGlobalApplicationState).setFavorites(results);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testSetIsFavoriteUnset() throws Exception {
		PaginatedResults<EntityHeader> favorites = new PaginatedResults<EntityHeader>();
		List<EntityHeader> results = new ArrayList<EntityHeader>();
		favorites.setResults(results);
		EntityHeader added = new EntityHeader();
		String getFavoritesJson = favorites.writeToJSONObject(jsonObjectAdapter.createNew()).toJSONString();
		String addedJson = added.writeToJSONObject(jsonObjectAdapter.createNew()).toJSONString();
		AsyncMockStubber.callSuccessWith(getFavoritesJson).when(mockSynapseClient).getFavorites(anyInt(), anyInt(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseClient).removeFavorite(anyString(), any(AsyncCallback.class));
		Mockito.<PaginatedResults<?>>when(mockNodeModelCreator.createPaginatedResults(anyString(), eq(EntityHeader.class))).thenReturn(favorites);
				
		entityMetadata.setIsFavorite(false);
				
		verify(mockSynapseClient).removeFavorite(eq(entityId), any(AsyncCallback.class));
		verify(mockSynapseClient).getFavorites(anyInt(), anyInt(), any(AsyncCallback.class));
		verify(mockGlobalApplicationState).setFavorites(results);
	}
	
}
