package org.sagebionetworks.web.unitclient.widget;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.LoadMoreWidgetContainer;
import org.sagebionetworks.web.client.widget.LoadMoreWidgetContainerView;

public class LoadMoreWidgetContainerTest {
	@Mock
	LoadMoreWidgetContainerView mockView;
	@Mock
	GWTWrapper mockGWT;
	@Mock
	Callback mockLoadMoreCallback;
	LoadMoreWidgetContainer widget;
	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		widget = new LoadMoreWidgetContainer(mockView, mockGWT);
		widget.configure(mockLoadMoreCallback);
	}

	@Test
	public void testSetIsMore() {
		boolean isMore = true;
		widget.setIsMore(isMore);
		verify(mockView).setLoadMoreVisibility(isMore);
		verify(mockGWT).scheduleExecution(any(Callback.class), anyInt());
	}
	
	@Test
	public void testSetIsNoMore() {
		boolean isMore = false;
		widget.setIsMore(isMore);
		verify(mockView).setLoadMoreVisibility(isMore);
		verify(mockGWT, never()).scheduleExecution(any(Callback.class), anyInt());
	}


	@Test
	public void testAsWidget() {
		widget.asWidget();
		verify(mockView).asWidget();
	}

	@Test
	public void testAdd() {
		widget.add(null);
		verify(mockView).add(null);
	}

	@Test
	public void testClear() {
		widget.clear();
		verify(mockView).clear();
	}

	@Test
	public void testCheckForInViewAndLoadDataNotAttached() {
		when(mockView.isLoadMoreAttached()).thenReturn(false);
		widget.checkForInViewAndLoadData();
		verify(mockGWT, never()).scheduleExecution(any(Callback.class), anyInt());
		verify(mockLoadMoreCallback, never()).invoke();
	}

	
	@Test
	public void testCheckForInViewAndLoadDataAttachedNotInViewport() {
		when(mockView.isLoadMoreAttached()).thenReturn(true);
		when(mockView.isLoadMoreInViewport()).thenReturn(false);
		widget.checkForInViewAndLoadData();
		verify(mockGWT).scheduleExecution(any(Callback.class), anyInt());
		verify(mockLoadMoreCallback, never()).invoke();
	}

	@Test
	public void testCheckForInViewAndLoadDataAttachedNotVisible() {
		when(mockView.isLoadMoreAttached()).thenReturn(true);
		when(mockView.isLoadMoreInViewport()).thenReturn(true);
		when(mockView.getLoadMoreVisibility()).thenReturn(false);
		widget.checkForInViewAndLoadData();
		verify(mockGWT).scheduleExecution(any(Callback.class), anyInt());
		verify(mockLoadMoreCallback, never()).invoke();
	}

	@Test
	public void testCheckForInViewAndLoadDataAttachedVisible() {
		when(mockView.isLoadMoreAttached()).thenReturn(true);
		when(mockView.isLoadMoreInViewport()).thenReturn(false);
		when(mockView.getLoadMoreVisibility()).thenReturn(true);
		widget.checkForInViewAndLoadData();
		verify(mockGWT).scheduleExecution(any(Callback.class), anyInt());
		verify(mockLoadMoreCallback, never()).invoke();
	}

	@Test
	public void testCheckForInViewAndLoadDataAttachedInViewAndVisible() {
		when(mockView.isLoadMoreAttached()).thenReturn(true);
		when(mockView.isLoadMoreInViewport()).thenReturn(true);
		when(mockView.getLoadMoreVisibility()).thenReturn(true);
		widget.checkForInViewAndLoadData();
		verify(mockGWT, never()).scheduleExecution(any(Callback.class), anyInt());
		verify(mockLoadMoreCallback).invoke();
	}

	@Test
	public void testCheckForInViewAndLoadDataAttachedInViewAndVisibleAndIsProcessing() {
		when(mockView.isLoadMoreAttached()).thenReturn(true);
		when(mockView.isLoadMoreInViewport()).thenReturn(true);
		when(mockView.getLoadMoreVisibility()).thenReturn(true);
		widget.setIsProcessing(true);
		widget.checkForInViewAndLoadData();
		verify(mockGWT).scheduleExecution(any(Callback.class), anyInt());
		verify(mockLoadMoreCallback, never()).invoke();
	}
}
