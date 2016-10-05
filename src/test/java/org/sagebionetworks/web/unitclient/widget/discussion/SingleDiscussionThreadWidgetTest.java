package org.sagebionetworks.web.unitclient.widget.discussion;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.client.widget.discussion.SingleDiscussionThreadWidget.LIMIT;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.gwtbootstrap3.extras.bootbox.client.callback.AlertCallback;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.discussion.CreateDiscussionReply;
import org.sagebionetworks.repo.model.discussion.DiscussionFilter;
import org.sagebionetworks.repo.model.discussion.DiscussionReplyBundle;
import org.sagebionetworks.repo.model.discussion.DiscussionReplyOrder;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadBundle;
import org.sagebionetworks.repo.model.subscription.SubscriptionObjectType;
import org.sagebionetworks.web.client.DiscussionForumClientAsync;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.RequestBuilderWrapper;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.LoadMoreWidgetContainer;
import org.sagebionetworks.web.client.widget.discussion.ForumWidget;
import org.sagebionetworks.web.client.widget.discussion.ReplyWidget;
import org.sagebionetworks.web.client.widget.discussion.SingleDiscussionThreadWidget;
import org.sagebionetworks.web.client.widget.discussion.SingleDiscussionThreadWidgetView;
import org.sagebionetworks.web.client.widget.discussion.modal.EditDiscussionThreadModal;
import org.sagebionetworks.web.client.widget.entity.MarkdownEditorWidget;
import org.sagebionetworks.web.client.widget.entity.MarkdownWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.refresh.ReplyCountAlert;
import org.sagebionetworks.web.client.widget.subscription.SubscribeButtonWidget;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import org.sagebionetworks.web.test.helper.RequestBuilderMockStubber;

