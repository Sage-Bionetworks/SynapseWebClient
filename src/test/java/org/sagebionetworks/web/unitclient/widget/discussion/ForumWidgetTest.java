package org.sagebionetworks.web.unitclient.widget.discussion;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.sagebionetworks.web.client.widget.discussion.ForumWidget.*;
import static junit.framework.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadBundle;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadOrder;
import org.sagebionetworks.repo.model.discussion.Forum;
import org.sagebionetworks.repo.model.subscription.SubscriptionObjectType;
import org.sagebionetworks.web.client.DiscussionForumClientAsync;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.place.ParameterizedToken;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.discussion.DiscussionThreadListWidget;
import org.sagebionetworks.web.client.widget.discussion.DiscussionThreadWidget;
import org.sagebionetworks.web.client.widget.discussion.ForumWidget;
import org.sagebionetworks.web.client.widget.discussion.ForumWidgetView;
import org.sagebionetworks.web.client.widget.discussion.modal.NewDiscussionThreadModal;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.entity.tabs.Tab;
import org.sagebionetworks.web.client.widget.subscription.SubscribeButtonWidget;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public class ForumWidgetTest {
	@Mock
	ForumWidgetView mockView;
	@Mock
	DiscussionThreadListWidget mockAvailableThreadListWidget;
	@Mock
	CallbackP<Tab> mockOnClickCallback;
	@Mock
	NewDiscussionThreadModal mockNewDiscussionThreadModal;
	@Mock
	SynapseAlert mockSynAlert;
	@Mock
	DiscussionForumClientAsync mockDiscussionForumClient;
	@Mock
	Forum mockForum;
	@Mock
	AuthenticationController mockAuthController;
	@Mock
	GlobalApplicationState mockGlobalApplicationState;
	@Mock
	PlaceChanger mockPlaceChanger;
	@Mock
	DiscussionThreadWidget mockDiscussionThreadWidget;
	@Mock
	DiscussionThreadBundle mockDiscussionThreadBundle;
	@Mock
	SubscribeButtonWidget mockSubscribeButtonWidget;
	
	ForumWidget forumWidget;
	private boolean canModerate = false;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		forumWidget = new ForumWidget(mockView, mockSynAlert, mockDiscussionForumClient,
				mockAvailableThreadListWidget, mockNewDiscussionThreadModal,
				mockAuthController, mockGlobalApplicationState, mockDiscussionThreadWidget,
				mockSubscribeButtonWidget);
		when(mockAuthController.isLoggedIn()).thenReturn(true);
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
	}

	@Test
	public void testConstruction() {
		verify(mockView).setThreadList(any(Widget.class));
		verify(mockView).setNewThreadModal(any(Widget.class));
		verify(mockView).setPresenter(forumWidget);
		verify(mockView).setAlert(any(Widget.class));
		verify(mockView).setSingleThread(any(Widget.class));
		verify(mockView).setSubscribeButton(any(Widget.class));
		
		ArgumentCaptor<Callback> captor = ArgumentCaptor.forClass(Callback.class);
		verify(mockSubscribeButtonWidget).setOnSubscribeCallback(captor.capture());
		Callback onSubscribeCallback = captor.getValue();
		verify(mockSubscribeButtonWidget).setOnUnsubscribeCallback(captor.capture());
		Callback onUnsubscribeCallback = captor.getValue();
		
		assertTrue(onSubscribeCallback.equals(onUnsubscribeCallback));
		//invoke callback to verify thread list is reconfigured
		onSubscribeCallback.invoke();
		verify(mockAvailableThreadListWidget).configure(anyString(), anyBoolean(), any(CallbackP.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testConfigureForumSuccess() {
		String forumId = "123";
		when(mockForum.getId()).thenReturn(forumId);
		AsyncMockStubber.callSuccessWith(mockForum).when(mockDiscussionForumClient)
				.getForumByProjectId(anyString(), any(AsyncCallback.class));

		String entityId = "syn1"; 
		String areaToken = "a=b&c=d";
		ParameterizedToken param = new ParameterizedToken(areaToken);
		Callback callback = null;
		forumWidget.configure(entityId, param, canModerate, callback);
		verify(mockSubscribeButtonWidget).clear();
		verify(mockSubscribeButtonWidget).configure(SubscriptionObjectType.FORUM, forumId);
		verify(mockSynAlert).clear();
		verify(mockView).setSingleThreadUIVisible(false);
		verify(mockView).setThreadListUIVisible(true);
		verify(mockView).setNewThreadButtonVisible(true);
		verify(mockView).setShowAllThreadsButtonVisible(false);
		verify(mockDiscussionForumClient).getForumByProjectId(anyString(), any(AsyncCallback.class));
		verify(mockNewDiscussionThreadModal).configure(anyString(), any(Callback.class));
		verify(mockAvailableThreadListWidget).configure(anyString(), eq(DEFAULT_MODERATOR_MODE), any(CallbackP.class));
		verify(mockView).setModeratorModeContainerVisibility(canModerate);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testConfigureForumFailure() {
		when(mockForum.getId()).thenReturn("123");
		AsyncMockStubber.callFailureWith(new Exception()).when(mockDiscussionForumClient)
				.getForumByProjectId(anyString(), any(AsyncCallback.class));

		String entityId = "syn1"; 
		String areaToken = "foo=bar";
		ParameterizedToken param = new ParameterizedToken(areaToken);
		Callback callback = null;
		forumWidget.configure(entityId, param, canModerate, callback);

		verify(mockSynAlert).clear();
		verify(mockView).setSingleThreadUIVisible(false);
		verify(mockView).setThreadListUIVisible(true);
		verify(mockView).setNewThreadButtonVisible(true);
		verify(mockView).setShowAllThreadsButtonVisible(false);
		verify(mockDiscussionForumClient).getForumByProjectId(anyString(), any(AsyncCallback.class));
		verify(mockNewDiscussionThreadModal, never()).configure(anyString(), any(Callback.class));
		verify(mockView).setModeratorModeContainerVisibility(canModerate);
		verify(mockSynAlert).handleException(any(Exception.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testConfigureForumSuccessWithModerator() {
		when(mockForum.getId()).thenReturn("123");
		AsyncMockStubber.callSuccessWith(mockForum).when(mockDiscussionForumClient)
				.getForumByProjectId(anyString(), any(AsyncCallback.class));

		String entityId = "syn1";
		String areaToken = "";
		canModerate = true;
		ParameterizedToken param = new ParameterizedToken(areaToken);
		Callback callback = null;
		forumWidget.configure(entityId, param, canModerate, callback);

		verify(mockAvailableThreadListWidget).configure(anyString(), eq(DEFAULT_MODERATOR_MODE), any(CallbackP.class));
		verify(mockView).setModeratorModeContainerVisibility(canModerate);
	}

	@Test
	public void onClickNewThreadTest() {
		forumWidget.onClickNewThread();
		verify(mockNewDiscussionThreadModal).show();
	}

	@Test
	public void onClickNewThreadAnonymousTest() {
		when(mockAuthController.isLoggedIn()).thenReturn(false);
		forumWidget.onClickNewThread();
		verify(mockNewDiscussionThreadModal, never()).show();
		verify(mockGlobalApplicationState).getPlaceChanger();
		verify(mockView).showErrorMessage(anyString());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testOnModeratorModeChange() {
		when(mockView.getModeratorMode()).thenReturn(true);
		forumWidget.onModeratorModeChange();
		verify(mockAvailableThreadListWidget).configure(anyString(), eq(true), any(CallbackP.class));
		verify(mockNewDiscussionThreadModal).configure(anyString(), any(Callback.class));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testConfigureSingleThreadSuccess() {
		//verify that collapsed thread is automatically toggled when showing a single thread
		when(mockDiscussionThreadWidget.isThreadCollapsed()).thenReturn(true);
		AsyncMockStubber.callSuccessWith(mockDiscussionThreadBundle).when(mockDiscussionForumClient)
				.getThread(anyString(), any(AsyncCallback.class));

		String entityId = "syn1";
		String threadId = "007";
		String areaToken = ForumWidget.THREAD_ID_KEY + "=" + threadId;
		ParameterizedToken param = new ParameterizedToken(areaToken);
		Callback callback = null;
		forumWidget.configure(entityId, param, canModerate, callback);

		verify(mockSynAlert).clear();
		verify(mockView).setSingleThreadUIVisible(true);
		verify(mockView).setThreadListUIVisible(false);
		verify(mockView).setNewThreadButtonVisible(false);
		verify(mockView).setShowAllThreadsButtonVisible(true);
		verify(mockDiscussionForumClient).getThread(eq(threadId), any(AsyncCallback.class));
		verify(mockDiscussionThreadWidget).configure(eq(mockDiscussionThreadBundle),
				eq(canModerate), any(Callback.class), eq(SHOW_THREAD_DETAILS_FOR_SINGLE_THREAD),
				eq(SHOW_REPLY_DETAILS_FOR_SINGLE_THREAD));
		verify(mockAvailableThreadListWidget, never()).configure(anyString(), anyBoolean(), any(CallbackP.class));
		verify(mockView).setEmptyUIVisible(false);
		verify(mockView).setThreadHeaderVisible(true);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testConfigureSingleThreadFailure() {
		Exception ex = new Exception("error");
		AsyncMockStubber.callFailureWith(ex).when(mockDiscussionForumClient)
				.getThread(anyString(), any(AsyncCallback.class));

		String entityId = "syn1";
		String threadId = "007";
		String areaToken = ForumWidget.THREAD_ID_KEY + "=" + threadId;
		ParameterizedToken param = new ParameterizedToken(areaToken);
		Callback callback = null;
		forumWidget.configure(entityId, param, canModerate, callback);

		verify(mockSynAlert).clear();
		verify(mockView).setSingleThreadUIVisible(true);
		verify(mockView).setThreadListUIVisible(false);
		verify(mockView).setNewThreadButtonVisible(false);
		verify(mockView).setShowAllThreadsButtonVisible(true);
		verify(mockView).setEmptyUIVisible(true);
		verify(mockView).setThreadHeaderVisible(false);
		verify(mockView).setSingleThreadUIVisible(false);
		verify(mockSynAlert).handleException(ex);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testOnClickShowAllThreads() {
		String entityId = "syn1";
		String threadId = "007";
		String areaToken = ForumWidget.THREAD_ID_KEY + "=" + threadId;
		ParameterizedToken param = new ParameterizedToken(areaToken);
		Callback callback = null;
		forumWidget.configure(entityId, param, canModerate, callback);
		forumWidget.onClickShowAllThreads();

		//attempts to show full thread list
		verify(mockDiscussionForumClient).getForumByProjectId(anyString(), any(AsyncCallback.class));
		verify(mockView).setSingleThreadUIVisible(false);
		verify(mockView).setThreadListUIVisible(true);
		verify(mockSynAlert, times(2)).clear();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testSortByRepliesForum() {
		when(mockForum.getId()).thenReturn("123");
		AsyncMockStubber.callSuccessWith(mockForum).when(mockDiscussionForumClient)
				.getForumByProjectId(anyString(), any(AsyncCallback.class));

		String entityId = "syn1"; 
		String areaToken = "a=b&c=d";
		ParameterizedToken param = new ParameterizedToken(areaToken);
		Callback callback = null;
		forumWidget.configure(entityId, param, canModerate, callback);
		forumWidget.sortByReplies();
		verify(mockAvailableThreadListWidget).sortBy(DiscussionThreadOrder.NUMBER_OF_REPLIES);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testSortByRepliesThread() {
		when(mockDiscussionThreadWidget.isThreadCollapsed()).thenReturn(true);
		AsyncMockStubber.callSuccessWith(mockDiscussionThreadBundle).when(mockDiscussionForumClient)
				.getThread(anyString(), any(AsyncCallback.class));

		String entityId = "syn1";
		String threadId = "007";
		String areaToken = ForumWidget.THREAD_ID_KEY + "=" + threadId;
		ParameterizedToken param = new ParameterizedToken(areaToken);
		Callback callback = null;
		forumWidget.configure(entityId, param, canModerate, callback);
		forumWidget.sortByReplies();
		verify(mockAvailableThreadListWidget, never()).sortBy(DiscussionThreadOrder.NUMBER_OF_REPLIES);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testSortByViewsForum() {
		when(mockForum.getId()).thenReturn("123");
		AsyncMockStubber.callSuccessWith(mockForum).when(mockDiscussionForumClient)
				.getForumByProjectId(anyString(), any(AsyncCallback.class));

		String entityId = "syn1"; 
		String areaToken = "a=b&c=d";
		ParameterizedToken param = new ParameterizedToken(areaToken);
		Callback callback = null;
		forumWidget.configure(entityId, param, canModerate, callback);
		forumWidget.sortByViews();
		verify(mockAvailableThreadListWidget).sortBy(DiscussionThreadOrder.NUMBER_OF_VIEWS);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testSortByViewsThread() {
		when(mockDiscussionThreadWidget.isThreadCollapsed()).thenReturn(true);
		AsyncMockStubber.callSuccessWith(mockDiscussionThreadBundle).when(mockDiscussionForumClient)
				.getThread(anyString(), any(AsyncCallback.class));

		String entityId = "syn1";
		String threadId = "007";
		String areaToken = ForumWidget.THREAD_ID_KEY + "=" + threadId;
		ParameterizedToken param = new ParameterizedToken(areaToken);
		Callback callback = null;
		forumWidget.configure(entityId, param, canModerate, callback);
		forumWidget.sortByViews();
		verify(mockAvailableThreadListWidget, never()).sortBy(DiscussionThreadOrder.NUMBER_OF_VIEWS);
	}


	@SuppressWarnings("unchecked")
	@Test
	public void testSortByActivityForum() {
		when(mockForum.getId()).thenReturn("123");
		AsyncMockStubber.callSuccessWith(mockForum).when(mockDiscussionForumClient)
				.getForumByProjectId(anyString(), any(AsyncCallback.class));

		String entityId = "syn1"; 
		String areaToken = "a=b&c=d";
		ParameterizedToken param = new ParameterizedToken(areaToken);
		Callback callback = null;
		forumWidget.configure(entityId, param, canModerate, callback);
		forumWidget.sortByActivity();
		verify(mockAvailableThreadListWidget).sortBy(DiscussionThreadOrder.LAST_ACTIVITY);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testSortByActivityThread() {
		when(mockDiscussionThreadWidget.isThreadCollapsed()).thenReturn(true);
		AsyncMockStubber.callSuccessWith(mockDiscussionThreadBundle).when(mockDiscussionForumClient)
				.getThread(anyString(), any(AsyncCallback.class));

		String entityId = "syn1";
		String threadId = "007";
		String areaToken = ForumWidget.THREAD_ID_KEY + "=" + threadId;
		ParameterizedToken param = new ParameterizedToken(areaToken);
		Callback callback = null;
		forumWidget.configure(entityId, param, canModerate, callback);
		forumWidget.sortByActivity();
		verify(mockAvailableThreadListWidget, never()).sortBy(DiscussionThreadOrder.LAST_ACTIVITY);
	}
}
