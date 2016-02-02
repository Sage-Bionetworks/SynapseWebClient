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

import org.gwtbootstrap3.extras.bootbox.client.callback.ConfirmCallback;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.discussion.DiscussionReplyBundle;
import org.sagebionetworks.repo.model.discussion.DiscussionReplyOrder;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadBundle;
import org.sagebionetworks.web.client.DiscussionForumClientAsync;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.RequestBuilderWrapper;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.discussion.ReplyWidget;
import org.sagebionetworks.web.client.widget.discussion.DiscussionThreadWidget;
import org.sagebionetworks.web.client.widget.discussion.DiscussionThreadWidgetView;
import org.sagebionetworks.web.client.widget.discussion.modal.NewReplyModal;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import org.sagebionetworks.web.test.helper.RequestBuilderMockStubber;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
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
	SynapseJSNIUtils mockJsniUtils;
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
	@Mock
	UserBadge mockAuthorWidget;
	@Mock
	RequestBuilderWrapper mockRequestBuilder;
	@Mock
	Response mockResponse;

	DiscussionThreadWidget discussionThreadWidget;
	List<DiscussionReplyBundle> bundleList;

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		when(mockGinInjector.createReplyWidget()).thenReturn(mockReplyWidget);
		when(mockGinInjector.getUserBadgeWidget()).thenReturn(mockUserBadge);
		discussionThreadWidget = new DiscussionThreadWidget(mockView, mockNewReplyModal,
				mockSynAlert, mockAuthorWidget, mockDiscussionForumClientAsync,
				mockGinInjector, mockJsniUtils, mockRequestBuilder);
	}

	@Test
	public void testConstructor() {
		verify(mockView).setPresenter(discussionThreadWidget);
		verify(mockView).setNewReplyModal(any(Widget.class));
		verify(mockView).setAlert(any(Widget.class));
		verify(mockView).setAuthor(any(Widget.class));
	}

	@Test
	public void testConfigure() {
		DiscussionThreadBundle threadBundle = createThreadBundle("1", "title",
				Arrays.asList("123"), 1L, 2L, new Date(), "messageKey", false);
		discussionThreadWidget.configure(threadBundle, false);
		verify(mockView).clear();
		verify(mockView).setTitle("title");
		verify(mockView).addActiveAuthor(any(Widget.class));
		verify(mockView).setNumberOfReplies("1");
		verify(mockView).setNumberOfViews("2");
		verify(mockView).setLastActivity(anyString());
		verify(mockView).setCreatedOn(anyString());
		verify(mockView).setShowRepliesVisibility(true);
		verify(mockGinInjector).getUserBadgeWidget();
		verify(mockJsniUtils, times(2)).getRelativeTime(any(Date.class));
		verify(mockNewReplyModal).configure(anyString(), any(Callback.class));
		verify(mockAuthorWidget).configure(anyString());
		verify(mockView).setDeleteButtonVisible(false);
	}

	@Test
	public void testConfigureWithDeletedThread() {
		DiscussionThreadBundle threadBundle = createThreadBundle("1", "title",
				Arrays.asList("123"), 1L, 2L, new Date(), "messageKey", true);
		discussionThreadWidget.configure(threadBundle, false);
		verify(mockView).clear();
		verify(mockView).setTitle("title");
		verify(mockView).addActiveAuthor(any(Widget.class));
		verify(mockView).setNumberOfReplies("1");
		verify(mockView).setNumberOfViews("2");
		verify(mockView).setLastActivity(anyString());
		verify(mockView).setTitleAsDeleted();
		verify(mockView).setThreadDownIconVisible(false);
		verify(mockView).setThreadUpIconVisible(false);
		verify(mockView).disableToggle();
		verify(mockView, never()).setCreatedOn(anyString());
		verify(mockView, never()).setShowRepliesVisibility(true);
		verify(mockGinInjector).getUserBadgeWidget();
		verify(mockJsniUtils).getRelativeTime(any(Date.class));
		verify(mockNewReplyModal, never()).configure(anyString(), any(Callback.class));
		verify(mockAuthorWidget, never()).configure(anyString());
		verify(mockView, never()).setDeleteButtonVisible(false);
	}

	@Test
	public void testConfigureWithModerator() {
		DiscussionThreadBundle threadBundle = createThreadBundle("1", "title",
				Arrays.asList("123"), 1L, 2L, new Date(), "messageKey", false);
		discussionThreadWidget.configure(threadBundle, true);
		verify(mockView).clear();
		verify(mockView).setTitle("title");
		verify(mockView).addActiveAuthor(any(Widget.class));
		verify(mockView).setNumberOfReplies("1");
		verify(mockView).setNumberOfViews("2");
		verify(mockView).setLastActivity(anyString());
		verify(mockView).setCreatedOn(anyString());
		verify(mockView).setShowRepliesVisibility(true);
		verify(mockGinInjector).getUserBadgeWidget();
		verify(mockJsniUtils, times(2)).getRelativeTime(any(Date.class));
		verify(mockNewReplyModal).configure(anyString(), any(Callback.class));
		verify(mockAuthorWidget).configure(anyString());
		verify(mockView).setDeleteButtonVisible(true);
	}

	@Test
	public void testConfigureWithZeroReplies(){
		DiscussionThreadBundle threadBundle = createThreadBundle("1", "title",
				Arrays.asList("123"), 0L, 2L, new Date(), "messageKey", false);
		discussionThreadWidget.configure(threadBundle, false);
		verify(mockView).clear();
		verify(mockView).setTitle("title");
		verify(mockView).addActiveAuthor(any(Widget.class));
		verify(mockView).setNumberOfReplies("0");
		verify(mockView).setNumberOfViews("2");
		verify(mockView).setLastActivity(anyString());
		verify(mockView).setCreatedOn(anyString());
		verify(mockView).setShowRepliesVisibility(false);
		verify(mockGinInjector).getUserBadgeWidget();
		verify(mockJsniUtils, times(2)).getRelativeTime(any(Date.class));
		verify(mockNewReplyModal).configure(anyString(), any(Callback.class));
		verify(mockAuthorWidget).configure(anyString());
		verify(mockView).setDeleteButtonVisible(false);
	}

	@Test
	public void asWidgetTest() {
		discussionThreadWidget.asWidget();
		verify(mockView).asWidget();
	}

	@Test
	public void testToggleThreadWithThreadDetailsCollapsed() {
		DiscussionThreadBundle threadBundle = createThreadBundle("1", "title",
				Arrays.asList("123"), 1L, 2L, new Date(), "messageKey", false);
		discussionThreadWidget.configure(threadBundle, false);
		when(mockView.isThreadCollapsed()).thenReturn(true);
		discussionThreadWidget.toggleThread();
		verify(mockView).setThreadDownIconVisible(false);
		verify(mockView).setThreadUpIconVisible(true);
		verify(mockView).toggleThread();
		verify(mockView).setDeleteButtonVisible(false);
	}

	@Test
	public void testToggleThreadWithThreadDetailsExpanded() {
		DiscussionThreadBundle threadBundle = createThreadBundle("1", "title",
				Arrays.asList("123"), 1L, 2L, new Date(), "messageKey", false);
		discussionThreadWidget.configure(threadBundle, false);
		when(mockView.isThreadCollapsed()).thenReturn(false);
		discussionThreadWidget.toggleThread();
		verify(mockView).setThreadDownIconVisible(true);
		verify(mockView).setThreadUpIconVisible(false);
		verify(mockView).toggleThread();
	}

	@Test
	public void testToggleThreadWithDeletedThreadExpanded() {
		DiscussionThreadBundle threadBundle = createThreadBundle("1", "title",
				Arrays.asList("123"), 1L, 2L, new Date(), "messageKey", true);
		discussionThreadWidget.configure(threadBundle, false);
		when(mockView.isThreadCollapsed()).thenReturn(false);
		discussionThreadWidget.toggleThread();
		verify(mockView, never()).toggleThread();
	}

	@Test
	public void testToggleThreadWithDeletedThreadCollapse() {
		DiscussionThreadBundle threadBundle = createThreadBundle("1", "title",
				Arrays.asList("123"), 1L, 2L, new Date(), "messageKey", true);
		discussionThreadWidget.configure(threadBundle, false);
		when(mockView.isThreadCollapsed()).thenReturn(true);
		discussionThreadWidget.toggleThread();
		verify(mockView, never()).toggleThread();
	}

	@Test
	public void testToggleRepliesWithReplyDetailsCollapsed() {
		when(mockView.isReplyCollapsed()).thenReturn(true);
		discussionThreadWidget.toggleReplies();
		verify(mockView).setReplyDownIconVisible(false);
		verify(mockView).setReplyUpIconVisible(true);
		verify(mockView).toggleReplies();
	}

	@Test
	public void testToggleRepliesWithReplyDetailsExpanded() {
		when(mockView.isReplyCollapsed()).thenReturn(false);
		discussionThreadWidget.toggleReplies();
		verify(mockView).setReplyDownIconVisible(true);
		verify(mockView).setReplyUpIconVisible(false);
		verify(mockView).toggleReplies();
	}

	@Test
	public void testOnClickNewReply() {
		discussionThreadWidget.onClickNewReply();
		verify(mockNewReplyModal).show();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testConfigureReplies() {
		DiscussionThreadBundle threadBundle = createThreadBundle("1", "title",
				Arrays.asList("123"), 0L, 2L, new Date(), "messageKey", false);
		discussionThreadWidget.configure(threadBundle, false);
		discussionThreadWidget.configureReplies();
		verify(mockSynAlert).clear();
		verify(mockView).clearReplies();
		verify(mockDiscussionForumClientAsync).getRepliesForThread(anyString(),
				anyLong(), anyLong(), any(DiscussionReplyOrder.class), anyBoolean(),
				any(AsyncCallback.class));
		verify(mockView).setDeleteButtonVisible(false);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testLoadmoreSuccess() {
		DiscussionThreadBundle threadBundle = createThreadBundle("1", "title",
				Arrays.asList("123"), 0L, 2L, new Date(), "messageKey", false);
		discussionThreadWidget.configure(threadBundle, false);
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
		verify(mockView).setLoadingVisible(true);
		verify(mockView).setLoadingVisible(false);
		verify(mockView).setDeleteButtonVisible(false);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testLoadmoreFailure() {
		DiscussionThreadBundle threadBundle = createThreadBundle("1", "title",
				Arrays.asList("123"), 0L, 2L, new Date(), "messageKey", false);
		discussionThreadWidget.configure(threadBundle, false);
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
		verify(mockView).setLoadingVisible(true);
		verify(mockView).setLoadingVisible(false);
		verify(mockView).setDeleteButtonVisible(false);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testLoadmoreHasNextPage() {
		DiscussionThreadBundle threadBundle = createThreadBundle("1", "title",
				Arrays.asList("123"), 2L, 2L, new Date(), "messageKey", false);
		discussionThreadWidget.configure(threadBundle, false);
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
		verify(mockView).setLoadingVisible(true);
		verify(mockView).setLoadingVisible(false);
		verify(mockView).setDeleteButtonVisible(false);
	}

	@Test
	public void testConfigureMessageFailToGetMessage() throws RequestException {
		DiscussionThreadBundle bundle = createThreadBundle("123", "title", Arrays.asList("1"),
				1L, 1L, new Date(), "messageKey", false);
		RequestBuilderMockStubber.callOnError(null, new Exception())
				.when(mockRequestBuilder).sendRequest(anyString(), any(RequestCallback.class));
		discussionThreadWidget.configure(bundle, false);
		discussionThreadWidget.configureMessage();
		verify(mockSynAlert).clear();
		verify(mockRequestBuilder).configure(eq(RequestBuilder.GET), anyString());
		verify(mockRequestBuilder).setHeader(WebConstants.CONTENT_TYPE, WebConstants.TEXT_PLAIN_CHARSET_UTF8);
		verify(mockSynAlert).handleException(any(Throwable.class));
		verify(mockView).setDeleteButtonVisible(false);
	}

	@Test
	public void testConfigureMessageFailToGetMessageCase2() throws RequestException {
		DiscussionThreadBundle bundle = createThreadBundle("123", "title", Arrays.asList("1"),
				1L, 1L, new Date(), "messageKey", false);
		when(mockResponse.getStatusCode()).thenReturn(Response.SC_OK+1);
		RequestBuilderMockStubber.callOnResponseReceived(null, mockResponse)
				.when(mockRequestBuilder).sendRequest(anyString(), any(RequestCallback.class));
		discussionThreadWidget.configure(bundle, false);
		discussionThreadWidget.configureMessage();
		verify(mockSynAlert).clear();
		verify(mockRequestBuilder).configure(eq(RequestBuilder.GET), anyString());
		verify(mockRequestBuilder).setHeader(WebConstants.CONTENT_TYPE, WebConstants.TEXT_PLAIN_CHARSET_UTF8);
		verify(mockSynAlert).handleException(any(Throwable.class));
		verify(mockView, never()).setMessage(anyString());
		verify(mockView).setDeleteButtonVisible(false);
	}

	@Test
	public void testConfigureMessageSuccess() throws RequestException {
		DiscussionThreadBundle bundle = createThreadBundle("123", "title", Arrays.asList("1"),
				1L, 1L, new Date(), "messageKey", false);
		when(mockResponse.getStatusCode()).thenReturn(Response.SC_OK);
		String message = "message";
		when(mockResponse.getText()).thenReturn(message);
		RequestBuilderMockStubber.callOnResponseReceived(null, mockResponse)
				.when(mockRequestBuilder).sendRequest(anyString(), any(RequestCallback.class));
		discussionThreadWidget.configure(bundle, false);
		discussionThreadWidget.configureMessage();
		verify(mockSynAlert).clear();
		verify(mockRequestBuilder).configure(eq(RequestBuilder.GET), anyString());
		verify(mockRequestBuilder).setHeader(WebConstants.CONTENT_TYPE, WebConstants.TEXT_PLAIN_CHARSET_UTF8);
		verify(mockSynAlert, never()).handleException(any(Throwable.class));
		verify(mockView).setMessage(message);
		verify(mockView).setDeleteButtonVisible(false);
	}

	@Test
	public void testOnClickDeleteThread() {
		discussionThreadWidget.onClickDeleteThread();
		verify(mockView).showDeleteConfirm(anyString(), any(ConfirmCallback.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testDeleteThreadSuccess() {
		DiscussionThreadBundle threadBundle = createThreadBundle("1", "title",
				Arrays.asList("123"), 1L, 2L, new Date(), "messageKey", false);
		discussionThreadWidget.configure(threadBundle, true);
		AsyncMockStubber.callSuccessWith((Void) null)
				.when(mockDiscussionForumClientAsync).markThreadAsDeleted(anyString(), any(AsyncCallback.class));
		discussionThreadWidget.deleteThread();
		verify(mockSynAlert, times(2)).clear();
		verify(mockDiscussionForumClientAsync).markThreadAsDeleted(eq("1"), any(AsyncCallback.class));
		verify(mockView, atLeast(1)).clear();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testDeleteThreadFailure() {
		DiscussionThreadBundle threadBundle = createThreadBundle("1", "title",
				Arrays.asList("123"), 1L, 2L, new Date(), "messageKey", false);
		discussionThreadWidget.configure(threadBundle, true);
		AsyncMockStubber.callFailureWith(new Exception())
				.when(mockDiscussionForumClientAsync).markThreadAsDeleted(anyString(), any(AsyncCallback.class));
		discussionThreadWidget.deleteThread();
		verify(mockSynAlert).clear();
		verify(mockDiscussionForumClientAsync).markThreadAsDeleted(eq("1"), any(AsyncCallback.class));
		verify(mockSynAlert).handleException(any(Throwable.class));
	}

	@Test
	public void testResetCollapsed() {
		when(mockView.isThreadCollapsed()).thenReturn(true);
		discussionThreadWidget.reset();
		verify(mockView).clear();
		verify(mockView, never()).toggleThread();
	}

	@Test
	public void testResetExpanded() {
		when(mockView.isThreadCollapsed()).thenReturn(false);
		discussionThreadWidget.reset();
		verify(mockView).clear();
		verify(mockView).toggleThread();
	}

	private DiscussionThreadBundle createThreadBundle(String threadId, String title,
			List<String> activeAuthors, Long numberOfReplies, Long numberOfViews,
			Date lastActivity, String messageKey, Boolean isDeleted) {
		DiscussionThreadBundle threadBundle = new DiscussionThreadBundle();
		threadBundle.setId(threadId);
		threadBundle.setTitle(title);
		threadBundle.setActiveAuthors(activeAuthors);
		threadBundle.setNumberOfReplies(numberOfReplies);
		threadBundle.setNumberOfViews(numberOfViews);
		threadBundle.setLastActivity(lastActivity);
		threadBundle.setMessageKey(messageKey);
		threadBundle.setIsDeleted(isDeleted);
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
