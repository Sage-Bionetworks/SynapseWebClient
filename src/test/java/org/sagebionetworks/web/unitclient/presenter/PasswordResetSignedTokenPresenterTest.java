package org.sagebionetworks.web.unitclient.presenter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.JoinTeamSignedToken;
import org.sagebionetworks.repo.model.auth.ChangePasswordWithToken;
import org.sagebionetworks.repo.model.auth.PasswordResetSignedToken;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.place.PasswordResetSignedTokenPlace;
import org.sagebionetworks.web.client.presenter.PasswordResetSignedTokenPresenter;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.view.PasswordResetSignedTokenView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

@RunWith(MockitoJUnitRunner.class)
public class PasswordResetSignedTokenPresenterTest {

	PasswordResetSignedTokenPresenter presenter;
	@Mock
	PasswordResetSignedTokenView mockView;
	@Mock
	SynapseClientAsync mockSynapseClient;
	@Mock
	SynapseJavascriptClient mockJsClient;
	@Mock
	SynapseAlert mockSynapseAlert;
	@Mock
	PasswordResetSignedTokenPlace testPlace;
	@Mock
	GlobalApplicationState mockGlobalApplicationState;
	public static final String TEST_TOKEN = "314159bar";
	@Mock
	PlaceChanger mockPlaceChanger;
	@Mock
	AuthenticationController mockAuthenticationController;
	@Captor
	ArgumentCaptor<AsyncCallback> asyncCaptor;
	@Mock
	PasswordResetSignedToken mockPasswordResetSignedToken;
	@Mock
	JoinTeamSignedToken mockJoinTeamSignedToken;
	@Captor
	ArgumentCaptor<ChangePasswordWithToken> changePasswordWithTokenCaptor;

	@Before
	public void setup() {
		presenter = new PasswordResetSignedTokenPresenter(mockView, mockSynapseClient, mockJsClient, mockSynapseAlert, mockAuthenticationController, mockGlobalApplicationState);

		when(testPlace.getToken()).thenReturn(TEST_TOKEN);

		// by default, decode into a PasswordResetSignedToken
		AsyncMockStubber.callSuccessWith(mockPasswordResetSignedToken).when(mockSynapseClient).hexDecodeAndDeserialize(anyString(), any(AsyncCallback.class));
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		AsyncMockStubber.callSuccessWith(null).when(mockJsClient).changePassword(any(ChangePasswordWithToken.class), any(AsyncCallback.class));
	}

	@Test
	public void testConstruction() {
		verify(mockView).setPresenter(presenter);
		verify(mockView).setSynAlertWidget(any(Widget.class));
	}

	@Test
	public void testSetPlacePasswordResetSignedToken() {
		presenter.setPlace(testPlace);

		verify(mockSynapseClient).hexDecodeAndDeserialize(anyString(), any(AsyncCallback.class));
		verify(mockView).clear();
		verify(mockSynapseAlert).clear();
	}

	@Test
	public void testSetPlaceDecodeFailure() {
		Exception ex = new Exception("something bad happened");
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseClient).hexDecodeAndDeserialize(anyString(), any(AsyncCallback.class));

		presenter.setPlace(testPlace);

		verify(mockSynapseClient).hexDecodeAndDeserialize(anyString(), any(AsyncCallback.class));
		verify(mockSynapseAlert).handleException(ex);
	}

	@Test
	public void testSetPlaceInvalidSignedTokenType() {
		AsyncMockStubber.callSuccessWith(mockJoinTeamSignedToken).when(mockSynapseClient).hexDecodeAndDeserialize(anyString(), any(AsyncCallback.class));

		presenter.setPlace(testPlace);

		verify(mockSynapseClient).hexDecodeAndDeserialize(anyString(), any(AsyncCallback.class));
		verify(mockView).clear();
		verify(mockSynapseAlert).showError(PasswordResetSignedTokenPresenter.INVALID_PASSWORD_RESET_SIGNED_TOKEN);
	}

	@Test
	public void testChangePasswordSuccess() {
		String newPassword = "new password";
		when(mockView.getPassword1Field()).thenReturn(newPassword);
		when(mockView.getPassword2Field()).thenReturn(newPassword);

		presenter.setPlace(testPlace);
		presenter.onChangePassword();

		verify(mockJsClient).changePassword(changePasswordWithTokenCaptor.capture(), any(AsyncCallback.class));
		ChangePasswordWithToken changePasswordWithTokenRequest = changePasswordWithTokenCaptor.getValue();
		assertEquals(newPassword, changePasswordWithTokenRequest.getNewPassword());
		assertEquals(mockPasswordResetSignedToken, changePasswordWithTokenRequest.getPasswordChangeToken());
		verify(mockView).showPasswordChangeSuccess();
		verify(mockView).setChangePasswordEnabled(true);
		verify(mockPlaceChanger).goTo(any(LoginPlace.class));
	}

	@Test
	public void testChangePasswordField1Undefined() {
		when(mockView.getPassword1Field()).thenReturn(null);
		when(mockView.getPassword2Field()).thenReturn("defined");

		presenter.onChangePassword();

		verify(mockSynapseAlert).showError(DisplayConstants.ERROR_ALL_FIELDS_REQUIRED);
	}

	@Test
	public void testChangePasswordField2Undefined() {
		when(mockView.getPassword1Field()).thenReturn("defined");
		when(mockView.getPassword2Field()).thenReturn("");

		presenter.onChangePassword();

		verify(mockSynapseAlert).showError(DisplayConstants.ERROR_ALL_FIELDS_REQUIRED);
	}

	@Test
	public void testChangePasswordFieldsMismatch() {
		when(mockView.getPassword1Field()).thenReturn("password1");
		when(mockView.getPassword2Field()).thenReturn("password2");

		presenter.onChangePassword();

		verify(mockSynapseAlert).showError(DisplayConstants.PASSWORDS_MISMATCH);
	}



}
