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
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.presenter.ProfileFormWidget;
import org.sagebionetworks.web.client.presenter.ProfileFormWidget.ProfileUpdatedCallback;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.view.ProfileFormView;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class ProfileFormPresenterTest {
	
	ProfileFormWidget profileForm;
	ProfileFormView mockView;
	AuthenticationController mockAuthenticationController;
	GlobalApplicationState mockGlobalApplicationState;
	SynapseClientAsync mockSynapseClient;
	GWTWrapper mockGWTWrapper;
	JSONObjectAdapter adapter = new JSONObjectAdapterImpl();
	ProfileUpdatedCallback mockProfileUpdatedCallback;
	
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
		profileForm = new ProfileFormWidget(mockView, mockAuthenticationController, mockSynapseClient, adapter, mockGlobalApplicationState, adapterFactory);
		profileForm.configure(userProfile, mockProfileUpdatedCallback);
		verify(mockView).setPresenter(profileForm);
		userProfile.writeToJSONObject(adapter.createNew());
		String userProfileJson = adapter.toJSONString();
		when(mockAuthenticationController.getCurrentUserSessionData()).thenReturn(testUser);		
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseClient).updateUserProfile(anyString(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(userProfileJson).when(mockSynapseClient).getUserProfile(anyString(), any(AsyncCallback.class));
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
				userProfile.getPic(), 
				userProfile.getTeamName(), 
				userProfile.getUrl(),
				userProfile.getUserName());
		
		verify(mockView).showUserUpdateSuccess();
		verify(mockAuthenticationController).loginUser(anyString(), captor.capture());
		//invoke the login callback to verify profile update success callback
		captor.getValue().onSuccess("");
		verify(mockProfileUpdatedCallback).profileUpdateSuccess();//successful update
	}
	
	@Test
	public void testCancelled() {
		profileForm.cancelClicked();
		verify(mockProfileUpdatedCallback).profileUpdateCancelled();//callback with cancel
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
				userProfile.getEmail(), userProfile.getPic(), null, null, userProfile.getUserName());
		
		verify(mockProfileUpdatedCallback).onFailure(eq(myException));//exception is thrown back
	}
	
	@Test
	public void testUpdateProfileFailureUpdateUserProfile() {
		Exception myException = new Exception("another test exception");
		AsyncMockStubber.callFailureWith(myException).when(mockSynapseClient).updateUserProfile(anyString(), any(AsyncCallback.class));
		profileForm.updateProfile(userProfile.getFirstName(), 
				userProfile.getLastName() + "_modifiedlastname", 
				userProfile.getSummary(), 
				userProfile.getPosition(), 
				userProfile.getLocation(), 
				userProfile.getIndustry(), 
				userProfile.getCompany(), 
				userProfile.getEmail(), userProfile.getPic(), null, null, userProfile.getUserName());
		
		verify(mockProfileUpdatedCallback).onFailure(eq(myException));//exception is thrown back
	}
	
}