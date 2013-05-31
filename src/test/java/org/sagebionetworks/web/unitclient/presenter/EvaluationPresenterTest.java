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
import org.sagebionetworks.repo.model.TermsOfUseAccessRequirement;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.Evaluation;
import org.sagebionetworks.web.client.presenter.EvaluationPresenter;
import org.sagebionetworks.web.client.view.EvaluationView;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class EvaluationPresenterTest {
	
	EvaluationPresenter presenter;
	EvaluationView mockView;
	SynapseClientAsync mockSynapseClient;
	PlaceChanger mockPlaceChanger;
	PaginatedResults<TermsOfUseAccessRequirement> requirements;
	
	@Before
	public void setup() throws Exception{
		mockView = mock(EvaluationView.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		UserSessionData currentUser = mock(UserSessionData.class);
		UserProfile currentUserProfile = mock(UserProfile.class);
		when(currentUser.getProfile()).thenReturn(currentUserProfile);
		when(currentUserProfile.getOwnerId()).thenReturn("1");
		
		AsyncMockStubber.callSuccessWith(true).when(mockSynapseClient).hasAccess(anyString(), anyString(), anyString(), any(AsyncCallback.class));
		
		presenter = new EvaluationPresenter(mockView, mockSynapseClient);
		verify(mockView).setPresenter(presenter);
	}	
	
	@Test
	public void testSetPlace() {
		Evaluation place = Mockito.mock(Evaluation.class);
		when(place.toToken()).thenReturn("myEvaluationId");
		presenter.setPlace(place);
		verify(mockView).showPage(any(WikiPageKey.class), anyBoolean());
	}

	@Test
	public void testNoAccess() {
		AsyncMockStubber.callSuccessWith(false).when(mockSynapseClient).hasAccess(anyString(), anyString(), anyString(), any(AsyncCallback.class));
		presenter.configure("evalId");
		verify(mockView).showPage(any(WikiPageKey.class), anyBoolean());
	}

	@Test
	public void testAccessCheckFailure() {
		AsyncMockStubber.callFailureWith(new Exception()).when(mockSynapseClient).hasAccess(anyString(), anyString(), anyString(), any(AsyncCallback.class));
		presenter.configure("evalId");
		verify(mockView).showErrorMessage(anyString());
	}
}
