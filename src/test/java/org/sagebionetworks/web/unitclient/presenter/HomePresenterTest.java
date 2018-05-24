package org.sagebionetworks.web.unitclient.presenter;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.repo.model.auth.Session;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.StackConfigServiceAsync;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.cookie.CookieKeys;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.Home;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.presenter.HomePresenter;
import org.sagebionetworks.web.client.resources.ResourceLoader;
import org.sagebionetworks.web.client.resources.WebResource;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.view.HomeView;
import org.sagebionetworks.web.shared.OpenUserInvitationBundle;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class HomePresenterTest {

	HomePresenter homePresenter;
	@Mock
	HomeView mockView;
	@Mock
	AuthenticationController mockAuthenticationController;
	@Mock
	GlobalApplicationState mockGlobalApplicationState;
	@Mock
	PlaceChanger mockPlaceChanger;
	@Mock
	StackConfigServiceAsync mockStackConfigService;
	@Mock
	SynapseClientAsync mockSynapseClient;
	@Mock
	CookieProvider mockCookies;
	@Mock
	SynapseJSNIUtils mockSynapseJSNIUtils;
	
	List<EntityHeader> testEvaluationResults;
	List<OpenUserInvitationBundle> openInvitations;
	
	UserSessionData testSessionData;
	@Mock
	ResourceLoader mockResourceLoader;
	
	@Before
	public void setup() throws RestServiceException, JSONObjectAdapterException{
		MockitoAnnotations.initMocks(this);
		
		org.sagebionetworks.reflection.model.PaginatedResults<EntityHeader> testBatchResults = new org.sagebionetworks.reflection.model.PaginatedResults<EntityHeader>();
		testEvaluationResults = new ArrayList<EntityHeader>();
		EntityHeader testEvaluation = new EntityHeader();
		testEvaluation.setId("eval project id 1");
		testEvaluation.setName("My Test Evaluation Project");
		testEvaluationResults.add(testEvaluation);
		testBatchResults.setTotalNumberOfResults(1);
		testBatchResults.setResults(testEvaluationResults);
		
		openInvitations = new ArrayList<OpenUserInvitationBundle>();
		AsyncMockStubber.callSuccessWith(openInvitations).when(mockSynapseClient).getOpenInvitations(anyString(), any(AsyncCallback.class));
		
		mockAuthenticationController = Mockito.mock(AuthenticationController.class);
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		homePresenter = new HomePresenter(mockView, 
				mockAuthenticationController, 
				mockGlobalApplicationState,
				mockCookies,
				mockResourceLoader,
				mockSynapseJSNIUtils
				);
		verify(mockView).setPresenter(homePresenter);
		
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
	public void testNewsFeed() throws JSONObjectAdapterException {
		homePresenter.loadNewsFeed();
		
		verify(mockView).prepareTwitterContainer(anyString(), anyInt());
		when(mockResourceLoader.isLoaded(any(WebResource.class))).thenReturn(false);
		homePresenter.twitterContainerReady("twitterElementId");
		verify(mockResourceLoader).isLoaded(any(WebResource.class));
		verify(mockResourceLoader).requires(any(WebResource.class), any(AsyncCallback.class));
		
		when(mockResourceLoader.isLoaded(any(WebResource.class))).thenReturn(true);
		homePresenter.twitterContainerReady("twitterElementId");
		verify(mockSynapseJSNIUtils).showTwitterFeed(anyString(), anyString(), anyString(), anyString(), eq(HomePresenter.TWEET_COUNT));
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
		verify(mockPlaceChanger).goTo(isA(Profile.class));
	}
}
