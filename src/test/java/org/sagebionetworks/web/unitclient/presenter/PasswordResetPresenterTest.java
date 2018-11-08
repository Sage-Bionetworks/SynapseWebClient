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
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.UserAccountServiceAsync;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.place.users.PasswordReset;
import org.sagebionetworks.web.client.presenter.users.PasswordResetPresenter;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.view.users.PasswordResetView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.login.PasswordStrengthWidget;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class PasswordResetPresenterTest {
	
	PasswordResetPresenter presenter;
	PasswordResetView mockView;
	CookieProvider mockCookieProvider;
	UserAccountServiceAsync mockUserService;
	GlobalApplicationState mockGlobalApplicationState;
	
	AuthenticationController mockAuthenticationController;
	SageImageBundle mockSageImageBundle;
	IconsImageBundle mockIconsImageBundle;
	PlaceChanger mockPlaceChanger;
	PasswordReset place = Mockito.mock(PasswordReset.class);
	
	@Mock
	PasswordStrengthWidget mockPasswordStrengthWidget;
	@Mock
	SynapseAlert mockSynAlert;
	UserProfile profile;
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		profile = new UserProfile();
		mockView = mock(PasswordResetView.class);
		mockCookieProvider = mock(CookieProvider.class);
		mockUserService = mock(UserAccountServiceAsync.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockSageImageBundle = mock(SageImageBundle.class);
		mockIconsImageBundle = mock(IconsImageBundle.class);
		mockPlaceChanger = mock(PlaceChanger.class);
		mockAuthenticationController = Mockito.mock(AuthenticationController.class);
		
		presenter = new PasswordResetPresenter(mockView, mockCookieProvider,
				mockUserService, mockAuthenticationController,
				mockSageImageBundle, mockIconsImageBundle,
				mockGlobalApplicationState, mockPasswordStrengthWidget, mockSynAlert);			
		verify(mockView).setPresenter(presenter);
		when(place.toToken()).thenReturn(ClientProperties.DEFAULT_PLACE_TOKEN);
	}
	
	private void resetAll(){
		reset(mockView);
		reset(mockCookieProvider);
		reset(mockUserService);
		reset(mockGlobalApplicationState);
		reset(mockAuthenticationController);
		reset(mockSageImageBundle);
		reset(mockIconsImageBundle);

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
		//mock a successful user service call
		resetAll();
		profile.setUserName("007");
		presenter.setPlace(place);
		AsyncMockStubber.callSuccessWith(null).when(mockUserService).changePassword(any(String.class), any(String.class), any(AsyncCallback.class));
		presenter.resetPassword("myPassword");
		//verify password reset text is shown in the view
		verify(mockView).showInfo(eq(DisplayConstants.PASSWORD_RESET_TEXT));
		//verify that place is changed to last place
		verify(mockGlobalApplicationState).gotoLastPlace();
	}
	
	@Test
	public void testResetPassword2() {
		//if there is a failure to re-login using the available credentials, send to the login page instead
		resetAll();
		AsyncMockStubber.callFailureWith(new Exception()).when(mockAuthenticationController).loginUser(anyString(), anyString(), any(AsyncCallback.class));
		presenter.setPlace(place);
		AsyncMockStubber.callSuccessWith(null).when(mockUserService).changePassword(any(String.class), any(String.class), any(AsyncCallback.class));
		presenter.resetPassword("myPassword");
		//verify password reset text is shown in the view
		verify(mockView).showInfo(eq(DisplayConstants.PASSWORD_RESET_TEXT));
		//verify that place is changed to login page
		verify(mockPlaceChanger).goTo(isA(LoginPlace.class));
	}
	
	@Test
	public void testResetPassword3() {
		//or if the profile username is not set
		resetAll();
		presenter.setPlace(place);
		AsyncMockStubber.callSuccessWith(null).when(mockUserService).changePassword(any(String.class), any(String.class), any(AsyncCallback.class));
		profile.setUserName(null);
		presenter.resetPassword("myPassword");
		//verify that place is changed to Login
		verify(mockPlaceChanger).goTo(isA(LoginPlace.class));
	}
	
	@Test
	public void testResetPassword4() {
		//or if the profile username is a temporary username
		resetAll();
		presenter.setPlace(place);
		AsyncMockStubber.callSuccessWith(null).when(mockUserService).changePassword(any(String.class), any(String.class), any(AsyncCallback.class));
		profile.setUserName(WebConstants.TEMPORARY_USERNAME_PREFIX + "123");
		presenter.resetPassword("myPassword");
		//verify that place is changed to Login
		verify(mockPlaceChanger).goTo(isA(LoginPlace.class));
	}
	
	@Test
	public void testServiceFailure() {
		//without the registration token set, mock a failed user service call
		resetAll();
		Exception ex = new RestServiceException("unknown error");
		AsyncMockStubber.callFailureWith(ex).when(mockUserService).changePassword(any(String.class), any(String.class), any(AsyncCallback.class));
		presenter.setPlace(place);
		presenter.resetPassword("myPassword");
		verify(mockSynAlert).clear();
		//verify password reset failed text is shown in the view
		verify(mockSynAlert).handleException(ex);
	}
}
