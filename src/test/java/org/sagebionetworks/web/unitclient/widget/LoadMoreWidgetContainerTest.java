package org.sagebionetworks.web.unitclient.widget;

import static org.mockito.Mockito.verify;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.LoadMoreWidgetContainer;
import org.sagebionetworks.web.client.widget.LoadMoreWidgetContainerView;

@RunWith(MockitoJUnitRunner.class)
public class LoadMoreWidgetContainerTest {
	@Mock
	LoadMoreWidgetContainerView mockView;
	@Mock
	Callback mockLoadMoreCallback;

	LoadMoreWidgetContainer widget;

	@Before
	public void before() {
		widget = new LoadMoreWidgetContainer(mockView);
		widget.configure(mockLoadMoreCallback);
	}

	@Test
	public void testSetIsMore() {
		boolean isMore = true;
		widget.setIsMore(isMore);
		verify(mockView).setIsProcessing(false);
		verify(mockView).setLoadMoreVisibility(isMore);
	}

	@Test
	public void testSetIsNoMore() {
		boolean isMore = false;
		widget.setIsMore(isMore);
		verify(mockView).setIsProcessing(false);
		verify(mockView).setLoadMoreVisibility(isMore);
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
		verify(mockView).setIsProcessing(false);
		verify(mockView).setLoadMoreVisibility(false);
	}
}
