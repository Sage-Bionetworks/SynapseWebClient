package org.sagebionetworks.web.unitclient.presenter;

import static org.mockito.Matchers.*;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.repo.model.auth.Session;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SearchServiceAsync;
import org.sagebionetworks.web.client.StackConfigServiceAsync;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.cookie.CookieKeys;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.Home;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.presenter.HomePresenter;
import org.sagebionetworks.web.client.resources.ResourceLoader;
import org.sagebionetworks.web.client.resources.WebResource;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.security.AuthenticationException;
import org.sagebionetworks.web.client.view.HomeView;
import org.sagebionetworks.web.shared.OpenUserInvitationBundle;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import org.sagebionetworks.web.unitclient.widget.entity.team.TeamListWidgetTest;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class HomePresenterTest {

	HomePresenter homePresenter;
	HomeView mockView;
	AuthenticationController mockAuthenticationController;
	GlobalApplicationState mockGlobalApplicationState;
	PlaceChanger mockPlaceChanger;
	StackConfigServiceAsync mockStackConfigService;
	SearchServiceAsync mockSearchService; 
	SynapseClientAsync mockSynapseClient;
	CookieProvider mockCookies;
	SynapseJSNIUtils mockSynapseJSNIUtils;
	GWTWrapper mockGwtWrapper;
	JSONObjectAdapter adapter = new JSONObjectAdapterImpl();
	
	List<EntityHeader> testEvaluationResults;
	List<OpenUserInvitationBundle> openInvitations;
	
	String testTeamId = "42";
	UserSessionData testSessionData;
	ResourceLoader mockResourceLoader;
	@Before
	public void setup() throws RestServiceException, JSONObjectAdapterException{
		mockView = mock(HomeView.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockPlaceChanger = mock(PlaceChanger.class);
		mockSearchService = mock(SearchServiceAsync.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockSynapseJSNIUtils = mock(SynapseJSNIUtils.class);
		mockGwtWrapper = mock(GWTWrapper.class);
		mockCookies = mock(CookieProvider.class);
		mockResourceLoader = mock(ResourceLoader.class);
		
		org.sagebionetworks.reflection.model.PaginatedResults<EntityHeader> testBatchResults = new org.sagebionetworks.reflection.model.PaginatedResults<EntityHeader>();
		testEvaluationResults = new ArrayList<EntityHeader>();
		EntityHeader testEvaluation = new EntityHeader();
		testEvaluation.setId("eval project id 1");
		testEvaluation.setName("My Test Evaluation Project");
		testEvaluationResults.add(testEvaluation);
		testBatchResults.setTotalNumberOfResults(1);
		testBatchResults.setResults(testEvaluationResults);
		
		ArrayList<EntityHeader> testBatchResultsList = new ArrayList<EntityHeader>();
		testBatchResultsList.addAll(testBatchResults.getResults());
		
		AsyncMockStubber.callSuccessWith(testTeamId).when(mockSynapseClient).createTeam(anyString(),any(AsyncCallback.class));
		
		AsyncMockStubber.callSuccessWith(testBatchResultsList).when(mockSynapseClient).getEntityHeaderBatch(anyList(),any(AsyncCallback.class));
		
		openInvitations = new ArrayList<OpenUserInvitationBundle>();
		AsyncMockStubber.callSuccessWith(openInvitations).when(mockSynapseClient).getOpenInvitations(anyString(), any(AsyncCallback.class));
		
		mockAuthenticationController = Mockito.mock(AuthenticationController.class);
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		
		homePresenter = new HomePresenter(mockView, 
				mockAuthenticationController, 
				mockGlobalApplicationState,
				adapter,
				mockCookies,
				mockResourceLoader,
				mockSynapseJSNIUtils
				);
		verify(mockView).setPresenter(homePresenter);
		TeamListWidgetTest.setupUserTeams(adapter, mockSynapseClient);
		
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		testSessionData = new UserSessionData();
		Session testSession = new Session();
		testSession.setAcceptsTermsOfUse(true);
		testSessionData.setSession(testSession);
		testSessionData.setIsSSO(false);
		when(mockAuthenticationController.getCurrentUserSessionData()).thenReturn(testSessionData);
		
		AsyncMockStubber.callSuccessWith(null).when(mockAuthenticationController).revalidateSession(anyString(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith("").when(mockSynapseClient).getCertifiedUserPassingRecord(anyString(),  any(AsyncCallback.class));
		when(mockCookies.getCookie(eq(DisplayUtils.SYNAPSE_TEST_WEBSITE_COOKIE_KEY))).thenReturn("true");
	}	
	
	@Test
	public void testSetPlace() {
		Home place = Mockito.mock(Home.class);
		homePresenter.setPlace(place);
		verify(mockView).refresh();
	}
	
	@Test
	public void testValidateTokenSSO() {
		homePresenter.validateToken();
		verify(mockAuthenticationController).revalidateSession(anyString(), any(AsyncCallback.class));
	}
	

	@Test
	public void testInvalidToken() {
		AsyncMockStubber.callFailureWith(new AuthenticationException()).when(mockAuthenticationController).revalidateSession(anyString(), any(AsyncCallback.class));
		homePresenter.validateToken();
		verify(mockAuthenticationController).revalidateSession(anyString(), any(AsyncCallback.class));
		verify(mockPlaceChanger).goTo(any(LoginPlace.class));
	}
	
	@Test
	public void testNewsFeed() throws JSONObjectAdapterException {
		homePresenter.loadNewsFeed();
		
		verify(mockView).prepareTwitterContainer(anyString());
		when(mockResourceLoader.isLoaded(any(WebResource.class))).thenReturn(false);
		homePresenter.twitterContainerReady("twitterElementId");
		verify(mockResourceLoader).isLoaded(any(WebResource.class));
		verify(mockResourceLoader).requires(any(WebResource.class), any(AsyncCallback.class));
		
		when(mockResourceLoader.isLoaded(any(WebResource.class))).thenReturn(true);
		homePresenter.twitterContainerReady("twitterElementId");
		verify(mockSynapseJSNIUtils).showTwitterFeed(anyString(), anyString(), anyString(), anyString(), anyInt());
	}	
	
	
	@Test
	public void testCheckAcceptToUAnonymous() {
		when(mockAuthenticationController.isLoggedIn()).thenReturn(false);
		homePresenter.checkAcceptToU();
		//should not ask for the session information, or try to log you out
		verify(mockAuthenticationController, never()).getCurrentUserSessionData();
		verify(mockAuthenticationController, never()).logoutUser();
	}

	@Test
	public void testCheckAcceptToULoggedInToUSigned() {
		homePresenter.checkAcceptToU();
		verify(mockAuthenticationController).getCurrentUserSessionData();
		//should not log you out
		verify(mockAuthenticationController, never()).logoutUser();
	}
	
	@Test
	public void testCheckAcceptToULoggedInToUUnsigned() {
		testSessionData.getSession().setAcceptsTermsOfUse(false);
		homePresenter.checkAcceptToU();
		
		verify(mockAuthenticationController).getCurrentUserSessionData();
		//should automatically log you out
		verify(mockAuthenticationController).logoutUser();
	}
	
	@Test
	public void testAnonymousNotLoggedInRecently() {
		when(mockCookies.getCookie(CookieKeys.USER_LOGGED_IN_RECENTLY)).thenReturn(null);
		when(mockAuthenticationController.isLoggedIn()).thenReturn(false);
		Home place = Mockito.mock(Home.class);
		homePresenter.setPlace(place);
		verify(mockView).showRegisterUI();
	}
	
	@Test
	public void testAnonymousLoggedInRecently() {
		when(mockCookies.getCookie(eq(CookieKeys.USER_LOGGED_IN_RECENTLY))).thenReturn("true");
		when(mockAuthenticationController.isLoggedIn()).thenReturn(false);
		Home place = Mockito.mock(Home.class);
		homePresenter.setPlace(place);
		verify(mockView).showLoginUI();
	}

	@Test
	public void testOnUserChange() {
		String userId = "77776";
		when(mockAuthenticationController.getCurrentUserPrincipalId()).thenReturn(userId);
		homePresenter.onUserChange();
		verify(mockPlaceChanger).goTo(any(Profile.class));
	}
}
