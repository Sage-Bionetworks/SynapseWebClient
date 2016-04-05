package org.sagebionetworks.web.unitclient.widget.refresh;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.util.Date;

import org.gwtbootstrap3.extras.bootbox.client.callback.AlertCallback;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.discussion.DiscussionReplyBundle;
import org.sagebionetworks.repo.model.subscription.Etag;
import org.sagebionetworks.web.client.DiscussionForumClientAsync;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.RequestBuilderWrapper;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.discussion.ReplyWidget;
import org.sagebionetworks.web.client.widget.discussion.ReplyWidgetView;
import org.sagebionetworks.web.client.widget.discussion.modal.EditReplyModal;
import org.sagebionetworks.web.client.widget.entity.MarkdownWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.refresh.RefreshAlert;
import org.sagebionetworks.web.client.widget.refresh.RefreshAlertView;
import org.sagebionetworks.web.client.widget.refresh.DiscussionThreadCountAlert;
import org.sagebionetworks.web.client.widget.refresh.DiscussionThreadCountAlert;
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

public class DiscussionThreadCountAlertTest {
	@Mock
	RefreshAlertView mockView;
	@Mock
	DiscussionForumClientAsync mockDiscussionForum;
	@Mock
	GWTWrapper mockGWTWrapper;
	@Mock
	GlobalApplicationState mockGlobalApplicationState;
	@Mock
	SynapseJSNIUtils mockSynapseJSNIUtils;
	
	Long count = 5L;
	DiscussionThreadCountAlert refreshAlert;

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		refreshAlert = new DiscussionThreadCountAlert(mockView, mockDiscussionForum, mockGWTWrapper, mockGlobalApplicationState, mockSynapseJSNIUtils);
		when(mockView.isAttached()).thenReturn(true);
		AsyncMockStubber.callSuccessWith(count).when(mockDiscussionForum).getThreadCount(anyString(), any(AsyncCallback.class));
		
	}

	@Test
	public void testConstructor() {
		verify(mockView).setPresenter(refreshAlert);
	}

	@Test
	public void testConfigureNotAttached() {
		when(mockView.isAttached()).thenReturn(false);
		refreshAlert.configure("123");
		//not ready to ask for the current etag
		verify(mockDiscussionForum, never()).getThreadCount(anyString(), any(AsyncCallback.class));
	}
	@Test
	public void testAttachedNotConfigured() {
		when(mockView.isAttached()).thenReturn(true);
		refreshAlert.onAttach();
		//not ready to ask for the current etag
		verify(mockDiscussionForum, never()).getThreadCount(anyString(), any(AsyncCallback.class));
	}
	
	@Test
	public void testConfiguredAndAttached() {
		when(mockView.isAttached()).thenReturn(true);
		refreshAlert.configure("123");
		
		verify(mockDiscussionForum).getThreadCount(anyString(), any(AsyncCallback.class));
		//and will call this again later
		verify(mockGWTWrapper).scheduleExecution(any(Callback.class), eq(DiscussionThreadCountAlert.DELAY));
	}
	
	@Test
	public void testOnRefresh() {
		refreshAlert.onRefresh();
		verify(mockGlobalApplicationState).refreshPage();
	}
}
