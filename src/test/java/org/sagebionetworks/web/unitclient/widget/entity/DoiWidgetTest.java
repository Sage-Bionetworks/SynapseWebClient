package org.sagebionetworks.web.unitclient.widget.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyLong;
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
import org.sagebionetworks.repo.model.doi.Doi;
import org.sagebionetworks.repo.model.doi.DoiStatus;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.EntitySchemaCache;
import org.sagebionetworks.web.client.EntityTypeProvider;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.StackConfigServiceAsync;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.utils.APPROVAL_TYPE;
import org.sagebionetworks.web.client.utils.RESTRICTION_LEVEL;
import org.sagebionetworks.web.client.widget.entity.DoiWidget;
import org.sagebionetworks.web.client.widget.entity.DoiWidgetView;
import org.sagebionetworks.web.client.widget.entity.EntityMetadata;
import org.sagebionetworks.web.client.widget.entity.EntityMetadataView;
import org.sagebionetworks.web.client.widget.entity.FavoriteWidget;
import org.sagebionetworks.web.client.widget.entity.FavoriteWidgetView;
import org.sagebionetworks.web.client.widget.entity.JiraURLHelper;
import org.sagebionetworks.web.shared.EntityWrapper;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gdata.data.Kind.Adaptable;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sun.grizzly.tcp.Adapter;

public class DoiWidgetTest {

	SynapseClientAsync mockSynapseClient;
	NodeModelCreator mockNodeModelCreator;
	GlobalApplicationState mockGlobalApplicationState;
	DoiWidgetView mockView;
	JSONObjectAdapter jsonObjectAdapter;
	String entityId = "syn123";
	DoiWidget doiWidget;
	Doi testDoi;
	StackConfigServiceAsync mockStackConfigService;
	
	@Before
	public void before() throws JSONObjectAdapterException {
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockNodeModelCreator = mock(NodeModelCreator.class);		
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockView = mock(DoiWidgetView.class);
		AsyncMockStubber.callSuccessWith("fake doi json").when(mockSynapseClient).getEntityDoi(anyString(), anyLong(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseClient).createDoi(anyString(), anyLong(), any(AsyncCallback.class));
		testDoi = new Doi();
		testDoi.setDoiStatus(DoiStatus.READY);
		when(mockNodeModelCreator.createJSONEntity(anyString(), any(Class.class))).thenReturn(testDoi);
		jsonObjectAdapter = new JSONObjectAdapterImpl();
		mockStackConfigService = mock(StackConfigServiceAsync.class);
		doiWidget = new DoiWidget(mockView, mockSynapseClient, mockNodeModelCreator, jsonObjectAdapter, mockGlobalApplicationState, mockStackConfigService);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testConfigureReadyStatus() throws Exception {
		doiWidget.configure(entityId, null);
		verify(mockSynapseClient).getEntityDoi(anyString(), anyLong(), any(AsyncCallback.class));
		verify(mockView).showDoi(DoiStatus.READY);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testConfigureErrorStatus() throws Exception {
		testDoi.setDoiStatus(DoiStatus.ERROR);
		doiWidget.configure(entityId, null);
		verify(mockSynapseClient).getEntityDoi(anyString(), anyLong(), any(AsyncCallback.class));
		verify(mockView).showDoi(DoiStatus.ERROR);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testConfigureNotFound() throws Exception {
		AsyncMockStubber.callFailureWith(new NotFoundException()).when(mockSynapseClient).getEntityDoi(anyString(), anyLong(), any(AsyncCallback.class));
		doiWidget.configure(entityId, null);
		verify(mockView).showCreateDoi();
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testConfigureOtherError() throws Exception {
		AsyncMockStubber.callFailureWith(new IllegalArgumentException()).when(mockSynapseClient).getEntityDoi(anyString(), anyLong(), any(AsyncCallback.class));
		doiWidget.configure(entityId, null);
		verify(mockView).showErrorMessage(anyString());
	}
	

	@SuppressWarnings("unchecked")
	@Test
	public void testCreateDoi() throws Exception {
		doiWidget.createDoi();
		verify(mockSynapseClient).createDoi(anyString(), anyLong(), any(AsyncCallback.class));
		verify(mockView).showInfo(anyString(), anyString());
		verify(mockSynapseClient).getEntityDoi(anyString(), anyLong(), any(AsyncCallback.class));
	}
	@SuppressWarnings("unchecked")
	@Test
	public void testCreateDoiFail() throws Exception {
		AsyncMockStubber.callFailureWith(new IllegalArgumentException()).when(mockSynapseClient).createDoi(anyString(), anyLong(), any(AsyncCallback.class));
		doiWidget.createDoi();
		verify(mockSynapseClient).createDoi(anyString(), anyLong(), any(AsyncCallback.class));
		verify(mockView).showErrorMessage(anyString());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testGetDoiPrefix() throws Exception {
		doiWidget.getDoiPrefix(mock(AsyncCallback.class));
		verify(mockStackConfigService).getDoiPrefix(any(AsyncCallback.class));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testGetDoiLink() throws Exception {
		String prefix = "10.5072/FK2.";
		Long version = 42l;
		doiWidget.configure(entityId, version);
		String link = doiWidget.getDoiLink(prefix);
		assertTrue(link.contains(entityId));
		assertTrue(link.contains(version.toString()));
		assertTrue(link.contains(prefix));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetDoiLinkNoPrefix() throws Exception {
		String prefix = "";
		Long version = 42l;
		doiWidget.configure(entityId, version);
		String link = doiWidget.getDoiLink(prefix);
		assertTrue(link.length() == 0);
		link = doiWidget.getDoiLink(null);
		assertTrue(link.length() == 0);
	}
	
}
