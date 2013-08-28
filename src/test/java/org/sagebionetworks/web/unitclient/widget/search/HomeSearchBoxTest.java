package org.sagebionetworks.web.unitclient.widget.search;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.Search;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.widget.search.HomeSearchBox;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class HomeSearchBoxTest {

	private GlobalApplicationState mockGlobalAppState;
	private PlaceChanger mockPlaceChanger;
	private SynapseClientAsync mockSynapseClient;
	
	@Before
	public void before() {
		mockGlobalAppState = mock(GlobalApplicationState.class);
		mockPlaceChanger = mock(PlaceChanger.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		when(mockGlobalAppState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseClient).getEntity(anyString(), any(AsyncCallback.class));
	}
	
	@Test
	public void testSearchForTerm() {
		//search for something that does not look like a Synapse ID
		HomeSearchBox.searchForTerm("not_an_id", mockGlobalAppState, mockSynapseClient);
		verify(mockPlaceChanger).goTo(any(Search.class));
	}
	
	@Test
	public void testSearchForSynId() {
		//mocking successful verification of the syn id, should change to Synapse entity place
		HomeSearchBox.searchForTerm("syn123", mockGlobalAppState, mockSynapseClient);
		verify(mockPlaceChanger).goTo(any(Synapse.class));
	}
	
	@Test
	public void testSearchForInvalidSynId() {
		//mocking failed verification of the syn id, should change to Search place
		AsyncMockStubber.callFailureWith(new Exception()).when(mockSynapseClient).getEntity(anyString(), any(AsyncCallback.class));
		HomeSearchBox.searchForTerm("syn123", mockGlobalAppState, mockSynapseClient);
		verify(mockPlaceChanger).goTo(any(Search.class));
	}
}





