package org.sagebionetworks.web.unitclient.widget.entity.browse;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.AutoGenFactory;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.wiki.WikiHeader;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONEntity;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.entity.browse.PagesBrowser;
import org.sagebionetworks.web.client.widget.entity.browse.PagesBrowserView;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.TreeItem;

public class PagesBrowserTest {

	PagesBrowserView mockView;
	SynapseClientAsync mockSynapseClient;
	NodeModelCreator mockNodeModelCreator;
	AdapterFactory adapterFactory;
	AutoGenFactory autoGenFactory;
	GlobalApplicationState mockGlobalApplicationState;
	AuthenticationController mockAuthenticationController;
	PagesBrowser pagesBrowser;
	List<JSONEntity> wikiHeadersList;
	WikiHeader testRootHeader;
	String entityId = "syn123";
	
	@Before
	public void before() throws JSONObjectAdapterException {
		mockView = mock(PagesBrowserView.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockNodeModelCreator = mock(NodeModelCreator.class);
		adapterFactory = new AdapterFactoryImpl();
		autoGenFactory = new AutoGenFactory();		
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		pagesBrowser = new PagesBrowser(mockView, mockSynapseClient, mockNodeModelCreator);
		verify(mockView).setPresenter(pagesBrowser);
		ArrayList<String> results = new ArrayList<String>();
		results.add("A Test Entity Header");
		
		AsyncMockStubber.callSuccessWith("entity id 1").when(mockSynapseClient).createOrUpdateEntity(anyString(), anyString(), anyBoolean(), any(AsyncCallback.class));
		
		when(mockNodeModelCreator.createJSONEntity(anyString(), eq(EntityHeader.class))).thenReturn(new EntityHeader());
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
	public void testConfigureProject() throws Exception {
		AsyncMockStubber.callFailureWith(new IllegalArgumentException()).when(mockSynapseClient).getWikiHeaderTree(anyString(), anyString(), any(AsyncCallback.class));
		pagesBrowser.configure(new WikiPageKey(entityId, WidgetConstants.WIKI_OWNER_ID_ENTITY, null), "syn123", "#!Synapse:syn123", "Project A", true);
		verify(mockSynapseClient).getWikiHeaderTree(anyString(), anyString(), any(AsyncCallback.class));
		verify(mockView).showErrorMessage(anyString());
	}
	
	@Test
	public void testConfigureProjectRootNotFound() throws Exception {
		AsyncMockStubber.callFailureWith(new NotFoundException()).when(mockSynapseClient).getWikiHeaderTree(anyString(), anyString(), any(AsyncCallback.class));
		pagesBrowser.configure(new WikiPageKey(entityId, WidgetConstants.WIKI_OWNER_ID_ENTITY, null), "syn123", "#!Synapse:syn123", "Project A", true);
		verify(mockSynapseClient).getWikiHeaderTree(anyString(), anyString(), any(AsyncCallback.class));
		verify(mockView).configure(anyBoolean(), any(TreeItem.class));
	}

	@Test
	public void testAsWidget() {
		pagesBrowser.asWidget();
		verify(mockView).asWidget();
	}
}











