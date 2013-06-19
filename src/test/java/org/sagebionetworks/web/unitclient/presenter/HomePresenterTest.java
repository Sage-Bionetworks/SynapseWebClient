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
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.RssServiceAsync;
import org.sagebionetworks.web.client.SearchServiceAsync;
import org.sagebionetworks.web.client.StackConfigServiceAsync;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.Home;
import org.sagebionetworks.web.client.presenter.HomePresenter;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.view.HomeView;
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
	SearchServiceAsync mockSearchService; 
	SynapseClientAsync mockSynapseClient; 
	AutoGenFactory autoGenFactory;
	AdapterFactory adapterFactory = new AdapterFactoryImpl();

	List<EntityHeader> testEvaluationResults;
	RSSFeed testFeed = null;
	
	@Before
	public void setup() throws RestServiceException, JSONObjectAdapterException{
		mockView = mock(HomeView.class);
		cookieProvider = mock(CookieProvider.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockRssService = mock(RssServiceAsync.class);
		mockNodeModelCreator = mock(NodeModelCreator.class);
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
		when(mockNodeModelCreator.createBatchResults(anyString(), any(Class.class))).thenReturn(testBatchResults);
		AsyncMockStubber.callSuccessWith("fake paginated evaluation results json").when(mockSynapseClient).getAvailableEvaluationEntities(any(AsyncCallback.class));
		testFeed = new RSSFeed();
		RSSEntry entry = new RSSEntry();
		entry.setTitle("A Title");
		entry.setAuthor("An Author");
		entry.setLink("http://somewhere");
		List<RSSEntry> entries = new ArrayList<RSSEntry>();
		entries.add(entry);
		testFeed.setEntries(entries);
		
		mockAuthenticationController = Mockito.mock(AuthenticationController.class);
		
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
	}
	
	@Test
	public void testNewsFeed() throws JSONObjectAdapterException {
		//when news is loaded, the view should be updated with the service result
		String exampleNewsFeedResult = "news feed";
		AsyncMockStubber.callSuccessWith(exampleNewsFeedResult).when(mockRssService).getCachedContent(anyString(), any(AsyncCallback.class));		
		when(mockNodeModelCreator.createJSONEntity(anyString(), eq(RSSFeed.class))).thenReturn(testFeed);
		homePresenter.loadNewsFeed();
		verify(mockView).showNews(anyString());
	}	
	
	@Test
	public void testLoadEvaluations() {
		//happy case
		AsyncCallback<List<EntityHeader>> mockCallback = mock(AsyncCallback.class);
		homePresenter.loadEvaluations(mockCallback);
		verify(mockCallback).onSuccess(testEvaluationResults);
	}
	
	
	@Test
	public void testLoadEvaluationsFailure() throws RestServiceException {
		Exception simulatedException = new Exception("Simulated Error");
		AsyncMockStubber.callFailureWith(simulatedException).when(mockSynapseClient).getAvailableEvaluationEntities(any(AsyncCallback.class));
		AsyncCallback<List<EntityHeader>> mockCallback = mock(AsyncCallback.class);
		homePresenter.loadEvaluations(mockCallback);
		verify(mockCallback).onFailure(simulatedException);
	}

}
