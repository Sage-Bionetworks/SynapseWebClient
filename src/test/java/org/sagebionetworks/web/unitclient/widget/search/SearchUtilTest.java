package org.sagebionetworks.web.unitclient.widget.search;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.Search;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.presenter.SearchUtil;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class SearchUtilTest {

	private GlobalApplicationState mockGlobalAppState;
	private PlaceChanger mockPlaceChanger;
	private SynapseClientAsync mockSynapseClient;
	
	@Before
	public void before() {
		mockGlobalAppState = mock(GlobalApplicationState.class);
		mockPlaceChanger = mock(PlaceChanger.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		when(mockGlobalAppState.getPlaceChanger()).thenReturn(mockPlaceChanger);
	}
	
	@Test
	public void testSearchForTerm() {
		//search for something that does not look like a Synapse ID
		SearchUtil.searchForTerm("not_an_id", mockGlobalAppState, mockSynapseClient);
		verify(mockPlaceChanger).goTo(any(Search.class));
	}
	
	@Test
	public void testSearchForSynId() {
		//mocking successful verification of the syn id, should change to Synapse entity place
		SearchUtil.searchForTerm("syn123", mockGlobalAppState, mockSynapseClient);
		verify(mockPlaceChanger).goTo(any(Synapse.class));
	}
	
	@Test //fix
	public void testSearchForInvalidSynId() {
		//mocking failed verification of the syn id, should change to Search place
		//AsyncMockStubber.callFailureWith(new Exception()).when(mockSynapseClient).getEntity(anyString(), any(AsyncCallback.class));
		SearchUtil.searchForTerm("syn123", mockGlobalAppState, mockSynapseClient);
		verify(mockPlaceChanger).goTo(any(Search.class));
	}
}





