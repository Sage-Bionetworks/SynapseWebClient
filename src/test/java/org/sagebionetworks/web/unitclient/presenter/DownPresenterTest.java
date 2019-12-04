package org.sagebionetworks.web.unitclient.presenter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.client.presenter.DownPresenter.DELAY_MS;
import static org.sagebionetworks.web.client.presenter.DownPresenter.SECOND_MS;
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
import org.sagebionetworks.web.client.StackConfigServiceAsync;
import org.sagebionetworks.web.client.place.Down;
import org.sagebionetworks.web.client.presenter.DownPresenter;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.view.DownView;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class DownPresenterTest {
	DownPresenter presenter;
	@Mock
	DownView mockView;
	@Mock
	GWTWrapper mockGWT;
	@Mock
	GlobalApplicationState mockGlobalAppState;
	@Mock
	StackConfigServiceAsync mockStackConfigService;
	@Mock
	PlaceChanger mockPlaceChanger;
	@Mock
	Down mockDownPlace;
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
		presenter = new DownPresenter(mockView, mockGWT, mockGlobalAppState, mockStackConfigService);
		AsyncMockStubber.callSuccessWith(mockStackStatus).when(mockStackConfigService).getCurrentStatus(any(AsyncCallback.class));
		when(mockView.isAttached()).thenReturn(true);
	}

	@Test
	public void testConstruction() {
		verify(mockGWT).scheduleFixedDelay(callbackCaptor.capture(), eq(SECOND_MS));
		Callback secondTimerFired = callbackCaptor.getValue();
		secondTimerFired.invoke();
		verify(mockView).updateTimeToNextRefresh(anyInt()); // in seconds
		verify(mockView).setTimerVisible(true);
	}

	@Test
	public void testSetPlace() {
		presenter.setPlace(mockDownPlace);
		verify(mockView).init();
		assertEquals(0, presenter.getTimeToNextRefresh());
	}

	@Test
	public void testUpdateTimer() {
		// verify initial stack status check, and then verify the timer based update.
		presenter.setPlace(mockDownPlace);

		// kick off the timer
		verify(mockGWT).scheduleFixedDelay(callbackCaptor.capture(), eq(SECOND_MS));
		Callback secondTimerFired = callbackCaptor.getValue();
		secondTimerFired.invoke();

		// verify it checks the stack status
		verify(mockView).setTimerVisible(false);
		verify(mockStackConfigService).getCurrentStatus(any(AsyncCallback.class));

		// now that we've verified the repeating scheduled execution, eat up the rest of the seconds to get
		// down to zero
		for (int i = 0; i < DELAY_MS / SECOND_MS; i++) {
			secondTimerFired.invoke();
		}
		// verify it checks the stack status
		verify(mockView, times(2)).setTimerVisible(false);
		verify(mockStackConfigService, times(2)).getCurrentStatus(any(AsyncCallback.class));
	}

	@Test
	public void testRepoUpHasLastPlace() {
		// (note that the stack status has been set up to return READ_WRITE already
		presenter.checkForRepoDown();
		verify(mockGlobalAppState).back();
	}

	@Test
	public void testRepoDown() {
		when(mockStackStatus.getStatus()).thenReturn(StatusEnum.DOWN);
		String currentMessage = "upgrading synapse to new version";
		when(mockStackStatus.getCurrentMessage()).thenReturn(currentMessage);
		presenter.checkForRepoDown();
		verify(mockView).setMessage(currentMessage);
	}

	@Test
	public void testCheckFailure() {
		String error = "Could not get status for some reason!";
		AsyncMockStubber.callFailureWith(new Exception(error)).when(mockStackConfigService).getCurrentStatus(any(AsyncCallback.class));
		presenter.checkForRepoDown();
		verify(mockView).setMessage(error);
	}
}
