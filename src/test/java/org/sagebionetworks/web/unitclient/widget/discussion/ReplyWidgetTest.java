package org.sagebionetworks.web.unitclient.widget.discussion;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.discussion.DiscussionReplyBundle;
import org.sagebionetworks.repo.model.discussion.MessageURL;
import org.sagebionetworks.web.client.DiscussionForumClientAsync;
import org.sagebionetworks.web.client.RequestBuilderWrapper;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.widget.discussion.ReplyWidget;
import org.sagebionetworks.web.client.widget.discussion.ReplyWidgetView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.user.UserBadge;
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
	DiscussionForumClientAsync mockDiscussionForumClientAsync;
	@Mock
	Response mockResponse;

	ReplyWidget replyWidget;

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		replyWidget = new ReplyWidget(mockView, mockAuthorWidget, mockJsniUtils,
				mockSynAlert, mockDiscussionForumClientAsync, mockRequestBuilder);
	}

	@Test
	public void testConstructor() {
		verify(mockView).setPresenter(replyWidget);
		verify(mockView).setAuthor(any(Widget.class));
		verify(mockView).setAlert(any(Widget.class));
	}

	@Test
	public void testConfigure() {
		DiscussionReplyBundle bundle = createReplyBundle("123", "author", new Date());
		when(mockJsniUtils.getRelativeTime(any(Date.class))).thenReturn("today");
		replyWidget.configure(bundle);
		verify(mockView).clear();
		verify(mockAuthorWidget).configure(anyString());
		verify(mockView).setCreatedOn(anyString());
		verify(mockJsniUtils).getRelativeTime(any(Date.class));
	}

	@Test
	public void setAsWidgetTest() {
		replyWidget.asWidget();
		verify(mockView).asWidget();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testConfigureMessageFailToGetUrl() {
		DiscussionReplyBundle bundle = createReplyBundle("123", "1", new Date());
		AsyncMockStubber.callFailureWith(new Exception())
				.when(mockDiscussionForumClientAsync).getReplyUrl(anyString(), any(AsyncCallback.class));
		replyWidget.configure(bundle);
		verify(mockSynAlert).clear();
		verify(mockSynAlert).handleException(any(Throwable.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testConfigureMessageFailToGetMessage() throws RequestException {
		DiscussionReplyBundle bundle = createReplyBundle("123", "1", new Date());
		MessageURL messageUrl = new MessageURL();
		messageUrl.setMessageUrl("messageURL");
		AsyncMockStubber.callSuccessWith(messageUrl)
				.when(mockDiscussionForumClientAsync).getReplyUrl(anyString(), any(AsyncCallback.class));
		RequestBuilderMockStubber.callOnError(null, new Exception())
				.when(mockRequestBuilder).sendRequest(anyString(), any(RequestCallback.class));
		replyWidget.configure(bundle);
		verify(mockSynAlert).clear();
		verify(mockRequestBuilder).configure(RequestBuilder.GET, messageUrl.getMessageUrl());
		verify(mockSynAlert).handleException(any(Throwable.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testConfigureMessageFailToGetMessageCase2() throws RequestException {
		DiscussionReplyBundle bundle = createReplyBundle("123", "1", new Date());
		MessageURL messageUrl = new MessageURL();
		messageUrl.setMessageUrl("messageURL");
		AsyncMockStubber.callSuccessWith(messageUrl)
				.when(mockDiscussionForumClientAsync).getReplyUrl(anyString(), any(AsyncCallback.class));
		when(mockResponse.getStatusCode()).thenReturn(Response.SC_OK+1);
		RequestBuilderMockStubber.callOnResponseReceived(null, mockResponse)
				.when(mockRequestBuilder).sendRequest(anyString(), any(RequestCallback.class));
		replyWidget.configure(bundle);
		verify(mockSynAlert).clear();
		verify(mockRequestBuilder).configure(RequestBuilder.GET, messageUrl.getMessageUrl());
		verify(mockSynAlert).handleException(any(Throwable.class));
		verify(mockView, never()).setMessage(anyString());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testConfigureMessageSuccess() throws RequestException {
		DiscussionReplyBundle bundle = createReplyBundle("123", "1", new Date());
		MessageURL messageUrl = new MessageURL();
		messageUrl.setMessageUrl("messageURL");
		AsyncMockStubber.callSuccessWith(messageUrl)
				.when(mockDiscussionForumClientAsync).getReplyUrl(anyString(), any(AsyncCallback.class));
		when(mockResponse.getStatusCode()).thenReturn(Response.SC_OK);
		String message = "message";
		when(mockResponse.getText()).thenReturn(message);
		RequestBuilderMockStubber.callOnResponseReceived(null, mockResponse)
				.when(mockRequestBuilder).sendRequest(anyString(), any(RequestCallback.class));
		replyWidget.configure(bundle);
		verify(mockSynAlert).clear();
		verify(mockRequestBuilder).configure(RequestBuilder.GET, messageUrl.getMessageUrl());
		verify(mockSynAlert, never()).handleException(any(Throwable.class));
		verify(mockView).setMessage(message);
	}

	private DiscussionReplyBundle createReplyBundle(String replyId, String author, Date createdOn) {
		DiscussionReplyBundle bundle = new DiscussionReplyBundle();
		bundle.setId(replyId);
		bundle.setCreatedBy(author);
		bundle.setCreatedOn(createdOn);
		return bundle;
	}

}
