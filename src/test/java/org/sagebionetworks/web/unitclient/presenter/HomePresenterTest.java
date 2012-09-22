package org.sagebionetworks.web.unitclient.presenter;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.AccessControlList;
import org.sagebionetworks.repo.model.RSSEntry;
import org.sagebionetworks.repo.model.RSSFeed;
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
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
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
	
	RSSFeed testFeed = null;
	
	@Before
	public void setup(){
		mockView = mock(HomeView.class);
		cookieProvider = mock(CookieProvider.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockRssService = mock(RssServiceAsync.class);
		mockNodeModelCreator = mock(NodeModelCreator.class);

		testFeed = new RSSFeed();
		RSSEntry entry = new RSSEntry();
		entry.setTitle("A Title");
		entry.setAuthor("An Author");
		entry.setLink("http://somewhere");
		List<RSSEntry> entries = new ArrayList<RSSEntry>();
		entries.add(entry);
		testFeed.setEntries(entries);
		
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
		AsyncMockStubber.callSuccessWith(result).when(mockRssService).getCachedContent(anyString(), any(AsyncCallback.class));		
		homePresenter.loadBccOverviewDescription();
		verify(mockView).showBccOverview(result);
	}	
	
	@Test
	public void testNewsFeed() throws RestServiceException {
		//when news is loaded, the view should be updated with the service result
		String exampleNewsFeedResult = "news feed";
		AsyncMockStubber.callSuccessWith(exampleNewsFeedResult).when(mockRssService).getCachedContent(anyString(), any(AsyncCallback.class));		
		when(mockNodeModelCreator.createEntity(anyString(), eq(RSSFeed.class))).thenReturn(testFeed);
		homePresenter.loadNewsFeed();
		verify(mockView).showNews(anyString());
	}	
	
	@Test
	public void testSupportFeed() throws RestServiceException {
		//when support feed is loaded, the view should be updated with the service result
		String exampleSupportFeedResult = "support feed";
		AsyncMockStubber.callSuccessWith(exampleSupportFeedResult).when(mockRssService).getCachedContent(anyString(), any(AsyncCallback.class));
		when(mockNodeModelCreator.createEntity(anyString(), eq(RSSFeed.class))).thenReturn(testFeed);
		homePresenter.loadSupportFeed();
		verify(mockView).showSupportFeed(anyString());
	}	
}
