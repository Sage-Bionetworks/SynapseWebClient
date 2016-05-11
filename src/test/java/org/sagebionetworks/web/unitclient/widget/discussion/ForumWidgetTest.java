package org.sagebionetworks.web.unitclient.widget.discussion;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.sagebionetworks.web.client.widget.discussion.ForumWidget.*;

import java.util.Set;

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

import com.google.gwt.dev.util.collect.HashSet;
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
	DiscussionThreadWidget mockDefaultThreadWidget;
	@Mock
	DiscussionThreadBundle mockDefaultDiscussionThreadBundle;
	@Mock
	SubscribeButtonWidget mockSubscribeButtonWidget;
	@Mock
	CallbackP<ParameterizedToken> mockParamChangeCallback;
	@Mock
	Callback mockURLChangeCallback;
	
	ForumWidget forumWidget;
	private boolean canModerate = false;
	Set<Long> moderatorIds;
	
	public static final String DEFAULT_THREAD_ID = "424242";
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		when(mockGlobalApplicationState.getSynapseProperty(ForumWidget.DEFAULT_THREAD_ID_KEY)).thenReturn(DEFAULT_THREAD_ID);
		AsyncMockStubber.callSuccessWith(mockDefaultDiscussionThreadBundle).when(mockDiscussionForumClient)
			.getThread(eq(DEFAULT_THREAD_ID), any(AsyncCallback.class));
		forumWidget = new ForumWidget(mockView, mockSynAlert, mockDiscussionForumClient,
				mockAvailableThreadListWidget, mockNewDiscussionThreadModal,
				mockAuthController, mockGlobalApplicationState, mockDiscussionThreadWidget,
				mockSubscribeButtonWidget, mockDefaultThreadWidget);
		when(mockAuthController.isLoggedIn()).thenReturn(true);
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		moderatorIds = new HashSet<Long>();
	}

	@SuppressWarnings({ "unchecked" })
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
		
		ArgumentCaptor<CallbackP> captorP = ArgumentCaptor.forClass(CallbackP.class);
		verify(mockAvailableThreadListWidget).configure(anyString(), anyBoolean(), anySet(), captorP.capture());
		
		verify(mockDiscussionForumClient).getThread(eq(DEFAULT_THREAD_ID), any(AsyncCallback.class));
		Set<Long> moderatorIds = new HashSet<Long>();
		Callback deleteCallback = null;
		boolean showThreadDetails = true;
		boolean showReplyDetails = false;
		boolean isCurrentUserModerator = false;
		verify(mockDefaultThreadWidget).configure(mockDefaultDiscussionThreadBundle, isCurrentUserModerator, moderatorIds, deleteCallback, showThreadDetails, showReplyDetails);
		verify(mockDefaultThreadWidget).setReplyButtonVisible(false);
		verify(mockDefaultThreadWidget).setCommandsVisible(false);
		
		//test empty thread callback
		CallbackP emptyThreadsCallback = captorP.getValue();
		reset(mockView);
		boolean isThreads = true;
		emptyThreadsCallback.invoke(isThreads);
		verify(mockView).setDefaultThreadWidgetVisible(!isThreads);
	}
	
	@Test
	public void testDefaultThreadBundleCached() {
		reset(mockDiscussionForumClient);
		forumWidget = new ForumWidget(mockView, mockSynAlert, mockDiscussionForumClient,
				mockAvailableThreadListWidget, mockNewDiscussionThreadModal,
				mockAuthController, mockGlobalApplicationState, mockDiscussionThreadWidget,
				mockSubscribeButtonWidget, mockDefaultThreadWidget);
		verify(mockDiscussionForumClient, never()).getThread(eq(DEFAULT_THREAD_ID), any(AsyncCallback.class));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testConfigureForumSuccess() {
		String forumId = "123";
		when(mockForum.getId()).thenReturn(forumId);
		AsyncMockStubber.callSuccessWith(mockForum).when(mockDiscussionForumClient)
				.getForumByProjectId(anyString(), any(AsyncCallback.class));

		String entityId = "syn1"; 
		String areaToken = "a=b&c=d";
		ParameterizedToken param = new ParameterizedToken(areaToken);
		forumWidget.configure(entityId, param, canModerate, moderatorIds, mockParamChangeCallback, mockURLChangeCallback);
		verify(mockSubscribeButtonWidget).clear();
		verify(mockSubscribeButtonWidget).configure(SubscriptionObjectType.FORUM, forumId);
		verify(mockSynAlert).clear();
		verify(mockView).setSingleThreadUIVisible(false);
		verify(mockView).setThreadListUIVisible(true);
		verify(mockView).setDefaultThreadWidgetVisible(false);
		verify(mockView).setNewThreadButtonVisible(true);
		verify(mockView).setShowAllThreadsButtonVisible(false);
		verify(mockDiscussionForumClient).getForumByProjectId(anyString(), any(AsyncCallback.class));
		verify(mockNewDiscussionThreadModal).configure(anyString(), any(Callback.class));
		verify(mockAvailableThreadListWidget).configure(anyString(), eq(canModerate), eq(moderatorIds), any(CallbackP.class));
		ArgumentCaptor<ParameterizedToken> captorToken = ArgumentCaptor.forClass(ParameterizedToken.class);
		verify(mockParamChangeCallback).invoke(captorToken.capture());
		assertEquals(ParameterizedToken.DEFAULT_TOKEN, captorToken.getValue().toString());
		verify(mockURLChangeCallback, never()).invoke();
		
		ArgumentCaptor<CallbackP> captorP = ArgumentCaptor.forClass(CallbackP.class);
		verify(mockAvailableThreadListWidget).setThreadIdClickedCallback(captorP.capture());
		CallbackP<String> threadIdClickedCallback = captorP.getValue();
		String threadId = "9584";
		threadIdClickedCallback.invoke(threadId);
		verify(mockDiscussionForumClient).getThread(eq(threadId), any(AsyncCallback.class));

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
		forumWidget.configure(entityId, param, canModerate, moderatorIds, mockParamChangeCallback, mockURLChangeCallback);

		verify(mockSynAlert).clear();
		verify(mockView).setSingleThreadUIVisible(false);
		verify(mockView).setThreadListUIVisible(true);
		verify(mockView).setNewThreadButtonVisible(true);
		verify(mockView).setShowAllThreadsButtonVisible(false);
		verify(mockDiscussionForumClient).getForumByProjectId(anyString(), any(AsyncCallback.class));
		verify(mockNewDiscussionThreadModal, never()).configure(anyString(), any(Callback.class));
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
		forumWidget.configure(entityId, param, canModerate, moderatorIds, mockParamChangeCallback, mockURLChangeCallback);

		verify(mockAvailableThreadListWidget).configure(anyString(), eq(canModerate), eq(moderatorIds), any(CallbackP.class));
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
	public void testConfigureSingleThreadSuccess() {
		//verify that collapsed thread is automatically toggled when showing a single thread
		when(mockDiscussionThreadWidget.isThreadCollapsed()).thenReturn(true);
		AsyncMockStubber.callSuccessWith(mockDiscussionThreadBundle).when(mockDiscussionForumClient)
				.getThread(anyString(), any(AsyncCallback.class));

		String entityId = "syn1";
		String threadId = "007";
		String areaToken = ForumWidget.THREAD_ID_KEY + "=" + threadId;
		ParameterizedToken param = new ParameterizedToken(areaToken);
		forumWidget.configure(entityId, param, canModerate, moderatorIds, mockParamChangeCallback, mockURLChangeCallback);

		verify(mockSynAlert).clear();
		verify(mockView).setSingleThreadUIVisible(true);
		verify(mockView).setThreadListUIVisible(false);
		verify(mockView).setNewThreadButtonVisible(false);
		verify(mockView).setShowAllThreadsButtonVisible(true);
		verify(mockView).setDefaultThreadWidgetVisible(false);
		ArgumentCaptor<Callback> onShowAllThreadsCallback = ArgumentCaptor.forClass(Callback.class);
		verify(mockDiscussionForumClient).getThread(eq(threadId), any(AsyncCallback.class));
		verify(mockDiscussionThreadWidget).configure(eq(mockDiscussionThreadBundle),
				eq(canModerate), eq(moderatorIds), onShowAllThreadsCallback.capture(), eq(SHOW_THREAD_DETAILS_FOR_SINGLE_THREAD),
				eq(SHOW_REPLY_DETAILS_FOR_SINGLE_THREAD));
		verify(mockAvailableThreadListWidget, never()).configure(anyString(), anyBoolean(), anySet(), any(CallbackP.class));
		verify(mockView).setThreadHeaderVisible(true);
		
		//verify param was updated
		ArgumentCaptor<ParameterizedToken> captorToken = ArgumentCaptor.forClass(ParameterizedToken.class);
		verify(mockParamChangeCallback).invoke(captorToken.capture());
		assertEquals(threadId, captorToken.getValue().get(THREAD_ID_KEY));
		
		//invoke callback to show all threads
		onShowAllThreadsCallback.getValue().invoke();
		verify(mockDiscussionForumClient).getForumByProjectId(anyString(), any(AsyncCallback.class));
		verify(mockURLChangeCallback).invoke();
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
		forumWidget.configure(entityId, param, canModerate, moderatorIds, mockParamChangeCallback, mockURLChangeCallback);

		verify(mockSynAlert).clear();
		verify(mockView).setSingleThreadUIVisible(true);
		verify(mockView).setThreadListUIVisible(false);
		verify(mockView).setNewThreadButtonVisible(false);
		verify(mockView).setShowAllThreadsButtonVisible(true);
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
		forumWidget.configure(entityId, param, canModerate, moderatorIds, mockParamChangeCallback, mockURLChangeCallback);
		forumWidget.onClickShowAllThreads();

		//attempts to show full thread list
		verify(mockDiscussionForumClient).getForumByProjectId(anyString(), any(AsyncCallback.class));
		verify(mockView).setSingleThreadUIVisible(false);
		verify(mockView).setThreadListUIVisible(true);
		verify(mockSynAlert, times(2)).clear();
		
		verify(mockURLChangeCallback).invoke();
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
		forumWidget.configure(entityId, param, canModerate, moderatorIds, mockParamChangeCallback, mockURLChangeCallback);
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
		forumWidget.configure(entityId, param, canModerate, moderatorIds, mockParamChangeCallback, mockURLChangeCallback);
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
		forumWidget.configure(entityId, param, canModerate, moderatorIds, mockParamChangeCallback, mockURLChangeCallback);
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
		forumWidget.configure(entityId, param, canModerate, moderatorIds, mockParamChangeCallback, mockURLChangeCallback);
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
		forumWidget.configure(entityId, param, canModerate, moderatorIds, mockParamChangeCallback, mockURLChangeCallback);
		forumWidget.sortByActivity();
		verify(mockAvailableThreadListWidget).sortBy(DiscussionThreadOrder.PINNED_AND_LAST_ACTIVITY);
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
		forumWidget.configure(entityId, param, canModerate, moderatorIds, mockParamChangeCallback, mockURLChangeCallback);
		forumWidget.sortByActivity();
		verify(mockAvailableThreadListWidget, never()).sortBy(DiscussionThreadOrder.PINNED_AND_LAST_ACTIVITY);
	}
}
