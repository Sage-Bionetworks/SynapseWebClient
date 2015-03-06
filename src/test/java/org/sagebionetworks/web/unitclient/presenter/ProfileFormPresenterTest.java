package org.sagebionetworks.web.unitclient.presenter;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.repo.model.auth.Session;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.LinkedInServiceAsync;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.cookie.CookieKeys;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.presenter.ProfileFormWidget;
import org.sagebionetworks.web.client.presenter.ProfileFormWidget.ProfileUpdatedCallback;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.view.ProfileFormView;
import org.sagebionetworks.web.client.widget.upload.FileInputWidget;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class ProfileFormPresenterTest {
	
	ProfileFormWidget profileForm;
	ProfileFormView mockView;
	AuthenticationController mockAuthenticationController;
	GlobalApplicationState mockGlobalApplicationState;
	SynapseClientAsync mockSynapseClient;
	GWTWrapper mockGWTWrapper;
	FileInputWidget mockFileInputWidget;
	JSONObjectAdapter adapter = new JSONObjectAdapterImpl();
	ProfileUpdatedCallback mockProfileUpdatedCallback;
	LinkedInServiceAsync mockLinkedInService;
	CookieProvider mockCookies;
	
	UserSessionData testUser = new UserSessionData();
	UserProfile userProfile = new UserProfile();
	String testUserJson;
	String password = "password";
	AdapterFactory adapterFactory = new AdapterFactoryImpl();
	
	@Before
	public void setup() throws JSONObjectAdapterException {
		mockView = mock(ProfileFormView.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockProfileUpdatedCallback = mock(ProfileUpdatedCallback.class);
		mockGWTWrapper = mock(GWTWrapper.class);
		mockLinkedInService = mock(LinkedInServiceAsync.class);
		mockCookies = mock(CookieProvider.class);
		mockFileInputWidget = Mockito.mock(FileInputWidget.class);
		profileForm = new ProfileFormWidget(mockView, mockAuthenticationController, mockSynapseClient, mockGlobalApplicationState, mockCookies, mockLinkedInService, mockGWTWrapper,mockFileInputWidget);
		profileForm.configure(userProfile, mockProfileUpdatedCallback);
		verify(mockView).setPresenter(profileForm);
		userProfile.writeToJSONObject(adapter.createNew());
		String userProfileJson = adapter.toJSONString();
		when(mockAuthenticationController.getCurrentUserSessionData()).thenReturn(testUser);		
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseClient).updateUserProfile(any(UserProfile.class), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(userProfile).when(mockSynapseClient).getUserProfile(anyString(), any(AsyncCallback.class));
		userProfile.setDisplayName("tester");
		userProfile.setOwnerId("1");
		userProfile.setEmail("original.email@sagebase.org");
		testUser.setProfile(userProfile);
		testUser.setSession(new Session());
		testUser.getSession().setSessionToken("token");
		testUser.setIsSSO(false);
		
		testUser.writeToJSONObject(adapter.createNew());
		testUserJson = adapter.toJSONString(); 
	}
	
	@Test
	public void testUpdateProfile() {
		//modify the last name only
		ArgumentCaptor<AsyncCallback> captor = ArgumentCaptor.forClass(AsyncCallback.class);
		profileForm.updateProfile(userProfile.getFirstName(), 
				userProfile.getLastName() + "_modifiedlastname", 
				userProfile.getSummary(), 
				userProfile.getPosition(), 
				userProfile.getLocation(), 
				userProfile.getIndustry(), 
				userProfile.getCompany(), 
				userProfile.getEmail(), 
				userProfile.getProfilePicureFileHandleId(), 
				userProfile.getTeamName(), 
				userProfile.getUrl(),
				userProfile.getUserName());
		
		verify(mockView).showUserUpdateSuccess();
		verify(mockAuthenticationController).revalidateSession(anyString(), captor.capture());
		//invoke the login callback to verify profile update success callback
		captor.getValue().onSuccess(testUser);
		verify(mockProfileUpdatedCallback).profileUpdateSuccess();//successful update
	}
	
	@Test
	public void testUpdateProfileFailureGetUserProfile() {
		Exception myException = new Exception("a test exception");
		AsyncMockStubber.callFailureWith(myException).when(mockSynapseClient).getUserProfile(anyString(), any(AsyncCallback.class));
		profileForm.updateProfile(userProfile.getFirstName(), 
				userProfile.getLastName() + "_modifiedlastname", 
				userProfile.getSummary(), 
				userProfile.getPosition(), 
				userProfile.getLocation(), 
				userProfile.getIndustry(), 
				userProfile.getCompany(), 
				userProfile.getEmail(), userProfile.getProfilePicureFileHandleId(), null, null, userProfile.getUserName());
		
		verify(mockProfileUpdatedCallback).onFailure(eq(myException));//exception is thrown back
	}
	
	@Test
	public void testUpdateProfileFailureUpdateUserProfile() {
		Exception myException = new Exception("another test exception");
		AsyncMockStubber.callFailureWith(myException).when(mockSynapseClient).updateUserProfile(any(UserProfile.class), any(AsyncCallback.class));
		profileForm.updateProfile(userProfile.getFirstName(), 
				userProfile.getLastName() + "_modifiedlastname", 
				userProfile.getSummary(), 
				userProfile.getPosition(), 
				userProfile.getLocation(), 
				userProfile.getIndustry(), 
				userProfile.getCompany(), 
				userProfile.getEmail(), userProfile.getProfilePicureFileHandleId(), null, null, userProfile.getUserName());
		
		verify(mockProfileUpdatedCallback).onFailure(eq(myException));//exception is thrown back
	}
	
	@Test
	public void testRedirectToLinkedIn() {
		profileForm.redirectToLinkedIn();
		verify(mockLinkedInService).returnAuthUrl(anyString(), any(AsyncCallback.class));
	}
	
	@Test
	public void testUpdateProfileWithLinkedIn() {
		String secret = "secret";
		when(mockCookies.getCookie(CookieKeys.LINKEDIN)).thenReturn(secret);
		String requestToken = "token";
		String verifier = "12345";
		profileForm.updateProfileWithLinkedIn(requestToken, verifier);
		verify(mockLinkedInService).getCurrentUserInfo(eq(requestToken), eq(secret), eq(verifier), anyString(), any(AsyncCallback.class));
	}
	
	@Test
	public void testStartEditingProfile() {
		profileForm.startEditing();
		verify(mockView).setIsDataModified(eq(true));
		verify(mockGlobalApplicationState).setIsEditing(eq(true));
	}
	
	@Test
	public void testStopEditingProfile() {
		profileForm.stopEditing();
		verify(mockView).setIsDataModified(eq(false));
		verify(mockGlobalApplicationState).setIsEditing(eq(false));
	}
	
	@Test
	public void testRollbackProfile() {
		profileForm.rollback();
		verify(mockView).setIsDataModified(eq(false));
		verify(mockGlobalApplicationState).setIsEditing(eq(false));
		verify(mockView, Mockito.times(2)).updateView(any(UserProfile.class));
	}

}