package org.sagebionetworks.web.unitclient.widget.discussion;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadBundle;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadOrder;
import org.sagebionetworks.web.client.DiscussionForumClientAsync;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.discussion.AvailableThreadListWidget;
import org.sagebionetworks.web.client.widget.discussion.DiscussionThreadListWidget;
import org.sagebionetworks.web.client.widget.discussion.DiscussionThreadListWidgetView;
import org.sagebionetworks.web.client.widget.discussion.DiscussionThreadWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public class AvailableThreadListWidgetTest {

	@Mock
	DiscussionThreadListWidgetView mockView;
	@Mock
	PortalGinInjector mockGinInjector;
	@Mock
	DiscussionThreadWidget mockDiscussionThreadWidget;
	@Mock
	DiscussionForumClientAsync mockDiscussionForumClient;
	@Mock
	PaginatedResults<DiscussionThreadBundle> mockThreadBundlePage;
	@Mock
	SynapseAlert mockSynAlert;

	List<DiscussionThreadBundle> discussionThreadBundleList = new ArrayList<DiscussionThreadBundle>();
	DiscussionThreadListWidget discussionThreadListWidget;

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		when(mockGinInjector.createThreadWidget()).thenReturn(mockDiscussionThreadWidget);
		discussionThreadListWidget = new AvailableThreadListWidget(mockView, mockGinInjector, mockDiscussionForumClient, mockSynAlert);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testLoadMoreSuccess() {
		boolean canModerate = false;
		AsyncMockStubber.callSuccessWith(mockThreadBundlePage)
				.when(mockDiscussionForumClient).getAvailableThreadsForForum(anyString(), anyLong(),
						anyLong(), any(DiscussionThreadOrder.class), anyBoolean(), any(AsyncCallback.class));
		when(mockThreadBundlePage.getTotalNumberOfResults()).thenReturn(1L);
		discussionThreadBundleList.add(new DiscussionThreadBundle());
		when(mockThreadBundlePage.getResults()).thenReturn(discussionThreadBundleList);
		discussionThreadListWidget.configure("123", canModerate);
		verify(mockView).clear();
		verify(mockSynAlert).clear();
		verify(mockDiscussionForumClient).getAvailableThreadsForForum(anyString(), anyLong(),
				anyLong(), any(DiscussionThreadOrder.class), anyBoolean(), any(AsyncCallback.class));
		verify(mockView).addThread(any(Widget.class));
		verify(mockGinInjector).createThreadWidget();
		verify(mockDiscussionThreadWidget).configure(any(DiscussionThreadBundle.class), eq(canModerate), any(Callback.class));
		verify(mockView).setLoadMoreButtonVisibility(false);
		verify(mockView).setEmptyUIVisible(false);
		verify(mockView).setThreadHeaderVisible(true);
		verify(mockView).setLoadingVisible(true);
		verify(mockView).setLoadingVisible(false);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testLoadMoreZeroThreads() {
		boolean canModerate = false;
		AsyncMockStubber.callSuccessWith(mockThreadBundlePage)
				.when(mockDiscussionForumClient).getAvailableThreadsForForum(anyString(), anyLong(),
						anyLong(), any(DiscussionThreadOrder.class), anyBoolean(), any(AsyncCallback.class));
		when(mockThreadBundlePage.getTotalNumberOfResults()).thenReturn(0L);
		when(mockThreadBundlePage.getResults()).thenReturn(discussionThreadBundleList);
		discussionThreadListWidget.configure("123", canModerate);
		verify(mockView).clear();
		verify(mockSynAlert).clear();
		verify(mockDiscussionForumClient).getAvailableThreadsForForum(anyString(), anyLong(),
				anyLong(), any(DiscussionThreadOrder.class), anyBoolean(), any(AsyncCallback.class));
		verify(mockView, never()).addThread(any(Widget.class));
		verify(mockGinInjector, never()).createThreadWidget();
		verify(mockDiscussionThreadWidget, never()).configure(any(DiscussionThreadBundle.class), eq(canModerate), any(Callback.class));
		verify(mockView).setLoadMoreButtonVisibility(false);
		verify(mockView).setEmptyUIVisible(true);
		verify(mockView).setThreadHeaderVisible(false);
		verify(mockView).setLoadingVisible(true);
		verify(mockView).setLoadingVisible(false);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testLoadmoreSuccessDisplayLoadmore() {
		boolean canModerate = false;
		AsyncMockStubber.callSuccessWith(mockThreadBundlePage)
				.when(mockDiscussionForumClient).getAvailableThreadsForForum(anyString(), anyLong(),
						anyLong(), any(DiscussionThreadOrder.class), anyBoolean(), any(AsyncCallback.class));
		when(mockThreadBundlePage.getTotalNumberOfResults()).thenReturn(11L);
		discussionThreadBundleList.add(new DiscussionThreadBundle());
		when(mockThreadBundlePage.getResults()).thenReturn(discussionThreadBundleList);
		discussionThreadListWidget.configure("123", canModerate);
		verify(mockView).clear();
		verify(mockSynAlert).clear();
		verify(mockDiscussionForumClient).getAvailableThreadsForForum(anyString(), anyLong(),
				anyLong(), any(DiscussionThreadOrder.class), anyBoolean(), any(AsyncCallback.class));
		verify(mockView).addThread(any(Widget.class));
		verify(mockGinInjector).createThreadWidget();
		verify(mockDiscussionThreadWidget).configure(any(DiscussionThreadBundle.class), eq(canModerate), any(Callback.class));
		verify(mockView).setLoadMoreButtonVisibility(true);
		verify(mockView).setEmptyUIVisible(false);
		verify(mockView).setThreadHeaderVisible(true);
		verify(mockView).setLoadingVisible(true);
		verify(mockView).setLoadingVisible(false);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testLoadmoreFailure() {
		boolean canModerate = false;
		AsyncMockStubber.callFailureWith(new Exception())
				.when(mockDiscussionForumClient).getAvailableThreadsForForum(anyString(), anyLong(),
						anyLong(), any(DiscussionThreadOrder.class), anyBoolean(), any(AsyncCallback.class));
		discussionThreadListWidget.configure("123", canModerate);
		verify(mockView).clear();
		verify(mockView, never()).addThread(any(Widget.class));
		verify(mockGinInjector, never()).createThreadWidget();
		verify(mockSynAlert).clear();
		verify(mockDiscussionForumClient).getAvailableThreadsForForum(anyString(), anyLong(),
				anyLong(), any(DiscussionThreadOrder.class), anyBoolean(), any(AsyncCallback.class));
		verify(mockSynAlert).handleException(any(Throwable.class));
		verify(mockView).setLoadingVisible(true);
		verify(mockView).setLoadingVisible(false);
	}
}
