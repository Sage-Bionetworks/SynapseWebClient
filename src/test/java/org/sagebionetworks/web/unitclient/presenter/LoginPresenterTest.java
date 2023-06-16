package org.sagebionetworks.web.unitclient.presenter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.place.ChangeUsername;
import org.sagebionetworks.web.client.place.Home;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.place.users.RegisterAccount;
import org.sagebionetworks.web.client.presenter.LoginPresenter;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.view.LoginView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

public class LoginPresenterTest {

  LoginPresenter loginPresenter;

  @Mock
  LoginView mockView;

  @Mock
  AuthenticationController mockAuthenticationController;

  @Mock
  GlobalApplicationState mockGlobalApplicationState;

  @Mock
  SynapseAlert mockSynAlert;

  @Mock
  PlaceChanger mockPlaceChanger;

  @Mock
  AcceptsOneWidget mockPanel;

  @Mock
  EventBus mockEventBus;

  @Mock
  LoginPlace mockLoginPlace;

  @Mock
  Callback mockTouCallback;

  @Captor
  ArgumentCaptor<Place> placeCaptor;

  @Captor
  ArgumentCaptor<Callback> callbackCaptor;

  @Captor
  ArgumentCaptor<AsyncCallback<UserProfile>> asyncCallbackCaptor;

  @Mock
  PopupUtilsView mockPopupUtils;

  @Mock
  UserProfile mockUserProfile;

