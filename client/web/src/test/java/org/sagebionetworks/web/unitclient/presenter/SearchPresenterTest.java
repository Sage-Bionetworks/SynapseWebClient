package org.sagebionetworks.web.unitclient.presenter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.*;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.search.query.SearchQuery;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.EntityTypeProvider;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.Search;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.presenter.SearchPresenter;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.view.SearchView;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class SearchPresenterTest {

	SearchPresenter searchPresenter;
	SearchView mockView;
	GlobalApplicationState mockGlobalApplicationState;
	AuthenticationController mockAuthenticationController;
	SynapseClientAsync mockSynapseClient;
	NodeModelCreator mockNodeModelCreator;
	JSONObjectAdapter jsonObjectAdapter;
	PlaceChanger mockPlaceChanger;
	EntityTypeProvider mockEntityTypeProvider;
	IconsImageBundle mockIconsImageBundle;

	String exampleTerm;
	String exampleTermSearchQueryJson;
	
	@Before
	public void setup() throws Exception{
		mockView = mock(SearchView.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockNodeModelCreator = mock(NodeModelCreator.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockPlaceChanger = mock(PlaceChanger.class);
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		EntityTypeProvider mockEntityTypeProvider = null; 
		IconsImageBundle mockIconsImageBundle = mock(IconsImageBundle.class);

		jsonObjectAdapter = new JSONObjectAdapterImpl();
		
		searchPresenter = new SearchPresenter(mockView,
				mockGlobalApplicationState, mockAuthenticationController,
				mockSynapseClient, mockNodeModelCreator,
				new JSONObjectAdapterImpl(), mockEntityTypeProvider,
				mockIconsImageBundle);
		
		exampleTerm = "searchQueryTerm";
		SearchQuery query = DisplayUtils.getDefaultSearchQuery();
		query.setQueryTerm(Arrays.asList(new String[] {exampleTerm}));
		exampleTermSearchQueryJson = query.writeToJSONObject(jsonObjectAdapter.createNew()).toJSONString();

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
		
		Search place = new Search(exampleTerm);
		searchPresenter.setPlace(place);
		verify(mockSynapseClient).search(eq(exampleTermSearchQueryJson), any(AsyncCallback.class));

	}

	@SuppressWarnings("unchecked")
	@Test
	public void testSearchQuery() throws Exception {		
		Search place = new Search(exampleTermSearchQueryJson);
		searchPresenter.setPlace(place);
		verify(mockSynapseClient).search(eq(exampleTermSearchQueryJson), any(AsyncCallback.class));

	}
	
	@SuppressWarnings("unchecked")
	@Test 
	public void testSetSearchTerm() throws Exception {
		searchPresenter.setSearchTerm(exampleTerm);
		verify(mockSynapseClient).search(eq(exampleTermSearchQueryJson), any(AsyncCallback.class));
	}

	@SuppressWarnings("unchecked")
	@Test 
	public void testSetSearchTermSynapseIdPrefixNotId() throws Exception {
		// test for a word with the prefix but not a synapse ID
		String term = DisplayUtils.SYNAPSE_ID_PREFIX + "apse"; // # 'synapse'
		SearchQuery query = DisplayUtils.getDefaultSearchQuery();
		query.setQueryTerm(Arrays.asList(new String[] {term}));
		String json = query.writeToJSONObject(jsonObjectAdapter.createNew()).toJSONString();

		searchPresenter.setSearchTerm(term);
		verify(mockSynapseClient).search(eq(json), any(AsyncCallback.class));
	}

	@SuppressWarnings("unchecked")
	@Test 
	public void testSetSearchTermSynapseIdPrefix() throws Exception {
		// test for a word with the prefix and is a synapse ID
		String term = DisplayUtils.SYNAPSE_ID_PREFIX + "1234567890"; // # 'syn1234567890'

		searchPresenter.setSearchTerm(term);		
		assertEquals(searchPresenter.getRedirect(), new Synapse(term)); 
	}

}

