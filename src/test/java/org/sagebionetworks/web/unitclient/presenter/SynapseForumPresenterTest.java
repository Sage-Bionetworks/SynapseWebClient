package org.sagebionetworks.web.unitclient.presenter;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.client.widget.discussion.ForumWidget.*;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadBundle;
import org.sagebionetworks.repo.model.discussion.Forum;
import org.sagebionetworks.web.client.DiscussionForumClientAsync;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.presenter.SynapseForumPresenter;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.view.SynapseForumView;
import org.sagebionetworks.web.client.widget.discussion.DiscussionThreadListWidget;
import org.sagebionetworks.web.client.widget.discussion.DiscussionThreadWidget;
import org.sagebionetworks.web.client.widget.discussion.modal.NewDiscussionThreadModal;
import org.sagebionetworks.web.client.widget.entity.MarkdownWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.entity.tabs.Tab;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public class SynapseForumPresenterTest {
	@Mock
	SynapseForumView mockView;
	@Mock
	DiscussionThreadListWidget mockDiscussionThreadListWidget;
	@Mock
	CallbackP<Tab> mockOnClickCallback;
	@Mock
	NewDiscussionThreadModal mockNewDiscussionThreadModal;
	@Mock
	CookieProvider mockCookies;
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
	MarkdownWidget mockMarkdownWidget;
	@Mock
	SynapseClientAsync mockSynapseClient;
	
	
	SynapseForumPresenter presenter;
	private boolean canModerate = false;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		presenter = new SynapseForumPresenter(mockView, mockSynAlert, mockDiscussionForumClient,
				mockDiscussionThreadListWidget, mockNewDiscussionThreadModal, 
				mockAuthController, mockGlobalApplicationState, mockMarkdownWidget, mockSynapseClient, mockCookies);
		when(mockCookies.getCookie(DisplayUtils.SYNAPSE_TEST_WEBSITE_COOKIE_KEY)).thenReturn("not null");
		when(mockAuthController.isLoggedIn()).thenReturn(true);
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
	}

	@Test
	public void testConstruction() {
		verify(mockView).setThreadList(any(Widget.class));
		verify(mockView).setNewThreadModal(any(Widget.class));
		verify(mockView).setPresenter(presenter);
		verify(mockView).setAlert(any(Widget.class));
		verify(mockView).setWikiWidget(any(Widget.class));
	}

	@Test
	public void testLoadWikiHelpContent() {
		HashMap<String,WikiPageKey> pageNameToWikiKeyMap = new HashMap<String,WikiPageKey>();
		WikiPageKey mockWikiPageKey = mock(WikiPageKey.class);
		pageNameToWikiKeyMap.put(WebConstants.FORUM, mockWikiPageKey);
		AsyncMockStubber.callSuccessWith(pageNameToWikiKeyMap).when(mockSynapseClient)
			.getPageNameToWikiKeyMap(any(AsyncCallback.class));

		presenter.loadWikiHelpContent();
		boolean isIgnoreLoadingFailure = false;
		verify(mockMarkdownWidget).loadMarkdownFromWikiPage(mockWikiPageKey, isIgnoreLoadingFailure);
	}
	
	@Test
	public void testLoadWikiHelpContentFailure() {
		Exception ex = new Exception("error occurred");
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseClient)
			.getPageNameToWikiKeyMap(any(AsyncCallback.class));

		presenter.loadWikiHelpContent();
		verify(mockSynAlert).handleException(ex);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testShowForumSuccess() {
		when(mockForum.getId()).thenReturn("123");
		AsyncMockStubber.callSuccessWith(mockForum).when(mockDiscussionForumClient)
				.getForumMetadata(anyString(), any(AsyncCallback.class));

		String entityId = "syn1"; 
		presenter.showForum(entityId);

		verify(mockDiscussionForumClient).getForumMetadata(anyString(), any(AsyncCallback.class));
		verify(mockNewDiscussionThreadModal).configure(anyString(), any(Callback.class));
		verify(mockDiscussionThreadListWidget).configure(anyString(), eq(DEFAULT_MODERATOR_MODE));
		verify(mockView).setModeratorModeContainerVisibility(DEFAULT_MODERATOR_MODE);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testShowForumFailure() {
		when(mockForum.getId()).thenReturn("123");
		AsyncMockStubber.callFailureWith(new Exception()).when(mockDiscussionForumClient)
				.getForumMetadata(anyString(), any(AsyncCallback.class));

		String entityId = "syn1"; 
		presenter.showForum(entityId);
		verify(mockDiscussionForumClient).getForumMetadata(anyString(), any(AsyncCallback.class));
		verify(mockNewDiscussionThreadModal, never()).configure(anyString(), any(Callback.class));
		verify(mockView).setModeratorModeContainerVisibility(DEFAULT_MODERATOR_MODE);
		verify(mockSynAlert).handleException(any(Exception.class));
	}

	@Test
	public void onCLickNewThreadTest() {
		presenter.onClickNewThread();
		verify(mockNewDiscussionThreadModal).show();;
	}

	@Test
	public void onCLickNewThreadAnonymousTest() {
		when(mockAuthController.isLoggedIn()).thenReturn(false);
		presenter.onClickNewThread();
		verify(mockNewDiscussionThreadModal, never()).show();
		verify(mockGlobalApplicationState).getPlaceChanger();
		verify(mockView).showErrorMessage(anyString());
	}

	@Test
	public void testOnModeratorModeChange() {
		when(mockView.getModeratorMode()).thenReturn(true);
		presenter.onModeratorModeChange();
		verify(mockDiscussionThreadListWidget).configure(anyString(), eq(true));
		verify(mockNewDiscussionThreadModal).configure(anyString(), any(Callback.class));
	}
	
}
