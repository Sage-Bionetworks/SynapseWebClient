package org.sagebionetworks.web.unitclient.widget.discussion;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.sagebionetworks.web.client.widget.discussion.ForumWidget.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;

import static junit.framework.Assert.*;

import org.gwtbootstrap3.client.ui.constants.IconType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.PaginatedIds;
import org.sagebionetworks.repo.model.discussion.DiscussionFilter;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadBundle;
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
import org.sagebionetworks.web.client.widget.discussion.ForumWidget;
import org.sagebionetworks.web.client.widget.discussion.ForumWidgetView;
import org.sagebionetworks.web.client.widget.discussion.SingleDiscussionThreadWidget;
import org.sagebionetworks.web.client.widget.discussion.modal.NewDiscussionThreadModal;
import org.sagebionetworks.web.client.widget.entity.controller.StuAlert;
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
	DiscussionThreadListWidget mockDeletedThreadListWidget;
	@Mock
	CallbackP<Tab> mockOnClickCallback;
	@Mock
	NewDiscussionThreadModal mockNewDiscussionThreadModal;
	@Mock
	StuAlert mockStuAlert;
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
	SingleDiscussionThreadWidget mockDiscussionThreadWidget;
	@Mock
	DiscussionThreadBundle mockDiscussionThreadBundle;
	@Mock
	SingleDiscussionThreadWidget mockDefaultThreadWidget;
	@Mock
	DiscussionThreadBundle mockDefaultDiscussionThreadBundle;
	@Mock
	SubscribeButtonWidget mockSubscribeButtonWidget;
	@Mock
	CallbackP<ParameterizedToken> mockParamChangeCallback;
	@Mock
	Callback mockCallback;
	
	ForumWidget forumWidget;
	private boolean canModerate = false;
	Set<String> moderatorIds;
	String forumId;
	
	public static final String DEFAULT_THREAD_ID = "424242";
	public static final String DEFAULT_THREAD_MESSAGE_KEY = "1234567";
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		when(mockGlobalApplicationState.getSynapseProperty(ForumWidget.DEFAULT_THREAD_ID_KEY)).thenReturn(DEFAULT_THREAD_ID);
		AsyncMockStubber.callSuccessWith(mockDefaultDiscussionThreadBundle).when(mockDiscussionForumClient)
			.getThread(eq(DEFAULT_THREAD_ID), any(AsyncCallback.class));
		when(mockDefaultDiscussionThreadBundle.getId()).thenReturn(DEFAULT_THREAD_ID);
		when(mockDefaultDiscussionThreadBundle.getMessageKey()).thenReturn(DEFAULT_THREAD_MESSAGE_KEY);
		forumWidget = new ForumWidget(mockView, mockStuAlert, mockDiscussionForumClient,
				mockAvailableThreadListWidget,mockDeletedThreadListWidget, mockNewDiscussionThreadModal,
				mockAuthController, mockGlobalApplicationState, mockDiscussionThreadWidget,
				mockSubscribeButtonWidget, mockDefaultThreadWidget);
		when(mockAuthController.isLoggedIn()).thenReturn(true);
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		moderatorIds = new HashSet<String>();

		forumId = "123";
		when(mockForum.getId()).thenReturn(forumId);
		AsyncMockStubber.callSuccessWith(mockForum).when(mockDiscussionForumClient)
				.getForumByProjectId(anyString(), any(AsyncCallback.class));
		PaginatedIds moderators = new PaginatedIds();
		moderators.setResults(new ArrayList<String>());
		moderators.setTotalNumberOfResults(MODERATOR_LIMIT);
		AsyncMockStubber.callSuccessWith(moderators).when(mockDiscussionForumClient)
				.getModerators(eq(forumId), eq(MODERATOR_LIMIT), eq(0L), any(AsyncCallback.class));


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
		verify(mockAvailableThreadListWidget).configure(anyString(), anyBoolean(), anySet(), captorP.capture(), any(DiscussionFilter.class));
		
		Set<String> moderatorIds = new HashSet<String>();
		Callback deleteCallback = null;
		boolean isCurrentUserModerator = false;
		String replyId = null;
		ArgumentCaptor<DiscussionThreadBundle> threadCaptor = ArgumentCaptor.forClass(DiscussionThreadBundle.class);
		verify(mockDefaultThreadWidget).configure(threadCaptor.capture(), eq(replyId), eq(isCurrentUserModerator), eq(moderatorIds), eq(deleteCallback));
		DiscussionThreadBundle defaultThreadBundle = threadCaptor.getValue();
		//verify default thread bundle stats
		assertEquals((Long)0L, defaultThreadBundle.getNumberOfReplies());
		assertEquals((Long)1L, defaultThreadBundle.getNumberOfViews());
		assertFalse(defaultThreadBundle.getIsPinned());
		assertFalse(defaultThreadBundle.getIsEdited());
		assertEquals(DEFAULT_THREAD_ID, defaultThreadBundle.getId());
		assertEquals(DEFAULT_THREAD_MESSAGE_KEY, defaultThreadBundle.getMessageKey());
		
		verify(mockDefaultThreadWidget).setNewReplyContainerVisible(false);
		verify(mockDefaultThreadWidget).setCommandsVisible(false);

		//test "New Thread" button tooltip shown if user attempts to follow default thread
		verify(mockView, never()).showNewThreadTooltip();
		ArgumentCaptor<CallbackP> threadClickedCallbackCaptor = ArgumentCaptor.forClass(CallbackP.class);
		verify(mockDefaultThreadWidget).setThreadIdClickedCallback(threadClickedCallbackCaptor.capture());
		threadClickedCallbackCaptor.getValue().invoke("");
		verify(mockView).showNewThreadTooltip();
		
		//test empty thread callback
		CallbackP emptyThreadsCallback = captorP.getValue();
		reset(mockView);
		boolean isThreads = true;
		emptyThreadsCallback.invoke(isThreads);
		verify(mockView).setDefaultThreadWidgetVisible(!isThreads);

		verify(mockAvailableThreadListWidget).setThreadIdClickedCallback(any(CallbackP.class));
		verify(mockDeletedThreadListWidget).setThreadIdClickedCallback(any(CallbackP.class));
	}
	
	@Test
	public void testDefaultBundleInit() {
		ForumWidget.defaultThreadBundle = null;
		forumWidget.initDefaultThread(DEFAULT_THREAD_ID);
		verify(mockDiscussionForumClient, atLeastOnce()).getThread(eq(DEFAULT_THREAD_ID), any(AsyncCallback.class));
		assertNotNull(ForumWidget.defaultThreadBundle);
		verify(mockDefaultThreadWidget).configure(defaultThreadBundle, null, false, new HashSet<String>(), null);
		ArgumentCaptor<CallbackP> captor = ArgumentCaptor.forClass(CallbackP.class);
		verify(mockDefaultThreadWidget, atLeastOnce()).setThreadIdClickedCallback(captor.capture());
		verify(mockDefaultThreadWidget, atLeastOnce()).setReplyListVisible(false);
		verify(mockDefaultThreadWidget, atLeastOnce()).setNewReplyContainerVisible(false);
		verify(mockDefaultThreadWidget, atLeastOnce()).setCommandsVisible(false);

		captor.getValue().invoke("threadId");
		verify(mockView).showNewThreadTooltip();
	}
	
	@Test
	public void testDefaultThreadBundleCached() {
		reset(mockDiscussionForumClient);
		forumWidget = new ForumWidget(mockView, mockStuAlert, mockDiscussionForumClient,
				mockAvailableThreadListWidget, mockDeletedThreadListWidget, mockNewDiscussionThreadModal,
				mockAuthController, mockGlobalApplicationState, mockDiscussionThreadWidget,
				mockSubscribeButtonWidget, mockDefaultThreadWidget);
		verify(mockDiscussionForumClient, never()).getThread(eq(DEFAULT_THREAD_ID), any(AsyncCallback.class));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testConfigureForum() {
		String entityId = "syn1"; 
		String areaToken = "a=b&c=d";
		ParameterizedToken param = new ParameterizedToken(areaToken);
		forumWidget.configure(entityId, param, canModerate, mockParamChangeCallback, mockCallback);
		verify(mockSubscribeButtonWidget).clear();
		verify(mockSubscribeButtonWidget).configure(SubscriptionObjectType.FORUM, forumId);
		verify(mockStuAlert, atLeastOnce()).clear();

		InOrder inOrder = inOrder(mockView);
		inOrder.verify(mockView).setMainContainerVisible(false);
		inOrder.verify(mockView).setSingleThreadUIVisible(false);
		inOrder.verify(mockView).setThreadListUIVisible(false);
		inOrder.verify(mockView).setNewThreadButtonVisible(false);
		inOrder.verify(mockView).setShowAllThreadsButtonVisible(false);
		inOrder.verify(mockView).setDefaultThreadWidgetVisible(false);
		inOrder.verify(mockView).setDeletedThreadListVisible(false);
		inOrder.verify(mockView).setDeletedThreadButtonVisible(false);
		inOrder.verify(mockView).setThreadListUIVisible(true);
		inOrder.verify(mockView).setNewThreadButtonVisible(true);
		inOrder.verify(mockView).setDeletedThreadButtonVisible(canModerate);
		inOrder.verify(mockView).setMainContainerVisible(true);
		
		verify(mockDiscussionForumClient).getForumByProjectId(anyString(), any(AsyncCallback.class));
		verify(mockNewDiscussionThreadModal).configure(anyString(), any(Callback.class));
		verify(mockAvailableThreadListWidget).clear();
		verify(mockAvailableThreadListWidget).configure(anyString(), eq(canModerate), eq(moderatorIds), any(CallbackP.class), eq(DiscussionFilter.EXCLUDE_DELETED));
		ArgumentCaptor<ParameterizedToken> captorToken = ArgumentCaptor.forClass(ParameterizedToken.class);
		verify(mockParamChangeCallback).invoke(captorToken.capture());
		assertEquals(ParameterizedToken.DEFAULT_TOKEN, captorToken.getValue().toString());
		verify(mockCallback, never()).invoke();
		
		ArgumentCaptor<CallbackP> captorP = ArgumentCaptor.forClass(CallbackP.class);
		verify(mockAvailableThreadListWidget).setThreadIdClickedCallback(captorP.capture());
		CallbackP<DiscussionThreadBundle> threadIdClickedCallback = captorP.getValue();
		String threadId = "9584";
		when(mockDiscussionThreadBundle.getId()).thenReturn(threadId);
		threadIdClickedCallback.invoke(mockDiscussionThreadBundle);
		verify(mockDiscussionForumClient).getThread(eq(threadId), any(AsyncCallback.class));
		verify(mockStuAlert, atLeastOnce()).clear();
		verify(mockDiscussionForumClient).getModerators(eq(mockForum.getId()), eq(MODERATOR_LIMIT), eq(0L), any(AsyncCallback.class));
		
		//going back to the forum should not cause the thread list to reconfigure
		reset(mockAvailableThreadListWidget);
		forumWidget.onClickShowAllThreads();
		verify(mockAvailableThreadListWidget, never()).clear();
		verify(mockAvailableThreadListWidget, never()).configure(anyString(), eq(canModerate), eq(moderatorIds), any(CallbackP.class), eq(DiscussionFilter.EXCLUDE_DELETED));
		verify(mockAvailableThreadListWidget).scrollToThread(threadId);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testConfigureForumSingleForum() {
		String entityId = "syn1"; 
		String areaToken = "a=b&c=d";
		ParameterizedToken param = new ParameterizedToken(areaToken);
		forumWidget.configure(entityId, param, canModerate, mockParamChangeCallback, mockCallback);
		
		ArgumentCaptor<CallbackP> captorP = ArgumentCaptor.forClass(CallbackP.class);
		verify(mockAvailableThreadListWidget).setThreadIdClickedCallback(captorP.capture());
		CallbackP<DiscussionThreadBundle> threadIdClickedCallback = captorP.getValue();
		String threadId = "9584";
		when(mockDiscussionThreadBundle.getId()).thenReturn(threadId);
		AsyncMockStubber.callSuccessWith(mockDiscussionThreadBundle).when(mockDiscussionForumClient)
			.getThread(anyString(), any(AsyncCallback.class));
		threadIdClickedCallback.invoke(mockDiscussionThreadBundle);
		verify(mockDiscussionForumClient).getThread(eq(threadId), any(AsyncCallback.class));
		
		//going back to the forum should cause the thread list to reconfigure if thread was deleted
		reset(mockAvailableThreadListWidget);
		
		ArgumentCaptor<Callback> onShowAllThreadsCallback = ArgumentCaptor.forClass(Callback.class);
		verify(mockDiscussionThreadWidget).configure(any(DiscussionThreadBundle.class), anyString(),
				anyBoolean(), anySet(), onShowAllThreadsCallback.capture());
		onShowAllThreadsCallback.getValue().invoke();
		verify(mockAvailableThreadListWidget).clear();
		verify(mockAvailableThreadListWidget).configure(anyString(), eq(canModerate), eq(moderatorIds), any(CallbackP.class), eq(DiscussionFilter.EXCLUDE_DELETED));
		verify(mockAvailableThreadListWidget, never()).scrollToThread(threadId);
	}

	@Test
	public void testLoadForumSuccess() {
		forumWidget.loadForum("1", mockCallback);

		verify(mockStuAlert, atLeastOnce()).clear();
		verify(mockDiscussionForumClient).getForumByProjectId(anyString(), any(AsyncCallback.class));
		verify(mockDiscussionForumClient).getModerators(anyString(), anyLong(), anyLong(), any(AsyncCallback.class));
		verify(mockStuAlert, never()).handleException(any(Exception.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testLoadForumFailure() {
		Exception exception = new Exception();
		AsyncMockStubber.callFailureWith(exception).when(mockDiscussionForumClient)
				.getForumByProjectId(anyString(), any(AsyncCallback.class));

		forumWidget.loadForum("1", mockCallback);

		verify(mockStuAlert).clear();
		verify(mockDiscussionForumClient).getForumByProjectId(anyString(), any(AsyncCallback.class));
		verify(mockDiscussionForumClient, never()).getModerators(anyString(), anyLong(), anyLong(), any(AsyncCallback.class));
		verify(mockStuAlert).handleException(exception);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testConfigureForumSuccessWithModerator() {
		String entityId = "syn1";
		String areaToken = "";
		canModerate = true;
		ParameterizedToken param = new ParameterizedToken(areaToken);
		forumWidget.configure(entityId, param, canModerate, mockParamChangeCallback, mockCallback);

		verify(mockAvailableThreadListWidget).configure(anyString(), eq(canModerate), eq(moderatorIds), any(CallbackP.class), any(DiscussionFilter.class));
		verify(mockView).setDeletedThreadButtonVisible(true);
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
		AsyncMockStubber.callSuccessWith(mockDiscussionThreadBundle).when(mockDiscussionForumClient)
				.getThread(anyString(), any(AsyncCallback.class));

		String entityId = "syn1";
		String threadId = "007";
		String replyId = null;
		String areaToken = ForumWidget.THREAD_ID_KEY + "=" + threadId;
		ParameterizedToken param = new ParameterizedToken(areaToken);
		forumWidget.configure(entityId, param, canModerate, mockParamChangeCallback, mockCallback);

		verify(mockStuAlert, atLeastOnce()).clear();
		
		InOrder inOrder = inOrder(mockView);
		inOrder.verify(mockView).setMainContainerVisible(false);
		inOrder.verify(mockView).setSingleThreadUIVisible(false);
		inOrder.verify(mockView).setThreadListUIVisible(false);
		inOrder.verify(mockView).setNewThreadButtonVisible(false);
		inOrder.verify(mockView).setShowAllThreadsButtonVisible(false);
		inOrder.verify(mockView).setDefaultThreadWidgetVisible(false);
		inOrder.verify(mockView).setDeletedThreadListVisible(false);
		inOrder.verify(mockView).setDeletedThreadButtonVisible(false);
		inOrder.verify(mockView).setSingleThreadUIVisible(true);
		inOrder.verify(mockView).setShowAllThreadsButtonVisible(true);
		inOrder.verify(mockView).setMainContainerVisible(true);
		
		ArgumentCaptor<Callback> onShowAllThreadsCallback = ArgumentCaptor.forClass(Callback.class);
		verify(mockDiscussionForumClient).getForumByProjectId(anyString(), any(AsyncCallback.class));
		verify(mockDiscussionForumClient).getThread(eq(threadId), any(AsyncCallback.class));
		verify(mockDiscussionThreadWidget).configure(eq(mockDiscussionThreadBundle), eq(replyId),
				eq(canModerate), eq(moderatorIds), onShowAllThreadsCallback.capture());
		verify(mockAvailableThreadListWidget, never()).configure(anyString(), anyBoolean(), anySet(), any(CallbackP.class), any(DiscussionFilter.class));
		
		//verify param was updated
		ArgumentCaptor<ParameterizedToken> captorToken = ArgumentCaptor.forClass(ParameterizedToken.class);
		verify(mockParamChangeCallback).invoke(captorToken.capture());
		assertEquals(threadId, captorToken.getValue().get(THREAD_ID_KEY));
		
		//invoke callback to show all threads
		onShowAllThreadsCallback.getValue().invoke();
		verify(mockDiscussionForumClient).getForumByProjectId(anyString(), any(AsyncCallback.class));
		verify(mockCallback).invoke();
		verify(mockView, atLeastOnce()).setDeletedThreadButtonVisible(false);
	}
	

	@SuppressWarnings("unchecked")
	@Test
	public void testConfigureSingleThreadSingleReplySuccess() {
		AsyncMockStubber.callSuccessWith(mockDiscussionThreadBundle).when(mockDiscussionForumClient)
				.getThread(anyString(), any(AsyncCallback.class));

		String entityId = "syn1";
		String threadId = "007";
		String replyId = "008";
		String areaToken = ForumWidget.THREAD_ID_KEY + "=" + threadId + "&" + ForumWidget.REPLY_ID_KEY + "=" + replyId;
		ParameterizedToken param = new ParameterizedToken(areaToken);
		forumWidget.configure(entityId, param, canModerate, mockParamChangeCallback, mockCallback);

		verify(mockStuAlert, atLeastOnce()).clear();
		
		InOrder inOrder = inOrder(mockView);
		inOrder.verify(mockView).setMainContainerVisible(false);
		inOrder.verify(mockView).setSingleThreadUIVisible(false);
		inOrder.verify(mockView).setThreadListUIVisible(false);
		inOrder.verify(mockView).setNewThreadButtonVisible(false);
		inOrder.verify(mockView).setShowAllThreadsButtonVisible(false);
		inOrder.verify(mockView).setDefaultThreadWidgetVisible(false);
		inOrder.verify(mockView).setDeletedThreadListVisible(false);
		inOrder.verify(mockView).setDeletedThreadButtonVisible(false);
		inOrder.verify(mockView).setSingleThreadUIVisible(true);
		inOrder.verify(mockView).setShowAllThreadsButtonVisible(true);
		inOrder.verify(mockView).setMainContainerVisible(true);
		
		verify(mockDiscussionForumClient).getForumByProjectId(anyString(), any(AsyncCallback.class));
		verify(mockDiscussionForumClient).getThread(eq(threadId), any(AsyncCallback.class));
		verify(mockDiscussionThreadWidget).configure(eq(mockDiscussionThreadBundle), eq(replyId),
				eq(canModerate), eq(moderatorIds), any(Callback.class));
		verify(mockAvailableThreadListWidget, never()).configure(anyString(), anyBoolean(), anySet(), any(CallbackP.class), any(DiscussionFilter.class));

		ArgumentCaptor<CallbackP> onReplyIdCallbackCaptor = ArgumentCaptor.forClass(CallbackP.class);
		verify(mockDiscussionThreadWidget).setReplyIdCallback(onReplyIdCallbackCaptor.capture());
		
		reset(mockParamChangeCallback);
		
		//test reply id callback
		String newReplyId = "009";
		onReplyIdCallbackCaptor.getValue().invoke(newReplyId);
		
		//verify param was updated
		ArgumentCaptor<ParameterizedToken> captorToken = ArgumentCaptor.forClass(ParameterizedToken.class);
		verify(mockParamChangeCallback).invoke(captorToken.capture());
		assertEquals(threadId, captorToken.getValue().get(THREAD_ID_KEY));
		assertEquals(newReplyId, captorToken.getValue().get(REPLY_ID_KEY));
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
		forumWidget.configure(entityId, param, canModerate, mockParamChangeCallback, mockCallback);

		verify(mockStuAlert, atLeastOnce()).clear();
		verify(mockStuAlert).handleException(ex);

		InOrder inOrder = inOrder(mockView);
		inOrder.verify(mockView).setMainContainerVisible(false);
		inOrder.verify(mockView).setSingleThreadUIVisible(false);
		inOrder.verify(mockView).setThreadListUIVisible(false);
		inOrder.verify(mockView).setNewThreadButtonVisible(false);
		inOrder.verify(mockView).setShowAllThreadsButtonVisible(false);
		inOrder.verify(mockView).setDefaultThreadWidgetVisible(false);
		inOrder.verify(mockView).setDeletedThreadListVisible(false);
		inOrder.verify(mockView).setDeletedThreadButtonVisible(false);
		verify(mockView, never()).setSingleThreadUIVisible(true);
		verify(mockView, never()).setShowAllThreadsButtonVisible(true);
		verify(mockView, never()).setMainContainerVisible(true);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testOnClickShowAllThreads() {
		String entityId = "syn1";
		String threadId = "007";
		String areaToken = ForumWidget.THREAD_ID_KEY + "=" + threadId;
		ParameterizedToken param = new ParameterizedToken(areaToken);
		forumWidget.configure(entityId, param, canModerate, mockParamChangeCallback, mockCallback);
		forumWidget.onClickShowAllThreads();

		//attempts to show full thread list
		verify(mockDiscussionForumClient).getForumByProjectId(anyString(), any(AsyncCallback.class));
		verify(mockStuAlert, atLeastOnce()).clear();

		InOrder inOrder = inOrder(mockView);
		inOrder.verify(mockView).setMainContainerVisible(false);
		inOrder.verify(mockView).setSingleThreadUIVisible(false);
		inOrder.verify(mockView).setThreadListUIVisible(false);
		inOrder.verify(mockView).setNewThreadButtonVisible(false);
		inOrder.verify(mockView).setShowAllThreadsButtonVisible(false);
		inOrder.verify(mockView).setDefaultThreadWidgetVisible(false);
		inOrder.verify(mockView).setDeletedThreadListVisible(false);
		inOrder.verify(mockView).setDeletedThreadButtonVisible(false);
		inOrder.verify(mockView).setThreadListUIVisible(true);
		inOrder.verify(mockView).setNewThreadButtonVisible(true);
		inOrder.verify(mockView).setDeletedThreadButtonVisible(canModerate);
		inOrder.verify(mockView).setMainContainerVisible(true);

		verify(mockCallback).invoke();
	}

	@Test
	public void testOnClickDeletedThreadButtonAlreadyShown() {
		when(mockView.isDeletedThreadListVisible()).thenReturn(true);
		forumWidget.onClickDeletedThreadButton();
		verify(mockView).setDeletedThreadListVisible(false);
		verify(mockView).setDeletedThreadButtonIcon(IconType.TOGGLE_RIGHT);
	}

	@Test
	public void testOnClickDeletedThreadButtonNotShown() {
		String forumId = "123";
		when(mockForum.getId()).thenReturn(forumId);
		AsyncMockStubber.callSuccessWith(mockForum).when(mockDiscussionForumClient)
				.getForumByProjectId(anyString(), any(AsyncCallback.class));

		String entityId = "syn1"; 
		String areaToken = "a=b&c=d";
		ParameterizedToken param = new ParameterizedToken(areaToken);
		when(mockView.isDeletedThreadListVisible()).thenReturn(false);
		forumWidget.configure(entityId, param, canModerate, mockParamChangeCallback, mockCallback);
		forumWidget.onClickDeletedThreadButton();
		verify(mockView).setDeletedThreadListVisible(true);
		verify(mockDeletedThreadListWidget).configure(anyString(), eq(canModerate), eq(moderatorIds), eq((CallbackP)null), eq(DiscussionFilter.DELETED_ONLY));
		verify(mockView).setDeletedThreadButtonIcon(IconType.TOGGLE_DOWN);
	}

	@Test
	public void testCreateDefaultTest(){
		DiscussionThreadBundle bundle = DiscussionTestUtils.createThreadBundle(
				"1", "2", "3", "title", "messageKey", false, false, false, "4",
				new Date(System.currentTimeMillis()),
				new Date(System.currentTimeMillis()),
				new Date(System.currentTimeMillis()), "etag",
				Arrays.asList("5"), 2L, 8L);
		DiscussionThreadBundle defaultThread = forumWidget.createDefaultThread(bundle);
		assertEquals(defaultThread.getId(), bundle.getId());
		assertEquals(defaultThread.getCreatedBy(), bundle.getCreatedBy());
		assertEquals(defaultThread.getMessageKey(), bundle.getMessageKey());
		assertEquals(defaultThread.getProjectId(), bundle.getProjectId());
		assertEquals(defaultThread.getTitle(), bundle.getTitle());
		assertNotNull(defaultThread.getActiveAuthors());
		assertNotNull(defaultThread.getIsDeleted());
		assertNotNull(defaultThread.getIsEdited());
		assertNotNull(defaultThread.getIsPinned());
		assertNotNull(defaultThread.getNumberOfReplies());
		assertNotNull(defaultThread.getNumberOfViews());
	}

	@Test
	public void testLoadModeratorsFail() {
		Exception exception = new Exception();
		AsyncMockStubber.callFailureWith(exception).when(mockDiscussionForumClient)
				.getModerators(anyString(), anyLong(), anyLong(), any(AsyncCallback.class));

		forumWidget.loadModerators(forumId, 0L, mockCallback);
		verify(mockStuAlert).clear();
		verify(mockDiscussionForumClient).getModerators(eq(forumId), eq(MODERATOR_LIMIT), eq(0L), any(AsyncCallback.class));
		verifyNoMoreInteractions(mockDiscussionForumClient);
		verify(mockStuAlert).handleException(exception);
		verify(mockCallback, never()).invoke();
	}

	@Test
	public void testLoadModeratorsOnePage() {
		forumWidget.loadModerators(forumId, 0L, mockCallback);
		verify(mockStuAlert).clear();
		verify(mockDiscussionForumClient).getModerators(eq(forumId), eq(MODERATOR_LIMIT), eq(0L), any(AsyncCallback.class));
		verifyNoMoreInteractions(mockDiscussionForumClient);
		verify(mockCallback).invoke();
	}

	@Test
	public void testLoadModeratorsTwoPage() {
		PaginatedIds moderators = new PaginatedIds();
		moderators.setResults(new ArrayList<String>());
		moderators.setTotalNumberOfResults(MODERATOR_LIMIT+1);
		AsyncMockStubber.callSuccessWith(moderators).when(mockDiscussionForumClient)
				.getModerators(eq(forumId), eq(MODERATOR_LIMIT), anyLong(), any(AsyncCallback.class));

		forumWidget.loadModerators(forumId, 0L, mockCallback);
		verify(mockStuAlert, times(2)).clear();
		verify(mockDiscussionForumClient, times(2)).getModerators(eq(forumId), eq(MODERATOR_LIMIT), anyLong(), any(AsyncCallback.class));
		verifyNoMoreInteractions(mockDiscussionForumClient);
		verify(mockCallback).invoke();
	}
}
