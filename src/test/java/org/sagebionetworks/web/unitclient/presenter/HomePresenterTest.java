package org.sagebionetworks.web.unitclient.presenter;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.AccessControlList;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.RssServiceAsync;
import org.sagebionetworks.web.client.StackConfigServiceAsync;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.Home;
import org.sagebionetworks.web.client.presenter.HomePresenter;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.view.HomeView;
import org.sagebionetworks.web.shared.EntityWrapper;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class HomePresenterTest {

	HomePresenter homePresenter;
	CookieProvider cookieProvider;
	HomeView mockView;
	AuthenticationController mockAuthenticationController;
	GlobalApplicationState mockGlobalApplicationState;
	StackConfigServiceAsync mockStackConfigService;
	RssServiceAsync mockRssService;
	NodeModelCreator mockNodeModelCreator;
	
	@Before
	public void setup(){
		mockView = mock(HomeView.class);
		cookieProvider = mock(CookieProvider.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockRssService = mock(RssServiceAsync.class);
		mockNodeModelCreator = mock(NodeModelCreator.class);
		homePresenter = new HomePresenter(mockView, 
				cookieProvider, 
				mockAuthenticationController, 
				mockGlobalApplicationState,
				mockStackConfigService,
				mockRssService,
				mockNodeModelCreator);
		
		verify(mockView).setPresenter(homePresenter);
	}	
	
	@Test
	public void testSetPlace() {
		Home place = Mockito.mock(Home.class);
		homePresenter.setPlace(place);
	}
	
	@Test
	public void testBccLoad() {
		String result = "challenge description";
		AsyncMockStubber.callSuccessWith(result).when(mockRssService).getWikiPageContent(anyString(), any(AsyncCallback.class));		
		homePresenter.loadBccOverviewDescription();
		verify(mockView).showBccOverview(result);
	}	
}
