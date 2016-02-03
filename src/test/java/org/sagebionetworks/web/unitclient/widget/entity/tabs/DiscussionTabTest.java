package org.sagebionetworks.web.unitclient.widget.entity.tabs;
import static org.mockito.Mockito.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.sagebionetworks.web.client.widget.entity.tabs.DiscussionTab.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.discussion.Forum;
import org.sagebionetworks.web.client.DiscussionForumClientAsync;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.discussion.DiscussionThreadListWidget;
import org.sagebionetworks.web.client.widget.discussion.modal.NewDiscussionThreadModal;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.entity.tabs.DiscussionTab;
import org.sagebionetworks.web.client.widget.entity.tabs.DiscussionTabView;
import org.sagebionetworks.web.client.widget.entity.tabs.Tab;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public class DiscussionTabTest {
	@Mock
	Tab mockTab;
	@Mock
	DiscussionTabView mockView;
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

	DiscussionTab tab;
	private boolean canModerate = false;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		tab = new DiscussionTab(mockView, mockTab, mockSynAlert, mockDiscussionForumClient,
				mockDiscussionThreadListWidget, mockNewDiscussionThreadModal, mockCookies,
				mockAuthController);
		when(mockCookies.getCookie(DisplayUtils.SYNAPSE_TEST_WEBSITE_COOKIE_KEY)).thenReturn("not null");
		when(mockAuthController.isLoggedIn()).thenReturn(true);
	}

	@Test
	public void testConstruction() {
		verify(mockView).setThreadList(any(Widget.class));
		verify(mockView).setNewThreadModal(any(Widget.class));
		verify(mockView).setPresenter(tab);
		verify(mockView).setAlert(any(Widget.class));
	}

	@Test
	public void testSetTabClickedCallback() {
		tab.setTabClickedCallback(mockOnClickCallback);
		verify(mockTab).addTabClickedCallback(mockOnClickCallback);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testConfigureSuccess() {
		when(mockForum.getId()).thenReturn("123");
		AsyncMockStubber.callSuccessWith(mockForum).when(mockDiscussionForumClient)
				.getForumMetadata(anyString(), any(AsyncCallback.class));

		String entityId = "syn1"; 
		String entityName = "discussion project test";
		tab.configure(entityId, entityName, canModerate);

		verify(mockTab).setTabListItemVisible(true);

		ArgumentCaptor<Synapse> captor = ArgumentCaptor.forClass(Synapse.class);
		verify(mockTab).setEntityNameAndPlace(eq(entityName), captor.capture());
		Synapse place = captor.getValue();
		assertEquals(entityId, place.getEntityId());
		assertNull(place.getVersionNumber());
		assertEquals(EntityArea.DISCUSSION, place.getArea());
		assertNull(place.getAreaToken());

		verify(mockDiscussionForumClient).getForumMetadata(anyString(), any(AsyncCallback.class));
		verify(mockNewDiscussionThreadModal).configure(anyString(), any(Callback.class));
		verify(mockDiscussionThreadListWidget).configure(anyString(), eq(DEFAULT_MODERATOR_MODE));
		verify(mockView).setModeratorModeContainerVisibility(canModerate);
		verify(mockView).setNewThreadVisible(true);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testConfigureSuccessAnonymous() {
		when(mockAuthController.isLoggedIn()).thenReturn(false);
		when(mockForum.getId()).thenReturn("123");
		AsyncMockStubber.callSuccessWith(mockForum).when(mockDiscussionForumClient)
				.getForumMetadata(anyString(), any(AsyncCallback.class));

		String entityId = "syn1"; 
		String entityName = "discussion project test";
		tab.configure(entityId, entityName, canModerate);

		verify(mockView).setNewThreadVisible(false);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testConfigureFailure() {
		when(mockForum.getId()).thenReturn("123");
		AsyncMockStubber.callFailureWith(new Exception()).when(mockDiscussionForumClient)
				.getForumMetadata(anyString(), any(AsyncCallback.class));

		String entityId = "syn1"; 
		String entityName = "discussion project test";
		tab.configure(entityId, entityName, canModerate);

		verify(mockTab).setTabListItemVisible(true);

		ArgumentCaptor<Synapse> captor = ArgumentCaptor.forClass(Synapse.class);
		verify(mockTab).setEntityNameAndPlace(eq(entityName), captor.capture());
		Synapse place = captor.getValue();
		assertEquals(entityId, place.getEntityId());
		assertNull(place.getVersionNumber());
		assertEquals(EntityArea.DISCUSSION, place.getArea());
		assertNull(place.getAreaToken());

		verify(mockDiscussionForumClient).getForumMetadata(anyString(), any(AsyncCallback.class));
		verify(mockNewDiscussionThreadModal, never()).configure(anyString(), any(Callback.class));
		verify(mockView).setModeratorModeContainerVisibility(canModerate);
		verify(mockSynAlert).handleException(any(Exception.class));
		verify(mockView).setNewThreadVisible(true);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testConfigureSuccessWithModerator() {
		when(mockForum.getId()).thenReturn("123");
		AsyncMockStubber.callSuccessWith(mockForum).when(mockDiscussionForumClient)
				.getForumMetadata(anyString(), any(AsyncCallback.class));

		String entityId = "syn1"; 
		String entityName = "discussion project test";
		canModerate = true;
		tab.configure(entityId, entityName, canModerate);
		verify(mockDiscussionThreadListWidget).configure(anyString(), eq(DEFAULT_MODERATOR_MODE));
		verify(mockView).setModeratorModeContainerVisibility(canModerate);
		verify(mockView).setNewThreadVisible(true);
	}

	@Test
	public void testNotInTestWebsite() {
		when(mockCookies.getCookie(DisplayUtils.SYNAPSE_TEST_WEBSITE_COOKIE_KEY)).thenReturn(null);
		String entityId = "syn1"; 
		String entityName = "discussion project test";
		reset(mockTab);
		tab.configure(entityId, entityName, canModerate);
		verify(mockTab).setTabListItemVisible(false);
	}

	@Test
	public void testAsTab() {
		assertEquals(mockTab, tab.asTab());
	}

	@Test
	public void onCLickNewThreadTest() {
		tab.onClickNewThread();
		verify(mockNewDiscussionThreadModal).show();;
	}

	@Test
	public void testOnModeratorModeChange() {
		when(mockView.getModeratorMode()).thenReturn(true);
		tab.onModeratorModeChange();
		verify(mockDiscussionThreadListWidget).configure(anyString(), eq(true));
		verify(mockNewDiscussionThreadModal).configure(anyString(), any(Callback.class));
	}
}
