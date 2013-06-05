package org.sagebionetworks.web.unitclient.presenter;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.repo.model.attachment.AttachmentData;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.presenter.ProfileFormPresenter;
import org.sagebionetworks.web.client.presenter.ProfileFormPresenter.ProfileUpdatedCallback;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.view.ProfileFormView;
import org.sagebionetworks.web.shared.EntityWrapper;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class ProfileFormPresenterTest {
	
	ProfileFormPresenter profileForm;
	ProfileFormView mockView;
	AuthenticationController mockAuthenticationController;
	SynapseClientAsync mockSynapseClient;
	NodeModelCreator mockNodeModelCreator;
	GWTWrapper mockGWTWrapper;
	JSONObjectAdapter adapter = new JSONObjectAdapterImpl();
	ProfileUpdatedCallback mockProfileUpdatedCallback;
	
	UserSessionData testUser = new UserSessionData();
	UserProfile userProfile = new UserProfile();
	String testUserJson;
	String password = "password";
	
	@Before
	public void setup() throws JSONObjectAdapterException {
		mockView = mock(ProfileFormView.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockNodeModelCreator = mock(NodeModelCreator.class);
		mockProfileUpdatedCallback = mock(ProfileUpdatedCallback.class);
		mockGWTWrapper = mock(GWTWrapper.class);
		profileForm = new ProfileFormPresenter(mockView, mockAuthenticationController, mockSynapseClient, mockNodeModelCreator, adapter);
		profileForm.configure(userProfile, mockProfileUpdatedCallback);
		verify(mockView).setPresenter(profileForm);
		when(mockNodeModelCreator.createJSONEntity(anyString(), any(Class.class))).thenReturn(userProfile);
		when(mockAuthenticationController.getLoggedInUser()).thenReturn(testUser);
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseClient).updateUserProfile(anyString(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith("").when(mockSynapseClient).getUserProfile(anyString(), any(AsyncCallback.class));
		userProfile.setDisplayName("tester");
		userProfile.setOwnerId("1");
		userProfile.setEmail("original.email@sagebase.org");
		testUser.setProfile(userProfile);
		testUser.setSessionToken("token");
		testUser.setIsSSO(false);
		
		JSONObjectAdapter adapter = new JSONObjectAdapterImpl().createNew();
		testUser.writeToJSONObject(adapter);
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
				userProfile.getUrl());
		
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
				userProfile.getEmail(), userProfile.getPic(), null, null);
		
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
				userProfile.getEmail(), userProfile.getPic(), null, null);
		
		verify(mockProfileUpdatedCallback).onFailure(eq(myException));//exception is thrown back
	}
	
}