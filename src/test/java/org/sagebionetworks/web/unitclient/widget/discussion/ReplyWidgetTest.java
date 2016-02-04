package org.sagebionetworks.web.unitclient.widget.discussion;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.sagebionetworks.web.client.widget.discussion.ReplyWidget.*;

import java.util.Date;

import org.gwtbootstrap3.extras.bootbox.client.callback.AlertCallback;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.discussion.DiscussionReplyBundle;
import org.sagebionetworks.web.client.DiscussionForumClientAsync;
import org.sagebionetworks.web.client.RequestBuilderWrapper;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.discussion.ReplyWidget;
import org.sagebionetworks.web.client.widget.discussion.ReplyWidgetView;
import org.sagebionetworks.web.client.widget.discussion.modal.EditReplyModal;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import org.sagebionetworks.web.test.helper.RequestBuilderMockStubber;

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
	SynapseJSNIUtils mockJsniUtils;
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

	ReplyWidget replyWidget;
	boolean isDeleted = false;
	boolean canModerate = false;
	String userId = "123";
	String nonAuthor = "456";
	boolean isEdited = false;

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		replyWidget = new ReplyWidget(mockView, mockAuthorWidget, mockJsniUtils,
				mockSynAlert, mockRequestBuilder, mockDiscussionForumClientAsync,
				mockAuthController, mockEditReplyModal);
		when(mockAuthController.getCurrentUserPrincipalId()).thenReturn(nonAuthor);
	}

	@Test
	public void testConstructor() {
		verify(mockView).setPresenter(replyWidget);
		verify(mockView).setAuthor(any(Widget.class));
		verify(mockView).setAlert(any(Widget.class));
		verify(mockView).setEditReplyModal(any(Widget.class));
	}

	@Test
	public void testConfigure() {
		DiscussionReplyBundle bundle = createReplyBundle("123", "author", "messageKey",
				new Date(), isDeleted, userId, isEdited);
		when(mockJsniUtils.getRelativeTime(any(Date.class))).thenReturn("today");
		replyWidget.configure(bundle, canModerate);
		verify(mockView).clear();
		verify(mockAuthorWidget).configure(anyString());
		verify(mockView).setCreatedOn(anyString());
		verify(mockJsniUtils).getRelativeTime(any(Date.class));
		verify(mockView).setDeleteButtonVisibility(canModerate);
		verify(mockView).setEditIconVisible(false);
		verify(mockView).setEditedVisible(isEdited);
	}

	@Test
	public void testConfigureEdited() {
		isEdited = true;
		DiscussionReplyBundle bundle = createReplyBundle("123", "author", "messageKey",
				new Date(), isDeleted, userId, isEdited);
		when(mockJsniUtils.getRelativeTime(any(Date.class))).thenReturn("today");
		replyWidget.configure(bundle, canModerate);
		verify(mockView).clear();
		verify(mockAuthorWidget).configure(anyString());
		verify(mockView).setCreatedOn(anyString());
		verify(mockJsniUtils).getRelativeTime(any(Date.class));
		verify(mockView).setDeleteButtonVisibility(canModerate);
		verify(mockView).setEditIconVisible(false);
		verify(mockView).setEditedVisible(isEdited);
	}

	@Test
	public void testConfigureWithAuthor() {
		when(mockAuthController.getCurrentUserPrincipalId()).thenReturn(userId);
		DiscussionReplyBundle bundle = createReplyBundle("123", "author", "messageKey",
				new Date(), isDeleted, userId, isEdited);
		when(mockJsniUtils.getRelativeTime(any(Date.class))).thenReturn("today");
		replyWidget.configure(bundle, canModerate);
		verify(mockView).clear();
		verify(mockAuthorWidget).configure(anyString());
		verify(mockView).setCreatedOn(anyString());
		verify(mockJsniUtils).getRelativeTime(any(Date.class));
		verify(mockView).setDeleteButtonVisibility(canModerate);
		verify(mockView).setEditIconVisible(true);
		verify(mockView).setEditedVisible(isEdited);
	}

	@Test
	public void testConfigureDeletedReply() {
		isDeleted = true;
		DiscussionReplyBundle bundle = createReplyBundle("123", "author", "messageKey",
				new Date(), isDeleted, userId, isEdited);
		when(mockJsniUtils.getRelativeTime(any(Date.class))).thenReturn("today");
		replyWidget.configure(bundle, canModerate);
		verify(mockView).clear();
		verify(mockAuthorWidget).configure(anyString());
		verify(mockView).setCreatedOn(anyString());
		verify(mockJsniUtils).getRelativeTime(any(Date.class));
		verify(mockView).setDeleteButtonVisibility(canModerate);
		verify(mockView).setMessage(DELETED_REPLY_DEFAULT_MESSAGE);
		verify(mockView).setEditIconVisible(false);
		verify(mockView, never()).setEditedVisible(isEdited);
	}

	@Test
	public void testConfigureDeletedReplyNonAuthor() {
		when(mockAuthController.getCurrentUserPrincipalId()).thenReturn(userId);
		isDeleted = true;
		DiscussionReplyBundle bundle = createReplyBundle("123", "author", "messageKey",
				new Date(), isDeleted, userId, isEdited);
		when(mockJsniUtils.getRelativeTime(any(Date.class))).thenReturn("today");
		replyWidget.configure(bundle, canModerate);
		verify(mockView).clear();
		verify(mockAuthorWidget).configure(anyString());
		verify(mockView).setCreatedOn(anyString());
		verify(mockJsniUtils).getRelativeTime(any(Date.class));
		verify(mockView).setDeleteButtonVisibility(canModerate);
		verify(mockView).setMessage(DELETED_REPLY_DEFAULT_MESSAGE);
		verify(mockView).setEditIconVisible(false);
		verify(mockView, never()).setEditedVisible(isEdited);
	}

	@Test
	public void testConfigureWithModerator() {
		canModerate = true;
		DiscussionReplyBundle bundle = createReplyBundle("123", "author", "messageKey",
				new Date(), isDeleted, userId, isEdited);
		when(mockJsniUtils.getRelativeTime(any(Date.class))).thenReturn("today");
		replyWidget.configure(bundle, canModerate);
		verify(mockView).clear();
		verify(mockAuthorWidget).configure(anyString());
		verify(mockView).setCreatedOn(anyString());
		verify(mockJsniUtils).getRelativeTime(any(Date.class));
		verify(mockView).setDeleteButtonVisibility(canModerate);
		verify(mockView).setEditedVisible(isEdited);
	}

	@Test
	public void setAsWidgetTest() {
		replyWidget.asWidget();
		verify(mockView).asWidget();
	}

	@Test
	public void testConfigureMessageFailToGetMessage() throws RequestException {
		DiscussionReplyBundle bundle = createReplyBundle("123", "1", "messageKey",
				new Date(), isDeleted, userId, isEdited);
		RequestBuilderMockStubber.callOnError(null, new Exception())
				.when(mockRequestBuilder).sendRequest(anyString(), any(RequestCallback.class));
		replyWidget.configure(bundle, canModerate);
		verify(mockSynAlert).clear();
		verify(mockRequestBuilder).configure(eq(RequestBuilder.GET), anyString());
		verify(mockRequestBuilder).setHeader(WebConstants.CONTENT_TYPE, WebConstants.TEXT_PLAIN_CHARSET_UTF8);
		verify(mockSynAlert).handleException(any(Throwable.class));
		verify(mockView).setDeleteButtonVisibility(canModerate);
	}

	@Test
	public void testConfigureMessageFailToGetMessageCase2() throws RequestException {
		DiscussionReplyBundle bundle = createReplyBundle("123", "1", "messageKey",
				new Date(), isDeleted, userId, isEdited);
		when(mockResponse.getStatusCode()).thenReturn(Response.SC_OK+1);
		RequestBuilderMockStubber.callOnResponseReceived(null, mockResponse)
				.when(mockRequestBuilder).sendRequest(anyString(), any(RequestCallback.class));
		replyWidget.configure(bundle, canModerate);
		verify(mockSynAlert).clear();
		verify(mockRequestBuilder).configure(eq(RequestBuilder.GET), anyString());
		verify(mockRequestBuilder).setHeader(WebConstants.CONTENT_TYPE, WebConstants.TEXT_PLAIN_CHARSET_UTF8);
		verify(mockSynAlert).handleException(any(Throwable.class));
		verify(mockView, never()).setMessage(anyString());
		verify(mockView).setDeleteButtonVisibility(canModerate);
	}

	@Test
	public void testConfigureMessageSuccess() throws RequestException {
		DiscussionReplyBundle bundle = createReplyBundle("123", "1", "messageKey",
				new Date(), isDeleted, userId, isEdited);
		when(mockResponse.getStatusCode()).thenReturn(Response.SC_OK);
		String message = "message";
		when(mockResponse.getText()).thenReturn(message);
		RequestBuilderMockStubber.callOnResponseReceived(null, mockResponse)
				.when(mockRequestBuilder).sendRequest(anyString(), any(RequestCallback.class));
		replyWidget.configure(bundle, canModerate);
		verify(mockSynAlert).clear();
		verify(mockRequestBuilder).configure(eq(RequestBuilder.GET), anyString());
		verify(mockRequestBuilder).setHeader(WebConstants.CONTENT_TYPE, WebConstants.TEXT_PLAIN_CHARSET_UTF8);
		verify(mockSynAlert, never()).handleException(any(Throwable.class));
		verify(mockView).setMessage(message);
		verify(mockView).setDeleteButtonVisibility(canModerate);
		verify(mockEditReplyModal).configure(anyString(), anyString(), any(Callback.class));
	}

	@Test
	public void testOnClickDeleteReply() {
		replyWidget.onClickDeleteReply();
		verify(mockView).showDeleteConfirm(anyString(), any(AlertCallback.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testDeleteReplySuccess() throws RequestException {
		DiscussionReplyBundle bundle = createReplyBundle("123", "1", "messageKey",
				new Date(), isDeleted, userId, isEdited);
		when(mockResponse.getStatusCode()).thenReturn(Response.SC_OK);
		String message = "message";
		when(mockResponse.getText()).thenReturn(message);
		RequestBuilderMockStubber.callOnResponseReceived(null, mockResponse)
				.when(mockRequestBuilder).sendRequest(anyString(), any(RequestCallback.class));
		replyWidget.configure(bundle, canModerate);
		AsyncMockStubber.callSuccessWith((Void) null)
				.when(mockDiscussionForumClientAsync).markReplyAsDeleted(anyString(), any(AsyncCallback.class));
		replyWidget.deleteReply();
		verify(mockSynAlert, atLeast(1)).clear();
		verify(mockDiscussionForumClientAsync).markReplyAsDeleted(eq("123"), any(AsyncCallback.class));
		verify(mockDiscussionForumClientAsync).getReply(anyString(), any(AsyncCallback.class));;
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testDeleteReplyFailure() throws RequestException {
		DiscussionReplyBundle bundle = createReplyBundle("123", "1", "messageKey",
				new Date(), isDeleted, userId, isEdited);
		when(mockResponse.getStatusCode()).thenReturn(Response.SC_OK);
		String message = "message";
		when(mockResponse.getText()).thenReturn(message);
		RequestBuilderMockStubber.callOnResponseReceived(null, mockResponse)
				.when(mockRequestBuilder).sendRequest(anyString(), any(RequestCallback.class));
		replyWidget.configure(bundle, canModerate);
		AsyncMockStubber.callFailureWith(new Exception())
				.when(mockDiscussionForumClientAsync).markReplyAsDeleted(anyString(), any(AsyncCallback.class));
		replyWidget.deleteReply();
		verify(mockSynAlert, atLeast(1)).clear();
		verify(mockDiscussionForumClientAsync).markReplyAsDeleted(eq("123"), any(AsyncCallback.class));
		verify(mockDiscussionForumClientAsync, never()).getReply(anyString(), any(AsyncCallback.class));;
		verify(mockSynAlert).handleException(any(Throwable.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testReconfigureSuccess() throws RequestException {
		DiscussionReplyBundle bundle = createReplyBundle("123", "1", "messageKey",
				new Date(), isDeleted, userId, isEdited);
		when(mockResponse.getStatusCode()).thenReturn(Response.SC_OK);
		String message = "message";
		when(mockResponse.getText()).thenReturn(message);
		RequestBuilderMockStubber.callOnResponseReceived(null, mockResponse)
				.when(mockRequestBuilder).sendRequest(anyString(), any(RequestCallback.class));
		replyWidget.configure(bundle, canModerate);
		AsyncMockStubber.callSuccessWith(bundle)
				.when(mockDiscussionForumClientAsync).getReply(anyString(), any(AsyncCallback.class));
		replyWidget.reconfigure();
		verify(mockSynAlert, atLeast(1)).clear();
		verify(mockView, times(2)).clear();
		verify(mockAuthorWidget, times(2)).configure(anyString());
		verify(mockView, times(2)).setCreatedOn(anyString());
		verify(mockJsniUtils, times(2)).getRelativeTime(any(Date.class));
		verify(mockView, times(2)).setDeleteButtonVisibility(false);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testReconfigureFailure() throws RequestException {
		DiscussionReplyBundle bundle = createReplyBundle("123", "1", "messageKey",
				new Date(), isDeleted, userId, isEdited);
		when(mockResponse.getStatusCode()).thenReturn(Response.SC_OK);
		String message = "message";
		when(mockResponse.getText()).thenReturn(message);
		RequestBuilderMockStubber.callOnResponseReceived(null, mockResponse)
				.when(mockRequestBuilder).sendRequest(anyString(), any(RequestCallback.class));
		replyWidget.configure(bundle, canModerate);
		AsyncMockStubber.callFailureWith(new Exception())
				.when(mockDiscussionForumClientAsync).getReply(anyString(), any(AsyncCallback.class));
		replyWidget.reconfigure();
		verify(mockSynAlert, atLeast(1)).clear();
		verify(mockView).clear();
		verify(mockAuthorWidget).configure(anyString());
		verify(mockView).setCreatedOn(anyString());
		verify(mockJsniUtils).getRelativeTime(any(Date.class));
		verify(mockView).setDeleteButtonVisibility(canModerate);
		verify(mockSynAlert).handleException(any(Throwable.class));
	}

	@Test
	public void testOnClickEditReply() {
		replyWidget.onClickEditReply();
		verify(mockEditReplyModal).show();
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
