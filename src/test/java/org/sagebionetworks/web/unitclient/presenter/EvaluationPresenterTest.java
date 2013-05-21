package org.sagebionetworks.web.unitclient.presenter;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.evaluation.model.UserEvaluationState;
import org.sagebionetworks.repo.model.TermsOfUseAccessRequirement;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.repo.model.VariableContentPaginatedResults;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.Evaluation;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.presenter.EvaluationPresenter;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.view.EvaluationView;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class EvaluationPresenterTest {
	
	EvaluationPresenter presenter;
	EvaluationView mockView;
	SynapseClientAsync mockSynapseClient;
	AuthenticationController mockAuthController;
	GlobalApplicationState mockGlobalApplicationState;
	PlaceChanger mockPlaceChanger;
	JSONObjectAdapter mockJSONObjectAdapter;
	NodeModelCreator mockNodeModelCreator;
	PaginatedResults<TermsOfUseAccessRequirement> requirements;
	
	@Before
	public void setup() throws Exception{
		mockView = mock(EvaluationView.class);
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
		
		presenter = new EvaluationPresenter(mockView, mockSynapseClient, mockAuthController, mockGlobalApplicationState,mockNodeModelCreator, mockJSONObjectAdapter );
		verify(mockView).setPresenter(presenter);
	}	
	
	@Test
	public void testSetPlace() {
		Evaluation place = Mockito.mock(Evaluation.class);
		when(place.toToken()).thenReturn("myEvaluationId");
		presenter.setPlace(place);
		verify(mockView).showPage(any(WikiPageKey.class), any(UserEvaluationState.class), anyBoolean());
	}

	@Test
	public void testNoAccess() {
		AsyncMockStubber.callSuccessWith(false).when(mockSynapseClient).hasAccess(anyString(), anyString(), anyString(), any(AsyncCallback.class));
		presenter.configure("evalId");
		verify(mockView).showPage(any(WikiPageKey.class), any(UserEvaluationState.class), anyBoolean());
	}

	@Test
	public void testAccessCheckFailure() {
		AsyncMockStubber.callFailureWith(new Exception()).when(mockSynapseClient).hasAccess(anyString(), anyString(), anyString(), any(AsyncCallback.class));
		presenter.configure("evalId");
		verify(mockView).showErrorMessage(anyString());
	}

	@Test
	public void testEvaluationStateCheckFailure() throws Exception {
		AsyncMockStubber.callFailureWith(new Exception()).when(mockSynapseClient).getUserEvaluationState(anyString(), any(AsyncCallback.class));
		presenter.configure("evalId");
		verify(mockView).showErrorMessage(anyString());
	}
	
	@Test
	public void testRegisterStep1NotLoggedIn() throws Exception {
		when(mockAuthController.isLoggedIn()).thenReturn(false);
		AsyncMockStubber.callSuccessWith("my participant json").when(mockSynapseClient).createParticipant(anyString(), any(AsyncCallback.class));
		presenter.configure("evalId");
		presenter.registerStep1();
		verify(mockPlaceChanger).goTo(any(LoginPlace.class));
	}
	
	@Test
	public void testRegisterStep1LoggedIn() throws Exception {
		AsyncMockStubber.callSuccessWith("my participant json").when(mockSynapseClient).createParticipant(anyString(), any(AsyncCallback.class));
		presenter.configure("evalId");
		presenter.registerStep1();
		verify(mockSynapseClient).getUnmetEvaluationAccessRequirements(anyString(), any(AsyncCallback.class));
	}
	
	@Test
	public void testRegisterStep2Failure() throws Exception {
		AsyncMockStubber.callFailureWith(new Exception()).when(mockSynapseClient).getUnmetEvaluationAccessRequirements(anyString(), any(AsyncCallback.class));
		presenter.configure("evalId");
		presenter.registerStep2();
		verify(mockView).showErrorMessage(anyString());
	}
	
	@Test
	public void testRegisterStep2() throws Exception {
		presenter.configure("evalId");
		presenter.registerStep2();
		//should go to step 3 (creating the participant)
		verify(mockSynapseClient).createParticipant(anyString(), any(AsyncCallback.class));
	}
	
	@Test
	public void testRegisterStep2WithTermsOfUse() throws Exception {
		requirements.setTotalNumberOfResults(1);
		TermsOfUseAccessRequirement requirement = new TermsOfUseAccessRequirement();
		requirement.setId(2l);
		requirement.setTermsOfUse("My test ToU");
		requirements.getResults().add(requirement);
		presenter.configure("evalId");
		presenter.registerStep2();

		//should show terms of use
		verify(mockView).showAccessRequirement(anyString(), any(Callback.class));
	}


	@Test
	public void testRegisterStep3() throws Exception {
		AsyncMockStubber.callSuccessWith("my participant json").when(mockSynapseClient).createParticipant(anyString(), any(AsyncCallback.class));
		presenter.configure("evalId");
		presenter.registerStep3();
		verify(mockSynapseClient).createParticipant(anyString(), any(AsyncCallback.class));
		verify(mockView).showInfo(anyString(), anyString());
	}
	
	@Test
	public void testRegisterStep3Failure() throws Exception {
		AsyncMockStubber.callFailureWith(new Exception()).when(mockSynapseClient).createParticipant(anyString(), any(AsyncCallback.class));
		presenter.configure("evalId");
		presenter.registerStep3();
		verify(mockSynapseClient).createParticipant(anyString(), any(AsyncCallback.class));
		verify(mockView).showErrorMessage(anyString());
	}
}
