package org.sagebionetworks.web.unitclient.widget.entity.renderer;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import org.sagebionetworks.repo.model.AutoGenFactory;
import org.sagebionetworks.repo.model.BatchResults;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiHeader;
import org.sagebionetworks.repo.model.wiki.WikiHeader;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONEntity;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Wiki;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpagesView;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpagesWidget;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class WikiSubpagesTest {

	WikiSubpagesView mockView;
	SynapseClientAsync mockSynapseClient;
	NodeModelCreator mockNodeModelCreator;
	AdapterFactory adapterFactory;
	AutoGenFactory autoGenFactory;
	GlobalApplicationState mockGlobalApplicationState;
	AuthenticationController mockAuthenticationController;
	
	WikiSubpagesWidget widget;
	List<JSONEntity> wikiHeadersList;
	V2WikiHeader testRootHeader;
	String entityId = "syn123";
	Map<String, String> descriptor = new HashMap<String, String>();
	
	@Before
	public void before() throws JSONObjectAdapterException {
		mockView = mock(WikiSubpagesView.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockNodeModelCreator = mock(NodeModelCreator.class);
		adapterFactory = new AdapterFactoryImpl();
		autoGenFactory = new AutoGenFactory();		
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		widget = new WikiSubpagesWidget(mockView, mockSynapseClient, mockNodeModelCreator, adapterFactory);
		verify(mockView).setPresenter(widget);
		ArrayList<JSONEntity> results = new ArrayList<JSONEntity>();
		results.add(new EntityHeader());
		
		AsyncMockStubber.callSuccessWith("entity id 1").when(mockSynapseClient).createOrUpdateEntity(anyString(), anyString(), anyBoolean(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith("entity id 1").when(mockSynapseClient).getEntityHeaderBatch(anyString(), any(AsyncCallback.class));
		BatchResults<JSONEntity> batchResults = new BatchResults<JSONEntity>();
		batchResults.setTotalNumberOfResults(1);
		batchResults.setResults(results);
		when(mockNodeModelCreator.createBatchResults(anyString(), eq(EntityHeader.class))).thenReturn(batchResults);
		
		AsyncMockStubber.callSuccessWith("").when(mockSynapseClient).getWikiHeaderTree(anyString(), anyString(), any(AsyncCallback.class));
		PaginatedResults<JSONEntity> wikiHeaders = new PaginatedResults<JSONEntity>();
		wikiHeadersList = new ArrayList<JSONEntity>();
		testRootHeader = new V2WikiHeader();
		testRootHeader.setId("123");
		testRootHeader.setParentId(null);
		testRootHeader.setTitle("my test root wiki header (page)");
		wikiHeadersList.add(testRootHeader);
		
		wikiHeaders.setResults(wikiHeadersList);
		when(mockNodeModelCreator.createPaginatedResults(anyString(), eq(WikiHeader.class))).thenReturn(wikiHeaders);
		reset(mockView);
	}

	@Test
	public void testConfigureEntityHeaderBatchFailure() throws Exception {
		AsyncMockStubber.callFailureWith(new IllegalArgumentException()).when(mockSynapseClient).getEntityHeaderBatch(anyString(), any(AsyncCallback.class));
		widget.configure(new WikiPageKey(entityId, ObjectType.ENTITY.toString(), null), descriptor, null, null);
		verify(mockSynapseClient).getEntityHeaderBatch(anyString(), any(AsyncCallback.class));
		verify(mockView).showErrorMessage(anyString());
	}
	
	
	@Test
	public void testConfigureFailure() throws Exception {
		AsyncMockStubber.callFailureWith(new IllegalArgumentException()).when(mockSynapseClient).getV2WikiHeaderTree(anyString(), anyString(), any(AsyncCallback.class));
		widget.configure(new WikiPageKey(entityId, ObjectType.ENTITY.toString(), null), descriptor, null, null);
		verify(mockSynapseClient).getV2WikiHeaderTree(anyString(), anyString(), any(AsyncCallback.class));
		verify(mockView).showErrorMessage(anyString());
	}
	
	@Test
	public void testConfigureProjectRootNotFound() throws Exception {
		AsyncMockStubber.callFailureWith(new NotFoundException()).when(mockSynapseClient).getV2WikiHeaderTree(anyString(), anyString(), any(AsyncCallback.class));
		widget.configure(new WikiPageKey(entityId, ObjectType.ENTITY.toString(), null), descriptor, null, null);
		verify(mockSynapseClient).getV2WikiHeaderTree(anyString(), anyString(), any(AsyncCallback.class));
		verify(mockView, times(3)).clear();
	}
	
	@Test
	public void testGetLinkPlaceSynapse() throws Exception {
		boolean embeddedInOwnerPage = true;
		widget.configure(new WikiPageKey(entityId, ObjectType.ENTITY.toString(), null), descriptor, null, null, null, embeddedInOwnerPage);
		String targetEntityId = "syn938";
		Long targetEntityVersion = 4L;
		String targetWikiId = "888";
		Place targetPlace = WikiSubpagesWidget.getLinkPlace(targetEntityId, targetEntityVersion, targetWikiId, embeddedInOwnerPage);
		assertTrue(targetPlace instanceof Synapse);
		Synapse targetSynapsePlace = (Synapse)targetPlace;
		assertEquals(targetEntityId, targetSynapsePlace.getEntityId());
		assertEquals(targetEntityVersion, targetSynapsePlace.getVersionNumber());
		assertEquals(Synapse.EntityArea.WIKI, targetSynapsePlace.getArea());
		assertEquals(targetWikiId, targetSynapsePlace.getAreaToken());
	}
	
	@Test
	public void testGetLinkPlaceWiki() throws Exception {
		boolean embeddedInOwnerPage = false;
		widget.configure(new WikiPageKey(entityId, ObjectType.ENTITY.toString(), null), descriptor, null, null, null, embeddedInOwnerPage);
		String targetEntityId = "syn938";
		Long targetEntityVersion = 4L;
		String targetWikiId = "888";
		Place targetPlace = WikiSubpagesWidget.getLinkPlace(targetEntityId, targetEntityVersion, targetWikiId, embeddedInOwnerPage);
		assertTrue(targetPlace instanceof Wiki);
		Wiki targetWikiPlace = (Wiki)targetPlace;
		assertEquals(targetEntityId, targetWikiPlace.getOwnerId());
		assertEquals(ObjectType.ENTITY.toString(), targetWikiPlace.getOwnerType());
		assertEquals(targetWikiId, targetWikiPlace.getWikiId());
	}

	@Test
	public void testAsWidget() {
		widget.asWidget();
		verify(mockView).asWidget();
	}
}











