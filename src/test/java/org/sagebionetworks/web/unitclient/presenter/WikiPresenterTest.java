package org.sagebionetworks.web.unitclient.presenter;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.RssServiceAsync;
import org.sagebionetworks.web.client.UserAccountServiceAsync;
import org.sagebionetworks.web.client.place.WikiPlace;
import org.sagebionetworks.web.client.presenter.WikiPresenter;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.view.WikiView;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class WikiPresenterTest {
	
	WikiPresenter loginPresenter;
	WikiView mockView;
	AuthenticationController mockAuthenticationController;
	UserAccountServiceAsync mockUserAccountServiceAsync;
	GlobalApplicationState mockGlobalApplicationState;
	NodeModelCreator mockNodeModelCreator;
	RssServiceAsync mockRssService;
	
	@Before
	public void setup(){
		mockView = mock(WikiView.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockUserAccountServiceAsync = mock(UserAccountServiceAsync.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockNodeModelCreator = mock(NodeModelCreator.class);
		mockRssService = mock(RssServiceAsync.class);
		loginPresenter = new WikiPresenter(mockView, mockAuthenticationController, mockGlobalApplicationState, mockNodeModelCreator, mockRssService);
		
		verify(mockView).setPresenter(loginPresenter);
	}	
	
	@Test
	public void testSetPlace() {
		WikiPlace place = Mockito.mock(WikiPlace.class);
		loginPresenter.setPlace(place);
	}
	
	@Test
	public void testSetWikiContent() throws RestServiceException {
		//when content is loaded, the view should be updated with the service result
		String exampleNewsFeedResult = "wiki content";
		AsyncMockStubber.callSuccessWith(exampleNewsFeedResult).when(mockRssService).getUncachedWikiPageSourceContent(anyString(), any(AsyncCallback.class));		
		loginPresenter.loadSourceContent("12345");
		verify(mockView).showPage(anyString());
	}
}
