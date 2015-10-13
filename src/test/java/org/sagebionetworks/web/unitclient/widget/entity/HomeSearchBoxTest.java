package org.sagebionetworks.web.unitclient.widget.entity;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.schema.adapter.JSONArrayAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.PeopleSearch;
import org.sagebionetworks.web.client.place.Search;
import org.sagebionetworks.web.client.widget.search.HomeSearchBox;
import org.sagebionetworks.web.client.widget.search.HomeSearchBoxView;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class HomeSearchBoxTest {

	@Mock
	HomeSearchBoxView mockView;
	
	@Mock
	GlobalApplicationState mockGlobalApplicationState;
	
	@Mock
	JSONObjectAdapter mockJSONObjectAdapter;
	
	@Mock
	JSONArrayAdapter mockJSONArrayAdapter;
	
	@Mock
	SynapseClientAsync mockSynapseClient;
	
	@Mock
	PlaceChanger mockPlaceChanger;
	
	HomeSearchBox presenter;
	
	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		presenter = new HomeSearchBox(mockView, mockGlobalApplicationState, mockJSONObjectAdapter, mockSynapseClient);
		Mockito.when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		Mockito.when(mockJSONObjectAdapter.createNew()).thenReturn(mockJSONObjectAdapter);
		Mockito.when(mockJSONObjectAdapter.createNewArray()).thenReturn(mockJSONArrayAdapter);
	}
	
	@Test
	public void testPeopleSearch() {
		presenter.search("@Tester");
		Mockito.verify(mockGlobalApplicationState).getPlaceChanger();
		Mockito.verify(mockPlaceChanger).goTo(Mockito.any(PeopleSearch.class));
	}
	
	@Test
	public void testSearch() {
		presenter.setSearchAll(false);
		presenter.search("Test");
		Mockito.verify(mockJSONObjectAdapter, Mockito.never()).createNew();
		Mockito.verify(mockGlobalApplicationState).getPlaceChanger();
		Mockito.verify(mockPlaceChanger).goTo(Mockito.any(Search.class));
	}
	
	@Test
	public void testSearchRegular() {
		presenter.setSearchAll(false);
		presenter.search("syn123");
		Mockito.verify(mockSynapseClient).getEntity(Mockito.eq("syn123"), Mockito.any(AsyncCallback.class));
	}
	
	@Test
	public void testSearchEnhanced() {
		Mockito.when(mockJSONObjectAdapter.toJSONString()).thenReturn("syn456");
		presenter.setSearchAll(true);
		presenter.search("syn123");
		Mockito.verify(mockJSONObjectAdapter, Mockito.atLeastOnce()).createNew();
		Mockito.verify(mockSynapseClient).getEntity(Mockito.eq("syn456"), Mockito.any(AsyncCallback.class));
	}
	
	@Test
	public void testSearchEmpty() {
		presenter.search("");
		Mockito.verify(mockJSONObjectAdapter, Mockito.never()).createNew();
		Mockito.verify(mockGlobalApplicationState, Mockito.never()).getPlaceChanger();
		Mockito.verify(mockSynapseClient, Mockito.never()).getEntity(Mockito.anyString(), Mockito.any(AsyncCallback.class));
	}
	
}
