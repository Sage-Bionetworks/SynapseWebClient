package org.sagebionetworks.web.unitclient.presenter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.search.query.KeyRange;
import org.sagebionetworks.repo.model.search.query.KeyValue;
import org.sagebionetworks.repo.model.search.query.SearchQuery;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.place.Search;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.presenter.SearchPresenter;
import org.sagebionetworks.web.client.presenter.SearchUtil;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.view.SearchView;
import org.sagebionetworks.web.client.widget.LoadMoreWidgetContainer;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.SearchQueryUtils;

import com.google.gwt.user.client.rpc.AsyncCallback;

@RunWith(MockitoJUnitRunner.class)
public class SearchPresenterTest {
	SearchPresenter searchPresenter;
	@Mock
	SearchView mockView;
	@Mock
	GlobalApplicationState mockGlobalApplicationState;
	@Mock
	AuthenticationController mockAuthenticationController;
	@Mock
	SynapseJavascriptClient mockJsClient;
	JSONObjectAdapter jsonObjectAdapter;
	@Mock
	PlaceChanger mockPlaceChanger;
	@Mock
	SynapseAlert mockSynAlert;
	@Mock
	LoadMoreWidgetContainer mockLoadMoreWidgetContainer;
	
	String exampleTerm;
	String exampleTermSearchQueryJson;
	SearchQuery exampleTermSearchQuery;
	@Before
	public void setup() throws Exception{
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		jsonObjectAdapter = new JSONObjectAdapterImpl();
		
		searchPresenter = new SearchPresenter(mockView,
				mockGlobalApplicationState,
				mockJsClient,
				new JSONObjectAdapterImpl(),
				mockSynAlert,
				mockLoadMoreWidgetContainer);
		
		exampleTerm = "searchQueryTerm";
		exampleTermSearchQuery = SearchQueryUtils.getDefaultSearchQuery();
		exampleTermSearchQuery.setQueryTerm(Arrays.asList(new String[] {exampleTerm}));
		exampleTermSearchQueryJson = exampleTermSearchQuery.writeToJSONObject(jsonObjectAdapter.createNew()).toJSONString();
	}	

	@Test 
	public void constructor() {
		verify(mockView).setPresenter(searchPresenter);		
	}
	
	@Test
	public void testSetPlace() {
		reset(mockView);
		// default, set presenter, null query
		Search place = Mockito.mock(Search.class);
		searchPresenter.setPlace(place);
		verify(mockView).setPresenter(searchPresenter);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testQueryTerm() throws Exception {		
		
		searchPresenter.setPlace(new Search(exampleTerm));
		verify(mockJsClient).getSearchResults(eq(exampleTermSearchQuery), any(AsyncCallback.class));

	}

	@SuppressWarnings("unchecked")
	@Test
	public void testSearchQuery() throws Exception {		
		Search place = new Search(exampleTermSearchQueryJson);
		searchPresenter.setPlace(place);
		verify(mockJsClient).getSearchResults(eq(exampleTermSearchQuery), any(AsyncCallback.class));

	}

	@SuppressWarnings("unchecked")
	@Test 
	public void testSetPlaceSynapseIdPrefixNotId() throws Exception {
		// test for a word with the prefix but not a synapse ID
		String term = ClientProperties.SYNAPSE_ID_PREFIX + "apse"; // # 'synapse'

		SearchQuery query = SearchQueryUtils.getDefaultSearchQuery();
		query.setQueryTerm(Arrays.asList(new String[] {term}));
		
		searchPresenter.setPlace(new Search(term));
		verify(mockJsClient).getSearchResults(eq(query), any(AsyncCallback.class));
	}

	@SuppressWarnings("unchecked")
	@Test 
	public void testSetPlaceSynapseIdPrefix() throws Exception {
		// test for a word with the prefix and is a synapse ID
		String term = ClientProperties.SYNAPSE_ID_PREFIX + "1234567890"; // # 'syn1234567890'
		assertEquals(new Synapse(term), SearchUtil.willRedirect(new Search(term))); 
	}

	private List<KeyValue> getFacet(String facetName) {
		List<KeyValue> facets = searchPresenter.getAppliedFacets();
		List<KeyValue> foundFacets = new ArrayList<KeyValue>();
		for(KeyValue facet : facets) {
			if(facetName.equals(facet.getKey())) {
				foundFacets.add(facet);
			}
		}
		return foundFacets;
	}
	
	private List<KeyRange> getTimeFacet(String facetName) {
		List<KeyRange> facets = searchPresenter.getAppliedTimeFacets();
		List<KeyRange> foundFacets = new ArrayList<KeyRange>();
		for(KeyRange facet : facets) {
			if(facetName.equals(facet.getKey())) {
				foundFacets.add(facet);
			}
		}
		return foundFacets;
	}

	
	@Test 
	public void testTimeFacets() {
		String facetName = "createdOn";
		String facetValue = "1";
		assertTrue(getFacet(facetName).isEmpty());
		
		searchPresenter.addTimeFacet(facetName, facetValue, "Yesterday");

		//When a new facet is added, we should:
		// 1. Add the facet to the current search.
		// 2. Clear existing search results.
		// 3. Update the address bar to the new place. 
		// 4. Execute the current search (that now has the new facet).

		// 1
		List<KeyRange> facetValues = getTimeFacet(facetName);
		assertEquals(1, facetValues.size());
		assertEquals(facetValue, facetValues.get(0).getMin());
		verify(mockView).clear(); // 2
		verify(mockLoadMoreWidgetContainer).clear(); // 2
		verify(mockGlobalApplicationState).pushCurrentPlace(any(Search.class)); // 3
		verify(mockLoadMoreWidgetContainer).setIsProcessing(true); // 4
		verify(mockJsClient).getSearchResults(any(SearchQuery.class), any(AsyncCallback.class)); // 4
		
		//verify setting the time facet to another value clears the previous
		facetValue = "2";
		searchPresenter.addTimeFacet(facetName, facetValue, "Yesterday");
		verify(mockJsClient, times(2)).getSearchResults(any(SearchQuery.class), any(AsyncCallback.class)); // 4
		facetValues = getTimeFacet(facetName);
		assertEquals(1, facetValues.size());
		assertEquals(facetValue, facetValues.get(0).getMin());
		
		searchPresenter.removeTimeFacetAndRefresh(facetName);
		//optimization.  if search is empty (no search term and no facets) then a search is not performed (empty results are shown).
		verify(mockJsClient, times(2)).getSearchResults(any(SearchQuery.class), any(AsyncCallback.class)); // 4
		assertTrue(getTimeFacet(facetName).isEmpty());
	}
}

