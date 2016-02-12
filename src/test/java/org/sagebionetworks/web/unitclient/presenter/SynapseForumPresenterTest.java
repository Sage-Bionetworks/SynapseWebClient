package org.sagebionetworks.web.unitclient.presenter;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.discussion.Forum;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.ParameterizedToken;
import org.sagebionetworks.web.client.place.SynapseForumPlace;
import org.sagebionetworks.web.client.presenter.SynapseForumPresenter;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.view.SynapseForumView;
import org.sagebionetworks.web.client.widget.discussion.ForumWidget;
import org.sagebionetworks.web.client.widget.entity.MarkdownWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public class SynapseForumPresenterTest {
	@Mock
	SynapseForumView mockView;
	@Mock
	CookieProvider mockCookies;
	@Mock
	SynapseAlert mockSynAlert;
	@Mock
	Forum mockForum;
	@Mock
	GlobalApplicationState mockGlobalApplicationState;
	@Mock
	PlaceChanger mockPlaceChanger;
	@Mock
	MarkdownWidget mockMarkdownWidget;
	@Mock
	SynapseClientAsync mockSynapseClient;
	@Mock
	ForumWidget mockForumWidget;
	@Mock
	SynapseForumPlace mockPlace;

	SynapseForumPresenter presenter;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		presenter = new SynapseForumPresenter(mockView, mockSynAlert,
				mockGlobalApplicationState, mockMarkdownWidget, mockSynapseClient,
				mockCookies, mockForumWidget);
		when(mockCookies.getCookie(DisplayUtils.SYNAPSE_TEST_WEBSITE_COOKIE_KEY)).thenReturn("not null");
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		when(mockPlace.toToken()).thenReturn("fake token");
	}

	@Test
	public void testConstruction() {
		verify(mockView).setPresenter(presenter);
		verify(mockView).setAlert(any(Widget.class));
		verify(mockView).setWikiWidget(any(Widget.class));
	}

	@SuppressWarnings("unchecked")
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

	@SuppressWarnings("unchecked")
	@Test
	public void testLoadWikiHelpContentFailure() {
		Exception ex = new Exception("error occurred");
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseClient)
			.getPageNameToWikiKeyMap(any(AsyncCallback.class));

		presenter.loadWikiHelpContent();
		verify(mockSynAlert).handleException(ex);
	}

	@Test
	public void testShowForum() {
		String entityId = "syn1";
		presenter.setPlace(mockPlace);
		presenter.showForum(entityId);

		verify(mockForumWidget).configure(anyString(), any(ParameterizedToken.class),
				anyBoolean(), any(Callback.class));
	}
}
