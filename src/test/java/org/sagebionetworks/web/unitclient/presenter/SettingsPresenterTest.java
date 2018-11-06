package org.sagebionetworks.web.unitclient.presenter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.gwtbootstrap3.extras.bootbox.client.callback.ConfirmCallback;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.repo.model.auth.Session;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.UserAccountServiceAsync;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.presenter.SettingsPresenter;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.view.SettingsView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.login.PasswordStrengthWidget;
import org.sagebionetworks.web.client.widget.profile.EmailAddressesWidget;
import org.sagebionetworks.web.client.widget.profile.UserProfileModalWidget;
import org.sagebionetworks.web.client.widget.subscription.SubscriptionListWidget;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public class SettingsPresenterTest {
	
	private static final String APIKEY = "MYAPIKEY";
	private static final String APIKEY2 = "MYAPIKEY2";
	SettingsPresenter presenter;
	SettingsView mockView;
	AuthenticationController mockAuthenticationController;
	UserAccountServiceAsync mockUserService;
	GlobalApplicationState mockGlobalApplicationState;
	PlaceChanger mockPlaceChanger;
	SynapseClientAsync mockSynapseClient;
	GWTWrapper mockGWT;
	PortalGinInjector mockInjector;
	SynapseAlert mockSynAlert;
	UserProfileModalWidget mockUserProfileModalWidget;
	UserProfile profile = new UserProfile();
	String password = "password";
	String newPassword = "otherpassword";
	String username = "testuser";
	String email = "testuser@test.com";
	AdapterFactory adapterFactory = new AdapterFactoryImpl();
	
	@Mock
	SubscriptionListWidget mockSubscriptionListWidget;
	@Mock
	PasswordStrengthWidget mockPasswordStrengthWidget;
	@Mock
	EmailAddressesWidget mockEmailAddressesWidget;
	@Mock
	SynapseJavascriptClient mockSynapseJavascriptClient;
	@Before
	public void setup() throws JSONObjectAdapterException{
		MockitoAnnotations.initMocks(this);
		mockView = mock(SettingsView.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockUserService = mock(UserAccountServiceAsync.class);
		mockPlaceChanger = mock(PlaceChanger.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockGWT = mock(GWTWrapper.class);
		mockInjector = mock(PortalGinInjector.class);
		mockSynAlert = mock(SynapseAlert.class);
		mockUserProfileModalWidget = mock(UserProfileModalWidget.class);
		when(mockInjector.getSynapseAlertWidget()).thenReturn(mockSynAlert);
		
		presenter = new SettingsPresenter(
				mockView, 
				mockAuthenticationController, 
				mockUserService, 
				mockGlobalApplicationState, 
				mockSynapseClient, 
				mockInjector, 
				mockUserProfileModalWidget, 
				mockSubscriptionListWidget,
				mockPasswordStrengthWidget, 
				mockEmailAddressesWidget, 
				mockSynapseJavascriptClient);	
		verify(mockView).setPresenter(presenter);
		verify(mockView).setSubscriptionsListWidget(any(Widget.class));
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		when(mockAuthenticationController.getCurrentUserProfile()).thenReturn(profile);
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		AsyncMockStubber.callSuccessWith(APIKEY).when(mockSynapseClient).getAPIKey(any(AsyncCallback.class));

		AsyncMockStubber.callSuccessWith(profile).when(mockSynapseJavascriptClient).getUserProfile(anyString(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseClient).updateUserProfile(any(UserProfile.class), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(email).when(mockSynapseClient).getNotificationEmail(any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseClient).setNotificationEmail(anyString(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseClient).additionalEmailValidation(anyString(), anyString(), anyString(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(APIKEY2).when(mockSynapseClient).deleteApiKey(any(AsyncCallback.class));
		
		profile.setDisplayName("tester");
		profile.setEmail(username);
		profile.setUserName(username);
		List<String> emails = new ArrayList<String>();
		emails.add(email);
		profile.setEmails(emails);
	}
	
	@Test
	public void testResetPassword() throws RestServiceException {
		AsyncMockStubber.callSuccessWith(profile).when(mockAuthenticationController).loginUser(eq(username), eq(password), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(null).when(mockUserService).changePassword(anyString(), eq(newPassword), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(profile).when(mockAuthenticationController).loginUser(eq(username), eq(newPassword), any(AsyncCallback.class));
		
		presenter.resetPassword(password, newPassword);
		verify(mockView).showPasswordChangeSuccess();
		verify(mockPasswordStrengthWidget).setVisible(false);
	}
	
	@Test
	public void testResetPasswordFailInitialLogin() throws RestServiceException {		
		AsyncMockStubber.callFailureWith(null).when(mockAuthenticationController).loginUser(eq(username), eq(password), any(AsyncCallback.class));
		
		presenter.resetPassword(password, newPassword);
		verify(mockSynAlert).showError("Incorrect password. Please enter your existing Synapse password.");
		verify(mockView).setCurrentPasswordInError(true);
	}

	@Test
	public void testResetPasswordFailChangePw() throws RestServiceException {		
		AsyncMockStubber.callSuccessWith(profile).when(mockAuthenticationController).loginUser(eq(username), eq(password), any(AsyncCallback.class));
		Exception ex = new Exception("pw change failed");
		AsyncMockStubber.callFailureWith(ex).when(mockUserService).changePassword(anyString(), eq(newPassword), any(AsyncCallback.class));
		
		presenter.resetPassword(password, newPassword);
		verify(mockSynAlert).clear();
		verify(mockSynAlert).handleException(ex);
	}
	
	@Test
	public void testResetPasswordFailFinalLogin() throws RestServiceException {		
		AsyncMockStubber.callSuccessWith(profile).when(mockAuthenticationController).loginUser(eq(username), eq(password), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(null).when(mockUserService).changePassword(anyString(), eq(newPassword), any(AsyncCallback.class));
		AsyncMockStubber.callFailureWith(new Exception()).when(mockAuthenticationController).loginUser(eq(username), eq(newPassword), any(AsyncCallback.class));
		
		presenter.resetPassword(password, newPassword);
		verify(mockView).showPasswordChangeSuccess();
		verify(mockPlaceChanger).goTo(isA(LoginPlace.class));		
	}
	
	//if notification settings are null, should still successfully update with user specified notification setting
	public void testUpdateMyNotificationSettingsLazyInstantiation() throws JSONObjectAdapterException {
		//creates new UserProfile notification settings
		boolean sendEmailNotifications = true;
		boolean markEmailedMessagesAsRead = true;
		assertNull(profile.getNotificationSettings());
		presenter.updateMyNotificationSettings(sendEmailNotifications, markEmailedMessagesAsRead);
		
		ArgumentCaptor<UserProfile> argument = ArgumentCaptor.forClass(UserProfile.class);
		//should have called updateUserProfile
		verify(mockSynapseClient).updateUserProfile(argument.capture(), any(AsyncCallback.class));
		//with our new notification settings
		UserProfile updatedProfile = argument.getValue();
		assertNotNull(updatedProfile.getNotificationSettings());
		assertEquals(sendEmailNotifications, updatedProfile.getNotificationSettings().getSendEmailNotifications());
		assertEquals(markEmailedMessagesAsRead, updatedProfile.getNotificationSettings().getMarkEmailedMessagesAsRead());
		verify(mockView).showInfo(eq(DisplayConstants.UPDATED_NOTIFICATION_SETTINGS));
	}
	
	@Test
	public void testUpdateMyNotificationSettings() throws JSONObjectAdapterException {
		//updates existing UserProfile notification settings
		boolean sendEmailNotifications = false;
		boolean markEmailedMessagesAsRead = false;
		
		org.sagebionetworks.repo.model.message.Settings notificationSettings = new org.sagebionetworks.repo.model.message.Settings();
		notificationSettings.setMarkEmailedMessagesAsRead(true);
		notificationSettings.setSendEmailNotifications(true);
		profile.setNotificationSettings(notificationSettings);
		assertNotNull(profile.getNotificationSettings());
		presenter.updateMyNotificationSettings(sendEmailNotifications, markEmailedMessagesAsRead);
		
		ArgumentCaptor<UserProfile> argument = ArgumentCaptor.forClass(UserProfile.class);
		//should have called updateUserProfile
		verify(mockSynapseClient).updateUserProfile(argument.capture(), any(AsyncCallback.class));
		//with our new notification settings
		UserProfile updatedProfile = argument.getValue();
		assertEquals(sendEmailNotifications, updatedProfile.getNotificationSettings().getSendEmailNotifications());
		assertEquals(markEmailedMessagesAsRead, updatedProfile.getNotificationSettings().getMarkEmailedMessagesAsRead());
		verify(mockView).showInfo(eq(DisplayConstants.UPDATED_NOTIFICATION_SETTINGS));
	}
	
	@Test
	public void testUpdateMyNotificationSettingsFailure() throws JSONObjectAdapterException {
		Exception ex = new Exception("unexpected exception");
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseClient).updateUserProfile(any(UserProfile.class), any(AsyncCallback.class));
		presenter.updateMyNotificationSettings(true, true);
		verify(mockSynapseClient).updateUserProfile(any(UserProfile.class), any(AsyncCallback.class));
		verify(mockSynAlert).handleException(ex);
	}
	
	@Test
	public void testGetAPIKey() {
		presenter.getAPIKey();
		verify(mockSynapseClient).getAPIKey(any(AsyncCallback.class));
		verify(mockView).setApiKey(APIKEY);

		//verify not cached
		presenter.getAPIKey();
		verify(mockSynapseClient, times(2)).getAPIKey(any(AsyncCallback.class));
	}
	@Test
	public void testGetAPIKeyFailure() {
		Exception e = new Exception();
		AsyncMockStubber.callFailureWith(e).when(mockSynapseClient).getAPIKey(any(AsyncCallback.class));
		presenter.getAPIKey();
		verify(mockSynapseClient).getAPIKey(any(AsyncCallback.class));
		verify(mockSynAlert).handleException(e);
	}
	
	@Test
	public void testOnEditProfile() {
		presenter.onEditProfile();
		ArgumentCaptor<Callback> captor = ArgumentCaptor.forClass(Callback.class);
		verify(mockUserProfileModalWidget).showEditProfile(anyString(), captor.capture());
		captor.getValue().invoke();
		verify(mockPlaceChanger).goTo(isA(Profile.class));
	}
	
	@Test
	public void testConfigure() {
		when(mockGlobalApplicationState.isShowingUTCTime()).thenReturn(false);
		presenter.configure();
		verify(mockSynAlert, times(3)).clear();
		verify(mockPasswordStrengthWidget).setVisible(false);
		verify(mockView).clear();
		verify(mockSubscriptionListWidget).configure();
		verify(mockView).updateNotificationCheckbox(profile);
		verify(mockAuthenticationController).updateCachedProfile(profile);
		verify(mockView).setShowingLocalTime();
	}
	
	@Test
	public void testConfigureIsShowingUTCTime() {
		when(mockGlobalApplicationState.isShowingUTCTime()).thenReturn(true);
		presenter.configure();
		verify(mockView).setShowingUTCTime();
	}
	
	@Test
	public void testConfigureFailure() {
		Exception ex = new Exception("error occurred");
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseJavascriptClient).getUserProfile(anyString(), any(AsyncCallback.class));
		presenter.configure();
		verify(mockView).clear();
		verify(mockSubscriptionListWidget).configure();
		verify(mockSynAlert).handleException(ex);
	}
	
	@Test
	public void testAsWidget() {
		presenter.asWidget();
		verify(mockView).asWidget();
	}
	
	@Test
	public void testConfigureAnonymousSWC2943() {
		//used to result in NPE before fix for SWC-2943
		when(mockAuthenticationController.isLoggedIn()).thenReturn(false);
		when(mockAuthenticationController.getCurrentUserProfile()).thenReturn(null);
		presenter.configure();
		verify(mockView).clear();
	}
	
	@Test
	public void testConfirmAPIKeyChange(){
		presenter.changeApiKey();
		ArgumentCaptor<Callback> captor = ArgumentCaptor.forClass(Callback.class);
		verify(mockView).showConfirm(anyString(),  captor.capture());
		
		Callback callback = captor.getValue();
		verify(mockSynapseClient, never()).deleteApiKey(any(AsyncCallback.class));
		verify(mockView, never()).setApiKey(APIKEY2);
		
		callback.invoke();
		verify(mockSynapseClient).deleteApiKey(any(AsyncCallback.class));
		verify(mockView).setApiKey(APIKEY2);
	}
	
	@Test
	public void testAPIKeyChangeConfirmedFailure(){
		Exception e = new Exception();
		AsyncMockStubber.callFailureWith(e).when(mockSynapseClient).deleteApiKey(any(AsyncCallback.class));
		presenter.changeApiKeyPostConfirmation();
		verify(mockSynapseClient).deleteApiKey(any(AsyncCallback.class));
		verify(mockSynAlert).handleException(e);
	}
	
	@Test
	public void testChangePasswordCurrentPasswordFailure() {
		when(mockView.getCurrentPasswordField()).thenReturn("");
		presenter.changePassword();
		verify(mockView).getCurrentPasswordField();
		verify(mockView).getPassword1Field();
		verify(mockView).getPassword2Field();
		verify(mockSynAlert).showError(DisplayConstants.ERROR_ALL_FIELDS_REQUIRED);
		verify(mockView).setCurrentPasswordInError(true);
	}
	
	@Test
	public void testChangePasswordPassword1Failure() {
		when(mockView.getCurrentPasswordField()).thenReturn(password);
		when(mockView.getPassword1Field()).thenReturn("");
		presenter.changePassword();
		verify(mockView).getCurrentPasswordField();
		verify(mockView).getPassword1Field();
		verify(mockView).getPassword2Field();
		verify(mockSynAlert).showError(DisplayConstants.ERROR_ALL_FIELDS_REQUIRED);
		verify(mockView).setPassword1InError(true);
	}
	
	@Test
	public void testChangePasswordPassword2Failure() {
		// empty second password
		when(mockView.getCurrentPasswordField()).thenReturn(password);
		when(mockView.getPassword1Field()).thenReturn(newPassword);
		when(mockView.getPassword2Field()).thenReturn("");
		presenter.changePassword();
		verify(mockView).getCurrentPasswordField();
		verify(mockView).getPassword1Field();
		verify(mockView).getPassword2Field();
		verify(mockSynAlert).showError(DisplayConstants.ERROR_ALL_FIELDS_REQUIRED);
		verify(mockView).setPassword2InError(true);
		
		// unmatching second password
		Mockito.reset(mockView);
		when(mockView.getCurrentPasswordField()).thenReturn(password);
		when(mockView.getPassword1Field()).thenReturn(newPassword);
		when(mockView.getPassword2Field()).thenReturn(newPassword + "abc");
		presenter.changePassword();
		verify(mockView).getCurrentPasswordField();
		verify(mockView).getPassword1Field();
		verify(mockView).getPassword2Field();
		verify(mockSynAlert).showError(DisplayConstants.PASSWORDS_MISMATCH);
		verify(mockView).setPassword2InError(true);
	}
	
	@Test
	public void testChangePasswordPasswordSuccess() {
		AsyncMockStubber.callSuccessWith(profile).when(mockAuthenticationController).loginUser(eq(username), eq(password), any(AsyncCallback.class));
		when(mockView.getCurrentPasswordField()).thenReturn(password);
		when(mockView.getPassword1Field()).thenReturn(newPassword);
		when(mockView.getPassword2Field()).thenReturn(newPassword);
		presenter.changePassword();
		verify(mockView).getCurrentPasswordField();
		verify(mockView).getPassword1Field();
		verify(mockView).getPassword2Field();
		verify(mockView).setChangePasswordEnabled(false);
		verify(mockUserService).changePassword(anyString(), anyString(), any(AsyncCallback.class));
	}
	
	@Test
	public void testClearPasswordErrors() {
		presenter.clearPasswordErrors();
		verify(mockSynAlert).clear();
		verify(mockView).setCurrentPasswordInError(false);
		verify(mockView).setPassword1InError(false);
		verify(mockView).setPassword2InError(false);
	}
	
	@Test
	public void testSetShowUTCTime() {
		presenter.setShowUTCTime(true);
		verify(mockGlobalApplicationState).setShowUTCTime(true);
	}
}