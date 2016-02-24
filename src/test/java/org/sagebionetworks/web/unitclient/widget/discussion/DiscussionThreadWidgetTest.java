package org.sagebionetworks.web.unitclient.widget.discussion;

import static org.sagebionetworks.web.client.widget.discussion.DiscussionThreadWidget.*;
import static org.sagebionetworks.web.client.widget.discussion.ForumWidget.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.gwtbootstrap3.extras.bootbox.client.callback.AlertCallback;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.discussion.DiscussionFilter;
import org.sagebionetworks.repo.model.discussion.DiscussionReplyBundle;
import org.sagebionetworks.repo.model.discussion.DiscussionReplyOrder;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadBundle;
import org.sagebionetworks.web.client.DiscussionForumClientAsync;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.RequestBuilderWrapper;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.discussion.DiscussionThreadWidget;
import org.sagebionetworks.web.client.widget.discussion.DiscussionThreadWidgetView;
import org.sagebionetworks.web.client.widget.discussion.ReplyWidget;
import org.sagebionetworks.web.client.widget.discussion.modal.EditDiscussionThreadModal;
import org.sagebionetworks.web.client.widget.discussion.modal.NewReplyModal;
import org.sagebionetworks.web.client.widget.entity.MarkdownWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.user.BadgeSize;
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
	@Mock
	AuthenticationController mockAuthController;
	@Mock
	GlobalApplicationState mockGlobalApplicationState;
	@Mock
	PlaceChanger mockPlaceChanger;
	@Mock
	EditDiscussionThreadModal mockEditThreadModal;
	@Mock
	MarkdownWidget mockMarkdownWidget;
	@Mock
	Callback mockCallback;
	@Mock
	UserBadge mockAuthorIconWidget;
	@Mock
	GWTWrapper mockGwtWrapper;

	DiscussionThreadWidget discussionThreadWidget;
	List<DiscussionReplyBundle> bundleList;
	private static final String CREATED_BY = "123";
	private static final String NON_AUTHOR = "456";

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		when(mockGinInjector.createReplyWidget()).thenReturn(mockReplyWidget);
		when(mockGinInjector.getUserBadgeWidget()).thenReturn(mockUserBadge);
		discussionThreadWidget = new DiscussionThreadWidget(mockView, mockNewReplyModal,
				mockSynAlert, mockAuthorWidget, mockDiscussionForumClientAsync,
				mockGinInjector, mockJsniUtils, mockRequestBuilder, mockAuthController,
				mockGlobalApplicationState, mockEditThreadModal, mockMarkdownWidget,
				mockAuthorIconWidget, mockGwtWrapper);
		when(mockAuthController.isLoggedIn()).thenReturn(true);
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		when(mockAuthController.getCurrentUserPrincipalId()).thenReturn(NON_AUTHOR);
	}

	@Test
	public void testConstructor() {
		verify(mockView).setPresenter(discussionThreadWidget);
		verify(mockView).setNewReplyModal(any(Widget.class));
		verify(mockView).setAlert(any(Widget.class));
		verify(mockView).setAuthor(any(Widget.class));
		verify(mockView).setEditThreadModal(any(Widget.class));
		verify(mockView).setThreadAuthor(any(Widget.class));
	}

	@Test
	public void testConfigure() {
		boolean isDeleted = false;
		boolean canModerate = false;
		boolean isEdited = false;
		DiscussionThreadBundle threadBundle = createThreadBundle("1", "title",
				Arrays.asList("123"), 1L, 2L, new Date(), "messageKey", isDeleted,
				CREATED_BY, isEdited);
		discussionThreadWidget.configure(threadBundle, canModerate, mockCallback,
				SHOW_THREAD_DETAILS_FOR_THREAD_LIST, SHOW_REPLY_DETAILS_FOR_THREAD_LIST);
		verify(mockView).clear();
		verify(mockView).setTitle("title");
		verify(mockView).addActiveAuthor(any(Widget.class));
		verify(mockView).setNumberOfReplies("1", REPLY);
		verify(mockView).setNumberOfViews("2");
		verify(mockView).setLastActivity(anyString());
		verify(mockView).setCreatedOn(anyString());
		verify(mockView).setShowRepliesVisibility(true);
		verify(mockGinInjector).getUserBadgeWidget();
		verify(mockJsniUtils, times(2)).getRelativeTime(any(Date.class));
		verify(mockNewReplyModal).configure(anyString(), any(Callback.class));
		verify(mockAuthorWidget).configure(anyString());
		verify(mockView).setDeleteIconVisible(false);
		verify(mockView).setEditIconVisible(false);
		verify(mockView).setEditedVisible(false);
		verify(mockAuthorIconWidget).configure(anyString());
		verify(mockAuthorIconWidget).setSize(any(BadgeSize.class));
		verify(mockView).hideThreadDetails();
		verify(mockView).hideReplyDetails();
		verify(mockView).setThreadLink(anyString());
	}

	@Test
	public void testConfigureSingleThread() {
		boolean isDeleted = false;
		boolean canModerate = false;
		boolean isEdited = false;
		DiscussionThreadBundle threadBundle = createThreadBundle("1", "title",
				Arrays.asList("123"), 1L, 2L, new Date(), "messageKey", isDeleted,
				CREATED_BY, isEdited);
		discussionThreadWidget.configure(threadBundle, canModerate, mockCallback,
				SHOW_THREAD_DETAILS_FOR_SINGLE_THREAD, SHOW_REPLY_DETAILS_FOR_SINGLE_THREAD);
		verify(mockView).showThreadDetails();
		verify(mockView).showReplyDetails();
	}

	@Test
	public void testConfigureWithEdited() {
		boolean isDeleted = false;
		boolean canModerate = false;
		boolean isEdited = true;
		DiscussionThreadBundle threadBundle = createThreadBundle("1", "title",
				Arrays.asList("123"), 1L, 2L, new Date(), "messageKey", isDeleted,
				CREATED_BY, isEdited);
		discussionThreadWidget.configure(threadBundle, canModerate, mockCallback,
				SHOW_THREAD_DETAILS_FOR_THREAD_LIST, SHOW_REPLY_DETAILS_FOR_THREAD_LIST);
		verify(mockView).setEditedVisible(true);
	}

	@Test
	public void testConfigureAuthor() {
		boolean isDeleted = false;
		boolean canModerate = false;
		boolean isEdited = false;
		when(mockAuthController.getCurrentUserPrincipalId()).thenReturn(CREATED_BY);
		DiscussionThreadBundle threadBundle = createThreadBundle("1", "title",
				Arrays.asList("123"), 1L, 2L, new Date(), "messageKey", isDeleted,
				CREATED_BY, isEdited);
		discussionThreadWidget.configure(threadBundle, canModerate, mockCallback,
				SHOW_THREAD_DETAILS_FOR_THREAD_LIST, SHOW_REPLY_DETAILS_FOR_THREAD_LIST);
		verify(mockView).setEditIconVisible(true);
	}

	@Test
	public void testConfigureWithModerator() {
		boolean isDeleted = false;
		boolean canModerate = true;
		boolean isEdited = false;
		DiscussionThreadBundle threadBundle = createThreadBundle("1", "title",
				Arrays.asList("123"), 1L, 2L, new Date(), "messageKey", isDeleted,
				CREATED_BY, isEdited);
		discussionThreadWidget.configure(threadBundle, canModerate, mockCallback,
				SHOW_THREAD_DETAILS_FOR_THREAD_LIST, SHOW_REPLY_DETAILS_FOR_THREAD_LIST);
		verify(mockView).clear();
		verify(mockView).setTitle("title");
		verify(mockView).addActiveAuthor(any(Widget.class));
		verify(mockView).setNumberOfReplies("1", REPLY);
		verify(mockView).setNumberOfViews("2");
		verify(mockView).setLastActivity(anyString());
		verify(mockView).setCreatedOn(anyString());
		verify(mockView).setShowRepliesVisibility(true);
		verify(mockGinInjector).getUserBadgeWidget();
		verify(mockJsniUtils, times(2)).getRelativeTime(any(Date.class));
		verify(mockNewReplyModal).configure(anyString(), any(Callback.class));
		verify(mockAuthorWidget).configure(anyString());
		verify(mockView).setDeleteIconVisible(true);
		verify(mockView).setEditedVisible(false);
	}

	@Test
	public void testConfigureWithZeroReplies(){
		boolean isDeleted = false;
		boolean canModerate = false;
		boolean isEdited = false;
		DiscussionThreadBundle threadBundle = createThreadBundle("1", "title",
				Arrays.asList("123"), 0L, 2L, new Date(), "messageKey", isDeleted,
				CREATED_BY, isEdited);
		discussionThreadWidget.configure(threadBundle, canModerate, mockCallback,
				SHOW_THREAD_DETAILS_FOR_THREAD_LIST, SHOW_REPLY_DETAILS_FOR_THREAD_LIST);
		verify(mockView).clear();
		verify(mockView).setTitle("title");
		verify(mockView).addActiveAuthor(any(Widget.class));
		verify(mockView).setNumberOfReplies("0", REPLIES);
		verify(mockView).setNumberOfViews("2");
		verify(mockView).setLastActivity(anyString());
		verify(mockView).setCreatedOn(anyString());
		verify(mockView).setShowRepliesVisibility(false);
		verify(mockGinInjector).getUserBadgeWidget();
		verify(mockJsniUtils, times(2)).getRelativeTime(any(Date.class));
		verify(mockNewReplyModal).configure(anyString(), any(Callback.class));
		verify(mockAuthorWidget).configure(anyString());
		verify(mockView).setDeleteIconVisible(false);
	}

	@Test
	public void asWidgetTest() {
		discussionThreadWidget.asWidget();
		verify(mockView).asWidget();
	}

	@Test
	public void testToggleThreadWithThreadDetailsCollapsed() {
		boolean isDeleted = false;
		boolean canModerate = false;
		boolean isEdited = false;
		DiscussionThreadBundle threadBundle = createThreadBundle("1", "title",
				Arrays.asList("123"), 1L, 2L, new Date(), "messageKey", isDeleted,
				CREATED_BY, isEdited);
		discussionThreadWidget.configure(threadBundle, canModerate, mockCallback,
				SHOW_THREAD_DETAILS_FOR_THREAD_LIST, SHOW_REPLY_DETAILS_FOR_THREAD_LIST);
		when(mockView.isThreadCollapsed()).thenReturn(true);
		discussionThreadWidget.toggleThread();
		verify(mockView).setThreadDownIconVisible(false);
		verify(mockView).setThreadUpIconVisible(true);
		verify(mockView).showThreadDetails();
		verify(mockView).setDeleteIconVisible(false);
	}

	@Test
	public void testToggleThreadWithThreadDetailsExpanded() {
		boolean isDeleted = false;
		boolean canModerate = false;
		boolean isEdited = false;
		DiscussionThreadBundle threadBundle = createThreadBundle("1", "title",
				Arrays.asList("123"), 1L, 2L, new Date(), "messageKey", isDeleted,
				CREATED_BY, isEdited);
		discussionThreadWidget.configure(threadBundle, canModerate, mockCallback,
				SHOW_THREAD_DETAILS_FOR_THREAD_LIST, SHOW_REPLY_DETAILS_FOR_THREAD_LIST);
		verify(mockView).setThreadDownIconVisible(true);
		verify(mockView).setThreadUpIconVisible(false);
		verify(mockView).hideThreadDetails();
		when(mockView.isThreadCollapsed()).thenReturn(false);
		discussionThreadWidget.toggleThread();
		verify(mockView, times(2)).setThreadDownIconVisible(true);
		verify(mockView, times(2)).setThreadUpIconVisible(false);
		verify(mockView, times(2)).hideThreadDetails();
	}

	@Test
	public void testToggleRepliesWithReplyDetailsCollapsed() {
		when(mockView.isReplyCollapsed()).thenReturn(true);
		discussionThreadWidget.toggleReplies();
		verify(mockView).setReplyDownIconVisible(false);
		verify(mockView).setReplyUpIconVisible(true);
		verify(mockView).showReplyDetails();
	}

	@Test
	public void testToggleRepliesWithReplyDetailsExpanded() {
		when(mockView.isReplyCollapsed()).thenReturn(false);
		discussionThreadWidget.toggleReplies();
		verify(mockView).setReplyDownIconVisible(true);
		verify(mockView).setReplyUpIconVisible(false);
		verify(mockView).hideReplyDetails();
		verify(mockView).setLoadMoreButtonVisibility(false);
	}

	@Test
	public void testOnClickNewReply() {
		discussionThreadWidget.onClickNewReply();
		verify(mockNewReplyModal).show();
	}

	@Test
	public void testOnClickNewReplyAnonymous() {
		when(mockAuthController.isLoggedIn()).thenReturn(false);
		discussionThreadWidget.onClickNewReply();
		verify(mockNewReplyModal, never()).show();
		verify(mockGlobalApplicationState).getPlaceChanger();
		verify(mockView).showErrorMessage(anyString());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testConfigureReplies() {
		boolean isDeleted = false;
		boolean canModerate = false;
		boolean isEdited = false;
		DiscussionThreadBundle threadBundle = createThreadBundle("1", "title",
				Arrays.asList("123"), 0L, 2L, new Date(), "messageKey", isDeleted,
				CREATED_BY, isEdited);
		discussionThreadWidget.configure(threadBundle, canModerate, mockCallback,
				SHOW_THREAD_DETAILS_FOR_THREAD_LIST, SHOW_REPLY_DETAILS_FOR_THREAD_LIST);
		discussionThreadWidget.configureReplies();
		verify(mockSynAlert).clear();
		verify(mockView).clearReplies();
		verify(mockDiscussionForumClientAsync).getRepliesForThread(anyString(),
				anyLong(), anyLong(), any(DiscussionReplyOrder.class), anyBoolean(),
				any(DiscussionFilter.class), any(AsyncCallback.class));
		verify(mockView).setDeleteIconVisible(false);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testLoadmoreSuccess() {
		boolean isDeleted = false;
		boolean canModerate = false;
		boolean isEdited = false;
		DiscussionThreadBundle threadBundle = createThreadBundle("1", "title",
				Arrays.asList("123"), 0L, 2L, new Date(), "messageKey", isDeleted,
				CREATED_BY, isEdited);
		discussionThreadWidget.configure(threadBundle, canModerate, mockCallback,
				SHOW_THREAD_DETAILS_FOR_THREAD_LIST, SHOW_REPLY_DETAILS_FOR_THREAD_LIST);
		bundleList = createReplyBundleList(2);
		when(mockReplyBundlePage.getTotalNumberOfResults()).thenReturn(2L);
		when(mockReplyBundlePage.getResults()).thenReturn(bundleList);
		AsyncMockStubber.callSuccessWith(mockReplyBundlePage)
				.when(mockDiscussionForumClientAsync).getRepliesForThread(anyString(), anyLong(),
						anyLong(), any(DiscussionReplyOrder.class), anyBoolean(), 
						any(DiscussionFilter.class), any(AsyncCallback.class));
		discussionThreadWidget.configureReplies();
		verify(mockSynAlert).clear();
		verify(mockDiscussionForumClientAsync).getRepliesForThread(anyString(),
				anyLong(), anyLong(), any(DiscussionReplyOrder.class), anyBoolean(),
				any(DiscussionFilter.class), any(AsyncCallback.class));
		verify(mockView).clearReplies();
		verify(mockView).setShowRepliesVisibility(true);
		verify(mockView, atLeastOnce()).setNumberOfReplies(anyString(), anyString());
		verify(mockView).showReplyDetails();
		verify(mockView, times(2)).addReply(any(Widget.class));
		verify(mockGinInjector, times(2)).createReplyWidget();
		verify(mockView).setLoadingRepliesVisible(true);
		verify(mockView).setLoadingRepliesVisible(false);
		verify(mockView).setDeleteIconVisible(false);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testLoadmoreEmpty() {
		boolean isDeleted = false;
		boolean canModerate = false;
		boolean isEdited = false;
		DiscussionThreadBundle threadBundle = createThreadBundle("1", "title",
				Arrays.asList("123"), 0L, 2L, new Date(), "messageKey", isDeleted,
				CREATED_BY, isEdited);
		discussionThreadWidget.configure(threadBundle, canModerate, mockCallback,
				SHOW_THREAD_DETAILS_FOR_THREAD_LIST, SHOW_REPLY_DETAILS_FOR_THREAD_LIST);
		bundleList = createReplyBundleList(0);
		when(mockReplyBundlePage.getTotalNumberOfResults()).thenReturn(2L);
		when(mockReplyBundlePage.getResults()).thenReturn(bundleList);
		AsyncMockStubber.callSuccessWith(mockReplyBundlePage)
				.when(mockDiscussionForumClientAsync).getRepliesForThread(anyString(), anyLong(),
						anyLong(), any(DiscussionReplyOrder.class), anyBoolean(), 
						any(DiscussionFilter.class), any(AsyncCallback.class));
		discussionThreadWidget.configureReplies();
		verify(mockSynAlert).clear();
		verify(mockDiscussionForumClientAsync).getRepliesForThread(anyString(),
				anyLong(), anyLong(), any(DiscussionReplyOrder.class), anyBoolean(),
				any(DiscussionFilter.class), any(AsyncCallback.class));
		verify(mockView).clearReplies();
		verify(mockView, atLeastOnce()).setShowRepliesVisibility(false);
		verify(mockView, never()).setShowRepliesVisibility(true);
		verify(mockView, times(2)).hideReplyDetails();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testLoadmoreFailure() {
		boolean isDeleted = false;
		boolean canModerate = false;
		boolean isEdited = false;
		DiscussionThreadBundle threadBundle = createThreadBundle("1", "title",
				Arrays.asList("123"), 0L, 2L, new Date(), "messageKey", isDeleted,
				CREATED_BY, isEdited);
		discussionThreadWidget.configure(threadBundle, canModerate, mockCallback,
				SHOW_THREAD_DETAILS_FOR_THREAD_LIST, SHOW_REPLY_DETAILS_FOR_THREAD_LIST);
		AsyncMockStubber.callFailureWith(new Exception())
				.when(mockDiscussionForumClientAsync).getRepliesForThread(anyString(), anyLong(),
						anyLong(), any(DiscussionReplyOrder.class), anyBoolean(),
						any(DiscussionFilter.class), any(AsyncCallback.class));
		discussionThreadWidget.configureReplies();
		verify(mockSynAlert).clear();
		verify(mockDiscussionForumClientAsync).getRepliesForThread(anyString(),
				anyLong(), anyLong(), any(DiscussionReplyOrder.class), anyBoolean(),
				any(DiscussionFilter.class), any(AsyncCallback.class));
		verify(mockView).clearReplies();
		verify(mockView, never()).addReply(any(Widget.class));
		verify(mockGinInjector, never()).createReplyWidget();
		verify(mockSynAlert).handleException(any(Throwable.class));
		verify(mockView).setLoadingRepliesVisible(true);
		verify(mockView).setLoadingRepliesVisible(false);
		verify(mockView).setDeleteIconVisible(false);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testLoadmoreHasNextPage() {
		boolean isDeleted = false;
		boolean canModerate = false;
		boolean isEdited = false;
		DiscussionThreadBundle threadBundle = createThreadBundle("1", "title",
				Arrays.asList("123"), 2L, 2L, new Date(), "messageKey", isDeleted,
				CREATED_BY, isEdited);
		discussionThreadWidget.configure(threadBundle, canModerate, mockCallback,
				SHOW_THREAD_DETAILS_FOR_THREAD_LIST, SHOW_REPLY_DETAILS_FOR_THREAD_LIST);
		bundleList = createReplyBundleList(2);
		when(mockReplyBundlePage.getTotalNumberOfResults()).thenReturn(LIMIT+1);
		when(mockReplyBundlePage.getResults()).thenReturn(bundleList);
		AsyncMockStubber.callSuccessWith(mockReplyBundlePage)
				.when(mockDiscussionForumClientAsync).getRepliesForThread(anyString(), anyLong(),
						anyLong(), any(DiscussionReplyOrder.class), anyBoolean(),
						any(DiscussionFilter.class), any(AsyncCallback.class));
		discussionThreadWidget.configureReplies();
		verify(mockSynAlert).clear();
		verify(mockDiscussionForumClientAsync).getRepliesForThread(anyString(),
				anyLong(), anyLong(), any(DiscussionReplyOrder.class), anyBoolean(),
				any(DiscussionFilter.class), any(AsyncCallback.class));
		verify(mockView).clearReplies();
		verify(mockView, atLeastOnce()).setShowRepliesVisibility(true);
		verify(mockView, atLeastOnce()).setNumberOfReplies(anyString(), anyString());
		verify(mockView).showReplyDetails();
		verify(mockView).setLoadMoreButtonVisibility(true);
		verify(mockView, times(2)).addReply(any(Widget.class));
		verify(mockGinInjector, times(2)).createReplyWidget();
		verify(mockView).setLoadingRepliesVisible(true);
		verify(mockView).setLoadingRepliesVisible(false);
		verify(mockView).setDeleteIconVisible(false);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testConfigureMessageFailToGetMessageURL() throws RequestException {
		boolean isDeleted = false;
		boolean canModerate = false;
		boolean isEdited = false;
		DiscussionThreadBundle bundle = createThreadBundle("123", "title", Arrays.asList("1"),
				1L, 1L, new Date(), "messageKey", isDeleted, CREATED_BY, isEdited);
		AsyncMockStubber.callFailureWith(new Exception())
				.when(mockDiscussionForumClientAsync).getThreadUrl(anyString(), any(AsyncCallback.class));
		discussionThreadWidget.configure(bundle, canModerate, mockCallback,
				SHOW_THREAD_DETAILS_FOR_THREAD_LIST, SHOW_REPLY_DETAILS_FOR_THREAD_LIST);
		discussionThreadWidget.configureMessage();
		verify(mockSynAlert).clear();
		verify(mockRequestBuilder, never()).configure(eq(RequestBuilder.GET), anyString());
		verify(mockRequestBuilder, never()).setHeader(WebConstants.CONTENT_TYPE, WebConstants.TEXT_PLAIN_CHARSET_UTF8);
		verify(mockSynAlert).handleException(any(Throwable.class));
		verify(mockView).setDeleteIconVisible(false);
		verify(mockView).setLoadingMessageVisible(true);
		verify(mockView).setLoadingMessageVisible(false);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testConfigureMessageFailToGetMessage() throws RequestException {
		boolean isDeleted = false;
		boolean canModerate = false;
		boolean isEdited = false;
		DiscussionThreadBundle bundle = createThreadBundle("123", "title", Arrays.asList("1"),
				1L, 1L, new Date(), "messageKey", isDeleted, CREATED_BY, isEdited);
		when(mockResponse.getStatusCode()).thenReturn(Response.SC_OK+1);
		String url = "url";
		AsyncMockStubber.callSuccessWith(url)
				.when(mockDiscussionForumClientAsync).getThreadUrl(anyString(), any(AsyncCallback.class));
		RequestBuilderMockStubber.callOnError(null, new Exception())
				.when(mockRequestBuilder).sendRequest(anyString(), any(RequestCallback.class));
		discussionThreadWidget.configure(bundle, canModerate, mockCallback,
				SHOW_THREAD_DETAILS_FOR_THREAD_LIST, SHOW_REPLY_DETAILS_FOR_THREAD_LIST);
		discussionThreadWidget.configureMessage();
		verify(mockSynAlert).clear();
		verify(mockRequestBuilder).configure(eq(RequestBuilder.GET), anyString());
		verify(mockRequestBuilder).setHeader(WebConstants.CONTENT_TYPE, WebConstants.TEXT_PLAIN_CHARSET_UTF8);
		verify(mockSynAlert).handleException(any(Throwable.class));
		verify(mockMarkdownWidget, never()).configure(anyString());
		verify(mockView).setDeleteIconVisible(false);
		verify(mockView).setLoadingMessageVisible(true);
		verify(mockView).setLoadingMessageVisible(false);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testConfigureMessageSuccess() throws RequestException {
		boolean isDeleted = false;
		boolean canModerate = false;
		boolean isEdited = false;
		DiscussionThreadBundle bundle = createThreadBundle("123", "title", Arrays.asList("1"),
				1L, 1L, new Date(), "messageKey", isDeleted, CREATED_BY, isEdited);
		when(mockResponse.getStatusCode()).thenReturn(Response.SC_OK);
		String message = "message";
		when(mockResponse.getText()).thenReturn(message);
		String url = "url";
		AsyncMockStubber.callSuccessWith(url)
				.when(mockDiscussionForumClientAsync).getThreadUrl(anyString(), any(AsyncCallback.class));
		RequestBuilderMockStubber.callOnResponseReceived(null, mockResponse)
				.when(mockRequestBuilder).sendRequest(anyString(), any(RequestCallback.class));
		discussionThreadWidget.configure(bundle, canModerate, mockCallback,
				SHOW_THREAD_DETAILS_FOR_THREAD_LIST, SHOW_REPLY_DETAILS_FOR_THREAD_LIST);
		discussionThreadWidget.configureMessage();
		verify(mockSynAlert).clear();
		verify(mockRequestBuilder).configure(eq(RequestBuilder.GET), anyString());
		verify(mockRequestBuilder).setHeader(WebConstants.CONTENT_TYPE, WebConstants.TEXT_PLAIN_CHARSET_UTF8);
		verify(mockSynAlert, never()).handleException(any(Throwable.class));
		verify(mockMarkdownWidget).configure(anyString());
		verify(mockView).setDeleteIconVisible(false);
		verify(mockEditThreadModal).configure(anyString(), anyString(), anyString(), any(Callback.class));
		verify(mockView).setLoadingMessageVisible(true);
		verify(mockView).setLoadingMessageVisible(false);
	}

	@Test
	public void testOnClickDeleteThread() {
		discussionThreadWidget.onClickDeleteThread();
		verify(mockView).showDeleteConfirm(anyString(), any(AlertCallback.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testDeleteThreadSuccess() {
		boolean isDeleted = false;
		boolean canModerate = true;
		boolean isEdited = false;
		DiscussionThreadBundle threadBundle = createThreadBundle("1", "title",
				Arrays.asList("123"), 1L, 2L, new Date(), "messageKey", isDeleted,
				CREATED_BY, isEdited);
		discussionThreadWidget.configure(threadBundle, canModerate, mockCallback,
				SHOW_THREAD_DETAILS_FOR_THREAD_LIST, SHOW_REPLY_DETAILS_FOR_THREAD_LIST);
		AsyncMockStubber.callSuccessWith((Void) null)
				.when(mockDiscussionForumClientAsync).markThreadAsDeleted(anyString(), any(AsyncCallback.class));
		discussionThreadWidget.deleteThread();
		verify(mockSynAlert).clear();
		verify(mockDiscussionForumClientAsync).markThreadAsDeleted(eq("1"), any(AsyncCallback.class));
		verify(mockCallback).invoke();
		verify(mockView).showSuccess(anyString(), anyString());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testDeleteThreadFailure() {
		boolean isDeleted = false;
		boolean canModerate = true;
		boolean isEdited = false;
		DiscussionThreadBundle threadBundle = createThreadBundle("1", "title",
				Arrays.asList("123"), 1L, 2L, new Date(), "messageKey", isDeleted,
				CREATED_BY, isEdited);
		discussionThreadWidget.configure(threadBundle, canModerate, mockCallback,
				SHOW_THREAD_DETAILS_FOR_THREAD_LIST, SHOW_REPLY_DETAILS_FOR_THREAD_LIST);
		AsyncMockStubber.callFailureWith(new Exception())
				.when(mockDiscussionForumClientAsync).markThreadAsDeleted(anyString(), any(AsyncCallback.class));
		discussionThreadWidget.deleteThread();
		verify(mockSynAlert).clear();
		verify(mockDiscussionForumClientAsync).markThreadAsDeleted(eq("1"), any(AsyncCallback.class));
		verify(mockSynAlert).handleException(any(Throwable.class));
	}

	@Test
	public void testOnClickEditThread() {
		discussionThreadWidget.onClickEditThread();
		verify(mockEditThreadModal).show();
	}

	private DiscussionThreadBundle createThreadBundle(String threadId, String title,
			List<String> activeAuthors, Long numberOfReplies, Long numberOfViews,
			Date lastActivity, String messageKey, Boolean isDeleted, String createdBy,
			Boolean isEdited) {
		DiscussionThreadBundle threadBundle = new DiscussionThreadBundle();
		threadBundle.setId(threadId);
		threadBundle.setTitle(title);
		threadBundle.setActiveAuthors(activeAuthors);
		threadBundle.setNumberOfReplies(numberOfReplies);
		threadBundle.setNumberOfViews(numberOfViews);
		threadBundle.setLastActivity(lastActivity);
		threadBundle.setMessageKey(messageKey);
		threadBundle.setIsDeleted(isDeleted);
		threadBundle.setCreatedBy(createdBy);
		threadBundle.setIsEdited(isEdited);
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

	@Test
	public void testBuildThreadLink() {
		String projectId = "syn123";
		String threadId = "456";
		String hostURL = "hostURL/";
		when(mockGwtWrapper.getHostPageBaseURL()).thenReturn(hostURL);
		assertEquals("hostURL/#!Synapse:syn123/discussion/threadId=456",
				DiscussionThreadWidget.buildThreadLink(projectId, threadId, mockGwtWrapper));
	}
}
