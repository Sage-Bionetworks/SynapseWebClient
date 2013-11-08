package org.sagebionetworks.web.unitclient.presenter;

import static org.mockito.Matchers.any;
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
import org.sagebionetworks.repo.model.AutoGenFactory;
import org.sagebionetworks.repo.model.BatchResults;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.RSSEntry;
import org.sagebionetworks.repo.model.RSSFeed;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.RssServiceAsync;
import org.sagebionetworks.web.client.SearchServiceAsync;
import org.sagebionetworks.web.client.StackConfigServiceAsync;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.Home;
import org.sagebionetworks.web.client.presenter.HomePresenter;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.view.HomeView;
import org.sagebionetworks.web.shared.MembershipInvitationBundle;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class HomePresenterTest {

	HomePresenter homePresenter;
	CookieProvider cookieProvider;
	HomeView mockView;
	AuthenticationController mockAuthenticationController;
	GlobalApplicationState mockGlobalApplicationState;
	PlaceChanger mockPlaceChanger;
	StackConfigServiceAsync mockStackConfigService;
	RssServiceAsync mockRssService;
	SearchServiceAsync mockSearchService; 
	SynapseClientAsync mockSynapseClient; 
	AutoGenFactory autoGenFactory;
	AdapterFactory adapterFactory = new AdapterFactoryImpl();

	List<EntityHeader> testEvaluationResults;
	List<MembershipInvitationBundle> openInvitations;
	
	RSSFeed testFeed = null;
	String testTeamId = "42";
	@Before
	public void setup() throws RestServiceException, JSONObjectAdapterException{
		mockView = mock(HomeView.class);
		cookieProvider = mock(CookieProvider.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockPlaceChanger = mock(PlaceChanger.class);
		mockRssService = mock(RssServiceAsync.class);
		mockSearchService = mock(SearchServiceAsync.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		autoGenFactory = new AutoGenFactory();
		BatchResults<EntityHeader> testBatchResults = new BatchResults<EntityHeader>();
		testEvaluationResults = new ArrayList<EntityHeader>();
		EntityHeader testEvaluation = new EntityHeader();
		testEvaluation.setId("eval project id 1");
		testEvaluation.setName("My Test Evaluation Project");
		testEvaluationResults.add(testEvaluation);
		testBatchResults.setTotalNumberOfResults(1);
		testBatchResults.setResults(testEvaluationResults);
		
		ArrayList<String> testBatchResultsList = new ArrayList<String>();
		for(EntityHeader eh : testBatchResults.getResults()) {
			testBatchResultsList.add(eh.writeToJSONObject(adapterFactory.createNew()).toJSONString());
		}
		
		AsyncMockStubber.callSuccessWith(testTeamId).when(mockSynapseClient).createTeam(anyString(),any(AsyncCallback.class));
		
		openInvitations = new ArrayList<MembershipInvitationBundle>();
		AsyncMockStubber.callSuccessWith(openInvitations).when(mockSynapseClient).getOpenInvitations(anyString(), any(AsyncCallback.class));
		
		testFeed = new RSSFeed();
		RSSEntry entry = new RSSEntry();
		entry.setTitle("A Title");
		entry.setAuthor("An Author");
		entry.setLink("http://somewhere");
		List<RSSEntry> entries = new ArrayList<RSSEntry>();
		entries.add(entry);
		testFeed.setEntries(entries);
		
		mockAuthenticationController = Mockito.mock(AuthenticationController.class);
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		homePresenter = new HomePresenter(mockView, 
				mockAuthenticationController, 
				mockGlobalApplicationState,
				mockRssService,
				mockSearchService,
				mockSynapseClient,
				adapterFactory);
		verify(mockView).setPresenter(homePresenter);
	}	
	
	@Test
	public void testSetPlace() {
		Home place = Mockito.mock(Home.class);
		homePresenter.setPlace(place);
		verify(mockView).refreshMyTeams(anyString());
	}
	
	@Test
	public void testNewsFeed() throws JSONObjectAdapterException {
		//when news is loaded, the view should be updated with the service result
		String exampleNewsFeedResult = "news feed";
		AsyncMockStubber.callSuccessWith(exampleNewsFeedResult).when(mockRssService).getCachedContent(anyString(), any(AsyncCallback.class));		
		homePresenter.loadNewsFeed();
		verify(mockView).showNews(anyString());
	}	
	
	@Test
	public void testCreateTeam() {
		//happy case
		homePresenter.createTeam("New Team");
		verify(mockSynapseClient).createTeam(anyString(), any(AsyncCallback.class));
	}
	
	@Test
	public void testCreateTeamFailure() throws RestServiceException {
		Exception simulatedException = new Exception("Simulated Error");
		AsyncMockStubber.callFailureWith(simulatedException).when(mockSynapseClient).createTeam(anyString(), any(AsyncCallback.class));
		homePresenter.createTeam("New Team");
		verify(mockSynapseClient).createTeam(anyString(), any(AsyncCallback.class));
		verify(mockView).showErrorMessage(anyString());
	}
	
	@Test
	public void testIsNoOpenInvites() {
		CallbackP<Boolean> mockCallback = mock(CallbackP.class);
		homePresenter.isOpenTeamInvites(mockCallback);
		verify(mockCallback).invoke(eq(false));
	}
	
	@Test
	public void testIsOpenInvites() {
		openInvitations.add(new MembershipInvitationBundle());
		CallbackP<Boolean> mockCallback = mock(CallbackP.class);
		homePresenter.isOpenTeamInvites(mockCallback);
		verify(mockCallback).invoke(eq(true));
	}
}
