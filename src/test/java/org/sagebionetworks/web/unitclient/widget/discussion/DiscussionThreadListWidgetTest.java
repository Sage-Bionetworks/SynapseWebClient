package org.sagebionetworks.web.unitclient.widget.discussion;
import static org.mockito.Mockito.*;

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
import org.sagebionetworks.web.client.widget.discussion.DiscussionThreadListWidget;
import org.sagebionetworks.web.client.widget.discussion.DiscussionThreadListWidgetView;
import org.sagebionetworks.web.client.widget.discussion.DiscussionThreadWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public class DiscussionThreadListWidgetTest {

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
		discussionThreadListWidget = new DiscussionThreadListWidget(mockView, mockGinInjector, mockDiscussionForumClient, mockSynAlert);
	}

	@Test
	public void testConstructor() {
		verify(mockView).setPresenter(discussionThreadListWidget);
		verify(mockView).setAlert(any(Widget.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testConfigure() {
		discussionThreadListWidget.configure("123");
		verify(mockView).clear();
		verify(mockSynAlert).clear();
		verify(mockDiscussionForumClient).getThreadsForForum(anyString(), anyLong(),
				anyLong(), any(DiscussionThreadOrder.class), anyBoolean(), any(AsyncCallback.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testLoadMoreSuccess() {
		AsyncMockStubber.callSuccessWith(mockThreadBundlePage)
				.when(mockDiscussionForumClient).getThreadsForForum(anyString(), anyLong(),
						anyLong(), any(DiscussionThreadOrder.class), anyBoolean(), any(AsyncCallback.class));
		when(mockThreadBundlePage.getTotalNumberOfResults()).thenReturn(1L);
		discussionThreadBundleList.add(new DiscussionThreadBundle());
		when(mockThreadBundlePage.getResults()).thenReturn(discussionThreadBundleList);
		discussionThreadListWidget.configure("123");
		verify(mockView).clear();
		verify(mockSynAlert).clear();
		verify(mockDiscussionForumClient).getThreadsForForum(anyString(), anyLong(),
				anyLong(), any(DiscussionThreadOrder.class), anyBoolean(), any(AsyncCallback.class));
		verify(mockView).addThread(any(Widget.class));
		verify(mockGinInjector).createThreadWidget();
		verify(mockDiscussionThreadWidget).configure(any(DiscussionThreadBundle.class));
		verify(mockView).setLoadMoreButtonVisibility(false);
		verify(mockView).setEmptyUIVisible(false);
		verify(mockView).setThreadHeaderVisible(true);
		verify(mockView).setLoadingVisible(true);
		verify(mockView).setLoadingVisible(false);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testLoadMoreZeroThreads() {
		AsyncMockStubber.callSuccessWith(mockThreadBundlePage)
				.when(mockDiscussionForumClient).getThreadsForForum(anyString(), anyLong(),
						anyLong(), any(DiscussionThreadOrder.class), anyBoolean(), any(AsyncCallback.class));
		when(mockThreadBundlePage.getTotalNumberOfResults()).thenReturn(0L);
		when(mockThreadBundlePage.getResults()).thenReturn(discussionThreadBundleList);
		discussionThreadListWidget.configure("123");
		verify(mockView).clear();
		verify(mockSynAlert).clear();
		verify(mockDiscussionForumClient).getThreadsForForum(anyString(), anyLong(),
				anyLong(), any(DiscussionThreadOrder.class), anyBoolean(), any(AsyncCallback.class));
		verify(mockView, never()).addThread(any(Widget.class));
		verify(mockGinInjector, never()).createThreadWidget();
		verify(mockDiscussionThreadWidget, never()).configure(any(DiscussionThreadBundle.class));
		verify(mockView).setLoadMoreButtonVisibility(false);
		verify(mockView).setEmptyUIVisible(true);
		verify(mockView).setThreadHeaderVisible(false);
		verify(mockView).setLoadingVisible(true);
		verify(mockView).setLoadingVisible(false);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testLoadmoreSuccessDisplayLoadmore() {
		AsyncMockStubber.callSuccessWith(mockThreadBundlePage)
				.when(mockDiscussionForumClient).getThreadsForForum(anyString(), anyLong(),
						anyLong(), any(DiscussionThreadOrder.class), anyBoolean(), any(AsyncCallback.class));
		when(mockThreadBundlePage.getTotalNumberOfResults()).thenReturn(11L);
		discussionThreadBundleList.add(new DiscussionThreadBundle());
		when(mockThreadBundlePage.getResults()).thenReturn(discussionThreadBundleList);
		discussionThreadListWidget.configure("123");
		verify(mockView).clear();
		verify(mockSynAlert).clear();
		verify(mockDiscussionForumClient).getThreadsForForum(anyString(), anyLong(),
				anyLong(), any(DiscussionThreadOrder.class), anyBoolean(), any(AsyncCallback.class));
		verify(mockView).addThread(any(Widget.class));
		verify(mockGinInjector).createThreadWidget();
		verify(mockDiscussionThreadWidget).configure(any(DiscussionThreadBundle.class));
		verify(mockView).setLoadMoreButtonVisibility(true);
		verify(mockView).setEmptyUIVisible(false);
		verify(mockView).setThreadHeaderVisible(true);
		verify(mockView).setLoadingVisible(true);
		verify(mockView).setLoadingVisible(false);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testLoadmoreFailure() {
		AsyncMockStubber.callFailureWith(new Exception())
				.when(mockDiscussionForumClient).getThreadsForForum(anyString(), anyLong(),
						anyLong(), any(DiscussionThreadOrder.class), anyBoolean(), any(AsyncCallback.class));
		discussionThreadListWidget.configure("123");
		verify(mockView).clear();
		verify(mockView, never()).addThread(any(Widget.class));
		verify(mockGinInjector, never()).createThreadWidget();
		verify(mockSynAlert).clear();
		verify(mockDiscussionForumClient).getThreadsForForum(anyString(), anyLong(),
				anyLong(), any(DiscussionThreadOrder.class), anyBoolean(), any(AsyncCallback.class));
		verify(mockSynAlert).handleException(any(Throwable.class));
		verify(mockView).setLoadingVisible(true);
		verify(mockView).setLoadingVisible(false);
	}

	@Test
	public void asWidgetTest() {
		discussionThreadListWidget.asWidget();
		verify(mockView).asWidget();
	}
}