import com.google.gwt.dev.util.collect.HashSet;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public class SingleDiscussionThreadWidgetTest {

	@Mock
	SingleDiscussionThreadWidgetView mockView;
	@Mock
	PortalGinInjector mockGinInjector;
	@Mock
	ReplyWidget mockReplyWidget;
	@Mock
	SynapseJSNIUtils mockJsniUtils;
	@Mock
	MarkdownEditorWidget mockMarkdownEditorWidget;
	@Mock
	SynapseAlert mockSynAlert;
	@Mock
	DiscussionForumClientAsync mockDiscussionForumClientAsync;
	@Mock
	PaginatedResults<DiscussionReplyBundle> mockReplyBundlePage;
	@Mock
	UserBadge mockUserBadge;
	@Mock
	UserBadge mockAuthorWidget;
	@Mock
	RequestBuilderWrapper mockRequestBuilder;
	@Mock
	Response mockResponse;
	@Mock
	AuthenticationController mockAuthController;
	@Mock
	GlobalApplicationState mockGlobalApplicationState;
	@Mock
	PlaceChanger mockPlaceChanger;
	@Mock
	EditDiscussionThreadModal mockEditThreadModal;
	@Mock
	MarkdownWidget mockMarkdownWidget;
	@Mock
	Callback mockCallback;
	@Mock
	SubscribeButtonWidget mockSubscribeButtonWidget;
	@Mock
	ReplyCountAlert mockRefreshAlert;
	@Mock
	CallbackP<String> mockThreadIdClickedCallback;
	@Mock
	CallbackP<String> mockReplyIdCallback;
	@Mock
	DiscussionReplyBundle mockDiscussionReplyBundle;
	@Mock
	LoadMoreWidgetContainer mockRepliesContainer;
	Set<String> moderatorIds;
	SingleDiscussionThreadWidget discussionThreadWidget;
	List<DiscussionReplyBundle> bundleList;
	private static final String CREATED_BY = "123";
	private static final String NON_AUTHOR = "456";
	
	private static final String REPLY_ID_NULL = null;

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		when(mockGinInjector.createReplyWidget()).thenReturn(mockReplyWidget);
		when(mockGinInjector.getUserBadgeWidget()).thenReturn(mockUserBadge);
		when(mockGinInjector.getReplyCountAlert()).thenReturn(mockRefreshAlert);
		discussionThreadWidget = new SingleDiscussionThreadWidget(mockView, mockMarkdownEditorWidget,
				mockSynAlert, mockAuthorWidget, mockDiscussionForumClientAsync,
				mockGinInjector, mockJsniUtils, mockRequestBuilder, mockAuthController,
				mockGlobalApplicationState, mockEditThreadModal, mockMarkdownWidget,
				mockRepliesContainer, mockSubscribeButtonWidget);
		when(mockAuthController.isLoggedIn()).thenReturn(true);
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		when(mockAuthController.getCurrentUserPrincipalId()).thenReturn(NON_AUTHOR);
		moderatorIds = new HashSet<String>();
	}

	@Test
	public void testConstructor() {
		verify(mockView).setPresenter(discussionThreadWidget);
		verify(mockView).setMarkdownEditorWidget(any(Widget.class));
		verify(mockView).setAlert(any(Widget.class));
		verify(mockView).setAuthor(any(Widget.class));
		verify(mockView).setEditThreadModal(any(Widget.class));
		verify(mockView).setSubscribeButtonWidget(any(Widget.class));
		verify(mockSubscribeButtonWidget).showIconOnly();
		verify(mockRepliesContainer).configure(any(Callback.class));
		verify(mockMarkdownEditorWidget).showExternalImageButton();
		verify(mockMarkdownEditorWidget).hideUploadRelatedCommands();
	}

	@Test
	public void testConfigure() {
		boolean isDeleted = false;
		boolean canModerate = false;
		boolean isEdited = false;
		boolean isPinned = false;
		String threadId = "1";
		DiscussionThreadBundle threadBundle = DiscussionTestUtils.createThreadBundle(threadId, "title",
				Arrays.asList("123"), 1L, 2L, new Date(), "messageKey", isDeleted,
				CREATED_BY, isEdited, isPinned);
		discussionThreadWidget.configure(threadBundle, REPLY_ID_NULL, canModerate, moderatorIds, mockCallback);
		verify(mockView).clear();
		verify(mockView).setTitle("title");
		verify(mockView).setCreatedOn(anyString());
		verify(mockView).setIsAuthorModerator(false);
		verify(mockAuthorWidget).configure(anyString());
		verify(mockView).setDeleteIconVisible(false);
		verify(mockView).setEditIconVisible(false);
		verify(mockView).setEditedLabelVisible(false);
		verify(mockView).setThreadLink(anyString());
		verify(mockView).setPinIconVisible(false);
		verify(mockView).setUnpinIconVisible(false);
		verify(mockView).setRefreshAlert(any(Widget.class));
		verify(mockRefreshAlert).setRefreshCallback(any(Callback.class));
		verify(mockRefreshAlert).configure(threadId);
		verify(mockView).setDeletedThreadVisible(false);
		verify(mockView).setReplyContainerVisible(true);
		verify(mockView).setCommandsVisible(true);
		verify(mockView).setRestoreIconVisible(false);
	}

	@Test
	public void testConfigureDeletedThread() {
		boolean isDeleted = true;
		boolean canModerate = false;
		boolean isEdited = false;
		boolean isPinned = false;
		String threadId = "1";
		DiscussionThreadBundle threadBundle = DiscussionTestUtils.createThreadBundle(threadId, "title",
				Arrays.asList("123"), 1L, 2L, new Date(), "messageKey", isDeleted,
				CREATED_BY, isEdited, isPinned);
		discussionThreadWidget.configure(threadBundle, REPLY_ID_NULL, canModerate, moderatorIds, mockCallback);
		verify(mockView).clear();
		verify(mockView).setTitle("title");
		verify(mockView).setCreatedOn(anyString());
		verify(mockView).setIsAuthorModerator(false);
		verify(mockAuthorWidget).configure(anyString());
		verify(mockView, never()).setDeleteIconVisible(false);
		verify(mockView, never()).setEditIconVisible(false);
		verify(mockView).setEditedLabelVisible(false);
		verify(mockView, never()).setThreadLink(anyString());
		verify(mockView, never()).setPinIconVisible(false);
		verify(mockView, never()).setUnpinIconVisible(false);
		verify(mockView).setRefreshAlert(any(Widget.class));
		verify(mockRefreshAlert).setRefreshCallback(any(Callback.class));
		verify(mockRefreshAlert).configure(threadId);
		verify(mockView).setDeletedThreadVisible(true);
		verify(mockView).setReplyContainerVisible(false);
		verify(mockView).setCommandsVisible(false);
		verify(mockView).setRestoreIconVisible(true);
	}

	@Test
	public void testConfigureDefaultThread() {
		boolean isDeleted = false;
		boolean canModerate = false;
		boolean isEdited = false;
		boolean isPinned = false;
		String threadId = "1";
		when(mockGlobalApplicationState.getSynapseProperty(ForumWidget.DEFAULT_THREAD_ID_KEY)).thenReturn(threadId);
		DiscussionThreadBundle threadBundle = DiscussionTestUtils.createThreadBundle(threadId, "title",
				Arrays.asList("123"), 1L, 2L, new Date(), "messageKey", isDeleted,
				CREATED_BY, isEdited, isPinned);
		discussionThreadWidget.configure(threadBundle, REPLY_ID_NULL, canModerate, moderatorIds, mockCallback);
		verify(mockGinInjector, never()).getReplyCountAlert();
		verify(mockDiscussionForumClientAsync, never()).getRepliesForThread(anyString(),
				anyLong(), anyLong(), any(DiscussionReplyOrder.class), anyBoolean(),
				any(DiscussionFilter.class), any(AsyncCallback.class));
	}

	@Test
	public void testConfigureSingleThread() {
		boolean isDeleted = false;
		boolean canModerate = false;
		boolean isEdited = false;
		boolean isPinned = false;
		String threadId = "1";
		DiscussionThreadBundle threadBundle = DiscussionTestUtils.createThreadBundle(threadId, "title",
				Arrays.asList("123"), 1L, 2L, new Date(), "messageKey", isDeleted,
				CREATED_BY, isEdited, isPinned);
		discussionThreadWidget.configure(threadBundle, REPLY_ID_NULL, canModerate, moderatorIds, mockCallback);
		verify(mockRefreshAlert).configure(threadId);
	}

	@Test
	public void testConfigureWithZeroReplies(){
		boolean isDeleted = false;
		boolean canModerate = false;
		boolean isEdited = false;
		boolean isPinned = false;
		DiscussionThreadBundle threadBundle = DiscussionTestUtils.createThreadBundle("1", "title",
				Arrays.asList("123"), 0L, 2L, new Date(), "messageKey", isDeleted,
				CREATED_BY, isEdited, isPinned);
		discussionThreadWidget.configure(threadBundle, REPLY_ID_NULL, canModerate, moderatorIds, mockCallback);
		verify(mockView).clear();
		verify(mockView).setTitle("title");
		verify(mockView).setCreatedOn(anyString());
		verify(mockAuthorWidget).configure(anyString());
		verify(mockView).setDeleteIconVisible(false);
	}

	@Test
	public void testConfigureIsAuthorModerator() {
		boolean isDeleted = false;
		boolean canModerate = false;
		boolean isEdited = false;
		boolean isPinned = false;
		moderatorIds.add(CREATED_BY);
		DiscussionThreadBundle threadBundle = DiscussionTestUtils.createThreadBundle("1", "title",
				Arrays.asList("123"), 1L, 2L, new Date(), "messageKey", isDeleted,
				CREATED_BY, isEdited, isPinned);
		discussionThreadWidget.configure(threadBundle, REPLY_ID_NULL, canModerate, moderatorIds,mockCallback);
		verify(mockView).setIsAuthorModerator(true);
	}
	
	@Test
	public void testConfigureWithEdited() {
		boolean isDeleted = false;
		boolean canModerate = false;
		boolean isEdited = true;
		boolean isPinned = false;
		DiscussionThreadBundle threadBundle = DiscussionTestUtils.createThreadBundle("1", "title",
				Arrays.asList("123"), 1L, 2L, new Date(), "messageKey", isDeleted,
				CREATED_BY, isEdited, isPinned);
		discussionThreadWidget.configure(threadBundle, REPLY_ID_NULL, canModerate, moderatorIds,mockCallback);
		verify(mockView).setEditedLabelVisible(true);
	}

	@Test
	public void testConfigureAuthor() {
		boolean isDeleted = false;
		boolean canModerate = false;
		boolean isEdited = false;
		boolean isPinned = false;
		when(mockAuthController.getCurrentUserPrincipalId()).thenReturn(CREATED_BY);
		DiscussionThreadBundle threadBundle = DiscussionTestUtils.createThreadBundle("1", "title",
				Arrays.asList("123"), 1L, 2L, new Date(), "messageKey", isDeleted,
				CREATED_BY, isEdited, isPinned);
		discussionThreadWidget.configure(threadBundle, REPLY_ID_NULL, canModerate, moderatorIds,mockCallback);
		verify(mockView).setEditIconVisible(true);
	}

	@Test
	public void testConfigureWithModerator() {
		boolean isDeleted = false;
		boolean canModerate = true;
		boolean isEdited = false;
		boolean isPinned = false;
		DiscussionThreadBundle threadBundle = DiscussionTestUtils.createThreadBundle("1", "title",
				Arrays.asList("123"), 1L, 2L, new Date(), "messageKey", isDeleted,
				CREATED_BY, isEdited, isPinned);
		discussionThreadWidget.configure(threadBundle, REPLY_ID_NULL, canModerate, moderatorIds,mockCallback);
		verify(mockView).clear();
		verify(mockView).setTitle("title");
		verify(mockView).setCreatedOn(anyString());
		verify(mockAuthorWidget).configure(anyString());
		verify(mockView).setDeleteIconVisible(true);
		verify(mockView).setEditedLabelVisible(false);
		verify(mockView).setPinIconVisible(true);
		verify(mockView).setUnpinIconVisible(false);
	}
	
	@Test
	public void testConfigurePinnedWithModerator() {
		boolean isDeleted = false;
		boolean canModerate = true;
		boolean isEdited = false;
		boolean isPinned = true;
		DiscussionThreadBundle threadBundle = DiscussionTestUtils.createThreadBundle("1", "title",
				Arrays.asList("123"), 1L, 2L, new Date(), "messageKey", isDeleted,
				CREATED_BY, isEdited, isPinned);
		discussionThreadWidget.configure(threadBundle, REPLY_ID_NULL, canModerate, moderatorIds,mockCallback);
		verify(mockView).setPinIconVisible(false);
		verify(mockView).setUnpinIconVisible(true);
	}
	
	@Test
	public void testConfigurePinned() {
		boolean isDeleted = false;
		boolean canModerate = false;
		boolean isEdited = false;
		boolean isPinned = true;
		DiscussionThreadBundle threadBundle = DiscussionTestUtils.createThreadBundle("1", "title",
				Arrays.asList("123"), 1L, 2L, new Date(), "messageKey", isDeleted,
				CREATED_BY, isEdited, isPinned);
		discussionThreadWidget.configure(threadBundle, REPLY_ID_NULL, canModerate, moderatorIds,mockCallback);
		verify(mockView).setPinIconVisible(false);
		verify(mockView).setUnpinIconVisible(false);
	}
	
	@Test
	public void testPin() {
		boolean isDeleted = false;
		boolean canModerate = true;
		boolean isEdited = false;
		boolean isPinned = false;
		DiscussionThreadBundle threadBundle = DiscussionTestUtils.createThreadBundle("1", "title",
				Arrays.asList("123"), 1L, 2L, new Date(), "messageKey", isDeleted,
				CREATED_BY, isEdited, isPinned);
		discussionThreadWidget.configure(threadBundle, REPLY_ID_NULL, canModerate, moderatorIds,mockCallback);
		AsyncMockStubber.callSuccessWith(null)
			.when(mockDiscussionForumClientAsync).pinThread(anyString(), any(AsyncCallback.class));
		reset(mockView);
		discussionThreadWidget.onClickPinThread();
		verify(mockDiscussionForumClientAsync).pinThread(anyString(), any(AsyncCallback.class));
		verify(mockView).setPinIconVisible(false);
		verify(mockView).setUnpinIconVisible(true);
	}
	

	@Test
	public void testPinFailure() {
		boolean isDeleted = false;
		boolean canModerate = true;
		boolean isEdited = false;
		boolean isPinned = false;
		DiscussionThreadBundle threadBundle = DiscussionTestUtils.createThreadBundle("1", "title",
				Arrays.asList("123"), 1L, 2L, new Date(), "messageKey", isDeleted,
				CREATED_BY, isEdited, isPinned);
		discussionThreadWidget.configure(threadBundle, REPLY_ID_NULL, canModerate, moderatorIds,mockCallback);
		Exception ex = new Exception("failed");
		AsyncMockStubber.callFailureWith(ex)
			.when(mockDiscussionForumClientAsync).pinThread(anyString(), any(AsyncCallback.class));
		reset(mockView);
		discussionThreadWidget.onClickPinThread();
		verify(mockDiscussionForumClientAsync).pinThread(anyString(), any(AsyncCallback.class));
		verify(mockSynAlert).handleException(ex);
	}
	
	@Test
	public void testUnpin() {
		boolean isDeleted = false;
		boolean canModerate = true;
		boolean isEdited = false;
		boolean isPinned = true;
		DiscussionThreadBundle threadBundle = DiscussionTestUtils.createThreadBundle("1", "title",
				Arrays.asList("123"), 1L, 2L, new Date(), "messageKey", isDeleted,
				CREATED_BY, isEdited, isPinned);
		discussionThreadWidget.configure(threadBundle, REPLY_ID_NULL, canModerate, moderatorIds,mockCallback);
		AsyncMockStubber.callSuccessWith(null)
			.when(mockDiscussionForumClientAsync).unpinThread(anyString(), any(AsyncCallback.class));
		reset(mockView);
		discussionThreadWidget.onClickUnpinThread();
		verify(mockDiscussionForumClientAsync).unpinThread(anyString(), any(AsyncCallback.class));
		verify(mockView).setPinIconVisible(true);
		verify(mockView).setUnpinIconVisible(false);
	}
	
	@Test
	public void testUnpinFailure() {
		boolean isDeleted = false;
		boolean canModerate = true;
		boolean isEdited = false;
		boolean isPinned = true;
		DiscussionThreadBundle threadBundle = DiscussionTestUtils.createThreadBundle("1", "title",
				Arrays.asList("123"), 1L, 2L, new Date(), "messageKey", isDeleted,
				CREATED_BY, isEdited, isPinned);
		discussionThreadWidget.configure(threadBundle, REPLY_ID_NULL, canModerate, moderatorIds, mockCallback);
		Exception ex = new Exception("failed");
		AsyncMockStubber.callFailureWith(ex)
			.when(mockDiscussionForumClientAsync).unpinThread(anyString(), any(AsyncCallback.class));
		reset(mockView);
		discussionThreadWidget.onClickUnpinThread();
		verify(mockDiscussionForumClientAsync).unpinThread(anyString(), any(AsyncCallback.class));
		verify(mockSynAlert).handleException(ex);
	}

	@Test
	public void asWidgetTest() {
		discussionThreadWidget.asWidget();
		verify(mockView).asWidget();
	}
	
	//Note: The discussion thread widget no longer supports toggling.  It is initially configured to show message/replies.

	@Test
	public void testOnClickNewReply() {
		discussionThreadWidget.onClickNewReply();
		verify(mockMarkdownEditorWidget).configure(anyString());
		verify(mockView).setReplyTextBoxVisible(false);
		verify(mockView).setNewReplyContainerVisible(true);
		verify(mockMarkdownEditorWidget).setMarkdownFocus();
	}

	@Test
	public void testOnClickNewReplyAnonymous() {
		when(mockAuthController.isLoggedIn()).thenReturn(false);
		discussionThreadWidget.onClickNewReply();
		verify(mockMarkdownEditorWidget, never()).configure(anyString());
		verify(mockGlobalApplicationState).getPlaceChanger();
		verify(mockView).showErrorMessage(anyString());
		verify(mockView, never()).setReplyTextBoxVisible(false);
		verify(mockView, never()).setNewReplyContainerVisible(true);
		verify(mockMarkdownEditorWidget, never()).setMarkdownFocus();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testConfigureReplies() {
		boolean isDeleted = false;
		boolean canModerate = false;
		boolean isEdited = false;
		boolean isPinned = false;
		DiscussionThreadBundle threadBundle = DiscussionTestUtils.createThreadBundle("1", "title",
				Arrays.asList("123"), 0L, 2L, new Date(), "messageKey", isDeleted,
				CREATED_BY, isEdited, isPinned);
		discussionThreadWidget.configure(threadBundle, REPLY_ID_NULL, canModerate, moderatorIds, mockCallback);
		verify(mockSynAlert, atLeastOnce()).clear();
		verify(mockRepliesContainer, atLeastOnce()).clear();
		verify(mockView).setShowAllRepliesButtonVisible(false);
		verify(mockDiscussionForumClientAsync).getRepliesForThread(anyString(),
				anyLong(), anyLong(), any(DiscussionReplyOrder.class), anyBoolean(),
				any(DiscussionFilter.class), any(AsyncCallback.class));
		verify(mockView).setDeleteIconVisible(false);
	}
	
	@Test
	public void testConfigureReply() {
		boolean isDeleted = false;
		boolean canModerate = false;
		boolean isEdited = false;
		boolean isPinned = false;
		String replyId = "123";
		AsyncMockStubber.callSuccessWith(mockDiscussionReplyBundle)
			.when(mockDiscussionForumClientAsync).getReply(anyString(), any(AsyncCallback.class));

		DiscussionThreadBundle threadBundle = DiscussionTestUtils.createThreadBundle("1", "title",
				Arrays.asList("123"), 0L, 2L, new Date(), "messageKey", isDeleted,
				CREATED_BY, isEdited, isPinned);
		discussionThreadWidget.setReplyIdCallback(mockReplyIdCallback);
		discussionThreadWidget.configure(threadBundle, replyId, canModerate, moderatorIds, mockCallback);
		verify(mockSynAlert, atLeastOnce()).clear();
		verify(mockRepliesContainer, atLeastOnce()).clear();
		verify(mockView).setShowAllRepliesButtonVisible(true);
		verify(mockDiscussionForumClientAsync).getReply(eq(replyId), any(AsyncCallback.class));
		verify(mockView).setDeleteIconVisible(false);
		verify(mockRepliesContainer).add(any(Widget.class));
		verify(mockReplyIdCallback).invoke(replyId);
	}
	
	@Test
	public void testConfigureReplyFailure() {
		boolean isDeleted = false;
		boolean canModerate = false;
		boolean isEdited = false;
		boolean isPinned = false;
		String replyId = "123";
		Exception ex = new Exception("error");
		AsyncMockStubber.callFailureWith(ex)
			.when(mockDiscussionForumClientAsync).getReply(anyString(), any(AsyncCallback.class));

		DiscussionThreadBundle threadBundle = DiscussionTestUtils.createThreadBundle("1", "title",
				Arrays.asList("123"), 0L, 2L, new Date(), "messageKey", isDeleted,
				CREATED_BY, isEdited, isPinned);
		discussionThreadWidget.configure(threadBundle, replyId, canModerate, moderatorIds, mockCallback);
		verify(mockSynAlert).handleException(ex);;
	}
	

	@Test
	public void testOnClickShowAllReplies() {
		discussionThreadWidget.setReplyIdCallback(mockReplyIdCallback);
		
		discussionThreadWidget.onClickShowAllReplies();
		verify(mockReplyIdCallback).invoke(null);
		verify(mockSynAlert, atLeastOnce()).clear();
		verify(mockRepliesContainer).clear();
		verify(mockView).setShowAllRepliesButtonVisible(false);
		verify(mockDiscussionForumClientAsync).getRepliesForThread(anyString(),
				anyLong(), anyLong(), any(DiscussionReplyOrder.class), anyBoolean(),
				any(DiscussionFilter.class), any(AsyncCallback.class));
		verify(mockRefreshAlert).configure(anyString());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testLoadmoreSuccess() {
		boolean isDeleted = false;
		boolean canModerate = false;
		boolean isEdited = false;
		boolean isPinned = false;
		DiscussionThreadBundle threadBundle = DiscussionTestUtils.createThreadBundle("1", "title",
				Arrays.asList("123"), 0L, 2L, new Date(), "messageKey", isDeleted,
				CREATED_BY, isEdited, isPinned);
		when(mockReplyBundlePage.getTotalNumberOfResults()).thenReturn(2L);
		bundleList = DiscussionTestUtils.createReplyBundleList(2);
		when(mockReplyBundlePage.getResults()).thenReturn(bundleList);
		AsyncMockStubber.callSuccessWith(mockReplyBundlePage)
				.when(mockDiscussionForumClientAsync).getRepliesForThread(anyString(), anyLong(),
						anyLong(), any(DiscussionReplyOrder.class), anyBoolean(), 
						any(DiscussionFilter.class), any(AsyncCallback.class));
		discussionThreadWidget.configure(threadBundle, REPLY_ID_NULL, canModerate, moderatorIds, mockCallback);
		verify(mockSynAlert, atLeastOnce()).clear();
		verify(mockDiscussionForumClientAsync).getRepliesForThread(anyString(),
				anyLong(), anyLong(), any(DiscussionReplyOrder.class), anyBoolean(),
				any(DiscussionFilter.class), any(AsyncCallback.class));
		verify(mockRepliesContainer, atLeastOnce()).clear();
		verify(mockRepliesContainer, times(2)).add(any(Widget.class));
		verify(mockGinInjector, times(2)).createReplyWidget();
		verify(mockView).setDeleteIconVisible(false);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testLoadmoreEmpty() {
		boolean isDeleted = false;
		boolean canModerate = false;
		boolean isEdited = false;
		boolean isPinned = false;
		DiscussionThreadBundle threadBundle = DiscussionTestUtils.createThreadBundle("1", "title",
				Arrays.asList("123"), 0L, 2L, new Date(), "messageKey", isDeleted,
				CREATED_BY, isEdited, isPinned);
		bundleList = DiscussionTestUtils.createReplyBundleList(0);
		when(mockReplyBundlePage.getTotalNumberOfResults()).thenReturn(2L);
		when(mockReplyBundlePage.getResults()).thenReturn(bundleList);
		AsyncMockStubber.callSuccessWith(mockReplyBundlePage)
				.when(mockDiscussionForumClientAsync).getRepliesForThread(anyString(), anyLong(),
						anyLong(), any(DiscussionReplyOrder.class), anyBoolean(), 
						any(DiscussionFilter.class), any(AsyncCallback.class));
		
		discussionThreadWidget.configure(threadBundle, REPLY_ID_NULL, canModerate, moderatorIds, mockCallback);
		verify(mockSynAlert, atLeastOnce()).clear();
		verify(mockDiscussionForumClientAsync).getRepliesForThread(anyString(),
				anyLong(), anyLong(), any(DiscussionReplyOrder.class), anyBoolean(),
				any(DiscussionFilter.class), any(AsyncCallback.class));
		verify(mockRepliesContainer, atLeastOnce()).clear();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testLoadmoreFailure() {
		boolean isDeleted = false;
		boolean canModerate = false;
		boolean isEdited = false;
		boolean isPinned = false;
		DiscussionThreadBundle threadBundle = DiscussionTestUtils.createThreadBundle("1", "title",
				Arrays.asList("123"), 0L, 2L, new Date(), "messageKey", isDeleted,
				CREATED_BY, isEdited, isPinned);
		AsyncMockStubber.callFailureWith(new Exception())
				.when(mockDiscussionForumClientAsync).getRepliesForThread(anyString(), anyLong(),
						anyLong(), any(DiscussionReplyOrder.class), anyBoolean(),
						any(DiscussionFilter.class), any(AsyncCallback.class));
		discussionThreadWidget.configure(threadBundle, REPLY_ID_NULL, canModerate, moderatorIds, mockCallback);
		verify(mockSynAlert, atLeastOnce()).clear();
		verify(mockDiscussionForumClientAsync).getRepliesForThread(anyString(),
				anyLong(), anyLong(), any(DiscussionReplyOrder.class), anyBoolean(),
				any(DiscussionFilter.class), any(AsyncCallback.class));
		verify(mockRepliesContainer, atLeastOnce()).clear();
		verify(mockRepliesContainer, never()).add(any(Widget.class));
		verify(mockGinInjector, never()).createReplyWidget();
		verify(mockSynAlert).handleException(any(Throwable.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testLoadmoreHasNextPage() {
		boolean isDeleted = false;
		boolean canModerate = false;
		boolean isEdited = false;
		boolean isPinned = false;
		DiscussionThreadBundle threadBundle = DiscussionTestUtils.createThreadBundle("1", "title",
				Arrays.asList("123"), 2L, 2L, new Date(), "messageKey", isDeleted,
				CREATED_BY, isEdited, isPinned);
		bundleList = DiscussionTestUtils.createReplyBundleList(2);
		when(mockReplyBundlePage.getTotalNumberOfResults()).thenReturn(LIMIT+1);
		when(mockReplyBundlePage.getResults()).thenReturn(bundleList);
		AsyncMockStubber.callSuccessWith(mockReplyBundlePage)
				.when(mockDiscussionForumClientAsync).getRepliesForThread(anyString(), anyLong(),
						anyLong(), any(DiscussionReplyOrder.class), anyBoolean(),
						any(DiscussionFilter.class), any(AsyncCallback.class));
		discussionThreadWidget.configure(threadBundle, REPLY_ID_NULL, canModerate, moderatorIds, mockCallback);
		verify(mockSynAlert, atLeastOnce()).clear();
		verify(mockDiscussionForumClientAsync).getRepliesForThread(anyString(),
				anyLong(), anyLong(), any(DiscussionReplyOrder.class), anyBoolean(),
				any(DiscussionFilter.class), any(AsyncCallback.class));
		verify(mockRepliesContainer, atLeastOnce()).clear();
		verify(mockRepliesContainer, times(2)).add(any(Widget.class));
		verify(mockGinInjector, times(2)).createReplyWidget();
		verify(mockView).setDeleteIconVisible(false);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testConfigureMessageFailToGetMessageURL() throws RequestException {
		boolean isDeleted = false;
		boolean canModerate = false;
		boolean isEdited = false;
		boolean isPinned = false;
		String threadId = "123";
		DiscussionThreadBundle bundle = DiscussionTestUtils.createThreadBundle(threadId, "title", Arrays.asList("1"),
				1L, 1L, new Date(), "messageKey", isDeleted, CREATED_BY, isEdited, isPinned);
		AsyncMockStubber.callFailureWith(new Exception())
				.when(mockDiscussionForumClientAsync).getThreadUrl(anyString(), any(AsyncCallback.class));
		discussionThreadWidget.configure(bundle, REPLY_ID_NULL, canModerate, moderatorIds, mockCallback);
		verify(mockSynAlert, atLeastOnce()).clear();
		verify(mockRequestBuilder, never()).configure(eq(RequestBuilder.GET), anyString());
		verify(mockRequestBuilder, never()).setHeader(WebConstants.CONTENT_TYPE, WebConstants.TEXT_PLAIN_CHARSET_UTF8);
		verify(mockSynAlert).handleException(any(Throwable.class));
		verify(mockView).setDeleteIconVisible(false);
		verify(mockView).setLoadingMessageVisible(true);
		verify(mockView).setLoadingMessageVisible(false);
		verify(mockSubscribeButtonWidget).configure(SubscriptionObjectType.THREAD, threadId);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testConfigureMessageFailToGetMessage() throws RequestException {
		boolean isDeleted = false;
		boolean canModerate = false;
		boolean isEdited = false;
		boolean isPinned = false;
		DiscussionThreadBundle bundle = DiscussionTestUtils.createThreadBundle("123", "title", Arrays.asList("1"),
				1L, 1L, new Date(), "messageKey", isDeleted, CREATED_BY, isEdited, isPinned);
		when(mockResponse.getStatusCode()).thenReturn(Response.SC_OK+1);
		String url = "url";
		AsyncMockStubber.callSuccessWith(url)
				.when(mockDiscussionForumClientAsync).getThreadUrl(anyString(), any(AsyncCallback.class));
		RequestBuilderMockStubber.callOnError(null, new Exception())
				.when(mockRequestBuilder).sendRequest(anyString(), any(RequestCallback.class));
		discussionThreadWidget.configure(bundle, REPLY_ID_NULL, canModerate, moderatorIds, mockCallback);
		verify(mockSynAlert, atLeastOnce()).clear();
		verify(mockRequestBuilder).configure(eq(RequestBuilder.GET), anyString());
		verify(mockRequestBuilder).setHeader(WebConstants.CONTENT_TYPE, WebConstants.TEXT_PLAIN_CHARSET_UTF8);
		verify(mockSynAlert).handleException(any(Throwable.class));
		verify(mockMarkdownWidget, never()).configure(anyString());
		verify(mockView).setDeleteIconVisible(false);
		verify(mockView).setLoadingMessageVisible(true);
		verify(mockView).setLoadingMessageVisible(false);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testConfigureMessageSuccess() throws RequestException {
		boolean isDeleted = false;
		boolean canModerate = false;
		boolean isEdited = false;
		boolean isPinned = false;
		DiscussionThreadBundle bundle = DiscussionTestUtils.createThreadBundle("123", "title", Arrays.asList("1"),
				1L, 1L, new Date(), "messageKey", isDeleted, CREATED_BY, isEdited, isPinned);
		when(mockResponse.getStatusCode()).thenReturn(Response.SC_OK);
		String message = "message";
		when(mockResponse.getText()).thenReturn(message);
		String url = "url";
		AsyncMockStubber.callSuccessWith(url)
				.when(mockDiscussionForumClientAsync).getThreadUrl(anyString(), any(AsyncCallback.class));
		RequestBuilderMockStubber.callOnResponseReceived(null, mockResponse)
				.when(mockRequestBuilder).sendRequest(anyString(), any(RequestCallback.class));
		discussionThreadWidget.configure(bundle, REPLY_ID_NULL, canModerate, moderatorIds, mockCallback);
		verify(mockSynAlert, atLeastOnce()).clear();
		verify(mockRequestBuilder).configure(eq(RequestBuilder.GET), anyString());
		verify(mockRequestBuilder).setHeader(WebConstants.CONTENT_TYPE, WebConstants.TEXT_PLAIN_CHARSET_UTF8);
		verify(mockSynAlert, never()).handleException(any(Throwable.class));
		verify(mockMarkdownWidget).configure(anyString());
		verify(mockView).setDeleteIconVisible(false);
		verify(mockEditThreadModal).configure(anyString(), anyString(), anyString(), any(Callback.class));
		verify(mockView).setLoadingMessageVisible(true);
		verify(mockView).setLoadingMessageVisible(false);
	}

	@Test
	public void testOnClickDeleteThread() {
		discussionThreadWidget.onClickDeleteThread();
		ArgumentCaptor<AlertCallback> captor = ArgumentCaptor.forClass(AlertCallback.class);
		verify(mockView).showConfirm(anyString(), anyString(), anyString(), anyString(), captor.capture());
		captor.getValue().callback();
		verify(mockSynAlert).clear();
		verify(mockDiscussionForumClientAsync).markThreadAsDeleted(anyString(), any(AsyncCallback.class));
	}

	@Test
	public void testOnClickRestoreThread() {
		discussionThreadWidget.onClickRestore();
		ArgumentCaptor<AlertCallback> captor = ArgumentCaptor.forClass(AlertCallback.class);
		verify(mockView).showConfirm(anyString(), anyString(), anyString(), anyString(), captor.capture());
		captor.getValue().callback();
		verify(mockSynAlert).clear();
		verify(mockDiscussionForumClientAsync).restoreThread(anyString(), any(AsyncCallback.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testDeleteThreadSuccess() {
		boolean isDeleted = false;
		boolean canModerate = true;
		boolean isEdited = false;
		boolean isPinned = false;
		DiscussionThreadBundle threadBundle = DiscussionTestUtils.createThreadBundle("1", "title",
				Arrays.asList("123"), 1L, 2L, new Date(), "messageKey", isDeleted,
				CREATED_BY, isEdited, isPinned);
		discussionThreadWidget.configure(threadBundle, REPLY_ID_NULL, canModerate, moderatorIds, mockCallback);
		AsyncMockStubber.callSuccessWith((Void) null)
				.when(mockDiscussionForumClientAsync).markThreadAsDeleted(anyString(), any(AsyncCallback.class));
		discussionThreadWidget.deleteThread();
		verify(mockSynAlert, atLeastOnce()).clear();
		verify(mockDiscussionForumClientAsync).markThreadAsDeleted(eq("1"), any(AsyncCallback.class));
		verify(mockCallback).invoke();
		verify(mockView).showSuccess(anyString(), anyString());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testDeleteThreadFailure() {
		boolean isDeleted = false;
		boolean canModerate = true;
		boolean isEdited = false;
		boolean isPinned = false;
		DiscussionThreadBundle threadBundle = DiscussionTestUtils.createThreadBundle("1", "title",
				Arrays.asList("123"), 1L, 2L, new Date(), "messageKey", isDeleted,
				CREATED_BY, isEdited, isPinned);
		discussionThreadWidget.configure(threadBundle, REPLY_ID_NULL, canModerate, moderatorIds, mockCallback);
		AsyncMockStubber.callFailureWith(new Exception())
				.when(mockDiscussionForumClientAsync).markThreadAsDeleted(anyString(), any(AsyncCallback.class));
		discussionThreadWidget.deleteThread();
		verify(mockSynAlert, atLeastOnce()).clear();
		verify(mockDiscussionForumClientAsync).markThreadAsDeleted(eq("1"), any(AsyncCallback.class));
		verify(mockSynAlert).handleException(any(Throwable.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testRestoreThreadSuccess() {
		boolean isDeleted = true;
		boolean canModerate = true;
		boolean isEdited = false;
		boolean isPinned = false;
		DiscussionThreadBundle threadBundle = DiscussionTestUtils.createThreadBundle("1", "title",
				Arrays.asList("123"), 1L, 2L, new Date(), "messageKey", isDeleted,
				CREATED_BY, isEdited, isPinned);
		discussionThreadWidget.configure(threadBundle, REPLY_ID_NULL, canModerate, moderatorIds, mockCallback);
		AsyncMockStubber.callSuccessWith((Void) null)
				.when(mockDiscussionForumClientAsync).restoreThread(anyString(), any(AsyncCallback.class));
		discussionThreadWidget.restoreThread();
		verify(mockSynAlert, atLeastOnce()).clear();
		verify(mockDiscussionForumClientAsync).restoreThread(eq("1"), any(AsyncCallback.class));
		verify(mockCallback).invoke();
		verify(mockView).showSuccess(anyString(), anyString());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testRestoreThreadFailure() {
		boolean isDeleted = true;
		boolean canModerate = true;
		boolean isEdited = false;
		boolean isPinned = false;
		DiscussionThreadBundle threadBundle = DiscussionTestUtils.createThreadBundle("1", "title",
				Arrays.asList("123"), 1L, 2L, new Date(), "messageKey", isDeleted,
				CREATED_BY, isEdited, isPinned);
		discussionThreadWidget.configure(threadBundle, REPLY_ID_NULL, canModerate, moderatorIds, mockCallback);
		AsyncMockStubber.callFailureWith(new Exception())
				.when(mockDiscussionForumClientAsync).restoreThread(anyString(), any(AsyncCallback.class));
		discussionThreadWidget.restoreThread();
		verify(mockSynAlert, atLeastOnce()).clear();
		verify(mockDiscussionForumClientAsync).restoreThread(eq("1"), any(AsyncCallback.class));
		verify(mockSynAlert).handleException(any(Throwable.class));
	}

	@Test
	public void testOnClickEditThread() {
		discussionThreadWidget.onClickEditThread();
		verify(mockEditThreadModal).show();
	}
	
	@Test
	public void testOnClickThreadNoCallback() {
		discussionThreadWidget.onClickThread();
		verify(mockPlaceChanger).goTo(isA(Synapse.class));
	}
	
	@Test
	public void testOnClickThreadWithCallback() {
		discussionThreadWidget.setThreadIdClickedCallback(mockThreadIdClickedCallback);
		discussionThreadWidget.onClickThread();
		verify(mockThreadIdClickedCallback).invoke(anyString());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testReconfigureThread() throws RequestException {
		boolean isDeleted = false;
		boolean canModerate = false;
		boolean isEdited = false;
		boolean isPinned = false;
		String threadId = "123";
		DiscussionThreadBundle bundle = DiscussionTestUtils.createThreadBundle(threadId, "title", Arrays.asList("1"),
				1L, 1L, new Date(), "messageKey", isDeleted, CREATED_BY, isEdited, isPinned);
		discussionThreadWidget.configure(bundle, REPLY_ID_NULL, canModerate, moderatorIds, mockCallback);
		reset(mockView);
		AsyncMockStubber.callSuccessWith(bundle)
		.when(mockDiscussionForumClientAsync).getThread(anyString(), any(AsyncCallback.class));
		discussionThreadWidget.reconfigureThread();
		verify(mockSynAlert, atLeastOnce()).clear();
		verify(mockDiscussionForumClientAsync).getThread(anyString(), any(AsyncCallback.class));
	}

	@Test
	public void testOnClickCancel() {
		discussionThreadWidget.onClickCancel();
		verify(mockView).resetButton();
		verify(mockView).setReplyTextBoxVisible(true);
		verify(mockView).setNewReplyContainerVisible(false);
	}

	@Test
	public void testOnClickSaveInvalidArgument() {
		when(mockMarkdownEditorWidget.getMarkdown()).thenReturn("");
		discussionThreadWidget.onClickSave();
		verify(mockSynAlert).clear();
		verify(mockMarkdownEditorWidget).getMarkdown();
		verify(mockSynAlert).showError(anyString());
		verifyZeroInteractions(mockDiscussionForumClientAsync);
	}

	@Test
	public void testOnClickSaveSuccess() {
		when(mockMarkdownEditorWidget.getMarkdown()).thenReturn("message");
		AsyncMockStubber.callSuccessWith(mockDiscussionReplyBundle)
			.when(mockDiscussionForumClientAsync).createReply(any(CreateDiscussionReply.class),
					any(AsyncCallback.class));
		discussionThreadWidget.onClickSave();
		verify(mockSynAlert, atLeastOnce()).clear();
		verify(mockView).showSaving();
		verify(mockView).showSuccess(anyString(), anyString());
		verify(mockDiscussionForumClientAsync).createReply(any(CreateDiscussionReply.class), any(AsyncCallback.class));
		verify(mockView).resetButton();
		verify(mockView).setReplyTextBoxVisible(true);
		verify(mockView).setNewReplyContainerVisible(false);
		verify(mockDiscussionForumClientAsync).getThread(anyString(), any(AsyncCallback.class));
	}

	@Test
	public void testOnSaveFailure() {
		when(mockMarkdownEditorWidget.getMarkdown()).thenReturn("message");
		Exception exception = new Exception();
		AsyncMockStubber.callFailureWith(exception)
			.when(mockDiscussionForumClientAsync).createReply(any(CreateDiscussionReply.class),
					any(AsyncCallback.class));
		discussionThreadWidget.onClickSave();
		verify(mockSynAlert).clear();
		verify(mockView).showSaving();
		verify(mockDiscussionForumClientAsync).createReply(any(CreateDiscussionReply.class), any(AsyncCallback.class));
		verifyZeroInteractions(mockCallback);
		verify(mockSynAlert).handleException(exception);
		verify(mockView).resetButton();
	}
}
