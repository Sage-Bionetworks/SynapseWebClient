package org.sagebionetworks.web.unitclient.presenter;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.UserProfile;
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
import org.sagebionetworks.web.client.presenter.HomePresenter;
import org.sagebionetworks.web.client.resources.ResourceLoader;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.view.HomeView;
import org.sagebionetworks.web.shared.OpenUserInvitationBundle;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.place.shared.Place;
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

	UserProfile testProfile;
	@Mock
	ResourceLoader mockResourceLoader;
	@Captor
	ArgumentCaptor<Place> placeCaptor;

	@Before
	public void setup() throws RestServiceException, JSONObjectAdapterException {
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
		homePresenter = new HomePresenter(mockView, mockAuthenticationController, mockGlobalApplicationState, mockCookies);

		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		testProfile = new UserProfile();
		when(mockAuthenticationController.getCurrentUserProfile()).thenReturn(testProfile);

		AsyncMockStubber.callSuccessWith("").when(mockSynapseClient).getCertifiedUserPassingRecord(anyString(), any(AsyncCallback.class));
		when(mockCookies.getCookie(eq(DisplayUtils.SYNAPSE_TEST_WEBSITE_COOKIE_KEY))).thenReturn("true");
	}

	@Test
	public void testSetPlace() {
		Home place = Mockito.mock(Home.class);
		homePresenter.setPlace(place);
		verify(mockView).refresh();
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
}
