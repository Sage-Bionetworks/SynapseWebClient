package org.sagebionetworks.web.unitclient.widget.discussion;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.client.widget.discussion.DiscussionThreadListWidget.DEFAULT_ASCENDING;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.discussion.DiscussionFilter;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadBundle;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadOrder;
import org.sagebionetworks.web.client.DiscussionForumClientAsync;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.LoadMoreWidgetContainer;
import org.sagebionetworks.web.client.widget.discussion.DiscussionThreadListItemWidget;
import org.sagebionetworks.web.client.widget.discussion.DiscussionThreadListWidget;
import org.sagebionetworks.web.client.widget.discussion.DiscussionThreadListWidgetView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.refresh.DiscussionThreadCountAlert;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.dev.util.collect.HashSet;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public class DiscussionThreadListWidgetTest {

	@Mock
	DiscussionThreadListWidgetView mockView;
	@Mock
	PortalGinInjector mockGinInjector;
	@Mock
	DiscussionThreadListItemWidget mockDiscussionThreadWidget;
	@Mock
	DiscussionForumClientAsync mockDiscussionForumClient;
	@Mock
	PaginatedResults<DiscussionThreadBundle> mockThreadBundlePage;
	@Mock
	SynapseAlert mockSynAlert;
	@Mock
	CallbackP<Boolean> mockEmptyListCallback;
	@Mock
	CallbackP<DiscussionThreadBundle> mockThreadIdClickedCallback;
	@Mock
	DiscussionThreadCountAlert mockDiscussionThreadCountAlert;
	@Mock
	LoadMoreWidgetContainer mockThreadsContainer;
	List<DiscussionThreadBundle> discussionThreadBundleList = new ArrayList<DiscussionThreadBundle>();
	DiscussionThreadListWidget discussionThreadListWidget;
	Set<Long> moderatorIds;

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		when(mockGinInjector.createThreadListItemWidget()).thenReturn(mockDiscussionThreadWidget);
		when(mockGinInjector.getDiscussionThreadCountAlert()).thenReturn(mockDiscussionThreadCountAlert);
		discussionThreadListWidget = new DiscussionThreadListWidget(mockView,
				mockGinInjector, mockDiscussionForumClient, mockSynAlert, mockThreadsContainer);
		moderatorIds = new HashSet<Long>();
	}

	@Test
	public void testConstructor() {
		verify(mockView).setPresenter(discussionThreadListWidget);
		verify(mockView).setAlert(any(Widget.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testConfigureWithEntity() {
		String entityId = "123";
		discussionThreadListWidget.configure(entityId, null, null);
		verify(mockThreadsContainer).clear();
		verify(mockSynAlert).clear();
		verify(mockDiscussionForumClient).getThreadsForEntity(eq(entityId), anyLong(),
				anyLong(), any(DiscussionThreadOrder.class), anyBoolean(), any(DiscussionFilter.class),
				any(AsyncCallback.class));
		ArgumentCaptor<Callback> captor = ArgumentCaptor.forClass(Callback.class);
		verify(mockThreadsContainer).configure(captor.capture());
		captor.getValue().invoke();
		verify(mockDiscussionForumClient, times(2)).getThreadsForEntity(eq(entityId), anyLong(),
				anyLong(), any(DiscussionThreadOrder.class), anyBoolean(), any(DiscussionFilter.class),
				any(AsyncCallback.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testConfigure() {
		boolean canModerate = false;
		String forumId = "123";
		discussionThreadListWidget.configure(forumId, canModerate, moderatorIds, mockEmptyListCallback, DiscussionFilter.EXCLUDE_DELETED);
		verify(mockThreadsContainer).clear();
		verify(mockSynAlert).clear();
		verify(mockDiscussionForumClient).getThreadsForForum(eq(forumId), anyLong(),
				anyLong(), any(DiscussionThreadOrder.class), anyBoolean(), any(DiscussionFilter.class),
				any(AsyncCallback.class));
		ArgumentCaptor<Callback> captor = ArgumentCaptor.forClass(Callback.class);
		verify(mockThreadsContainer).configure(captor.capture());
		captor.getValue().invoke();
		verify(mockDiscussionForumClient, times(2)).getThreadsForForum(eq(forumId), anyLong(),
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
		discussionThreadListWidget.setThreadIdClickedCallback(mockThreadIdClickedCallback);
		discussionThreadListWidget.configure("123", canModerate, moderatorIds, mockEmptyListCallback, DiscussionFilter.EXCLUDE_DELETED);
		verify(mockThreadsContainer).clear();
		verify(mockSynAlert).clear();
		verify(mockDiscussionForumClient).getThreadsForForum(anyString(), anyLong(),
				anyLong(), any(DiscussionThreadOrder.class), anyBoolean(),
				any(DiscussionFilter.class), any(AsyncCallback.class));
		verify(mockDiscussionThreadWidget).configure(any(DiscussionThreadBundle.class));
		verify(mockDiscussionThreadWidget).setThreadIdClickedCallback(mockThreadIdClickedCallback);
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
		discussionThreadListWidget.configure("123", canModerate, moderatorIds, mockEmptyListCallback, DiscussionFilter.EXCLUDE_DELETED);
		verify(mockThreadsContainer).clear();
		verify(mockSynAlert).clear();
		verify(mockDiscussionForumClient).getThreadsForForum(anyString(),
				anyLong(), anyLong(), any(DiscussionThreadOrder.class),
				anyBoolean(), any(DiscussionFilter.class), any(AsyncCallback.class));
		verify(mockThreadsContainer).add(any(Widget.class));
		verify(mockGinInjector).createThreadListItemWidget();
		verify(mockDiscussionThreadWidget).configure(any(DiscussionThreadBundle.class));
		verify(mockEmptyListCallback).invoke(anyBoolean());
		verify(mockView).setThreadHeaderVisible(true);
		verify(mockView).setNoThreadsFoundVisible(false);
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
		discussionThreadListWidget.configure("123", canModerate, moderatorIds, mockEmptyListCallback, DiscussionFilter.EXCLUDE_DELETED);
		verify(mockThreadsContainer).clear();
		verify(mockSynAlert).clear();
		verify(mockDiscussionForumClient).getThreadsForForum(anyString(),
				anyLong(), anyLong(), any(DiscussionThreadOrder.class),
				anyBoolean(), any(DiscussionFilter.class), any(AsyncCallback.class));
		verify(mockThreadsContainer, never()).add(any(Widget.class));
		verify(mockGinInjector, never()).createThreadListItemWidget();
		verify(mockDiscussionThreadWidget, never()).configure(any(DiscussionThreadBundle.class));
		verify(mockEmptyListCallback).invoke(anyBoolean());
		verify(mockView).setThreadHeaderVisible(false);
		verify(mockView).setNoThreadsFoundVisible(true);
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
		discussionThreadListWidget.configure("123", canModerate, moderatorIds, mockEmptyListCallback, DiscussionFilter.EXCLUDE_DELETED);
		verify(mockThreadsContainer).clear();
		verify(mockSynAlert).clear();
		verify(mockDiscussionForumClient).getThreadsForForum(anyString(),
				anyLong(), anyLong(), any(DiscussionThreadOrder.class),
				anyBoolean(), any(DiscussionFilter.class), any(AsyncCallback.class));
		verify(mockThreadsContainer).add(any(Widget.class));
		verify(mockGinInjector).createThreadListItemWidget();
		verify(mockDiscussionThreadWidget).configure(any(DiscussionThreadBundle.class));
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
		discussionThreadListWidget.configure("123", canModerate, moderatorIds, mockEmptyListCallback, DiscussionFilter.EXCLUDE_DELETED);
		verify(mockThreadsContainer).clear();
		verify(mockThreadsContainer, never()).add(any(Widget.class));
		verify(mockGinInjector, never()).createThreadListItemWidget();
		verify(mockSynAlert).clear();
		verify(mockDiscussionForumClient).getThreadsForForum(anyString(),
				anyLong(), anyLong(), any(DiscussionThreadOrder.class),
				anyBoolean(), any(DiscussionFilter.class), any(AsyncCallback.class));
		verify(mockSynAlert).handleException(any(Throwable.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testSortByRepliesRepeated() {
		boolean canModerate = false;
		discussionThreadListWidget.configure("123", canModerate, moderatorIds, mockEmptyListCallback, DiscussionFilter.EXCLUDE_DELETED);
		discussionThreadListWidget.sortBy(DiscussionThreadOrder.NUMBER_OF_REPLIES);
		verify(mockDiscussionForumClient).getThreadsForForum(anyString(), anyLong(),
				anyLong(), eq(DiscussionThreadOrder.NUMBER_OF_REPLIES), eq(DEFAULT_ASCENDING),
				any(DiscussionFilter.class), any(AsyncCallback.class));
		discussionThreadListWidget.sortBy(DiscussionThreadOrder.NUMBER_OF_REPLIES);
		verify(mockDiscussionForumClient).getThreadsForForum(anyString(), anyLong(),
				anyLong(), eq(DiscussionThreadOrder.NUMBER_OF_REPLIES), eq(!DEFAULT_ASCENDING),
				any(DiscussionFilter.class), any(AsyncCallback.class));
		discussionThreadListWidget.sortBy(DiscussionThreadOrder.NUMBER_OF_REPLIES);
		verify(mockDiscussionForumClient, times(2)).getThreadsForForum(anyString(), anyLong(),
				anyLong(), eq(DiscussionThreadOrder.NUMBER_OF_REPLIES), eq(DEFAULT_ASCENDING),
				any(DiscussionFilter.class), any(AsyncCallback.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testSortByViewsRepeated() {
		boolean canModerate = false;
		discussionThreadListWidget.configure("123", canModerate, moderatorIds, mockEmptyListCallback, DiscussionFilter.EXCLUDE_DELETED);
		discussionThreadListWidget.sortBy(DiscussionThreadOrder.NUMBER_OF_VIEWS);
		verify(mockDiscussionForumClient).getThreadsForForum(anyString(), anyLong(),
				anyLong(), eq(DiscussionThreadOrder.NUMBER_OF_VIEWS), eq(DEFAULT_ASCENDING),
				any(DiscussionFilter.class), any(AsyncCallback.class));
		discussionThreadListWidget.sortBy(DiscussionThreadOrder.NUMBER_OF_VIEWS);
		verify(mockDiscussionForumClient).getThreadsForForum(anyString(), anyLong(),
				anyLong(), eq(DiscussionThreadOrder.NUMBER_OF_VIEWS), eq(!DEFAULT_ASCENDING),
				any(DiscussionFilter.class), any(AsyncCallback.class));
		discussionThreadListWidget.sortBy(DiscussionThreadOrder.NUMBER_OF_VIEWS);
		verify(mockDiscussionForumClient, times(2)).getThreadsForForum(anyString(), anyLong(),
				anyLong(), eq(DiscussionThreadOrder.NUMBER_OF_VIEWS), eq(DEFAULT_ASCENDING),
				any(DiscussionFilter.class), any(AsyncCallback.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testSortByLatActivityRepeated() {
		boolean canModerate = false;
		discussionThreadListWidget.configure("123", canModerate, moderatorIds, mockEmptyListCallback, DiscussionFilter.EXCLUDE_DELETED);
		discussionThreadListWidget.sortBy(DiscussionThreadOrder.PINNED_AND_LAST_ACTIVITY);
		verify(mockDiscussionForumClient).getThreadsForForum(anyString(), anyLong(),
				anyLong(), eq(DiscussionThreadOrder.PINNED_AND_LAST_ACTIVITY), eq(!DEFAULT_ASCENDING),
				any(DiscussionFilter.class), any(AsyncCallback.class));
		discussionThreadListWidget.sortBy(DiscussionThreadOrder.PINNED_AND_LAST_ACTIVITY);
		verify(mockDiscussionForumClient, times(2)).getThreadsForForum(anyString(), anyLong(),
				anyLong(), eq(DiscussionThreadOrder.PINNED_AND_LAST_ACTIVITY), eq(DEFAULT_ASCENDING),
				any(DiscussionFilter.class), any(AsyncCallback.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testSort() {
		boolean canModerate = false;
		discussionThreadListWidget.configure("123", canModerate, moderatorIds, mockEmptyListCallback, DiscussionFilter.EXCLUDE_DELETED);

		discussionThreadListWidget.sortBy(DiscussionThreadOrder.NUMBER_OF_REPLIES);
		verify(mockDiscussionForumClient).getThreadsForForum(anyString(), anyLong(),
				anyLong(), eq(DiscussionThreadOrder.NUMBER_OF_REPLIES), eq(DEFAULT_ASCENDING),
				any(DiscussionFilter.class), any(AsyncCallback.class));

		discussionThreadListWidget.sortBy(DiscussionThreadOrder.NUMBER_OF_VIEWS);
		verify(mockDiscussionForumClient).getThreadsForForum(anyString(), anyLong(),
				anyLong(), eq(DiscussionThreadOrder.NUMBER_OF_VIEWS), eq(DEFAULT_ASCENDING),
				any(DiscussionFilter.class), any(AsyncCallback.class));

		discussionThreadListWidget.sortBy(DiscussionThreadOrder.NUMBER_OF_REPLIES);
		verify(mockDiscussionForumClient, times(2)).getThreadsForForum(anyString(), anyLong(),
				anyLong(), eq(DiscussionThreadOrder.NUMBER_OF_REPLIES), eq(DEFAULT_ASCENDING),
				any(DiscussionFilter.class), any(AsyncCallback.class));
	}
}
