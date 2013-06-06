package org.sagebionetworks.web.unitclient.widget.entity.renderer;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.sagebionetworks.evaluation.model.UserEvaluationState;
import org.sagebionetworks.repo.model.Page;
import org.sagebionetworks.repo.model.TermsOfUseAccessRequirement;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.repo.model.message.ObjectType;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.client.widget.entity.renderer.JoinWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.JoinWidgetView;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class JoinWidgetTest {
	
	JoinWidget widget;
	JoinWidgetView mockView;
	AuthenticationController mockAuthenticationController;
	Page testPage;
	Map<String, String> descriptor;
	WikiPageKey wikiKey = new WikiPageKey("", ObjectType.ENTITY.toString(), null);
	
	SynapseClientAsync mockSynapseClient;
	AuthenticationController mockAuthController;
	GlobalApplicationState mockGlobalApplicationState;
	PlaceChanger mockPlaceChanger;
	JSONObjectAdapter mockJSONObjectAdapter;
	NodeModelCreator mockNodeModelCreator;
	PaginatedResults<TermsOfUseAccessRequirement> requirements;
	
	@Before
	public void setup() throws Exception{
		mockView = mock(JoinWidgetView.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockAuthController = mock(AuthenticationController.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockPlaceChanger = mock(PlaceChanger.class);
		mockNodeModelCreator = mock(NodeModelCreator.class);
		mockJSONObjectAdapter = mock(JSONObjectAdapter.class);
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		when(mockAuthController.isLoggedIn()).thenReturn(true);
		UserSessionData currentUser = mock(UserSessionData.class);
		UserProfile currentUserProfile = mock(UserProfile.class);
		when(mockAuthController.getLoggedInUser()).thenReturn(currentUser);
		when(currentUser.getProfile()).thenReturn(currentUserProfile);
		when(currentUserProfile.getOwnerId()).thenReturn("1");
		requirements = new PaginatedResults<TermsOfUseAccessRequirement>();
		requirements.setTotalNumberOfResults(0);
		List<TermsOfUseAccessRequirement> ars = new ArrayList<TermsOfUseAccessRequirement>();
		requirements.setResults(ars);
		when(mockNodeModelCreator.createPaginatedResults(anyString(), any(Class.class))).thenReturn(requirements);
		AsyncMockStubber.callSuccessWith(true).when(mockSynapseClient).hasAccess(anyString(), anyString(), anyString(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith("").when(mockSynapseClient).getUnmetEvaluationAccessRequirements(anyString(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(UserEvaluationState.EVAL_REGISTRATION_UNAVAILABLE).when(mockSynapseClient).getUserEvaluationState(anyString(), any(AsyncCallback.class));
		
		widget = new JoinWidget(mockView, mockSynapseClient, mockAuthController, mockGlobalApplicationState,mockNodeModelCreator, mockJSONObjectAdapter);
		descriptor = new HashMap<String, String>();
		descriptor.put(WidgetConstants.JOIN_WIDGET_EVALUATION_ID_KEY, "my eval id");
	}	
	
	
	@Test
	public void testEvaluationStateCheckFailure() throws Exception {
		AsyncMockStubber.callFailureWith(new Exception()).when(mockSynapseClient).getUserEvaluationState(anyString(), any(AsyncCallback.class));
		widget.configure(wikiKey, descriptor);
		verify(mockView).showError(anyString());
	}
	
	@Test
	public void testRegisterStep1NotLoggedIn() throws Exception {
		when(mockAuthController.isLoggedIn()).thenReturn(false);
		AsyncMockStubber.callSuccessWith("my participant json").when(mockSynapseClient).createParticipants(any(String[].class), any(AsyncCallback.class));
		widget.configure(wikiKey, descriptor);
		widget.registerStep1();
		verify(mockView).showAnonymousRegistrationMessage();
	}
	
	@Test
	public void testRegisterStep1LoggedIn() throws Exception {
		AsyncMockStubber.callSuccessWith("my participant json").when(mockSynapseClient).createParticipants(any(String[].class), any(AsyncCallback.class));
		widget.configure(wikiKey, descriptor);
		widget.registerStep1();
		verify(mockView).showProfileForm(any(UserProfile.class), any(AsyncCallback.class));
	}
	
	@Test
	public void testRegisterStep2Success() {
		widget.configure(wikiKey, descriptor);
		widget.registerStep2();
		ArgumentCaptor<AsyncCallback> captor = ArgumentCaptor.forClass(AsyncCallback.class);
		verify(mockView).showProfileForm(any(UserProfile.class), captor.capture());
		captor.getValue().onSuccess(null);
		//should continue to step 3
		verify(mockSynapseClient).getUnmetEvaluationAccessRequirements(anyString(), any(AsyncCallback.class));
	}
	
	@Test
	public void testRegisterStep2Failure() {
		widget.configure(wikiKey, descriptor);
		widget.registerStep2();
		ArgumentCaptor<AsyncCallback> captor = ArgumentCaptor.forClass(AsyncCallback.class);
		verify(mockView).showProfileForm(any(UserProfile.class), captor.capture());
		captor.getValue().onFailure(new Exception());
		//should continue to step 3
		verify(mockSynapseClient).getUnmetEvaluationAccessRequirements(anyString(), any(AsyncCallback.class));
	}
	
	@Test
	public void testRegisterStep3Failure() throws Exception {
		AsyncMockStubber.callFailureWith(new Exception()).when(mockSynapseClient).getUnmetEvaluationAccessRequirements(anyString(), any(AsyncCallback.class));
		widget.configure(wikiKey, descriptor);
		widget.registerStep3(0);
		verify(mockView).showError(anyString());
	}
	@Test 
	public void testRegisterStep3MultipleSubchallenges() throws Exception {
		descriptor.remove(WidgetConstants.JOIN_WIDGET_EVALUATION_ID_KEY);
		descriptor.put(WidgetConstants.JOIN_WIDGET_SUBCHALLENGE_ID_LIST_KEY, "evalId1, evalId2");
		widget.configure(wikiKey, descriptor);
		widget.registerStep3(0);
		verify(mockSynapseClient, times(2)).getUnmetEvaluationAccessRequirements(anyString(), any(AsyncCallback.class));
	}
	
	@Test
	public void testRegisterStep3() throws Exception {
		widget.configure(wikiKey, descriptor);
		widget.registerStep3(0);
		//should go to step 3 (creating the participant)
		verify(mockSynapseClient).createParticipants(any(String[].class), any(AsyncCallback.class));
	}
	
	@Test
	public void testRegisterStep3WithTermsOfUse() throws Exception {
		requirements.setTotalNumberOfResults(1);
		TermsOfUseAccessRequirement requirement = new TermsOfUseAccessRequirement();
		requirement.setId(2l);
		requirement.setTermsOfUse("My test ToU");
		requirements.getResults().add(requirement);
		widget.configure(wikiKey, descriptor);
		widget.registerStep3(0);

		//should show terms of use
		verify(mockView).showAccessRequirement(anyString(), any(Callback.class));
	}


	@Test
	public void testRegisterStep4() throws Exception {
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseClient).createParticipants(any(String[].class), any(AsyncCallback.class));
		widget.configure(wikiKey, descriptor);
		widget.registerStep4();
		verify(mockSynapseClient).createParticipants(any(String[].class), any(AsyncCallback.class));
		verify(mockView).showInfo(anyString(), anyString());
	}
	
	@Test
	public void testRegisterStep4Failure() throws Exception {
		AsyncMockStubber.callFailureWith(new Exception()).when(mockSynapseClient).createParticipants(any(String[].class), any(AsyncCallback.class));
		widget.configure(wikiKey, descriptor);
		widget.registerStep4();
		verify(mockSynapseClient).createParticipants(any(String[].class), any(AsyncCallback.class));
		verify(mockView).showError(anyString());
	}
}
