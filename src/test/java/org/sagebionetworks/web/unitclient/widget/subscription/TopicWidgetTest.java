package org.sagebionetworks.web.unitclient.widget.subscription;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadBundle;
import org.sagebionetworks.repo.model.subscription.SubscriptionObjectType;
import org.sagebionetworks.web.client.DiscussionForumClientAsync;
import org.sagebionetworks.web.client.widget.discussion.DiscussionThreadWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.subscription.TopicWidget;
import org.sagebionetworks.web.client.widget.subscription.TopicWidgetView;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;


public class TopicWidgetTest {
	TopicWidget widget;
	
	@Mock
	TopicWidgetView mockView; 
	@Mock
	DiscussionForumClientAsync mockForumClient;
	@Mock
	SynapseAlert mockSynAlert;
	@Mock
	DiscussionThreadBundle mockDiscussionThreadBundle;
	@Mock
	Project mockProject;
	
	private static final String TEST_OBJECT_ID = "42";
	private static final String THREAD_TITLE = "It was the best of times...";
	private static final String THREAD_PROJECT_ID = "syn123";
	private static final String PROJECT_TITLE = "Best project ever";
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		widget = new TopicWidget(mockView, mockForumClient, mockSynAlert);
		
		when(mockDiscussionThreadBundle.getTitle()).thenReturn(THREAD_TITLE);
		when(mockDiscussionThreadBundle.getProjectId()).thenReturn(THREAD_PROJECT_ID);
		when(mockDiscussionThreadBundle.getId()).thenReturn(TEST_OBJECT_ID);
		
		when(mockProject.getId()).thenReturn(TEST_OBJECT_ID);
		when(mockProject.getName()).thenReturn(PROJECT_TITLE);
	}

	@Test
	public void testConstruction() {
		verify(mockView).setPresenter(widget);
	}

	@Test
	public void testConfigureAllTypesTest() {
		for (SubscriptionObjectType type : SubscriptionObjectType.values()) {
			widget.configure(type, TEST_OBJECT_ID);
		}
		verify(mockSynAlert, never()).showError(anyString());
	}
	
	@Test
	public void testConfigureDiscussionThread() {
		AsyncMockStubber.callSuccessWith(mockDiscussionThreadBundle).when(mockForumClient)
			.getThread(anyString(), any(AsyncCallback.class));
		
		widget.configure(SubscriptionObjectType.DISCUSSION_THREAD, TEST_OBJECT_ID);
		verify(mockView).setTopicText(THREAD_TITLE);
		String expectedHref = DiscussionThreadWidget.buildThreadLink(THREAD_PROJECT_ID, TEST_OBJECT_ID);
		verify(mockView).setTopicHref(expectedHref);
	}
	
	@Test
	public void testConfigureDiscussionThreadFailure() {
		Exception ex = new Exception("error");
		AsyncMockStubber.callFailureWith(ex).when(mockForumClient)
			.getThread(anyString(), any(AsyncCallback.class));
		
		widget.configure(SubscriptionObjectType.DISCUSSION_THREAD, TEST_OBJECT_ID);
		verify(mockSynAlert).handleException(ex);
	}

	@Test
	public void testConfigureForum() {
		AsyncMockStubber.callSuccessWith(mockProject).when(mockForumClient)
			.getForumProject(anyString(), any(AsyncCallback.class));
		
		widget.configure(SubscriptionObjectType.FORUM, TEST_OBJECT_ID);
		verify(mockView).setTopicText(PROJECT_TITLE);
		String expectedHref = DiscussionThreadWidget.buildForumLink(TEST_OBJECT_ID);
		verify(mockView).setTopicHref(expectedHref);
	}

	@Test
	public void testConfigureForumFailure() {
		Exception ex = new Exception("error");
		AsyncMockStubber.callFailureWith(ex).when(mockForumClient)
			.getForumProject(anyString(), any(AsyncCallback.class));
		
		widget.configure(SubscriptionObjectType.FORUM, TEST_OBJECT_ID);
		verify(mockSynAlert).handleException(ex);
	}

	@Test
	public void testAddStyleNames() {
		String styleNames = "font-size-32";
		widget.addStyleNames(styleNames);
		verify(mockView).addStyleNames(styleNames);
	}

	@Test
	public void testAsWidget() {
		widget.asWidget();
		verify(mockView).asWidget();
	}

}
