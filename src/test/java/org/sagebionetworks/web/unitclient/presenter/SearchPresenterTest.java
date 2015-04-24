package org.sagebionetworks.web.unitclient.presenter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.search.query.KeyValue;
import org.sagebionetworks.repo.model.search.query.SearchQuery;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.Search;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.presenter.SearchPresenter;
import org.sagebionetworks.web.client.presenter.SearchUtil;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.view.SearchView;
import org.sagebionetworks.web.shared.SearchQueryUtils;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class SearchPresenterTest {

	SearchPresenter searchPresenter;
	SearchView mockView;
	GlobalApplicationState mockGlobalApplicationState;
	AuthenticationController mockAuthenticationController;
	SynapseClientAsync mockSynapseClient;
	JSONObjectAdapter jsonObjectAdapter;
	PlaceChanger mockPlaceChanger;
	IconsImageBundle mockIconsImageBundle;

	String exampleTerm;
	String exampleTermSearchQueryJson;
	SearchQuery exampleTermSearchQuery;
	@Before
	public void setup() throws Exception{
		mockView = mock(SearchView.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockPlaceChanger = mock(PlaceChanger.class);
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		IconsImageBundle mockIconsImageBundle = mock(IconsImageBundle.class);

		jsonObjectAdapter = new JSONObjectAdapterImpl();
		
		searchPresenter = new SearchPresenter(mockView,
				mockGlobalApplicationState, mockAuthenticationController,
				mockSynapseClient,
				new JSONObjectAdapterImpl(),
				mockIconsImageBundle);
		
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
		verify(mockSynapseClient).search(eq(exampleTermSearchQuery), any(AsyncCallback.class));

	}

	@SuppressWarnings("unchecked")
	@Test
	public void testSearchQuery() throws Exception {		
		Search place = new Search(exampleTermSearchQueryJson);
		searchPresenter.setPlace(place);
		verify(mockSynapseClient).search(eq(exampleTermSearchQuery), any(AsyncCallback.class));

	}

	@SuppressWarnings("unchecked")
	@Test 
	public void testSetPlaceSynapseIdPrefixNotId() throws Exception {
		// test for a word with the prefix but not a synapse ID
		String term = ClientProperties.SYNAPSE_ID_PREFIX + "apse"; // # 'synapse'

		SearchQuery query = SearchQueryUtils.getDefaultSearchQuery();
		query.setQueryTerm(Arrays.asList(new String[] {term}));
		
		searchPresenter.setPlace(new Search(term));
		verify(mockSynapseClient).search(eq(query), any(AsyncCallback.class));
	}

	@SuppressWarnings("unchecked")
	@Test 
	public void testSetPlaceSynapseIdPrefix() throws Exception {
		// test for a word with the prefix and is a synapse ID
		String term = ClientProperties.SYNAPSE_ID_PREFIX + "1234567890"; // # 'syn1234567890'
		assertEquals(new Synapse(term), SearchUtil.willRedirect(new Search(term))); 
	}

	@Test 
	public void testGetAppliedFacets() {
		List<KeyValue> facets = searchPresenter.getAppliedFacets();
		boolean found = false;
		for(KeyValue facet : facets) {
			if("project".equals(facet.getValue()) && "node_type".equals(facet.getKey()))
				found = true;

		}
		assertTrue(found);
	}
	
}

