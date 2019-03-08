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
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.discussion.DiscussionFilter;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadBundle;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadOrder;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.LoadMoreWidgetContainer;
import org.sagebionetworks.web.client.widget.discussion.DiscussionThreadListItemWidget;
import org.sagebionetworks.web.client.widget.discussion.DiscussionThreadListWidget;
import org.sagebionetworks.web.client.widget.discussion.DiscussionThreadListWidgetView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.refresh.DiscussionThreadCountAlert;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.dev.util.collect.HashSet;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

@RunWith(MockitoJUnitRunner.class)
public class DiscussionThreadListWidgetTest {
	@Mock
	DiscussionThreadListWidgetView mockView;
	@Mock
	PortalGinInjector mockGinInjector;
	@Mock
	DiscussionThreadListItemWidget mockDiscussionThreadWidget;
	@Mock
	SynapseAlert mockSynAlert;
	@Mock
	CallbackP<Boolean> mockEmptyListCallback;
	@Mock
	CallbackP<DiscussionThreadBundle> mockThreadIdClickedCallback;
	@Mock
	DiscussionThreadCountAlert mockDiscussionThreadCountAlert;
	@Mock
	SynapseJSNIUtils mockSynapseJSNIUtils;
	@Mock
	DiscussionThreadBundle mockDiscussionThreadBundle;
	@Mock
	LoadMoreWidgetContainer mockThreadsContainer;
	@Mock
	SynapseJavascriptClient mockSynapseJavascriptClient;
	
	List<DiscussionThreadBundle> discussionThreadBundleList = new ArrayList<DiscussionThreadBundle>();
	DiscussionThreadListWidget discussionThreadListWidget;
	Set<String> moderatorIds;

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		when(mockGinInjector.createThreadListItemWidget()).thenReturn(mockDiscussionThreadWidget);
		when(mockGinInjector.getDiscussionThreadCountAlert()).thenReturn(mockDiscussionThreadCountAlert);
		discussionThreadListWidget = new DiscussionThreadListWidget(mockView,
				mockGinInjector, mockSynAlert, mockThreadsContainer,mockSynapseJSNIUtils, mockSynapseJavascriptClient);
		moderatorIds = new HashSet<String>();
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
		verify(mockSynapseJavascriptClient).getThreadsForEntity(eq(entityId), anyLong(),
				anyLong(), eq(DiscussionThreadOrder.PINNED_AND_LAST_ACTIVITY), eq(false), any(DiscussionFilter.class),
				any(AsyncCallback.class));
		ArgumentCaptor<Callback> captor = ArgumentCaptor.forClass(Callback.class);
		verify(mockThreadsContainer).configure(captor.capture());
		captor.getValue().invoke();
		verify(mockSynapseJavascriptClient, times(2)).getThreadsForEntity(eq(entityId), anyLong(),
				anyLong(), any(DiscussionThreadOrder.class), anyBoolean(), any(DiscussionFilter.class),
				any(AsyncCallback.class));
		verify(mockView).clearSort();
		verify(mockView, never()).setSorted(any(DiscussionThreadOrder.class), anyBoolean());
		
		// sort by number of replies, verify call, sort again, verify ascending is toggled.
		discussionThreadListWidget.sortBy(DiscussionThreadOrder.NUMBER_OF_REPLIES);
		verify(mockSynapseJavascriptClient).getThreadsForEntity(eq(entityId), anyLong(),
				anyLong(), eq(DiscussionThreadOrder.NUMBER_OF_REPLIES), eq(false), any(DiscussionFilter.class),
				any(AsyncCallback.class));
		verify(mockView).setSorted(DiscussionThreadOrder.NUMBER_OF_REPLIES, false);
		
