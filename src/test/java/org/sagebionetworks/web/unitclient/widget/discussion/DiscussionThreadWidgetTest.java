package org.sagebionetworks.web.unitclient.widget.discussion;

import static org.sagebionetworks.web.client.widget.discussion.DiscussionThreadWidget.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.discussion.DiscussionReplyBundle;
import org.sagebionetworks.repo.model.discussion.DiscussionReplyOrder;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadBundle;
import org.sagebionetworks.web.client.DiscussionForumClientAsync;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.discussion.ReplyWidget;
import org.sagebionetworks.web.client.widget.discussion.DiscussionThreadWidget;
import org.sagebionetworks.web.client.widget.discussion.DiscussionThreadWidgetView;
import org.sagebionetworks.web.client.widget.discussion.modal.NewReplyModal;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public class DiscussionThreadWidgetTest {

	@Mock
	DiscussionThreadWidgetView mockView;
	@Mock
	PortalGinInjector mockGinInjector;
	@Mock
	ReplyWidget mockReplyWidget;
	@Mock
	GWTWrapper mockGwtWrapper;
	@Mock
	NewReplyModal mockNewReplyModal;
	@Mock
	SynapseAlert mockSynAlert;
	@Mock
	DiscussionForumClientAsync mockDiscussionForumClientAsync;
	@Mock
	PaginatedResults<DiscussionReplyBundle> mockReplyBundlePage;
	@Mock
	UserBadge mockUserBadge;

	DiscussionThreadWidget discussionThreadWidget;
	List<DiscussionReplyBundle> bundleList;

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		when(mockGinInjector.createReplyWidget()).thenReturn(mockReplyWidget);
		when(mockGinInjector.getUserBadgeWidget()).thenReturn(mockUserBadge);
		discussionThreadWidget = new DiscussionThreadWidget(mockView, mockNewReplyModal,
				mockSynAlert, mockDiscussionForumClientAsync, mockGinInjector, mockGwtWrapper);
	}

	@Test
	public void testConstructor() {
		verify(mockView).setPresenter(discussionThreadWidget);
		verify(mockView).setNewReplyModal(any(Widget.class));
		verify(mockView).setAlert(any(Widget.class));
	}

	@Test
	public void testConfigure() {
		DiscussionThreadBundle threadBundle = createThreadBundle("1", "title",
				Arrays.asList("123"), 1L, 2L, new Date());
		discussionThreadWidget.configure(threadBundle );
		verify(mockView).clear();
		verify(mockView).setTitle("title");
		verify(mockView).addActiveAuthor(any(Widget.class));
		verify(mockView).setNumberOfReplies("1");
		verify(mockView).setNumberOfViews("2");
		verify(mockView).setLastActivity(anyString());
		verify(mockView).setAuthor(any(Widget.class));
		verify(mockView).setCreatedOn(anyString());
		verify(mockView).setShowRepliesVisibility(true);
		verify(mockGinInjector, times(2)).getUserBadgeWidget();
		verify(mockGwtWrapper, times(2)).getFormattedDateString(any(Date.class));
		verify(mockNewReplyModal).configure(anyString(), any(Callback.class));
	}

	@Test
	public void testConfigureWithZeroReplies(){
		DiscussionThreadBundle threadBundle = createThreadBundle("1", "title",
				Arrays.asList("123"), 0L, 2L, new Date());
		discussionThreadWidget.configure(threadBundle );
		verify(mockView).clear();
		verify(mockView).setTitle("title");
		verify(mockView).addActiveAuthor(any(Widget.class));
		verify(mockView).setNumberOfReplies("0");
		verify(mockView).setNumberOfViews("2");
		verify(mockView).setLastActivity(anyString());
		verify(mockView).setAuthor(any(Widget.class));
		verify(mockView).setCreatedOn(anyString());
		verify(mockView).setShowRepliesVisibility(false);
		verify(mockGinInjector, times(2)).getUserBadgeWidget();
		verify(mockGwtWrapper, times(2)).getFormattedDateString(any(Date.class));
		verify(mockNewReplyModal).configure(anyString(), any(Callback.class));
	}

	@Test
	public void asWidgetTest() {
		discussionThreadWidget.asWidget();
		verify(mockView).asWidget();
	}

	@Test
	public void toggleThreadTest() {
		discussionThreadWidget.toggleThread();
		verify(mockView).toggleThread();
	}

	@Test
	public void toggleRepliesTest() {
		discussionThreadWidget.toggleReplies();
		verify(mockView).toggleReplies();
	}

	@Test
	public void testOnClickNewReply() {
		discussionThreadWidget.onClickNewReply();
		verify(mockNewReplyModal).show();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testConfigureRepliesSuccess() {
		DiscussionThreadBundle threadBundle = createThreadBundle("1", "title",
				Arrays.asList("123"), 0L, 2L, new Date());
		discussionThreadWidget.configure(threadBundle );
		bundleList = createReplyBundleList(2);
		when(mockReplyBundlePage.getTotalNumberOfResults()).thenReturn(2L);
		when(mockReplyBundlePage.getResults()).thenReturn(bundleList);
		AsyncMockStubber.callSuccessWith(mockReplyBundlePage)
				.when(mockDiscussionForumClientAsync).getRepliesForThread(anyString(), anyLong(),
						anyLong(), any(DiscussionReplyOrder.class), anyBoolean(), any(AsyncCallback.class));
		discussionThreadWidget.configureReplies();
		verify(mockSynAlert).clear();
		verify(mockDiscussionForumClientAsync).getRepliesForThread(anyString(),
				anyLong(), anyLong(), any(DiscussionReplyOrder.class), anyBoolean(),
				any(AsyncCallback.class));
		verify(mockView).clearReplies();
		verify(mockView).setShowRepliesVisibility(true);
		verify(mockView, atLeastOnce()).setNumberOfReplies(anyString());
		verify(mockView).showReplyDetails();
		verify(mockView).setLoadMoreButtonVisibility(anyBoolean());
		verify(mockView, times(2)).addReply(any(Widget.class));
		verify(mockGinInjector, times(2)).createReplyWidget();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testConfigureRepliesFailure() {
		DiscussionThreadBundle threadBundle = createThreadBundle("1", "title",
				Arrays.asList("123"), 0L, 2L, new Date());
		discussionThreadWidget.configure(threadBundle );
		AsyncMockStubber.callFailureWith(new Exception())
				.when(mockDiscussionForumClientAsync).getRepliesForThread(anyString(), anyLong(),
						anyLong(), any(DiscussionReplyOrder.class), anyBoolean(), any(AsyncCallback.class));
		discussionThreadWidget.configureReplies();
		verify(mockSynAlert).clear();
		verify(mockDiscussionForumClientAsync).getRepliesForThread(anyString(),
				anyLong(), anyLong(), any(DiscussionReplyOrder.class), anyBoolean(),
				any(AsyncCallback.class));
		verify(mockView).clearReplies();
		verify(mockView, never()).setLoadMoreButtonVisibility(anyBoolean());
		verify(mockView, never()).addReply(any(Widget.class));
		verify(mockGinInjector, never()).createReplyWidget();
		verify(mockSynAlert).handleException(any(Throwable.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testConfigureRepliesHasNextPage() {
		DiscussionThreadBundle threadBundle = createThreadBundle("1", "title",
				Arrays.asList("123"), 2L, 2L, new Date());
		discussionThreadWidget.configure(threadBundle );
		bundleList = createReplyBundleList(2);
		when(mockReplyBundlePage.getTotalNumberOfResults()).thenReturn(LIMIT+1);
		when(mockReplyBundlePage.getResults()).thenReturn(bundleList);
		AsyncMockStubber.callSuccessWith(mockReplyBundlePage)
				.when(mockDiscussionForumClientAsync).getRepliesForThread(anyString(), anyLong(),
						anyLong(), any(DiscussionReplyOrder.class), anyBoolean(), any(AsyncCallback.class));
		discussionThreadWidget.configureReplies();
		verify(mockSynAlert).clear();
		verify(mockDiscussionForumClientAsync).getRepliesForThread(anyString(),
				anyLong(), anyLong(), any(DiscussionReplyOrder.class), anyBoolean(),
				any(AsyncCallback.class));
		verify(mockView).clearReplies();
		verify(mockView, atLeastOnce()).setShowRepliesVisibility(true);
		verify(mockView, atLeastOnce()).setNumberOfReplies(anyString());
		verify(mockView).showReplyDetails();
		verify(mockView).setLoadMoreButtonVisibility(true);
		verify(mockView, times(2)).addReply(any(Widget.class));
		verify(mockGinInjector, times(2)).createReplyWidget();
	}

	private DiscussionThreadBundle createThreadBundle(String threadId, String title,
			List<String> activeAuthors, Long numberOfReplies,
			Long numberOfViews, Date lastActivity) {
		DiscussionThreadBundle threadBundle = new DiscussionThreadBundle();
		threadBundle.setId(threadId);
		threadBundle.setTitle(title);
		threadBundle.setActiveAuthors(activeAuthors);
		threadBundle.setNumberOfReplies(numberOfReplies);
		threadBundle.setNumberOfViews(numberOfViews);
		threadBundle.setLastActivity(lastActivity);
		return threadBundle;
	}

	private List<DiscussionReplyBundle> createReplyBundleList(int numberOfBundle) {
		List<DiscussionReplyBundle> list = new ArrayList<DiscussionReplyBundle>();
		for (int i = 0; i < numberOfBundle; i++) {
			DiscussionReplyBundle bundle = new DiscussionReplyBundle();
			list.add(bundle);
		}
		return list;
	}
}
