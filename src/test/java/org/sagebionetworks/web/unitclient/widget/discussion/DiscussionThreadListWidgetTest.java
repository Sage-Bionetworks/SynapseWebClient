package org.sagebionetworks.web.unitclient.widget.discussion;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.sagebionetworks.web.client.widget.discussion.ForumWidget.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.discussion.DiscussionFilter;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadBundle;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadOrder;
import org.sagebionetworks.web.client.DiscussionForumClientAsync;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
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
	@Mock
	CallbackP<Boolean> mockEmptyListCallback;

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
		boolean canModerate = false;
		discussionThreadListWidget.configure("123", canModerate, mockEmptyListCallback);
		verify(mockView).clear();
		verify(mockSynAlert).clear();
		verify(mockDiscussionForumClient).getThreadsForForum(anyString(), anyLong(),
				anyLong(), any(DiscussionThreadOrder.class), anyBoolean(), any(DiscussionFilter.class),
				any(AsyncCallback.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testConfigureWithModerator() {
		boolean canModerate = true;
		AsyncMockStubber.callSuccessWith(mockThreadBundlePage)
				.when(mockDiscussionForumClient).getThreadsForForum(anyString(), anyLong(),
						anyLong(), any(DiscussionThreadOrder.class), anyBoolean(),
						any(DiscussionFilter.class), any(AsyncCallback.class));
		when(mockThreadBundlePage.getTotalNumberOfResults()).thenReturn(1L);
		discussionThreadBundleList.add(new DiscussionThreadBundle());
		when(mockThreadBundlePage.getResults()).thenReturn(discussionThreadBundleList);
		discussionThreadListWidget.configure("123", canModerate, mockEmptyListCallback);
		verify(mockView).clear();
		verify(mockSynAlert).clear();
		verify(mockDiscussionForumClient).getThreadsForForum(anyString(), anyLong(),
				anyLong(), any(DiscussionThreadOrder.class), anyBoolean(),
				any(DiscussionFilter.class), any(AsyncCallback.class));
		verify(mockDiscussionThreadWidget).configure(any(DiscussionThreadBundle.class),
				eq(canModerate), any(Callback.class), eq(SHOW_THREAD_DETAILS_FOR_THREAD_LIST),
				eq(SHOW_REPLY_DETAILS_FOR_THREAD_LIST));
	}

	@Test
	public void asWidgetTest() {
		discussionThreadListWidget.asWidget();
		verify(mockView).asWidget();
	}


	@SuppressWarnings("unchecked")
	@Test
	public void testLoadMoreSuccess() {
		boolean canModerate = false;
		AsyncMockStubber.callSuccessWith(mockThreadBundlePage)
				.when(mockDiscussionForumClient).getThreadsForForum(anyString(),
						anyLong(), anyLong(), any(DiscussionThreadOrder.class),
						anyBoolean(), any(DiscussionFilter.class), any(AsyncCallback.class));
		when(mockThreadBundlePage.getTotalNumberOfResults()).thenReturn(1L);
		discussionThreadBundleList.add(new DiscussionThreadBundle());
		when(mockThreadBundlePage.getResults()).thenReturn(discussionThreadBundleList);
		discussionThreadListWidget.configure("123", canModerate, mockEmptyListCallback);
		verify(mockView).clear();
		verify(mockSynAlert).clear();
		verify(mockDiscussionForumClient).getThreadsForForum(anyString(),
				anyLong(), anyLong(), any(DiscussionThreadOrder.class),
				anyBoolean(), any(DiscussionFilter.class), any(AsyncCallback.class));
		verify(mockView).addThread(any(Widget.class));
		verify(mockGinInjector).createThreadWidget();
		verify(mockDiscussionThreadWidget).configure(any(DiscussionThreadBundle.class),
				eq(canModerate), any(Callback.class), eq(SHOW_THREAD_DETAILS_FOR_THREAD_LIST),
				eq(SHOW_REPLY_DETAILS_FOR_THREAD_LIST));
		verify(mockView).setLoadMoreButtonVisibility(false);
		verify(mockView).setLoadingVisible(true);
		verify(mockView).setLoadingVisible(false);
		verify(mockEmptyListCallback).invoke(anyBoolean());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testLoadMoreZeroThreads() {
		boolean canModerate = false;
		AsyncMockStubber.callSuccessWith(mockThreadBundlePage)
				.when(mockDiscussionForumClient).getThreadsForForum(anyString(),
						anyLong(), anyLong(), any(DiscussionThreadOrder.class),
						anyBoolean(), any(DiscussionFilter.class), any(AsyncCallback.class));
		when(mockThreadBundlePage.getTotalNumberOfResults()).thenReturn(0L);
		when(mockThreadBundlePage.getResults()).thenReturn(discussionThreadBundleList);
		discussionThreadListWidget.configure("123", canModerate, mockEmptyListCallback);
		verify(mockView).clear();
		verify(mockSynAlert).clear();
		verify(mockDiscussionForumClient).getThreadsForForum(anyString(),
				anyLong(), anyLong(), any(DiscussionThreadOrder.class),
				anyBoolean(), any(DiscussionFilter.class), any(AsyncCallback.class));
		verify(mockView, never()).addThread(any(Widget.class));
		verify(mockGinInjector, never()).createThreadWidget();
		verify(mockDiscussionThreadWidget, never()).configure(any(DiscussionThreadBundle.class),
				eq(canModerate), any(Callback.class), eq(SHOW_THREAD_DETAILS_FOR_THREAD_LIST),
				eq(SHOW_REPLY_DETAILS_FOR_THREAD_LIST));
		verify(mockView).setLoadMoreButtonVisibility(false);
		verify(mockView).setLoadingVisible(true);
		verify(mockView).setLoadingVisible(false);
		verify(mockEmptyListCallback).invoke(anyBoolean());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testLoadmoreSuccessDisplayLoadmore() {
		boolean canModerate = false;
		AsyncMockStubber.callSuccessWith(mockThreadBundlePage)
				.when(mockDiscussionForumClient).getThreadsForForum(anyString(),
						anyLong(), anyLong(), any(DiscussionThreadOrder.class),
						anyBoolean(), any(DiscussionFilter.class), any(AsyncCallback.class));
		when(mockThreadBundlePage.getTotalNumberOfResults()).thenReturn(11L);
		discussionThreadBundleList.add(new DiscussionThreadBundle());
		when(mockThreadBundlePage.getResults()).thenReturn(discussionThreadBundleList);
		discussionThreadListWidget.configure("123", canModerate, mockEmptyListCallback);
		verify(mockView).clear();
		verify(mockSynAlert).clear();
		verify(mockDiscussionForumClient).getThreadsForForum(anyString(),
				anyLong(), anyLong(), any(DiscussionThreadOrder.class),
				anyBoolean(), any(DiscussionFilter.class), any(AsyncCallback.class));
		verify(mockView).addThread(any(Widget.class));
		verify(mockGinInjector).createThreadWidget();
		verify(mockDiscussionThreadWidget).configure(any(DiscussionThreadBundle.class),
				eq(canModerate), any(Callback.class), eq(SHOW_THREAD_DETAILS_FOR_THREAD_LIST),
				eq(SHOW_REPLY_DETAILS_FOR_THREAD_LIST));
		verify(mockView).setLoadMoreButtonVisibility(true);
		verify(mockView).setLoadingVisible(true);
		verify(mockView).setLoadingVisible(false);
		verify(mockEmptyListCallback).invoke(anyBoolean());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testLoadmoreFailure() {
		boolean canModerate = false;
		AsyncMockStubber.callFailureWith(new Exception())
				.when(mockDiscussionForumClient).getThreadsForForum(anyString(),
						anyLong(), anyLong(), any(DiscussionThreadOrder.class),
						anyBoolean(), any(DiscussionFilter.class), any(AsyncCallback.class));
		discussionThreadListWidget.configure("123", canModerate, mockEmptyListCallback);
		verify(mockView).clear();
		verify(mockView, never()).addThread(any(Widget.class));
		verify(mockGinInjector, never()).createThreadWidget();
		verify(mockSynAlert).clear();
		verify(mockDiscussionForumClient).getThreadsForForum(anyString(),
				anyLong(), anyLong(), any(DiscussionThreadOrder.class),
				anyBoolean(), any(DiscussionFilter.class), any(AsyncCallback.class));
		verify(mockSynAlert).handleException(any(Throwable.class));
		verify(mockView).setLoadingVisible(true);
		verify(mockView).setLoadingVisible(false);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testSortByRepliesRepeated() {
		boolean canModerate = false;
		discussionThreadListWidget.configure("123", canModerate, mockEmptyListCallback);
		discussionThreadListWidget.sortByReplies();
		verify(mockDiscussionForumClient).getThreadsForForum(anyString(), anyLong(),
				anyLong(), eq(DiscussionThreadOrder.NUMBER_OF_REPLIES), eq(Boolean.TRUE),
				any(DiscussionFilter.class), any(AsyncCallback.class));
		discussionThreadListWidget.sortByReplies();
		verify(mockDiscussionForumClient).getThreadsForForum(anyString(), anyLong(),
				anyLong(), eq(DiscussionThreadOrder.NUMBER_OF_REPLIES), eq(Boolean.FALSE),
				any(DiscussionFilter.class), any(AsyncCallback.class));
		discussionThreadListWidget.sortByReplies();
		verify(mockDiscussionForumClient, times(2)).getThreadsForForum(anyString(), anyLong(),
				anyLong(), eq(DiscussionThreadOrder.NUMBER_OF_REPLIES), eq(Boolean.TRUE),
				any(DiscussionFilter.class), any(AsyncCallback.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testSortByViewsRepeated() {
		boolean canModerate = false;
		discussionThreadListWidget.configure("123", canModerate, mockEmptyListCallback);
		discussionThreadListWidget.sortByViews();
		verify(mockDiscussionForumClient).getThreadsForForum(anyString(), anyLong(),
				anyLong(), eq(DiscussionThreadOrder.NUMBER_OF_VIEWS), eq(Boolean.TRUE),
				any(DiscussionFilter.class), any(AsyncCallback.class));
		discussionThreadListWidget.sortByViews();
		verify(mockDiscussionForumClient).getThreadsForForum(anyString(), anyLong(),
				anyLong(), eq(DiscussionThreadOrder.NUMBER_OF_VIEWS), eq(Boolean.FALSE),
				any(DiscussionFilter.class), any(AsyncCallback.class));
		discussionThreadListWidget.sortByViews();
		verify(mockDiscussionForumClient, times(2)).getThreadsForForum(anyString(), anyLong(),
				anyLong(), eq(DiscussionThreadOrder.NUMBER_OF_VIEWS), eq(Boolean.TRUE),
				any(DiscussionFilter.class), any(AsyncCallback.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testSortByLatActivityRepeated() {
		boolean canModerate = false;
		discussionThreadListWidget.configure("123", canModerate, mockEmptyListCallback);
		discussionThreadListWidget.sortByActivity();
		verify(mockDiscussionForumClient).getThreadsForForum(anyString(), anyLong(),
				anyLong(), eq(DiscussionThreadOrder.LAST_ACTIVITY), eq(Boolean.TRUE),
				any(DiscussionFilter.class), any(AsyncCallback.class));
		discussionThreadListWidget.sortByActivity();
		verify(mockDiscussionForumClient, times(2)).getThreadsForForum(anyString(), anyLong(),
				anyLong(), eq(DiscussionThreadOrder.LAST_ACTIVITY), eq(Boolean.FALSE),
				any(DiscussionFilter.class), any(AsyncCallback.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testSort() {
		boolean canModerate = false;
		discussionThreadListWidget.configure("123", canModerate, mockEmptyListCallback);
		discussionThreadListWidget.sortByReplies();
		verify(mockDiscussionForumClient).getThreadsForForum(anyString(), anyLong(),
				anyLong(), eq(DiscussionThreadOrder.NUMBER_OF_REPLIES), eq(Boolean.TRUE),
				any(DiscussionFilter.class), any(AsyncCallback.class));
		discussionThreadListWidget.sortByViews();
		verify(mockDiscussionForumClient).getThreadsForForum(anyString(), anyLong(),
				anyLong(), eq(DiscussionThreadOrder.NUMBER_OF_VIEWS), eq(Boolean.TRUE),
				any(DiscussionFilter.class), any(AsyncCallback.class));
		discussionThreadListWidget.sortByViews();
		discussionThreadListWidget.sortByReplies();
		verify(mockDiscussionForumClient, times(2)).getThreadsForForum(anyString(), anyLong(),
				anyLong(), eq(DiscussionThreadOrder.NUMBER_OF_REPLIES), eq(Boolean.TRUE),
				any(DiscussionFilter.class), any(AsyncCallback.class));
		discussionThreadListWidget.sortByViews();
	}
}