		discussionThreadListWidget.sortBy(DiscussionThreadOrder.NUMBER_OF_REPLIES);
		verify(mockSynapseJavascriptClient).getThreadsForEntity(eq(entityId), anyLong(),
				anyLong(), eq(DiscussionThreadOrder.NUMBER_OF_REPLIES), eq(true), any(DiscussionFilter.class),
				any(AsyncCallback.class));
		verify(mockView).setSorted(DiscussionThreadOrder.NUMBER_OF_REPLIES, true);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testConfigure() {
		boolean canModerate = false;
		String forumId = "123";
		discussionThreadListWidget.configure(forumId, canModerate, moderatorIds, mockEmptyListCallback, DiscussionFilter.EXCLUDE_DELETED);
		verify(mockThreadsContainer).clear();
		verify(mockSynAlert).clear();
		verify(mockSynapseJavascriptClient).getThreadsForForum(eq(forumId), anyLong(),
				anyLong(), eq(DiscussionThreadOrder.PINNED_AND_LAST_ACTIVITY), eq(false), any(DiscussionFilter.class),
				any(AsyncCallback.class));
		ArgumentCaptor<Callback> captor = ArgumentCaptor.forClass(Callback.class);
		verify(mockThreadsContainer).configure(captor.capture());
		captor.getValue().invoke();
		verify(mockSynapseJavascriptClient, times(2)).getThreadsForForum(eq(forumId), anyLong(),
				anyLong(), any(DiscussionThreadOrder.class), anyBoolean(), any(DiscussionFilter.class),
				any(AsyncCallback.class));
		verify(mockView).clearSort();
		verify(mockView, never()).setSorted(any(DiscussionThreadOrder.class), anyBoolean());
		
		// sort by number of replies, verify call, sort again, verify ascending is toggled.
		discussionThreadListWidget.sortBy(DiscussionThreadOrder.NUMBER_OF_VIEWS);
		verify(mockSynapseJavascriptClient).getThreadsForForum(eq(forumId), anyLong(),
				anyLong(), eq(DiscussionThreadOrder.NUMBER_OF_VIEWS), eq(false), any(DiscussionFilter.class),
				any(AsyncCallback.class));
		verify(mockView).setSorted(DiscussionThreadOrder.NUMBER_OF_VIEWS, false);
		
		discussionThreadListWidget.sortBy(DiscussionThreadOrder.NUMBER_OF_VIEWS);
		verify(mockSynapseJavascriptClient).getThreadsForForum(eq(forumId), anyLong(),
				anyLong(), eq(DiscussionThreadOrder.NUMBER_OF_VIEWS), eq(true), any(DiscussionFilter.class),
				any(AsyncCallback.class));
		verify(mockView).setSorted(DiscussionThreadOrder.NUMBER_OF_VIEWS, true);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testConfigureWithModerator() {
		boolean canModerate = true;
		AsyncMockStubber.callSuccessWith(discussionThreadBundleList)
				.when(mockSynapseJavascriptClient).getThreadsForForum(anyString(), anyLong(),
						anyLong(), any(DiscussionThreadOrder.class), anyBoolean(),
						any(DiscussionFilter.class), any(AsyncCallback.class));
		DiscussionThreadBundle threadBundle = new DiscussionThreadBundle();
		String threadId = "987654";
		threadBundle.setId(threadId);
		discussionThreadBundleList.add(threadBundle);
		discussionThreadListWidget.setThreadIdClickedCallback(mockThreadIdClickedCallback);
		discussionThreadListWidget.configure("123", canModerate, moderatorIds, mockEmptyListCallback, DiscussionFilter.EXCLUDE_DELETED);
		verify(mockThreadsContainer).clear();
		verify(mockSynAlert).clear();
		verify(mockSynapseJavascriptClient).getThreadsForForum(anyString(), anyLong(),
				anyLong(), any(DiscussionThreadOrder.class), anyBoolean(),
				any(DiscussionFilter.class), any(AsyncCallback.class));
		verify(mockDiscussionThreadWidget).configure(any(DiscussionThreadBundle.class));
		verify(mockDiscussionThreadWidget).setThreadIdClickedCallback(mockThreadIdClickedCallback);
		
		// test scroll to thread
		AsyncMockStubber.callSuccessWith(mockDiscussionThreadBundle)
			.when(mockSynapseJavascriptClient).getThread(anyString(), any(AsyncCallback.class));
		discussionThreadListWidget.scrollToThread("invalidid");
		verify(mockView, never()).scrollIntoView(any(Widget.class));
		discussionThreadListWidget.scrollToThread(threadId);
		verify(mockView).scrollIntoView(any(Widget.class));
		verify(mockSynapseJavascriptClient).getThread(anyString(), any(AsyncCallback.class));
		verify(mockDiscussionThreadWidget).configure(mockDiscussionThreadBundle);
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
		AsyncMockStubber.callSuccessWith(discussionThreadBundleList)
				.when(mockSynapseJavascriptClient).getThreadsForForum(anyString(),
						anyLong(), anyLong(), any(DiscussionThreadOrder.class),
						anyBoolean(), any(DiscussionFilter.class), any(AsyncCallback.class));
		DiscussionThreadBundle threadBundle = new DiscussionThreadBundle();
		String threadId = "987654";
		threadBundle.setId(threadId);
		discussionThreadBundleList.add(threadBundle);
		discussionThreadListWidget.configure("123", canModerate, moderatorIds, mockEmptyListCallback, DiscussionFilter.EXCLUDE_DELETED);
		verify(mockThreadsContainer).clear();
		verify(mockSynAlert).clear();
		verify(mockSynapseJavascriptClient).getThreadsForForum(anyString(),
				anyLong(), anyLong(), any(DiscussionThreadOrder.class),
				anyBoolean(), any(DiscussionFilter.class), any(AsyncCallback.class));
		verify(mockThreadsContainer).add(any(Widget.class));
		verify(mockGinInjector).createThreadListItemWidget();
		verify(mockDiscussionThreadWidget).configure(any(DiscussionThreadBundle.class));
		verify(mockEmptyListCallback).invoke(anyBoolean());
		verify(mockView).setThreadHeaderVisible(true);
		verify(mockView).setNoThreadsFoundVisible(false);
		verify(mockThreadsContainer).setIsMore(false);
		
		// test scroll to thread, rpc failure
		String error = "unable to refresh thread data";
		AsyncMockStubber.callFailureWith(new Exception(error))
			.when(mockSynapseJavascriptClient).getThread(anyString(), any(AsyncCallback.class));
		discussionThreadListWidget.scrollToThread(threadId);
		verify(mockView).scrollIntoView(any(Widget.class));
		verify(mockSynapseJavascriptClient).getThread(anyString(), any(AsyncCallback.class));
		verify(mockDiscussionThreadWidget, never()).configure(mockDiscussionThreadBundle);
		verify(mockSynapseJSNIUtils).consoleError(error);
	}
	
	@Test
	public void testLoadMoreVisible() {
		boolean canModerate = false;
		AsyncMockStubber.callSuccessWith(discussionThreadBundleList)
				.when(mockSynapseJavascriptClient).getThreadsForForum(anyString(),
						anyLong(), anyLong(), any(DiscussionThreadOrder.class),
						anyBoolean(), any(DiscussionFilter.class), any(AsyncCallback.class));
		for (int i = 0; i < DiscussionThreadListWidget.LIMIT; i++) {
			DiscussionThreadBundle threadBundle = new DiscussionThreadBundle();
			threadBundle.setId("thread_"+i);
			discussionThreadBundleList.add(threadBundle);
		}
		discussionThreadListWidget.configure("123", canModerate, moderatorIds, mockEmptyListCallback, DiscussionFilter.EXCLUDE_DELETED);
		verify(mockThreadsContainer).setIsMore(true);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testLoadMoreZeroThreads() {
		boolean canModerate = false;
		AsyncMockStubber.callSuccessWith(discussionThreadBundleList)
				.when(mockSynapseJavascriptClient).getThreadsForForum(anyString(),
						anyLong(), anyLong(), any(DiscussionThreadOrder.class),
						anyBoolean(), any(DiscussionFilter.class), any(AsyncCallback.class));
		discussionThreadListWidget.configure("123", canModerate, moderatorIds, mockEmptyListCallback, DiscussionFilter.EXCLUDE_DELETED);
		verify(mockThreadsContainer).clear();
		verify(mockSynAlert).clear();
		verify(mockSynapseJavascriptClient).getThreadsForForum(anyString(),
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
		AsyncMockStubber.callSuccessWith(discussionThreadBundleList)
				.when(mockSynapseJavascriptClient).getThreadsForForum(anyString(),
						anyLong(), anyLong(), any(DiscussionThreadOrder.class),
						anyBoolean(), any(DiscussionFilter.class), any(AsyncCallback.class));
		discussionThreadBundleList.add(new DiscussionThreadBundle());
		discussionThreadListWidget.configure("123", canModerate, moderatorIds, mockEmptyListCallback, DiscussionFilter.EXCLUDE_DELETED);
		verify(mockThreadsContainer).clear();
		verify(mockSynAlert).clear();
		verify(mockSynapseJavascriptClient).getThreadsForForum(anyString(),
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
				.when(mockSynapseJavascriptClient).getThreadsForForum(anyString(),
						anyLong(), anyLong(), any(DiscussionThreadOrder.class),
						anyBoolean(), any(DiscussionFilter.class), any(AsyncCallback.class));
		discussionThreadListWidget.configure("123", canModerate, moderatorIds, mockEmptyListCallback, DiscussionFilter.EXCLUDE_DELETED);
		verify(mockThreadsContainer).clear();
		verify(mockThreadsContainer, never()).add(any(Widget.class));
		verify(mockGinInjector, never()).createThreadListItemWidget();
		verify(mockSynAlert).clear();
		verify(mockSynapseJavascriptClient).getThreadsForForum(anyString(),
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
		verify(mockSynapseJavascriptClient).getThreadsForForum(anyString(), anyLong(),
				anyLong(), eq(DiscussionThreadOrder.NUMBER_OF_REPLIES), eq(DEFAULT_ASCENDING),
				any(DiscussionFilter.class), any(AsyncCallback.class));
		discussionThreadListWidget.sortBy(DiscussionThreadOrder.NUMBER_OF_REPLIES);
		verify(mockSynapseJavascriptClient).getThreadsForForum(anyString(), anyLong(),
				anyLong(), eq(DiscussionThreadOrder.NUMBER_OF_REPLIES), eq(!DEFAULT_ASCENDING),
				any(DiscussionFilter.class), any(AsyncCallback.class));
		discussionThreadListWidget.sortBy(DiscussionThreadOrder.NUMBER_OF_REPLIES);
		verify(mockSynapseJavascriptClient, times(2)).getThreadsForForum(anyString(), anyLong(),
				anyLong(), eq(DiscussionThreadOrder.NUMBER_OF_REPLIES), eq(DEFAULT_ASCENDING),
				any(DiscussionFilter.class), any(AsyncCallback.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testSortByViewsRepeated() {
		boolean canModerate = false;
		discussionThreadListWidget.configure("123", canModerate, moderatorIds, mockEmptyListCallback, DiscussionFilter.EXCLUDE_DELETED);
		discussionThreadListWidget.sortBy(DiscussionThreadOrder.NUMBER_OF_VIEWS);
		verify(mockSynapseJavascriptClient).getThreadsForForum(anyString(), anyLong(),
				anyLong(), eq(DiscussionThreadOrder.NUMBER_OF_VIEWS), eq(DEFAULT_ASCENDING),
				any(DiscussionFilter.class), any(AsyncCallback.class));
		discussionThreadListWidget.sortBy(DiscussionThreadOrder.NUMBER_OF_VIEWS);
		verify(mockSynapseJavascriptClient).getThreadsForForum(anyString(), anyLong(),
				anyLong(), eq(DiscussionThreadOrder.NUMBER_OF_VIEWS), eq(!DEFAULT_ASCENDING),
				any(DiscussionFilter.class), any(AsyncCallback.class));
		discussionThreadListWidget.sortBy(DiscussionThreadOrder.NUMBER_OF_VIEWS);
		verify(mockSynapseJavascriptClient, times(2)).getThreadsForForum(anyString(), anyLong(),
				anyLong(), eq(DiscussionThreadOrder.NUMBER_OF_VIEWS), eq(DEFAULT_ASCENDING),
				any(DiscussionFilter.class), any(AsyncCallback.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testSortByLastActivityRepeated() {
		boolean canModerate = false;
		discussionThreadListWidget.configure("123", canModerate, moderatorIds, mockEmptyListCallback, DiscussionFilter.EXCLUDE_DELETED);
		discussionThreadListWidget.sortBy(DiscussionThreadOrder.PINNED_AND_LAST_ACTIVITY);
		verify(mockSynapseJavascriptClient).getThreadsForForum(anyString(), anyLong(),
				anyLong(), eq(DiscussionThreadOrder.PINNED_AND_LAST_ACTIVITY), eq(!DEFAULT_ASCENDING),
				any(DiscussionFilter.class), any(AsyncCallback.class));
		discussionThreadListWidget.sortBy(DiscussionThreadOrder.PINNED_AND_LAST_ACTIVITY);
		verify(mockSynapseJavascriptClient, times(2)).getThreadsForForum(anyString(), anyLong(),
				anyLong(), eq(DiscussionThreadOrder.PINNED_AND_LAST_ACTIVITY), eq(DEFAULT_ASCENDING),
				any(DiscussionFilter.class), any(AsyncCallback.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testSortByTitleRepeated() {
		boolean canModerate = false;
		discussionThreadListWidget.configure("123", canModerate, moderatorIds, mockEmptyListCallback, DiscussionFilter.EXCLUDE_DELETED);
		discussionThreadListWidget.sortBy(DiscussionThreadOrder.THREAD_TITLE);
		verify(mockSynapseJavascriptClient).getThreadsForForum(anyString(), anyLong(),
				anyLong(), eq(DiscussionThreadOrder.THREAD_TITLE), eq(DEFAULT_ASCENDING),
				any(DiscussionFilter.class), any(AsyncCallback.class));
		discussionThreadListWidget.sortBy(DiscussionThreadOrder.THREAD_TITLE);
		verify(mockSynapseJavascriptClient).getThreadsForForum(anyString(), anyLong(),
				anyLong(), eq(DiscussionThreadOrder.THREAD_TITLE), eq(!DEFAULT_ASCENDING),
				any(DiscussionFilter.class), any(AsyncCallback.class));
		discussionThreadListWidget.sortBy(DiscussionThreadOrder.THREAD_TITLE);
		verify(mockSynapseJavascriptClient, times(2)).getThreadsForForum(anyString(), anyLong(),
				anyLong(), eq(DiscussionThreadOrder.THREAD_TITLE), eq(DEFAULT_ASCENDING),
				any(DiscussionFilter.class), any(AsyncCallback.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testSort() {
		boolean canModerate = false;
		discussionThreadListWidget.configure("123", canModerate, moderatorIds, mockEmptyListCallback, DiscussionFilter.EXCLUDE_DELETED);

		discussionThreadListWidget.sortBy(DiscussionThreadOrder.NUMBER_OF_REPLIES);
		verify(mockSynapseJavascriptClient).getThreadsForForum(anyString(), anyLong(),
				anyLong(), eq(DiscussionThreadOrder.NUMBER_OF_REPLIES), eq(DEFAULT_ASCENDING),
				any(DiscussionFilter.class), any(AsyncCallback.class));

		discussionThreadListWidget.sortBy(DiscussionThreadOrder.NUMBER_OF_VIEWS);
		verify(mockSynapseJavascriptClient).getThreadsForForum(anyString(), anyLong(),
				anyLong(), eq(DiscussionThreadOrder.NUMBER_OF_VIEWS), eq(DEFAULT_ASCENDING),
				any(DiscussionFilter.class), any(AsyncCallback.class));

		discussionThreadListWidget.sortBy(DiscussionThreadOrder.NUMBER_OF_REPLIES);
		verify(mockSynapseJavascriptClient, times(2)).getThreadsForForum(anyString(), anyLong(),
				anyLong(), eq(DiscussionThreadOrder.NUMBER_OF_REPLIES), eq(DEFAULT_ASCENDING),
				any(DiscussionFilter.class), any(AsyncCallback.class));
	}
}
