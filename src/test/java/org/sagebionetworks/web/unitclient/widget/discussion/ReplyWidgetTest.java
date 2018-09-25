package org.sagebionetworks.web.unitclient.widget.discussion;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.discussion.DiscussionReplyBundle;
import org.sagebionetworks.web.client.DateTimeUtils;
import org.sagebionetworks.web.client.DiscussionForumClientAsync;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.RequestBuilderWrapper;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.cache.ClientCache;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.CopyTextModal;
import org.sagebionetworks.web.client.widget.discussion.ReplyWidget;
import org.sagebionetworks.web.client.widget.discussion.ReplyWidgetView;
import org.sagebionetworks.web.client.widget.discussion.modal.EditReplyModal;
import org.sagebionetworks.web.client.widget.entity.MarkdownWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import org.sagebionetworks.web.test.helper.RequestBuilderMockStubber;

import com.google.gwt.dev.javac.testing.impl.MockJavaResource;
import com.google.gwt.dev.util.collect.HashSet;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public class ReplyWidgetTest {
	@Mock
	ReplyWidgetView mockView;
	@Mock
	UserBadge mockAuthorWidget;
	@Mock
	RequestBuilderWrapper mockRequestBuilder;
	@Mock
	SynapseAlert mockSynAlert;
	@Mock
	Response mockResponse;
	@Mock
	DiscussionForumClientAsync mockDiscussionForumClientAsync;
	@Mock
	AuthenticationController mockAuthController;
	@Mock
	EditReplyModal mockEditReplyModal;
	@Mock
	MarkdownWidget mockMarkdownWidget;
	@Mock
	Callback mockDeleteCallback;
	@Mock
	GWTWrapper mockGwt;
	@Mock
	CopyTextModal mockCopyTextModal;
	@Mock
	DateTimeUtils mockDateTimeUtils;
	@Mock
	SynapseJavascriptClient mockSynapseJavascriptClient;
	@Mock
	ClientCache mockClientCache;
	@Mock
	PopupUtilsView mockPopupUtils;
	Set<String> moderatorIds;
	ReplyWidget replyWidget;
	
	private static final String CREATED_BY = "123";
	private static final String NON_AUTHOR = "456";


	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		replyWidget = new ReplyWidget(mockView, mockAuthorWidget, mockDateTimeUtils,
				mockSynAlert, mockRequestBuilder, mockDiscussionForumClientAsync,
				mockAuthController, mockEditReplyModal, mockMarkdownWidget, mockGwt, mockCopyTextModal,
				mockSynapseJavascriptClient, mockClientCache,mockPopupUtils);
		when(mockAuthController.getCurrentUserPrincipalId()).thenReturn(NON_AUTHOR);
		moderatorIds = new HashSet<String>();
	}

	@Test
	public void testConstructor() {
		verify(mockView).setPresenter(replyWidget);
		verify(mockView).setAuthor(any(Widget.class));
		verify(mockView).setAlert(any(Widget.class));
		verify(mockView).setEditReplyModal(any(Widget.class));
		verify(mockCopyTextModal).setTitle(ReplyWidget.REPLY_URL);
	}

	@Test
	public void testConfigure() {
		boolean isDeleted = false;
		boolean canModerate = false;
		boolean isEdited = false;
		boolean isThreadDeleted = false;
		DiscussionReplyBundle bundle = createReplyBundle("123", "author", "messageKey",
				new Date(), isDeleted, CREATED_BY, isEdited);
		when(mockDateTimeUtils.getRelativeTime(any(Date.class))).thenReturn("today");
		replyWidget.configure(bundle, canModerate, moderatorIds, mockDeleteCallback, isThreadDeleted);
		verify(mockView).clear();
		verify(mockAuthorWidget).configure(anyString());
		verify(mockView).setCreatedOn(anyString());
		verify(mockDateTimeUtils).getRelativeTime(any(Date.class));
		verify(mockView).setDeleteIconVisibility(false);
		verify(mockView).setEditIconVisible(false);
		verify(mockView).setEditedVisible(false);
		verify(mockView).setMessageVisible(true);
		verify(mockView).setIsAuthorModerator(false);
		verify(mockView).setCommandsContainerVisible(true);
	}

	@Test
	public void testConfigureReplyInAnDeletedThread() {
		boolean isDeleted = false;
		boolean canModerate = false;
		boolean isEdited = false;
		boolean isThreadDeleted = true;
		DiscussionReplyBundle bundle = createReplyBundle("123", "author", "messageKey",
				new Date(), isDeleted, CREATED_BY, isEdited);
		when(mockDateTimeUtils.getRelativeTime(any(Date.class))).thenReturn("today");
		replyWidget.configure(bundle, canModerate, moderatorIds, mockDeleteCallback, isThreadDeleted);
		verify(mockView).clear();
		verify(mockAuthorWidget).configure(anyString());
		verify(mockView).setCreatedOn(anyString());
		verify(mockDateTimeUtils).getRelativeTime(any(Date.class));
		verify(mockView, never()).setDeleteIconVisibility(false);
		verify(mockView, never()).setEditIconVisible(false);
		verify(mockView).setEditedVisible(false);
		verify(mockView).setMessageVisible(true);
		verify(mockView).setIsAuthorModerator(false);
		verify(mockView).setCommandsContainerVisible(false);
	}

	@Test
	public void testConfigureAuthorIsModerator() {
		boolean isDeleted = false;
		boolean canModerate = false;
		boolean isEdited = false;
		boolean isThreadDeleted = false;
		moderatorIds.add(CREATED_BY);
		DiscussionReplyBundle bundle = createReplyBundle("123", "author", "messageKey",
				new Date(), isDeleted, CREATED_BY, isEdited);
		when(mockDateTimeUtils.getRelativeTime(any(Date.class))).thenReturn("today");
		replyWidget.configure(bundle, canModerate, moderatorIds, mockDeleteCallback, isThreadDeleted);
		verify(mockView).setIsAuthorModerator(true);
	}
	
	@Test
	public void testConfigureEdited() {
		boolean isDeleted = false;
		boolean canModerate = false;
		boolean isEdited = true;
		boolean isThreadDeleted = false;
		DiscussionReplyBundle bundle = createReplyBundle("123", "author", "messageKey",
				new Date(), isDeleted, CREATED_BY, isEdited);
		when(mockDateTimeUtils.getRelativeTime(any(Date.class))).thenReturn("today");
		replyWidget.configure(bundle, canModerate, moderatorIds, mockDeleteCallback, isThreadDeleted);
		verify(mockView).setEditedVisible(true);
	}

	@Test
	public void testConfigureWithAuthor() {
		boolean isDeleted = false;
		boolean canModerate = false;
		boolean isEdited = false;
		boolean isThreadDeleted = false;
		when(mockAuthController.getCurrentUserPrincipalId()).thenReturn(CREATED_BY);
		DiscussionReplyBundle bundle = createReplyBundle("123", "author", "messageKey",
				new Date(), isDeleted, CREATED_BY, isEdited);
		when(mockDateTimeUtils.getRelativeTime(any(Date.class))).thenReturn("today");
		replyWidget.configure(bundle, canModerate, moderatorIds, mockDeleteCallback, isThreadDeleted );
		verify(mockView).setEditIconVisible(true);
	}

	@Test
	public void testConfigureWithModerator() {
		boolean isDeleted = false;
		boolean isEdited = false;
		boolean isThreadDeleted = false;
		DiscussionReplyBundle bundle = createReplyBundle("123", "author", "messageKey",
				new Date(), isDeleted, CREATED_BY, isEdited);
		when(mockDateTimeUtils.getRelativeTime(any(Date.class))).thenReturn("today");
		replyWidget.configure(bundle, true, moderatorIds, mockDeleteCallback, isThreadDeleted);
		verify(mockView).setDeleteIconVisibility(true);
	}

	@Test
	public void setAsWidgetTest() {
		replyWidget.asWidget();
		verify(mockView).asWidget();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testConfigureMessageFailToGetMessageURL() throws RequestException {
		boolean isDeleted = false;
		boolean canModerate = false;
		boolean isEdited = false;
		boolean isThreadDeleted = false;
		DiscussionReplyBundle bundle = createReplyBundle("123", "1", "messageKey",
				new Date(), isDeleted, CREATED_BY, isEdited);
		AsyncMockStubber.callFailureWith(new Exception())
				.when(mockSynapseJavascriptClient).getReplyUrl(anyString(), any(AsyncCallback.class));
		replyWidget.configure(bundle, canModerate, moderatorIds, mockDeleteCallback, isThreadDeleted);
		verify(mockSynAlert).clear();
		verify(mockRequestBuilder, never()).configure(eq(RequestBuilder.GET), anyString());
		verify(mockRequestBuilder, never()).setHeader(WebConstants.CONTENT_TYPE, WebConstants.TEXT_PLAIN_CHARSET_UTF8);
		verify(mockSynAlert).handleException(any(Throwable.class));
		verify(mockView).setDeleteIconVisibility(false);
		verify(mockView).setLoadingMessageVisible(true);
		verify(mockView).setLoadingMessageVisible(false);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testConfigureMessageFailToGetMessage() throws RequestException {
		boolean isDeleted = false;
		boolean canModerate = false;
		boolean isEdited = false;
		boolean isThreadDeleted = false;
		DiscussionReplyBundle bundle = createReplyBundle("123", "1", "messageKey",
				new Date(), isDeleted, CREATED_BY, isEdited);
		when(mockResponse.getStatusCode()).thenReturn(Response.SC_OK+1);
		String url = "url";
		AsyncMockStubber.callSuccessWith(url)
				.when(mockSynapseJavascriptClient).getReplyUrl(anyString(), any(AsyncCallback.class));
		RequestBuilderMockStubber.callOnError(null, new Exception())
				.when(mockRequestBuilder).sendRequest(anyString(), any(RequestCallback.class));
		replyWidget.configure(bundle, canModerate, moderatorIds, mockDeleteCallback, isThreadDeleted);
		verify(mockSynAlert).clear();
		verify(mockRequestBuilder).configure(eq(RequestBuilder.GET), anyString());
		verify(mockRequestBuilder).setHeader(WebConstants.CONTENT_TYPE, WebConstants.TEXT_PLAIN_CHARSET_UTF8);
		verify(mockSynAlert).handleException(any(Throwable.class));
		verify(mockMarkdownWidget, never()).configure(anyString());
		verify(mockView).setDeleteIconVisibility(false);
		verify(mockView).setLoadingMessageVisible(true);
		verify(mockView).setLoadingMessageVisible(false);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testConfigureMessageSuccess() throws RequestException {
		boolean isDeleted = false;
		boolean canModerate = false;
		boolean isEdited = false;
		boolean isThreadDeleted = false;
		DiscussionReplyBundle bundle = createReplyBundle("123", "1", "messageKey",
				new Date(), isDeleted, CREATED_BY, isEdited);
		when(mockResponse.getStatusCode()).thenReturn(Response.SC_OK);
		String message = "message";
		when(mockResponse.getText()).thenReturn(message);
		String url = "url";
		AsyncMockStubber.callSuccessWith(url)
				.when(mockSynapseJavascriptClient).getReplyUrl(anyString(), any(AsyncCallback.class));
		RequestBuilderMockStubber.callOnResponseReceived(null, mockResponse)
				.when(mockRequestBuilder).sendRequest(anyString(), any(RequestCallback.class));
		replyWidget.configure(bundle, canModerate, moderatorIds, mockDeleteCallback, isThreadDeleted);
		verify(mockSynAlert).clear();
		verify(mockRequestBuilder).configure(eq(RequestBuilder.GET), anyString());
		verify(mockRequestBuilder).setHeader(WebConstants.CONTENT_TYPE, WebConstants.TEXT_PLAIN_CHARSET_UTF8);
		verify(mockSynAlert, never()).handleException(any(Throwable.class));
		verify(mockMarkdownWidget).configure(message);
		verify(mockView).setDeleteIconVisibility(false);
		verify(mockView).setLoadingMessageVisible(true);
		verify(mockView).setLoadingMessageVisible(false);
		verify(mockEditReplyModal, never()).configure(anyString(), anyString(), any(Callback.class));
		
		replyWidget.onClickEditReply();
		verify(mockEditReplyModal).configure(anyString(), anyString(), any(Callback.class));
		verify(mockEditReplyModal).show();
	}
	
	@Test
	public void testConfigureMessageFromCache() throws RequestException {
		boolean isDeleted = false;
		boolean canModerate = false;
		boolean isEdited = false;
		boolean isThreadDeleted = false;
		String messageKey = "messageKey1112";
		DiscussionReplyBundle bundle = createReplyBundle("123", "1", messageKey,
				new Date(), isDeleted, CREATED_BY, isEdited);
		String message = "message";
		when(mockClientCache.contains(messageKey + WebConstants.MESSAGE_SUFFIX)).thenReturn(true);
		when(mockClientCache.get(messageKey + WebConstants.MESSAGE_SUFFIX)).thenReturn(message);
		
		replyWidget.configure(bundle, canModerate, moderatorIds, mockDeleteCallback, isThreadDeleted);
		
		verifyZeroInteractions(mockSynapseJavascriptClient, mockRequestBuilder);
		verify(mockMarkdownWidget).configure(message);
	}

	@Test
	public void testOnClickDeleteReply() {
		replyWidget.onClickDeleteReply();
		verify(mockPopupUtils).showConfirmDelete(anyString(), any(Callback.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testDeleteReplySuccess() throws RequestException {
		boolean isDeleted = false;
		boolean canModerate = false;
		boolean isEdited = false;
		boolean isThreadDeleted = false;
		DiscussionReplyBundle bundle = createReplyBundle("123", "1", "messageKey",
				new Date(), isDeleted, CREATED_BY, isEdited);
		when(mockResponse.getStatusCode()).thenReturn(Response.SC_OK);
		String message = "message";
		when(mockResponse.getText()).thenReturn(message);
		RequestBuilderMockStubber.callOnResponseReceived(null, mockResponse)
				.when(mockRequestBuilder).sendRequest(anyString(), any(RequestCallback.class));
		replyWidget.configure(bundle, canModerate, moderatorIds, mockDeleteCallback, isThreadDeleted);
		AsyncMockStubber.callSuccessWith((Void) null)
				.when(mockDiscussionForumClientAsync).markReplyAsDeleted(anyString(), any(AsyncCallback.class));
		replyWidget.deleteReply();
		verify(mockSynAlert, atLeast(1)).clear();
		verify(mockDiscussionForumClientAsync).markReplyAsDeleted(eq("123"), any(AsyncCallback.class));
		verify(mockView).showSuccess(anyString(), anyString());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testDeleteReplyFailure() throws RequestException {
		boolean isDeleted = false;
		boolean canModerate = false;
		boolean isEdited = false;
		boolean isThreadDeleted = false;
		DiscussionReplyBundle bundle = createReplyBundle("123", "1", "messageKey",
				new Date(), isDeleted, CREATED_BY, isEdited);
		when(mockResponse.getStatusCode()).thenReturn(Response.SC_OK);
		String message = "message";
		when(mockResponse.getText()).thenReturn(message);
		RequestBuilderMockStubber.callOnResponseReceived(null, mockResponse)
				.when(mockRequestBuilder).sendRequest(anyString(), any(RequestCallback.class));
		replyWidget.configure(bundle, canModerate, moderatorIds, mockDeleteCallback, isThreadDeleted);
		AsyncMockStubber.callFailureWith(new Exception())
				.when(mockDiscussionForumClientAsync).markReplyAsDeleted(anyString(), any(AsyncCallback.class));
		replyWidget.deleteReply();
		verify(mockSynAlert, atLeast(1)).clear();
		verify(mockDiscussionForumClientAsync).markReplyAsDeleted(eq("123"), any(AsyncCallback.class));
		verify(mockSynapseJavascriptClient, never()).getReply(anyString(), any(AsyncCallback.class));;
		verify(mockSynAlert).handleException(any(Throwable.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testReconfigureSuccess() throws RequestException {
		boolean isDeleted = false;
		boolean canModerate = false;
		boolean isEdited = false;
		boolean isThreadDeleted = false;
		DiscussionReplyBundle bundle = createReplyBundle("123", "1", "messageKey",
				new Date(), isDeleted, CREATED_BY, isEdited);
		when(mockResponse.getStatusCode()).thenReturn(Response.SC_OK);
		String message = "message";
		when(mockResponse.getText()).thenReturn(message);
		RequestBuilderMockStubber.callOnResponseReceived(null, mockResponse)
				.when(mockRequestBuilder).sendRequest(anyString(), any(RequestCallback.class));
		replyWidget.configure(bundle, canModerate, moderatorIds, mockDeleteCallback, isThreadDeleted);
		AsyncMockStubber.callSuccessWith(bundle)
				.when(mockSynapseJavascriptClient).getReply(anyString(), any(AsyncCallback.class));
		replyWidget.reconfigure();
		verify(mockSynAlert, atLeast(1)).clear();
		verify(mockView, times(2)).clear();
		verify(mockAuthorWidget, times(2)).configure(anyString());
		verify(mockView, times(2)).setCreatedOn(anyString());
		verify(mockDateTimeUtils, times(2)).getRelativeTime(any(Date.class));
		verify(mockView, times(2)).setDeleteIconVisibility(false);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testReconfigureFailure() throws RequestException {
		boolean isDeleted = false;
		boolean canModerate = false;
		boolean isEdited = false;
		boolean isThreadDeleted = false;
		DiscussionReplyBundle bundle = createReplyBundle("123", "1", "messageKey",
				new Date(), isDeleted, CREATED_BY, isEdited);
		when(mockResponse.getStatusCode()).thenReturn(Response.SC_OK);
		String message = "message";
		when(mockResponse.getText()).thenReturn(message);
		RequestBuilderMockStubber.callOnResponseReceived(null, mockResponse)
				.when(mockRequestBuilder).sendRequest(anyString(), any(RequestCallback.class));
		replyWidget.configure(bundle, canModerate, moderatorIds, mockDeleteCallback, isThreadDeleted);
		AsyncMockStubber.callFailureWith(new Exception())
				.when(mockSynapseJavascriptClient).getReply(anyString(), any(AsyncCallback.class));
		replyWidget.reconfigure();
		verify(mockSynAlert, atLeast(1)).clear();
		verify(mockView).clear();
		verify(mockAuthorWidget).configure(anyString());
		verify(mockView).setCreatedOn(anyString());
		verify(mockDateTimeUtils).getRelativeTime(any(Date.class));
		verify(mockView).setDeleteIconVisibility(false);
		verify(mockSynAlert).handleException(any(Throwable.class));
	}

	@Test
	public void testOnClickEditReply() {
		replyWidget.onClickEditReply();
		verify(mockEditReplyModal).show();
	}

	@Test
	public void testReplyClicked() {
		boolean isDeleted = false;
		boolean canModerate = false;
		boolean isEdited = false;
		boolean isThreadDeleted = false;
		String projectId = "syn007";
		String threadId = "321";
		String replyId = "123";
		DiscussionReplyBundle bundle = createReplyBundle("123", "author", "messageKey",
				new Date(), isDeleted, CREATED_BY, isEdited);
		bundle.setProjectId(projectId);
		bundle.setThreadId(threadId);
		replyWidget.configure(bundle, canModerate, moderatorIds, mockDeleteCallback, isThreadDeleted);
		String hostPageBaseURL = "https://www.synapse.org/";
		when(mockGwt.getHostPageBaseURL()).thenReturn(hostPageBaseURL);
		replyWidget.onClickReplyLink();
		
		verify(mockCopyTextModal).setText("https://www.synapse.org/#!Synapse:syn007/discussion/threadId=321&replyId=123");
		verify(mockCopyTextModal).show();
	}
	
	private DiscussionReplyBundle createReplyBundle(String replyId, String author,
			String messageKey, Date createdOn, Boolean isDeleted, String createdBy,
			Boolean isEdited) {
		DiscussionReplyBundle bundle = new DiscussionReplyBundle();
		bundle.setId(replyId);
		bundle.setCreatedBy(author);
		bundle.setMessageKey(messageKey);
		bundle.setCreatedOn(createdOn);
		bundle.setIsDeleted(isDeleted);
		bundle.setCreatedBy(createdBy);
		bundle.setIsEdited(isEdited);
		return bundle;
	}

}
