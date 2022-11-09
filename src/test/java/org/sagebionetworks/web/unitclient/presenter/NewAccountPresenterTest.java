package org.sagebionetworks.web.unitclient.presenter;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.principal.AccountCreationToken;
import org.sagebionetworks.repo.model.principal.AliasType;
import org.sagebionetworks.repo.model.principal.EmailValidationSignedToken;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.UserAccountServiceAsync;
import org.sagebionetworks.web.client.place.NewAccount;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.place.users.RegisterAccount;
import org.sagebionetworks.web.client.presenter.NewAccountPresenter;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.view.NewAccountView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

public class NewAccountPresenterTest {

  NewAccountPresenter newAccountPresenter;
  NewAccountView mockView;
  UserAccountServiceAsync mockUserService;
  GlobalApplicationState mockGlobalApplicationState;
  RegisterAccount place = Mockito.mock(RegisterAccount.class);
  SynapseClientAsync mockSynapseClient;
  AuthenticationController mockAuthController;
  PlaceChanger mockPlaceChanger;

  @Mock
  SynapseAlert mockSynAlert;

  String testSessionToken = "1239381foobar";

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
    mockView = mock(NewAccountView.class);

    mockUserService = mock(UserAccountServiceAsync.class);
    mockGlobalApplicationState = mock(GlobalApplicationState.class);
    mockSynapseClient = mock(SynapseClientAsync.class);
    mockAuthController = mock(AuthenticationController.class);
    mockPlaceChanger = mock(PlaceChanger.class);
    newAccountPresenter =
      new NewAccountPresenter(
        mockView,
        mockSynapseClient,
        mockGlobalApplicationState,
        mockUserService,
        mockAuthController,
        mockSynAlert
      );
    verify(mockView).setPresenter(newAccountPresenter);
    verify(mockView).setSynAlert(mockSynAlert);

    AsyncMockStubber
      .callSuccessWith(true)
      .when(mockSynapseClient)
      .isAliasAvailable(
        anyString(),
        eq(AliasType.USER_NAME.toString()),
        any(AsyncCallback.class)
      );
    AsyncMockStubber
      .callSuccessWith(true)
      .when(mockSynapseClient)
      .isAliasAvailable(
        anyString(),
        eq(AliasType.USER_EMAIL.toString()),
        any(AsyncCallback.class)
      );
    when(mockGlobalApplicationState.getPlaceChanger())
      .thenReturn(mockPlaceChanger);

