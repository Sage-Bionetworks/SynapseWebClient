package org.sagebionetworks.web.unitclient.widget.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.sagebionetworks.repo.model.Data;
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
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.entity.EntityMetadata;
import org.sagebionetworks.web.client.widget.entity.EntityMetadataView;
import org.sagebionetworks.web.client.widget.entity.JiraURLHelper;
import org.sagebionetworks.web.shared.EntityWrapper;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class EntityMetadataTest {

	SynapseClientAsync mockSynapseClient;
	AuthenticationController mockAuthenticationController;
	NodeModelCreator mockNodeModelCreator;
	GlobalApplicationState mockGlobalApplicationState;
	EntityMetadataView mockView;
	EntitySchemaCache mockSchemaCache;
	JSONObjectAdapter mockJsonObjectAdapter;
	EntityTypeProvider mockEntityTypeProvider;
	IconsImageBundle mockIconsImageBundle;
	EventBus mockEventBus;
	JiraURLHelper mockJiraURLHelper;

	EntityMetadata entityMetadata;

	@Before
	public void before() throws JSONObjectAdapterException {
		mockAuthenticationController = mock(AuthenticationController.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockNodeModelCreator = mock(NodeModelCreator.class);
		
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockView = mock(EntityMetadataView.class);
		mockSchemaCache = mock(EntitySchemaCache.class);
		mockJsonObjectAdapter = mock(JSONObjectAdapter.class);
		mockEntityTypeProvider = mock(EntityTypeProvider.class);
		mockIconsImageBundle = mock(IconsImageBundle.class);
		mockEventBus = mock(EventBus.class);
		mockJiraURLHelper = mock(JiraURLHelper.class);

		entityMetadata = new EntityMetadata(mockView, mockSynapseClient, mockNodeModelCreator, mockAuthenticationController, mockJsonObjectAdapter, mockGlobalApplicationState, mockEntityTypeProvider, mockJiraURLHelper, mockEventBus);
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
		Versionable vb = new Data();
		vb.setId("syn123");
		vb.setVersionNumber(new Long(1));
		vb.setVersionLabel("");
		vb.setVersionComment("");
		EntityBundle mockBundle = mock(EntityBundle.class, RETURNS_DEEP_STUBS);
		when(mockBundle.getPermissions().getCanEdit()).thenReturn(true);
		when(mockBundle.getEntity()).thenReturn(vb);
		when(mockJsonObjectAdapter.createNew()).thenReturn(new JSONObjectAdapterImpl());
		entityMetadata.setEntityBundle(mockBundle, false);

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
}
