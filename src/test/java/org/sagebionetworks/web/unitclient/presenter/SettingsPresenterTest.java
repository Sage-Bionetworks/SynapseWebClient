package org.sagebionetworks.web.unitclient.presenter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
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
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.repo.model.auth.Session;
import org.sagebionetworks.repo.model.storage.StorageUsageSummary;
import org.sagebionetworks.repo.model.storage.StorageUsageSummaryList;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.UserAccountServiceAsync;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.presenter.SettingsPresenter;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.view.SettingsView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.profile.UserProfileModalWidget;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class SettingsPresenterTest {
	
	private static final String APIKEY = "MYAPIKEY";
	private static final String APIKEY2 = "MYAPIKEY2";
	SettingsPresenter profilePresenter;
	SettingsView mockView;
	AuthenticationController mockAuthenticationController;
	UserAccountServiceAsync mockUserService;
	GlobalApplicationState mockGlobalApplicationState;
	PlaceChanger mockPlaceChanger;	
	CookieProvider mockCookieProvider;
	SynapseClientAsync mockSynapseClient;
	GWTWrapper mockGWT;
	PortalGinInjector mockInjector;
	SynapseAlert mockSynAlert;
	UserProfileModalWidget mockUserProfileModalWidget;
	UserSessionData testUser = new UserSessionData();
	UserProfile profile = new UserProfile();
	String password = "password";
	String newPassword = "otherpassword";
	String username = "testuser";
	String email = "testuser@test.com";
	AdapterFactory adapterFactory = new AdapterFactoryImpl();
	
	@Before
	public void setup() throws JSONObjectAdapterException{
		mockView = mock(SettingsView.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockUserService = mock(UserAccountServiceAsync.class);
		mockPlaceChanger = mock(PlaceChanger.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockCookieProvider = mock(CookieProvider.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockGWT = mock(GWTWrapper.class);
		mockInjector = mock(PortalGinInjector.class);
		mockSynAlert = mock(SynapseAlert.class);
		mockUserProfileModalWidget = mock(UserProfileModalWidget.class);
		when(mockInjector.getSynapseAlertWidget()).thenReturn(mockSynAlert);
		
		profilePresenter = new SettingsPresenter(mockView, mockAuthenticationController, mockUserService, mockGlobalApplicationState, mockSynapseClient, mockGWT, mockInjector, mockUserProfileModalWidget);	
		verify(mockView).setPresenter(profilePresenter);
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		when(mockAuthenticationController.getCurrentUserSessionData()).thenReturn(testUser);
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		AsyncMockStubber.callSuccessWith(APIKEY).when(mockSynapseClient).getAPIKey(any(AsyncCallback.class));

		AsyncMockStubber.callSuccessWith(profile).when(mockSynapseClient).getUserProfile(anyString(), any(AsyncCallback.class));
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
		testUser.setProfile(profile);
		testUser.setSession(new Session());
		testUser.getSession().setSessionToken("token");
		testUser.setIsSSO(false);
	}
	
	@Test
	public void testResetPassword() throws RestServiceException {
		AsyncMockStubber.callSuccessWith(testUser).when(mockAuthenticationController).loginUser(eq(username), eq(password), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(null).when(mockUserService).changePassword(anyString(), eq(newPassword), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(testUser).when(mockAuthenticationController).loginUser(eq(username), eq(newPassword), any(AsyncCallback.class));
		
		profilePresenter.resetPassword(password, newPassword);
		verify(mockView).showPasswordChangeSuccess();		
	}
	
	@Test
	public void testResetPasswordFailInitialLogin() throws RestServiceException {		
		AsyncMockStubber.callFailureWith(null).when(mockAuthenticationController).loginUser(eq(username), eq(password), any(AsyncCallback.class));
		
		profilePresenter.resetPassword(password, newPassword);
		verify(mockView).passwordChangeFailed(anyString());		
	}

	@Test
	public void testResetPasswordFailChangePw() throws RestServiceException {		
		AsyncMockStubber.callSuccessWith(testUser).when(mockAuthenticationController).loginUser(eq(username), eq(password), any(AsyncCallback.class));
		AsyncMockStubber.callFailureWith(null).when(mockUserService).changePassword(anyString(), eq(newPassword), any(AsyncCallback.class));
		
		profilePresenter.resetPassword(password, newPassword);
		verify(mockView).passwordChangeFailed(anyString());		
	}
	
	@Test
	public void testResetPasswordFailFinalLogin() throws RestServiceException {		
		AsyncMockStubber.callSuccessWith(testUser).when(mockAuthenticationController).loginUser(eq(username), eq(password), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(null).when(mockUserService).changePassword(anyString(), eq(newPassword), any(AsyncCallback.class));
		AsyncMockStubber.callFailureWith(new Exception()).when(mockAuthenticationController).loginUser(eq(username), eq(newPassword), any(AsyncCallback.class));
		
		profilePresenter.resetPassword(password, newPassword);
		verify(mockView).showPasswordChangeSuccess();
		verify(mockPlaceChanger).goTo(any(LoginPlace.class));		
	}
	
	@Test
	public void testUsage() throws RestServiceException, JSONObjectAdapterException {
		StorageUsageSummaryList usageSummary = new StorageUsageSummaryList();
		Long totalSize = 12345l;
		usageSummary.setTotalSize(totalSize);
		usageSummary.setTotalCount(54321L);
		usageSummary.setSummaryList(new ArrayList<StorageUsageSummary>());
		
		AsyncMockStubber.callSuccessWith(usageSummary).when(mockUserService).getStorageUsage(any(AsyncCallback.class));		
		profilePresenter.updateUserStorage();
		verify(mockView).updateStorageUsage(eq(totalSize));
	}
	@Test
	public void testUsageFailure() throws RestServiceException {
		AsyncMockStubber.callFailureWith(new Exception()).when(mockUserService).getStorageUsage(any(AsyncCallback.class));
		profilePresenter.updateUserStorage();
		verify(mockView).clearStorageUsageUI();
	}
	
	//if notification settings are null, should still successfully update with user specified notification setting
	public void testUpdateMyNotificationSettingsLazyInstantiation() throws JSONObjectAdapterException {
		//creates new UserProfile notification settings
		boolean sendEmailNotifications = true;
		boolean markEmailedMessagesAsRead = true;
		assertNull(profile.getNotificationSettings());
		profilePresenter.updateMyNotificationSettings(sendEmailNotifications, markEmailedMessagesAsRead);
		
		ArgumentCaptor<UserProfile> argument = ArgumentCaptor.forClass(UserProfile.class);
		//should have called updateUserProfile
		verify(mockSynapseClient).updateUserProfile(argument.capture(), any(AsyncCallback.class));
		//with our new notification settings
		UserProfile updatedProfile = argument.getValue();
		assertNotNull(updatedProfile.getNotificationSettings());
		assertEquals(sendEmailNotifications, updatedProfile.getNotificationSettings().getSendEmailNotifications());
		assertEquals(markEmailedMessagesAsRead, updatedProfile.getNotificationSettings().getMarkEmailedMessagesAsRead());
		verify(mockView).showInfo(eq(DisplayConstants.UPDATED_NOTIFICATION_SETTINGS), anyString());
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
		profilePresenter.updateMyNotificationSettings(sendEmailNotifications, markEmailedMessagesAsRead);
		
		ArgumentCaptor<UserProfile> argument = ArgumentCaptor.forClass(UserProfile.class);
		//should have called updateUserProfile
		verify(mockSynapseClient).updateUserProfile(argument.capture(), any(AsyncCallback.class));
		//with our new notification settings
		UserProfile updatedProfile = argument.getValue();
		assertEquals(sendEmailNotifications, updatedProfile.getNotificationSettings().getSendEmailNotifications());
		assertEquals(markEmailedMessagesAsRead, updatedProfile.getNotificationSettings().getMarkEmailedMessagesAsRead());
		verify(mockView).showInfo(eq(DisplayConstants.UPDATED_NOTIFICATION_SETTINGS), anyString());
	}
	
	@Test
	public void testUpdateMyNotificationSettingsFailure() throws JSONObjectAdapterException {
		AsyncMockStubber.callFailureWith(new Exception("unexpected exception")).when(mockSynapseClient).updateUserProfile(any(UserProfile.class), any(AsyncCallback.class));
		profilePresenter.updateMyNotificationSettings(true, true);
		verify(mockSynapseClient).updateUserProfile(any(UserProfile.class), any(AsyncCallback.class));
		verify(mockView).showErrorMessage(anyString());
	}
	
	@Test
	public void testGetUserNotificationEmail() throws JSONObjectAdapterException {
		profilePresenter.getUserNotificationEmail();
		verify(mockSynapseClient).getNotificationEmail(any(AsyncCallback.class));
		verify(mockView).showNotificationEmailAddress(eq(email));
	}
	
	@Test
	public void testGetUserNotificationEmailFailure() throws JSONObjectAdapterException {
		Exception caught = new Exception("unexpected exception");
		AsyncMockStubber.callFailureWith(caught).when(mockSynapseClient).getNotificationEmail(any(AsyncCallback.class));
		profilePresenter.getUserNotificationEmail();
		verify(mockSynapseClient).getNotificationEmail(any(AsyncCallback.class));
		verify(mockSynAlert).handleException(caught);
	}
	
	@Test
	public void testSetUserNotificationEmail() throws JSONObjectAdapterException {
		profilePresenter.setUserNotificationEmail(email);
		verify(mockSynapseClient).setNotificationEmail(eq(email), any(AsyncCallback.class));
		//reload profile
		verify(mockPlaceChanger).goTo(any(Profile.class));
	}
	
	@Test
	public void testSetUserNotificationEmailFailure() throws JSONObjectAdapterException {
		Exception caught = new Exception("unexpected exception");
		AsyncMockStubber.callFailureWith(caught).when(mockSynapseClient).setNotificationEmail(anyString(), any(AsyncCallback.class));
		profilePresenter.setUserNotificationEmail(email);
		verify(mockSynapseClient).setNotificationEmail(anyString(), any(AsyncCallback.class));
		verify(mockSynAlert).handleException(caught);

	}
	
	@Test
	public void testAdditionalEmailValidation() throws JSONObjectAdapterException {
		profilePresenter.additionalEmailValidation(email);
		verify(mockSynapseClient).additionalEmailValidation(anyString(), anyString(), anyString(), any(AsyncCallback.class));
		verify(mockView).showEmailChangeSuccess(anyString());
	}
	
	@Test
	public void testAdditionalEmailValidationFailure() throws JSONObjectAdapterException {
		AsyncMockStubber.callFailureWith(new Exception("unexpected exception")).when(mockSynapseClient).additionalEmailValidation(anyString(), anyString(), anyString(), any(AsyncCallback.class));
		profilePresenter.additionalEmailValidation(email);
		verify(mockSynapseClient).additionalEmailValidation(anyString(), anyString(), anyString(), any(AsyncCallback.class));
		verify(mockView).showEmailChangeFailed(anyString());
	}
	
	@Test (expected=IllegalStateException.class)
	public void testAddEmailNullEmails(){
		profile.setEmails(null);
		profilePresenter.addEmail(email);
	}
	
	@Test (expected=IllegalStateException.class)
	public void testAddEmailEmptyEmails(){
		profile.setEmails(new ArrayList());
		profilePresenter.addEmail(email);
	}
	
	@Test
	public void testAddEmailNewEmail(){
		String email2 = "testuser2@test.com";
		profilePresenter.addEmail(email2);
		verify(mockSynapseClient).additionalEmailValidation(anyString(), anyString(), anyString(), any(AsyncCallback.class));
	}
	
	@Test
	public void testAddEmailExistingEmail(){
		profilePresenter.addEmail(email);
		verify(mockSynapseClient).setNotificationEmail(eq(email), any(AsyncCallback.class));
	}
	
	@Test
	public void testGetAPIKey() {
		profilePresenter.getAPIKey();
		verify(mockSynapseClient).getAPIKey(any(AsyncCallback.class));
		verify(mockView).setApiKey(APIKEY);
	}
	@Test
	public void testGetAPIKeyFailure() {
		Exception e = new Exception();
		AsyncMockStubber.callFailureWith(e).when(mockSynapseClient).getAPIKey(any(AsyncCallback.class));
		profilePresenter.getAPIKey();
		verify(mockSynapseClient).getAPIKey(any(AsyncCallback.class));
		verify(mockSynAlert).handleException(e);
	}
	
	@Test
	public void testOnEditProfile() {
		profilePresenter.onEditProfile();
		ArgumentCaptor<Callback> captor = ArgumentCaptor.forClass(Callback.class);
		verify(mockUserProfileModalWidget).showEditProfile(anyString(), captor.capture());
		captor.getValue().invoke();
		verify(mockPlaceChanger).goTo(any(Profile.class));
	}
	
	@Test
	public void testAsWidget() {
		profilePresenter.asWidget();
		verify(mockSynAlert, times(7)).clear();
		verify(mockView).hideAPIKey();
		verify(mockView).asWidget();
	}
	
	@Test
	public void testConfirmAPIKeyChange(){
		profilePresenter.changeApiKey();
		ArgumentCaptor<ConfirmCallback> captor = ArgumentCaptor.forClass(ConfirmCallback.class);
		verify(mockView).showConfirm(anyString(),  captor.capture());
		
		ConfirmCallback callback = captor.getValue();
		//test not confirmed (user clicked cancel)
		callback.callback(false);
		verify(mockSynapseClient, never()).deleteApiKey(any(AsyncCallback.class));
		verify(mockView, never()).setApiKey(APIKEY2);
		
		callback.callback(true);
		verify(mockSynapseClient).deleteApiKey(any(AsyncCallback.class));
		verify(mockView).setApiKey(APIKEY2);
	}
	
	@Test
	public void testAPIKeyChangeConfirmedFailure(){
		Exception e = new Exception();
		AsyncMockStubber.callFailureWith(e).when(mockSynapseClient).deleteApiKey(any(AsyncCallback.class));
		profilePresenter.changeApiKeyPostConfirmation();
		verify(mockSynapseClient).deleteApiKey(any(AsyncCallback.class));
		verify(mockSynAlert).handleException(e);
	}
}