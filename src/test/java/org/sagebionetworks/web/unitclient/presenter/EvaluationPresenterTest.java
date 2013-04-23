package org.sagebionetworks.web.unitclient.presenter;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.Evaluation;
import org.sagebionetworks.web.client.presenter.EvaluationPresenter;
import org.sagebionetworks.web.client.presenter.UserEvaluationState;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.view.EvaluationView;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class EvaluationPresenterTest {
	
	EvaluationPresenter presenter;
	EvaluationView mockView;
	SynapseClientAsync mockSynapseClient;
	AuthenticationController mockAuthController;
	GlobalApplicationState mockGlobalApplicationState;
	PlaceChanger mockPlaceChanger;
	
	@Before
	public void setup() throws Exception{
		mockView = mock(EvaluationView.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockAuthController = mock(AuthenticationController.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockPlaceChanger = mock(PlaceChanger.class);
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		when(mockAuthController.isLoggedIn()).thenReturn(true);
		AsyncMockStubber.callSuccessWith(true).when(mockSynapseClient).hasAccess(anyString(), anyString(), anyString(), any(AsyncCallback.class));		
		AsyncMockStubber.callSuccessWith(UserEvaluationState.EVAL_REGISTRATION_UNAVAILABLE).when(mockSynapseClient).getUserEvaluationState(anyString(), any(AsyncCallback.class));
		presenter = new EvaluationPresenter(mockView, mockSynapseClient, mockAuthController, mockGlobalApplicationState);
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
	public void testRegister() throws Exception {
		AsyncMockStubber.callSuccessWith("my participant json").when(mockSynapseClient).createParticipant(anyString(), any(AsyncCallback.class));
		presenter.configure("evalId");
		presenter.register();
		verify(mockSynapseClient).createParticipant(anyString(), any(AsyncCallback.class));
		verify(mockView).showInfo(anyString(), anyString());
	}
	
	@Test
	public void testRegisterNotLoggedIn() throws Exception {
		when(mockAuthController.isLoggedIn()).thenReturn(false);
		AsyncMockStubber.callSuccessWith("my participant json").when(mockSynapseClient).createParticipant(anyString(), any(AsyncCallback.class));
		presenter.configure("evalId");
		presenter.register();
		verify(mockPlaceChanger).goTo(any(Place.class));
	}
	
	@Test
	public void testRegisterFailure() throws Exception {
		AsyncMockStubber.callFailureWith(new Exception()).when(mockSynapseClient).createParticipant(anyString(), any(AsyncCallback.class));
		presenter.configure("evalId");
		presenter.register();
		verify(mockSynapseClient).createParticipant(anyString(), any(AsyncCallback.class));
		verify(mockView).showErrorMessage(anyString());
	}
	
	@Test
	public void testUnRegister() throws Exception {
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseClient).deleteParticipant(anyString(), any(AsyncCallback.class));
		presenter.configure("evalId");
		presenter.unregister();
		verify(mockSynapseClient).deleteParticipant(anyString(), any(AsyncCallback.class));
		verify(mockView).showInfo(anyString(), anyString());
	}
	
	@Test
	public void testUnRegisterFailure() throws Exception {
		AsyncMockStubber.callFailureWith(new Exception()).when(mockSynapseClient).deleteParticipant(anyString(), any(AsyncCallback.class));
		presenter.configure("evalId");
		presenter.unregister();
		verify(mockSynapseClient).deleteParticipant(anyString(), any(AsyncCallback.class));
		verify(mockView).showErrorMessage(anyString());
	}
	
	
}
