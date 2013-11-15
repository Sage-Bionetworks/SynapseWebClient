package org.sagebionetworks.web.unitclient.widget.entity.renderer;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import org.sagebionetworks.repo.model.wiki.WikiHeader;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONEntity;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpagesView;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpagesWidget;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

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
	WikiHeader testRootHeader;
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
		testRootHeader = new WikiHeader();
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
		widget.configure(new WikiPageKey(entityId, ObjectType.ENTITY.toString(), null), descriptor, null);
		verify(mockSynapseClient).getEntityHeaderBatch(anyString(), any(AsyncCallback.class));
		verify(mockView).showErrorMessage(anyString());
	}
	
	
	@Test
	public void testConfigureFailure() throws Exception {
		AsyncMockStubber.callFailureWith(new IllegalArgumentException()).when(mockSynapseClient).getWikiHeaderTree(anyString(), anyString(), any(AsyncCallback.class));
		widget.configure(new WikiPageKey(entityId, ObjectType.ENTITY.toString(), null), descriptor, null);
		verify(mockSynapseClient).getWikiHeaderTree(anyString(), anyString(), any(AsyncCallback.class));
		verify(mockView).showErrorMessage(anyString());
	}
	
	@Test
	public void testConfigureProjectRootNotFound() throws Exception {
		AsyncMockStubber.callFailureWith(new NotFoundException()).when(mockSynapseClient).getWikiHeaderTree(anyString(), anyString(), any(AsyncCallback.class));
		widget.configure(new WikiPageKey(entityId, ObjectType.ENTITY.toString(), null), descriptor, null);
		verify(mockSynapseClient).getWikiHeaderTree(anyString(), anyString(), any(AsyncCallback.class));
		verify(mockView, times(2)).clear();
	}

	@Test
	public void testAsWidget() {
		widget.asWidget();
		verify(mockView).asWidget();
	}
}











