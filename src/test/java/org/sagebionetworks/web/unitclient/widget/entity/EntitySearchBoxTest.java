package org.sagebionetworks.web.unitclient.widget.entity;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.search.Hit;
import org.sagebionetworks.repo.model.search.SearchResults;
import org.sagebionetworks.repo.model.search.query.SearchQuery;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.entity.EntitySearchBox;
import org.sagebionetworks.web.client.widget.entity.EntitySearchBoxOracle;
import org.sagebionetworks.web.client.widget.entity.EntitySearchBoxView;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.SuggestOracle;

public class EntitySearchBoxTest {

	EntitySearchBoxView mockView;
	SynapseClientAsync mockSynapseClient;
	SageImageBundle mockSageImageBundle;
	AuthenticationController mockAuthenticationController;
	GlobalApplicationState mockGlobalApplicationState;
	NodeModelCreator mockNodeModelCreator;
	EntitySearchBox suggestBox;
	EntitySearchBoxOracle mockOracle;
	
	@Before
	public void before() {
		mockView = mock(EntitySearchBoxView.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockSageImageBundle = mock(SageImageBundle.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockNodeModelCreator = mock(NodeModelCreator.class);
		mockOracle = mock(EntitySearchBoxOracle.class);
		suggestBox = new EntitySearchBox(mockView, mockSynapseClient, mockNodeModelCreator);
		suggestBox.setOracle(mockOracle);
	}
	
	@Test
	public void testGetSuggestions() throws RestServiceException {
		SearchResults testPage = getResponsePage();
		AsyncMockStubber.callSuccessWith(testPage).when(mockSynapseClient).search(any(SearchQuery.class), any(AsyncCallback.class));
		when(mockOracle.makeEntitySuggestion(any(Hit.class), anyString())).thenReturn(null);
		
		SuggestOracle.Request mockRequest = mock(SuggestOracle.Request.class);
		when(mockRequest.getQuery()).thenReturn("test");
		SuggestOracle.Callback mockCallback = mock(SuggestOracle.Callback.class);
		
		suggestBox.getSuggestions(mockRequest, mockCallback);
		
		verify(mockRequest).getQuery();
		verify(mockSynapseClient).search(any(SearchQuery.class), any(AsyncCallback.class));
		verify(mockView).updateFieldStateForSuggestions(any(SearchResults.class), anyLong());
		verify(mockCallback).onSuggestionsReady(any(SuggestOracle.Request.class), any(SuggestOracle.Response.class));
	}
	
	private SearchResults getResponsePage() {
		SearchResults results = new SearchResults();
		results.setFound(6L);
		results.setMatchExpression("test");
		
		Hit hit1 = new Hit();
		hit1.setId("syn1");
		hit1.setName("Entity 1");
		
		Hit hit2 = new Hit();
		hit2.setId("syn2");
		hit2.setName("Entity 2");
		
		List<Hit> children = new ArrayList<Hit>();
		children.add(hit1);
		children.add(hit2);
		
		results.setHits(children);
		return results;
	}
}
