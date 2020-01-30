package org.sagebionetworks.web.unitclient.widget.lazyload;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.lazyload.LazyLoadCallbackQueue;
import org.sagebionetworks.web.client.widget.lazyload.LazyLoadHelper;
import org.sagebionetworks.web.client.widget.lazyload.SupportsLazyLoadInterface;

public class LazyLoadHelperTest {
	LazyLoadHelper lazyLoadHelper;
	@Mock
	Callback mockInViewportCallback;
	@Mock
	SupportsLazyLoadInterface mockView;
	@Mock
	LazyLoadCallbackQueue mockLazyLoadCallbackQueue;
	@Mock
	GWTWrapper mockGWT;
	@Captor
	ArgumentCaptor<Callback> callbackCaptor;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		lazyLoadHelper = new LazyLoadHelper(mockLazyLoadCallbackQueue);
		when(mockView.isAttached()).thenReturn(false);
		when(mockView.isInViewport()).thenReturn(false);
		lazyLoadHelper.configure(mockInViewportCallback, mockView);
	}

	private void simulateAttachEvent() {
		verify(mockView).setOnAttachCallback(callbackCaptor.capture());
		callbackCaptor.getValue().invoke();
	}

	@Test
	public void testHappyCase() {
		// in before, simulated the view is not yet attached, or in viewport, and underlying widget is not
		// yet configured
		lazyLoadHelper.startCheckingIfAttachedAndConfigured();
		verifyZeroInteractions(mockLazyLoadCallbackQueue);

		// configure
		lazyLoadHelper.setIsConfigured();

		// has not yet started looking loading data, because it's been configured but not attached (view
		// tells presenter when it's attached).
		verifyZeroInteractions(mockLazyLoadCallbackQueue);
		verify(mockInViewportCallback, never()).invoke();


		// attach, but still not in viewport
		when(mockView.isAttached()).thenReturn(true);
		simulateAttachEvent();

		// should start the process of asking if this is in the viewport
		ArgumentCaptor<Callback> captor = ArgumentCaptor.forClass(Callback.class);
		verify(mockLazyLoadCallbackQueue).subscribe(captor.capture());
		Callback callback = captor.getValue();

		// simulate the view is now attached and in the viewport, and widget is configure, so it's time to
		// load data
		when(mockView.isInViewport()).thenReturn(true);
		callback.invoke();

		verify(mockInViewportCallback).invoke();

		// verify calling attach again does not cause viewport callback to invoke a second time (only load
		// the data once)
		verify(mockView, times(2)).setOnAttachCallback(callbackCaptor.capture());
		callbackCaptor.getValue().invoke();
		verify(mockInViewportCallback).invoke(); // still only called once, from above
	}

	/**
	 * This tests the case when the widget is attached to the dom and remains outside the viewport, and
	 * is eventually detached
	 */
	@Test
	public void testNeverInViewport() {
		// configure
		lazyLoadHelper.setIsConfigured();

		// attach
		when(mockView.isAttached()).thenReturn(true);
		simulateAttachEvent();

		ArgumentCaptor<Callback> captor = ArgumentCaptor.forClass(Callback.class);

		verify(mockLazyLoadCallbackQueue).subscribe(captor.capture());
		Callback callback = captor.getValue();

		// simulate the view detached before it's ever scrolled into view
		when(mockView.isAttached()).thenReturn(false);
		callback.invoke();
		// verify that this cycle is dead
		verify(mockLazyLoadCallbackQueue).unsubscribe(callback);
	}
}