  String userId = "007";

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
    when(mockUserProfile.getUserName()).thenReturn("a_username");
    when(mockAuthenticationController.getCurrentUserProfile())
      .thenReturn(mockUserProfile);
    when(mockGlobalApplicationState.getPlaceChanger())
      .thenReturn(mockPlaceChanger);
    loginPresenter =
      new LoginPresenter(
        mockView,
        mockAuthenticationController,
        mockGlobalApplicationState,
        mockSynAlert,
        mockPopupUtils
      );
    loginPresenter.start(mockPanel, mockEventBus);
    verify(mockView).setPresenter(loginPresenter);
    when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
    when(mockAuthenticationController.getCurrentUserPrincipalId())
      .thenReturn(userId);
    AsyncMockStubber
      .callSuccessWith(mockUserProfile)
      .when(mockAuthenticationController)
      .setNewAccessToken(anyString(), any(AsyncCallback.class));
  }

  @Test
  public void testSetPlaceLogout() {
    when(mockLoginPlace.toToken()).thenReturn(LoginPlace.LOGOUT_TOKEN);
    loginPresenter.setPlace(mockLoginPlace);
    verify(mockAuthenticationController).logoutUser();
    verify(mockGlobalApplicationState).clearLastPlace();
    verify(mockPlaceChanger).goTo(any(Home.class));
  }

  @Test
  public void testSetPlaceRedirectToLastPlace() {
    when(mockLoginPlace.toToken())
      .thenReturn(WebConstants.REDIRECT_TO_LAST_PLACE);
    loginPresenter.setPlace(mockLoginPlace);
    verify(mockGlobalApplicationState).gotoLastPlace();
  }

  @Test
  public void testSetPlaceUnknownSSOUser() {
    when(mockLoginPlace.toToken())
      .thenReturn(WebConstants.OPEN_ID_UNKNOWN_USER_ERROR_TOKEN);
    loginPresenter.setPlace(mockLoginPlace);
    verify(mockView).showErrorMessage(anyString());
    verify(mockPlaceChanger).goTo(placeCaptor.capture());
    assertTrue(placeCaptor.getValue() instanceof RegisterAccount);
  }

  @Test
  public void testSetPlaceUnknownSSOError() {
    when(mockLoginPlace.toToken()).thenReturn(WebConstants.OPEN_ID_ERROR_TOKEN);
    loginPresenter.setPlace(mockLoginPlace);
    verify(mockView).showErrorMessage(anyString());
    verify(mockView).showLogin();
  }

  @Test
  public void testSetPlaceShowAndAcceptToU() {
    when(mockLoginPlace.toToken()).thenReturn(LoginPlace.SHOW_TOU);
    when(mockAuthenticationController.getCurrentUserAccessToken())
      .thenReturn("valid session token");

    // method under test
    loginPresenter.setPlace(mockLoginPlace);

    verify(mockView).showTermsOfUse(eq(false));
    AsyncMockStubber
      .callSuccessWith(null)
      .when(mockAuthenticationController)
      .signTermsOfUse(any(AsyncCallback.class));

    loginPresenter.onAcceptTermsOfUse();

    // Verify the AuthenticationController method was called and invoke the passed callback
    verify(mockAuthenticationController)
      .initializeFromExistingAccessTokenCookie(
        asyncCallbackCaptor.capture(),
        eq(true)
      );
    asyncCallbackCaptor.getValue().onSuccess(mockUserProfile);

    verify(mockAuthenticationController)
      .signTermsOfUse(any(AsyncCallback.class));
    verify(mockAuthenticationController)
      .initializeFromExistingAccessTokenCookie(
        any(AsyncCallback.class),
        eq(true)
      );
    // verify we only showed this once:
    verify(mockView).showTermsOfUse(eq(false));
    // go to the last place (or the user dashboard Profile place if last place is not set)
    verify(mockGlobalApplicationState).gotoLastPlace(any(Profile.class));
  }

  @Test
  public void testCancelToU() {
    when(mockLoginPlace.toToken()).thenReturn(LoginPlace.SHOW_TOU);
    when(mockAuthenticationController.getCurrentUserAccessToken())
      .thenReturn("valid session token");

    loginPresenter.setPlace(mockLoginPlace);
    loginPresenter.onCancelAcceptTermsOfUse();

    // verify confirmation popup
    verify(mockPopupUtils)
      .showConfirmDialog(
        eq(LoginPresenter.ARE_YOU_SURE_YOU_WANT_TO_CANCEL),
        eq(LoginPresenter.CANCEL_TERMS_OF_USE_CONFIRM_MESSAGE),
        callbackCaptor.capture()
      );

    // simulate confirm
    callbackCaptor.getValue().invoke();

    verify(mockAuthenticationController, never())
      .signTermsOfUse(any(AsyncCallback.class));
    verify(mockPlaceChanger).goTo(any(LoginPlace.class));
  }

  @Test
  public void testSetPlaceShowAcceptedToU() {
    when(mockLoginPlace.toToken()).thenReturn(LoginPlace.SHOW_SIGNED_TOU);
    when(mockAuthenticationController.getCurrentUserAccessToken())
      .thenReturn("valid session token");

    // method under test
    loginPresenter.setPlace(mockLoginPlace);

    verify(mockView).showTermsOfUse(eq(true));
  }

  // note: if user has already accepted Synapse ToU and they go to the ToU page, no need to block
  // (show ToU).

  @Test
  public void testSetPlaceSSOLogin() throws JSONObjectAdapterException {
    String fakeToken = "0e79b99-4bf8-4999-b3a2-5f8c0a9499eb";
    when(mockLoginPlace.toToken()).thenReturn(fakeToken);
    when(mockAuthenticationController.getCurrentUserAccessToken())
      .thenReturn(fakeToken);

    loginPresenter.setPlace(mockLoginPlace);

    // SWC-6192: We no longer support setting the access token in the URL
    verify(mockAuthenticationController, never())
      .setNewAccessToken(eq(fakeToken), any(AsyncCallback.class));
  }

  @Test
  public void testCheckTempUsername() {
    when(mockUserProfile.getOwnerId()).thenReturn("1233");
    when(mockUserProfile.getUserName())
      .thenReturn(WebConstants.TEMPORARY_USERNAME_PREFIX + "222");

    loginPresenter.checkForTempUsername();

    verify(mockPlaceChanger).goTo(isA(ChangeUsername.class));
  }

  @Test
  public void testCheckTempUsernameNotTemp() {
    when(mockUserProfile.getOwnerId()).thenReturn("1233");
    when(mockUserProfile.getUserName()).thenReturn("not-temp");

    loginPresenter.checkForTempUsername();

    // should go to the last place, since this is not a temporary username
    verify(mockGlobalApplicationState).gotoLastPlace(any(Place.class));
  }

  @Test
  public void testSetPlaceChangeUsername() {
    when(mockLoginPlace.toToken()).thenReturn(LoginPlace.CHANGE_USERNAME);
    loginPresenter.setPlace(mockLoginPlace);
    verify(mockPlaceChanger).goTo(isA(ChangeUsername.class));
  }

  @Test
  public void testUserAuthenticated() {
    when(mockAuthenticationController.isLoggedIn()).thenReturn(false);
    loginPresenter.userAuthenticated();
    verify(mockView).showErrorMessage(anyString());
    verify(mockView).showLogin();
  }

  @Test
  public void testLastPlaceAfterLogin() {
    // this should send to this user's profile (dashboard) by default
    loginPresenter.goToLastPlace();

    verify(mockGlobalApplicationState).gotoLastPlace(placeCaptor.capture());
    Place defaultPlace = placeCaptor.getValue();
    assertTrue(defaultPlace instanceof Profile);
    assertEquals(userId, ((Profile) defaultPlace).getUserId());
  }

  @Test
  public void testGotoPlace() {
    loginPresenter.goTo(mockLoginPlace);
    verify(mockPlaceChanger).goTo(mockLoginPlace);
  }
}
