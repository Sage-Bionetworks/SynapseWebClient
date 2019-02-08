package org.sagebionetworks.web.unitclient.widget.entity;

import static junit.framework.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.search.Hit;
import org.sagebionetworks.repo.model.search.SearchResults;
import org.sagebionetworks.repo.model.search.query.SearchQuery;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.entity.EntitySearchBox;
import org.sagebionetworks.web.client.widget.entity.EntitySearchBoxOracle;
import org.sagebionetworks.web.client.widget.entity.EntitySearchBoxView;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.SuggestOracle;

@RunWith(MockitoJUnitRunner.class)
public class EntitySearchBoxTest {

	@Mock
	EntitySearchBoxView mockView;
	@Mock
	SynapseClientAsync mockSynapseClient;
	@Mock
	SageImageBundle mockSageImageBundle;
	@Mock
	AuthenticationController mockAuthenticationController;
	@Mock
	GlobalApplicationState mockGlobalApplicationState;
	EntitySearchBox suggestBox;
	@Mock
	EntitySearchBoxOracle mockOracle;
	@Mock
	SynapseJavascriptClient mockJsClient;
	@Before
	public void before() {
		suggestBox = new EntitySearchBox(mockView, mockSynapseClient, mockJsClient);
		suggestBox.setOracle(mockOracle);
	}
	
	@Test
	public void testGetSuggestions() throws RestServiceException {
		SearchResults testPage = getResponsePage();
		AsyncMockStubber.callSuccessWith(testPage).when(mockJsClient).getSearchResults(any(SearchQuery.class), any(AsyncCallback.class));
		when(mockOracle.makeEntitySuggestion(any(Hit.class), anyString())).thenReturn(null);
		
		SuggestOracle.Request mockRequest = mock(SuggestOracle.Request.class);
		when(mockRequest.getQuery()).thenReturn("test");
		SuggestOracle.Callback mockCallback = mock(SuggestOracle.Callback.class);
		
		suggestBox.getSuggestions(mockRequest, mockCallback);
		
		verify(mockRequest).getQuery();
		verify(mockJsClient).getSearchResults(any(SearchQuery.class), any(AsyncCallback.class));
		verify(mockView).updateFieldStateForSuggestions(any(SearchResults.class), anyLong());
		verify(mockCallback).onSuggestionsReady(any(SuggestOracle.Request.class), any(SuggestOracle.Response.class));
	}
	
	@Test
	public void testSetNullSuggestion() throws RestServiceException {
		suggestBox.setSelectedSuggestion(null);
		assertNull(suggestBox.getSelectedSuggestion());
		verifyZeroInteractions(mockSynapseClient);
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
