package org.sagebionetworks.web.unitclient.presenter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.Search;
import org.sagebionetworks.web.client.presenter.SearchPresenter;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.view.SearchView;

public class SearchPresenterTest {

	SearchPresenter searchPresenter;
	SearchView mockView;
	GlobalApplicationState mockGlobalApplicationState;
	AuthenticationController mockAuthenticationController;
	SynapseClientAsync mockSynapseClient;
	NodeModelCreator mockNodeModelCreator;

	
	@Before
	public void setup(){
		mockView = mock(SearchView.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockNodeModelCreator = mock(NodeModelCreator.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockSynapseClient = mock(SynapseClientAsync.class);

		searchPresenter = new SearchPresenter(mockView, mockGlobalApplicationState, mockAuthenticationController, mockSynapseClient, mockNodeModelCreator, new JSONObjectAdapterImpl());
		
		verify(mockView).setPresenter(searchPresenter);
	}	
	
	@Test
	public void testSetPlace() {
		Search place = Mockito.mock(Search.class);
		searchPresenter.setPlace(place);
	}	
}

