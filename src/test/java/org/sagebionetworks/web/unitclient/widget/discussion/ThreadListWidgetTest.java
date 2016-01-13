package org.sagebionetworks.web.unitclient.widget.discussion;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadBundle;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadOrder;
import org.sagebionetworks.web.client.DiscussionForumClientAsync;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.widget.discussion.ThreadListWidget;
import org.sagebionetworks.web.client.widget.discussion.ThreadListWidgetView;
import org.sagebionetworks.web.client.widget.discussion.ThreadWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public class ThreadListWidgetTest {

	@Mock
	ThreadListWidgetView mockView;
	@Mock
	PortalGinInjector mockGinInjector;
	@Mock
	ThreadWidget mockThreadWidget;
	@Mock
	DiscussionForumClientAsync mockDiscussionForumClient;
	@Mock
	PaginatedResults<DiscussionThreadBundle> mockThreadBundlePage;
	@Mock
	SynapseAlert mockSynAlert;
	List<DiscussionThreadBundle> threadBundleList = new ArrayList<DiscussionThreadBundle>();

	ThreadListWidget discussionListWidget;


	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		when(mockGinInjector.createThreadWidget()).thenReturn(mockThreadWidget);
		discussionListWidget = new ThreadListWidget(mockView, mockGinInjector, mockDiscussionForumClient, mockSynAlert);
	}

	@Test
	public void testConstructor() {
		verify(mockView).setPresenter(discussionListWidget);
		verify(mockView).setAlert(any(Widget.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testConfigureSuccess() {
		AsyncMockStubber.callSuccessWith(mockThreadBundlePage)
				.when(mockDiscussionForumClient).getThreadsForForum(anyString(), anyLong(),
						anyLong(), any(DiscussionThreadOrder.class), anyBoolean(), any(AsyncCallback.class));
		when(mockThreadBundlePage.getTotalNumberOfResults()).thenReturn(1L);
		threadBundleList.add(new DiscussionThreadBundle());
		when(mockThreadBundlePage.getResults()).thenReturn(threadBundleList);
		discussionListWidget.configure("123");
		verify(mockView).clear();
		verify(mockView).addThread(any(Widget.class));
		verify(mockGinInjector).createThreadWidget();
		verify(mockThreadWidget).configure(any(DiscussionThreadBundle.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testConfigureFailure() {
		AsyncMockStubber.callFailureWith(new Exception())
				.when(mockDiscussionForumClient).getThreadsForForum(anyString(), anyLong(),
						anyLong(), any(DiscussionThreadOrder.class), anyBoolean(), any(AsyncCallback.class));
		discussionListWidget.configure("123");
		verify(mockView).clear();
		verify(mockView, never()).addThread(any(Widget.class));
		verify(mockGinInjector, never()).createThreadWidget();
		verify(mockSynAlert).clear();
		verify(mockSynAlert).handleException(any(Throwable.class));
	}

	@Test
	public void asWidgetTest() {
		discussionListWidget.asWidget();
		verify(mockView).asWidget();
	}
}
