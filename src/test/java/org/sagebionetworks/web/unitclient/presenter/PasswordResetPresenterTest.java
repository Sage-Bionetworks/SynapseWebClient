package org.sagebionetworks.web.unitclient.presenter;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.auth.ChangePasswordWithCurrentPassword;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.place.users.PasswordReset;
import org.sagebionetworks.web.client.presenter.users.PasswordResetPresenter;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.view.users.PasswordResetView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

@RunWith(MockitoJUnitRunner.class)
public class PasswordResetPresenterTest {

	PasswordResetPresenter presenter;
	@Mock
	PasswordResetView mockView;
	@Mock
	GlobalApplicationState mockGlobalApplicationState;
	@Mock
	AuthenticationController mockAuthenticationController;
	@Mock
	PlaceChanger mockPlaceChanger;
	@Mock
	PasswordReset place;
	@Mock
	SynapseJavascriptClient mockJsClient;

	@Mock
	SynapseAlert mockSynAlert;
	UserProfile profile;

	@Before
	public void setup() {
		profile = new UserProfile();
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockPlaceChanger = mock(PlaceChanger.class);
		mockAuthenticationController = Mockito.mock(AuthenticationController.class);

		presenter = new PasswordResetPresenter(mockView, mockAuthenticationController, mockGlobalApplicationState, mockJsClient, mockSynAlert);
		verify(mockView).setPresenter(presenter);
		when(place.toToken()).thenReturn(ClientProperties.DEFAULT_PLACE_TOKEN);
	}

	private void resetAll() {
		reset(mockView);
		reset(mockGlobalApplicationState);
		reset(mockAuthenticationController);

		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		when(mockAuthenticationController.getCurrentUserProfile()).thenReturn(profile);
		AsyncMockStubber.callSuccessWith(profile).when(mockAuthenticationController).loginUser(anyString(), anyString(), any(AsyncCallback.class));
	}

	@Test
	public void testStart() {
		presenter.setPlace(place);

		AcceptsOneWidget panel = mock(AcceptsOneWidget.class);
		EventBus eventBus = mock(EventBus.class);

		presenter.start(panel, eventBus);
		verify(panel).setWidget(mockView);
	}

	@Test
	public void testSetPasswordLoad() {
		AsyncMockStubber.callSuccessWith(profile).when(mockAuthenticationController).setNewSessionToken(anyString(), any(AsyncCallback.class));
		PasswordReset place = new PasswordReset("someSessionToken");
		presenter.setPlace(place);
		verify(mockView).showResetForm();
	}

	@Test
	public void testSetPasswordLoadFail() {
		AsyncMockStubber.callFailureWith(new Exception()).when(mockAuthenticationController).setNewSessionToken(anyString(), any(AsyncCallback.class));
		PasswordReset place = new PasswordReset("someSessionToken");
		presenter.setPlace(place);
		verify(mockView).showExpiredRequest();
	}

	@Test
	public void testResetPassword() {
		// mock a successful user service call
		resetAll();
		profile.setUserName("007");
		presenter.setPlace(place);
		AsyncMockStubber.callSuccessWith(null).when(mockJsClient).changePassword(any(ChangePasswordWithCurrentPassword.class), any(AsyncCallback.class));
		presenter.resetPassword("oldPassword", "myPassword");
		// verify password reset text is shown in the view
		verify(mockView).showInfo(eq(DisplayConstants.PASSWORD_RESET_TEXT));
		// verify that place is changed to last place
		verify(mockGlobalApplicationState).gotoLastPlace();
	}

	@Test
	public void testResetPassword2() {
		// if there is a failure to re-login using the available credentials, send to the login page instead
		resetAll();
		AsyncMockStubber.callFailureWith(new Exception()).when(mockAuthenticationController).loginUser(anyString(), anyString(), any(AsyncCallback.class));
		presenter.setPlace(place);
		AsyncMockStubber.callSuccessWith(null).when(mockJsClient).changePassword(any(ChangePasswordWithCurrentPassword.class), any(AsyncCallback.class));
		presenter.resetPassword("oldPassword", "myPassword");
		// verify password reset text is shown in the view
		verify(mockView).showInfo(eq(DisplayConstants.PASSWORD_RESET_TEXT));
		// verify that place is changed to login page
		verify(mockPlaceChanger).goTo(isA(LoginPlace.class));
	}

	@Test
	public void testResetPassword3() {
		// or if the profile username is not set
		resetAll();
		presenter.setPlace(place);
		AsyncMockStubber.callSuccessWith(null).when(mockJsClient).changePassword(any(ChangePasswordWithCurrentPassword.class), any(AsyncCallback.class));
		profile.setUserName(null);
		presenter.resetPassword("oldPassword", "myPassword");
		// verify that place is changed to Login
		verify(mockPlaceChanger).goTo(isA(LoginPlace.class));
	}

	@Test
	public void testResetPassword4() {
		// or if the profile username is a temporary username
		resetAll();
		presenter.setPlace(place);
		AsyncMockStubber.callSuccessWith(null).when(mockJsClient).changePassword(any(ChangePasswordWithCurrentPassword.class), any(AsyncCallback.class));
		profile.setUserName(WebConstants.TEMPORARY_USERNAME_PREFIX + "123");
		presenter.resetPassword("oldPassword", "myPassword");
		// verify that place is changed to Login
		verify(mockPlaceChanger).goTo(isA(LoginPlace.class));
	}

	@Test
	public void testServiceFailure() {
		// without the registration token set, mock a failed user service call
		resetAll();
		String errorMessage = "unknown error";
		Exception ex = new RestServiceException(errorMessage);
		AsyncMockStubber.callFailureWith(ex).when(mockJsClient).changePassword(any(ChangePasswordWithCurrentPassword.class), any(AsyncCallback.class));
		presenter.setPlace(place);
		presenter.resetPassword("oldPassword", "myPassword");
		verify(mockSynAlert).clear();
		// verify password reset failed text is shown in the view
		verify(mockSynAlert).showError(errorMessage);
	}
}