    AsyncMockStubber
      .callSuccessWith(testSessionToken)
      .when(mockUserService)
      .createUserStep2(
        anyString(),
        anyString(),
        anyString(),
        anyString(),
        any(EmailValidationSignedToken.class),
        any(AsyncCallback.class)
      );
  }

  @Test
  public void testSetPlace() {
    reset(mockView);
    when(mockAuthController.isLoggedIn()).thenReturn(true);
    NewAccount newPlace = Mockito.mock(NewAccount.class);
    when(newPlace.toToken()).thenReturn("");
    newAccountPresenter.setPlace(newPlace);
    verify(mockView).setPresenter(newAccountPresenter);
    verify(mockGlobalApplicationState).clearLastPlace();
    verify(mockAuthController).logoutUser();
  }

  @Test
  public void testIsUsernameAvailableTooSmall() {
    // should not check if too short
    newAccountPresenter.checkUsernameAvailable("abc");
    verify(mockSynapseClient, never())
      .isAliasAvailable(
        anyString(),
        eq(AliasType.USER_NAME.toString()),
        any(AsyncCallback.class)
      );
  }

  @Test
  public void testIsUsernameAvailableTrue() {
    newAccountPresenter.checkUsernameAvailable("abcd");
    verify(mockSynapseClient)
      .isAliasAvailable(
        anyString(),
        eq(AliasType.USER_NAME.toString()),
        any(AsyncCallback.class)
      );
    verify(mockView, never()).markUsernameUnavailable();
  }

  @Test
  public void testIsUsernameAvailableFalse() {
    AsyncMockStubber
      .callSuccessWith(false)
      .when(mockSynapseClient)
      .isAliasAvailable(
        anyString(),
        eq(AliasType.USER_NAME.toString()),
        any(AsyncCallback.class)
      );
    newAccountPresenter.checkUsernameAvailable("abcd");
    verify(mockSynapseClient)
      .isAliasAvailable(
        anyString(),
        eq(AliasType.USER_NAME.toString()),
        any(AsyncCallback.class)
      );
    verify(mockView).markUsernameUnavailable();
  }

  @Test
  public void testIsEmailAvailableTrue() {
    newAccountPresenter.checkEmailAvailable("abcd@efg.com");
    verify(mockSynapseClient)
      .isAliasAvailable(
        anyString(),
        eq(AliasType.USER_EMAIL.toString()),
        any(AsyncCallback.class)
      );
  }

  @Test
  public void testIsEmailAvailableFalse() {
    AsyncMockStubber
      .callSuccessWith(false)
      .when(mockSynapseClient)
      .isAliasAvailable(
        anyString(),
        eq(AliasType.USER_EMAIL.toString()),
        any(AsyncCallback.class)
      );
    newAccountPresenter.checkEmailAvailable("abcd@efg.com");
    verify(mockSynapseClient)
      .isAliasAvailable(
        anyString(),
        eq(AliasType.USER_EMAIL.toString()),
        any(AsyncCallback.class)
      );
    // attempts to go to the last place
    verify(mockGlobalApplicationState).gotoLastPlace();
  }

  @Test
  public void testCompleteRegistrationAccountCreationToken() {
    String firstName = "   Mara  ";
    String lastName = " Jade     ";
    String userName = "skywalker290";
    String password = "  farfaraway";
    final AccountCreationToken accountCreationToken = new AccountCreationToken();
    accountCreationToken.setEmailValidationSignedToken(
      new EmailValidationSignedToken()
    );
    NewAccount newPlace = Mockito.mock(NewAccount.class);
    when(newPlace.toToken()).thenReturn("0123456789ABCDEF");
    AsyncMockStubber
      .callSuccessWith(accountCreationToken)
      .when(mockSynapseClient)
      .hexDecodeAndDeserializeAccountCreationToken(
        anyString(),
        any(AsyncCallback.class)
      );
    newAccountPresenter.setPlace(newPlace);
    newAccountPresenter.completeRegistration(
      userName,
      firstName,
      lastName,
      password
    );
    verify(mockView).setLoading(true);
    verify(mockView).setLoading(false);
    verify(mockUserService)
      .createUserStep2(
        eq(userName),
        eq(firstName.trim()),
        eq(lastName.trim()),
        eq(password),
        eq(accountCreationToken.getEmailValidationSignedToken()),
        any(AsyncCallback.class)
      );

    ArgumentCaptor<AsyncCallback<UserProfile>> captor = new ArgumentCaptor<AsyncCallback<UserProfile>>();
    verify(mockAuthController)
      .setNewAccessToken(eq(testSessionToken), captor.capture());
    AsyncCallback<UserProfile> onSetNewAccessToken = captor.getValue();

    Throwable ex = new Throwable("test message");
    onSetNewAccessToken.onFailure(ex);
    verify(mockView, times(2)).setLoading(false);
    verify(mockSynAlert).handleException(ex);

    onSetNewAccessToken.onSuccess(new UserProfile());
    verify(mockPlaceChanger).goTo(any(Profile.class));
  }

  @Test
  public void testCompleteRegistrationAccountCreationTokenFailure() {
    String firstName = "   Mara  ";
    String lastName = " Jade     ";
    String userName = "skywalker290";
    String password = "  farfaraway";
    final AccountCreationToken accountCreationToken = new AccountCreationToken();
    accountCreationToken.setEmailValidationSignedToken(
      new EmailValidationSignedToken()
    );
    NewAccount newPlace = Mockito.mock(NewAccount.class);
    when(newPlace.toToken()).thenReturn("0123456789ABCDEF");
    AsyncMockStubber
      .callSuccessWith(accountCreationToken)
      .when(mockSynapseClient)
      .hexDecodeAndDeserializeAccountCreationToken(
        anyString(),
        any(AsyncCallback.class)
      );
    newAccountPresenter.setPlace(newPlace);
    String failureMessage = "test message";
    Throwable ex = new Throwable(failureMessage);
    AsyncMockStubber
      .callFailureWith(ex)
      .when(mockUserService)
      .createUserStep2(
        eq(userName.trim()),
        eq(firstName.trim()),
        eq(lastName.trim()),
        eq(password),
        any(EmailValidationSignedToken.class),
        any(AsyncCallback.class)
      );
    newAccountPresenter.completeRegistration(
      userName,
      firstName,
      lastName,
      password
    );
    verify(mockView).setLoading(false);
    verify(mockSynAlert).handleException(ex);
  }
}
