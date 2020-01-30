package org.sagebionetworks.web.unitclient.widget.refresh;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.discussion.DiscussionFilter;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.refresh.RefreshAlertView;
import org.sagebionetworks.web.client.widget.refresh.ReplyCountAlert;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class ReplyCountAlertTest {
	@Mock
	RefreshAlertView mockView;
	@Mock
	GWTWrapper mockGWTWrapper;
	@Mock
	GlobalApplicationState mockGlobalApplicationState;
	@Mock
	SynapseJSNIUtils mockSynapseJSNIUtils;
	@Mock
	Callback mockRefreshCallback;
	@Mock
	SynapseJavascriptClient mockSynapseJavascriptClient;
	ReplyCountAlert refreshAlert;
	Long count = 3L;

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		refreshAlert = new ReplyCountAlert(mockView, mockGWTWrapper, mockGlobalApplicationState, mockSynapseJSNIUtils, mockSynapseJavascriptClient);
		when(mockView.isAttached()).thenReturn(true);
		AsyncMockStubber.callSuccessWith(count).when(mockSynapseJavascriptClient).getReplyCountForThread(anyString(), any(DiscussionFilter.class), any(AsyncCallback.class));
	}

	@Test
	public void testConstructor() {
		verify(mockView).setPresenter(refreshAlert);
	}

	@Test
	public void testConfigureNotAttached() {
		when(mockView.isAttached()).thenReturn(false);
		refreshAlert.configure("123");
		// not ready to ask for the current count
		verify(mockSynapseJavascriptClient, never()).getReplyCountForThread(anyString(), any(DiscussionFilter.class), any(AsyncCallback.class));
	}

	@Test
	public void testAttachedNotConfigured() {
		when(mockView.isAttached()).thenReturn(true);
		refreshAlert.onAttach();
		// not ready to ask for the current etag
		verify(mockSynapseJavascriptClient, never()).getReplyCountForThread(anyString(), any(DiscussionFilter.class), any(AsyncCallback.class));
	}

	@Test
	public void testConfiguredAndAttached() {
		when(mockView.isAttached()).thenReturn(true);
		refreshAlert.configure("123");

		verify(mockSynapseJavascriptClient).getReplyCountForThread(anyString(), any(DiscussionFilter.class), any(AsyncCallback.class));
		// and will call this again later
		verify(mockGWTWrapper).scheduleExecution(any(Callback.class), eq(ReplyCountAlert.DELAY));
	}

	@Test
	public void testOnRefresh() {
		refreshAlert.onRefresh();
		verify(mockGlobalApplicationState).refreshPage();
	}

	@Test
	public void testOnRefreshCallback() {
		refreshAlert.setRefreshCallback(mockRefreshCallback);
		refreshAlert.onRefresh();
		verify(mockRefreshCallback).invoke();
	}
}
