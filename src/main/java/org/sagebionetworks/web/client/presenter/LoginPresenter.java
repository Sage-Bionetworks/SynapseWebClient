package org.sagebionetworks.web.client.presenter;

import static org.sagebionetworks.web.client.ClientProperties.DEFAULT_PLACE_TOKEN;
import static org.sagebionetworks.web.client.DisplayConstants.CREATE_ACCOUNT_MESSAGE_SSO;
import static org.sagebionetworks.web.client.DisplayConstants.SSO_ERROR_UNKNOWN;
import static org.sagebionetworks.web.client.place.LoginPlace.CHANGE_USERNAME;
import static org.sagebionetworks.web.client.place.LoginPlace.LOGOUT_TOKEN;
import static org.sagebionetworks.web.client.place.LoginPlace.SHOW_SIGNED_TOU;
import static org.sagebionetworks.web.client.place.LoginPlace.SHOW_TOU;
import static org.sagebionetworks.web.shared.WebConstants.OPEN_ID_ERROR_TOKEN;
import static org.sagebionetworks.web.shared.WebConstants.OPEN_ID_UNKNOWN_USER_ERROR_TOKEN;
import static org.sagebionetworks.web.shared.WebConstants.REDIRECT_TO_LAST_PLACE;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.place.ChangeUsername;
import org.sagebionetworks.web.client.place.Home;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.place.Synapse.ProfileArea;
import org.sagebionetworks.web.client.place.users.RegisterAccount;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.view.LoginView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;

