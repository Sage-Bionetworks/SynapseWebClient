package org.sagebionetworks.web.unitclient.presenter;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import static org.sagebionetworks.web.client.presenter.DownPresenter.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.status.StackStatus;
import org.sagebionetworks.repo.model.status.StatusEnum;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.place.Down;
import org.sagebionetworks.web.client.place.Home;
import org.sagebionetworks.web.client.presenter.DownPresenter;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.view.DownView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public class DownPresenterTest {
	DownPresenter presenter;
	@Mock
	DownView mockView;
	@Mock
	GWTWrapper mockGWT;
	@Mock
	GlobalApplicationState mockGlobalAppState;
	@Mock
	SynapseJavascriptClient mockSynapseJavascriptClient;
	@Mock
	PlaceChanger mockPlaceChanger;
	@Mock
	Down mockDownPlace;
	@Mock
	Place mockLastPlace;
	@Captor
	ArgumentCaptor<Callback> callbackCaptor;
	@Captor
	ArgumentCaptor<Place> placeCaptor;
	
	@Mock
	StackStatus mockStackStatus;
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		when(mockGlobalAppState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		when(mockStackStatus.getStatus()).thenReturn(StatusEnum.READ_WRITE);
		presenter = new DownPresenter(mockView, mockGWT, mockGlobalAppState, mockSynapseJavascriptClient);
		AsyncMockStubber.callSuccessWith(mockStackStatus).when(mockSynapseJavascriptClient).getStackStatus(any(AsyncCallback.class));
	}

	@Test
	public void testSetPlace() {
		presenter.setPlace(mockDownPlace);
		verify(mockView).init();
		verify(mockView).setTimerVisible(false);
		verify(mockSynapseJavascriptClient).getStackStatus(any(AsyncCallback.class));
	}

	@Test
	public void testUpdateTimer() {
		//kick off the timer
		presenter.scheduleRepoDownCheck();
		verify(mockGWT).scheduleExecution(callbackCaptor.capture(), eq(SECOND_MS));
		Callback secondTimerFired = callbackCaptor.getValue();
		secondTimerFired.invoke();
		verify(mockView).updateTimeToNextRefresh((DELAY_MS-SECOND_MS)/1000); //in seconds
		verify(mockView).setTimerVisible(true);
		verify(mockGWT, times(2)).scheduleExecution(eq(secondTimerFired), eq(SECOND_MS));
		//now that we've verified the repeating scheduled execution, eat up the rest of the seconds to get down to zero
		for (int i = 1; i < DELAY_MS/SECOND_MS; i++) {
			secondTimerFired.invoke();
		}
		//verify it checks the stack status
		verify(mockView).setTimerVisible(false);
		verify(mockSynapseJavascriptClient).getStackStatus(any(AsyncCallback.class));
	}
	
	@Test
	public void testRepoUpNoLastPlace() {
		// (note that the stack status has been set up to return READ_WRITE already
		presenter.checkForRepoDown();
		verify(mockPlaceChanger).goTo(placeCaptor.capture());
		assertTrue(placeCaptor.getValue() instanceof Home);
	}
	
	@Test
	public void testRepoUpHasLastPlace() {
		// (note that the stack status has been set up to return READ_WRITE already
		when(mockGlobalAppState.getLastPlace()).thenReturn(mockLastPlace);
		presenter.checkForRepoDown();
		verify(mockGlobalAppState).gotoLastPlace();
	}
	
	@Test
	public void testRepoDown() {
		when(mockStackStatus.getStatus()).thenReturn(StatusEnum.DOWN);
		String currentMessage = "upgrading synapse to new version";
		when(mockStackStatus.getCurrentMessage()).thenReturn(currentMessage);
		presenter.checkForRepoDown();
		verify(mockView).setMessage(currentMessage);
		verify(mockGWT).scheduleExecution(any(Callback.class), eq(SECOND_MS));
	}

	@Test
	public void testCheckFailure() {
		String error = "Could not get status for some reason!";
		AsyncMockStubber.callFailureWith(new Exception(error)).when(mockSynapseJavascriptClient).getStackStatus(any(AsyncCallback.class));
		presenter.checkForRepoDown();
		verify(mockView).setMessage(error);
		verify(mockGWT).scheduleExecution(any(Callback.class), eq(SECOND_MS));
	}
}
