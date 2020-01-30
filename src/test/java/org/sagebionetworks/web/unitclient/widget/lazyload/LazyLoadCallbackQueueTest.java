package org.sagebionetworks.web.unitclient.widget.lazyload;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.lazyload.LazyLoadCallbackQueueImpl;

public class LazyLoadCallbackQueueTest {
	LazyLoadCallbackQueueImpl lazyLoadCallbackQueue;
	@Mock
	Callback mockCallback;
	@Mock
	GWTWrapper mockGWT;
	@Captor
	ArgumentCaptor<Callback> checkForMoreWorkCallbackCaptor;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		lazyLoadCallbackQueue = new LazyLoadCallbackQueueImpl(mockGWT);

		// verify initial fire (looking for work)
		verify(mockGWT).scheduleExecution(checkForMoreWorkCallbackCaptor.capture(), eq(LazyLoadCallbackQueueImpl.DELAY));
	}

	@Test
	public void testHappyCase() {
		lazyLoadCallbackQueue.subscribe(mockCallback);
		verify(mockCallback, never()).invoke();

		// simulate timer event fired
		checkForMoreWorkCallbackCaptor.getValue().invoke();

		verify(mockCallback).invoke();

		// again
		checkForMoreWorkCallbackCaptor.getValue().invoke();
		verify(mockCallback, times(2)).invoke();

		// unsubscribe
		lazyLoadCallbackQueue.unsubscribe(mockCallback);

		// fire again
		checkForMoreWorkCallbackCaptor.getValue().invoke();

		// verify our callback was not invoked again (after unsubscribing)
		verify(mockCallback, times(2)).invoke();
	}

}
