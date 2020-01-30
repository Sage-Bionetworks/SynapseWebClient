package org.sagebionetworks.web.unitclient.widget.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.place.PeopleSearch;
import org.sagebionetworks.web.client.place.Search;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.widget.search.SearchBox;
import org.sagebionetworks.web.client.widget.search.SearchBoxView;
import com.google.gwt.place.shared.Place;

public class SearchBoxTest {
	@Mock
	SearchBoxView mockView;
	@Mock
	GlobalApplicationState mockGlobalApplicationState;
	@Mock
	PlaceChanger mockPlaceChanger;
	@Captor
	ArgumentCaptor<Place> placeCaptor;
	SearchBox presenter;

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		presenter = new SearchBox(mockView, mockGlobalApplicationState);
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
	}

	@Test
	public void testPeopleSearch() {
		presenter.search("@Tester");
		verify(mockGlobalApplicationState).getPlaceChanger();
		verify(mockPlaceChanger).goTo(any(PeopleSearch.class));
	}

	@Test
	public void testSearch() {
		presenter.search("Test");
		verify(mockGlobalApplicationState).getPlaceChanger();
		verify(mockPlaceChanger).goTo(any(Search.class));
	}

	@Test
	public void testSearchDoi() {
		String synId = "syn123.4";
		presenter.search("10.7303/" + synId);
		verify(mockGlobalApplicationState).getPlaceChanger();
		verify(mockPlaceChanger).goTo(placeCaptor.capture());
		Place place = placeCaptor.getValue();
		assertTrue(place instanceof Synapse);
		assertEquals(synId, ((Synapse) place).toToken());
	}

	@Test
	public void testSearchEmpty() {
		presenter.search("");
		verify(mockGlobalApplicationState, Mockito.never()).getPlaceChanger();
	}

	@Test
	public void testTrim() {
		String synId = "syn111";

		presenter.search("  \t " + synId + " ");

		verify(mockPlaceChanger).goTo(placeCaptor.capture());
		Place place = placeCaptor.getValue();
		assertTrue(place instanceof Synapse);
		assertEquals(synId, ((Synapse) place).toToken());
	}
}