public class LoginPresenter
  extends AbstractActivity
  implements LoginView.Presenter, Presenter<LoginPlace> {

  public static final String CANCEL_TERMS_OF_USE_CONFIRM_MESSAGE =
    "Canceling now will log you out. You can always log in again to resume the registration process from where you left off.";
  public static final String ARE_YOU_SURE_YOU_WANT_TO_CANCEL =
    "Are you sure you want to cancel?";
  private LoginView view;
  private AuthenticationController authenticationController;
  private GlobalApplicationState globalApplicationState;
  private SynapseAlert synAlert;
  private PopupUtilsView popupUtils;

  @Inject
  public LoginPresenter(
    LoginView view,
    AuthenticationController authenticationController,
    GlobalApplicationState globalApplicationState,
    SynapseAlert synAlert,
    PopupUtilsView popupUtils
  ) {
    this.view = view;
    this.authenticationController = authenticationController;
    this.globalApplicationState = globalApplicationState;
    this.synAlert = synAlert;
    this.popupUtils = popupUtils;
    view.setSynAlert(synAlert);
    view.setPresenter(this);
  }

  @Override
  public void onAcceptTermsOfUse() {
    synAlert.clear();
    view.showLoggingInLoader();
    authenticationController.signTermsOfUse(
      new AsyncCallback<Void>() {
        @Override
        public void onFailure(Throwable caught) {
          synAlert.handleException(caught);
          view.showLogin();
        }

        @Override
        public void onSuccess(Void result) {
          // Have to get the UserSessionData again,
          // since it won't contain the UserProfile if the terms haven't been signed
          // We also need to force-reset the QueryClient so the React components know to refetch
          boolean forceResetQueryClient = true;
          synAlert.clear();
          authenticationController.initializeFromExistingAccessTokenCookie(
            new AsyncCallback<UserProfile>() {
              @Override
              public void onFailure(Throwable caught) {
                synAlert.handleException(caught);
                view.showLogin();
              }

              @Override
              public void onSuccess(UserProfile result) {
                // Signed ToU. Check for temp username, passing record, and then forward
                userAuthenticated();
              }
            },
            forceResetQueryClient
          );
        }
      }
    );
  }

  @Override
  public void onCancelAcceptTermsOfUse() {
    // confirm
    popupUtils.showConfirmDialog(
      ARE_YOU_SURE_YOU_WANT_TO_CANCEL,
      CANCEL_TERMS_OF_USE_CONFIRM_MESSAGE,
      () -> {
        globalApplicationState
          .getPlaceChanger()
          .goTo(new LoginPlace(LoginPlace.LOGOUT_TOKEN));
      }
    );
  }

  @Override
  public void start(AcceptsOneWidget panel, EventBus eventBus) {
    panel.setWidget(this.view.asWidget());
  }

  @Override
  public void setPlace(final LoginPlace place) {
    view.setPresenter(this);
    view.clear();
    showView(place);
  }

  public void showView(final LoginPlace place) {
    String token = place.toToken();
    if (LOGOUT_TOKEN.equals(token)) {
      authenticationController.logoutUser();
      globalApplicationState.clearLastPlace();
      view.showInfo(DisplayConstants.LOGOUT_TEXT);
      globalApplicationState
        .getPlaceChanger()
        .goTo(new Home(DEFAULT_PLACE_TOKEN));
    } else if (REDIRECT_TO_LAST_PLACE.equals(token)) {
      globalApplicationState.gotoLastPlace();
    } else if (OPEN_ID_UNKNOWN_USER_ERROR_TOKEN.equals(token)) {
      // User does not exist, redirect to Registration page
      view.showErrorMessage(CREATE_ACCOUNT_MESSAGE_SSO);
      globalApplicationState
        .getPlaceChanger()
        .goTo(new RegisterAccount(DEFAULT_PLACE_TOKEN));
    } else if (OPEN_ID_ERROR_TOKEN.equals(token)) {
      globalApplicationState
        .getPlaceChanger()
        .goTo(new LoginPlace(DEFAULT_PLACE_TOKEN));
      view.showErrorMessage(SSO_ERROR_UNKNOWN);
      view.showLogin();
    } else if (
      CHANGE_USERNAME.equals(token) && authenticationController.isLoggedIn()
    ) {
      // go to the change username page
      gotoChangeUsernamePlace();
    } else if (
      SHOW_TOU.equals(token) &&
      authenticationController.getCurrentUserAccessToken() != null
    ) {
      showTermsOfUse(false);
    } else if (
      SHOW_SIGNED_TOU.equals(token) &&
      authenticationController.getCurrentUserAccessToken() != null
    ) {
      showTermsOfUse(true);
    } else {
      if (authenticationController.isLoggedIn()) {
        Place defaultPlace = new Profile(
          authenticationController.getCurrentUserPrincipalId(),
          ProfileArea.PROJECTS
        );
        globalApplicationState.gotoLastPlace(defaultPlace);
      } else {
        // standard view
        view.showLogin();
      }
    }
  }

  private void gotoChangeUsernamePlace() {
    globalApplicationState
      .getPlaceChanger()
      .goTo(new ChangeUsername(ClientProperties.DEFAULT_PLACE_TOKEN));
  }

  /**
   * Check for temp username, and prompt for change if user has not set
   */
  public void checkForTempUsername() {
    // get my profile, and check for a default username
    UserProfile userProfile = authenticationController.getCurrentUserProfile();
    if (
      userProfile != null &&
      DisplayUtils.isTemporaryUsername(userProfile.getUserName())
    ) {
      gotoChangeUsernamePlace();
    } else {
      goToLastPlace();
    }
  }

  @Override
  public String mayStop() {
    view.clear();
    return null;
  }

  @Override
  public void goTo(Place place) {
    globalApplicationState.getPlaceChanger().goTo(place);
  }

  @Override
  public void goToLastPlace() {
    view.hideLoggingInLoader();
    Place defaultPlace = new Profile(
      authenticationController.getCurrentUserPrincipalId(),
      ProfileArea.PROJECTS
    );
    globalApplicationState.gotoLastPlace(defaultPlace);
  }

  public void showTermsOfUse(boolean isSigned) {
    synAlert.clear();
    view.hideLoggingInLoader();
    view.showTermsOfUse(isSigned);
  }

  public void userAuthenticated() {
    view.hideLoggingInLoader();
    // the user should be logged in now.
    if (authenticationController.getCurrentUserAccessToken() == null) {
      view.showErrorMessage(
        "An error occurred during login. Please try logging in again."
      );
      view.showLogin();
    } else {
      checkForTempUsername();
    }
  }
}
