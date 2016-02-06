package org.sagebionetworks.web.unitclient.widget.entity.tabs;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import static org.sagebionetworks.web.client.widget.entity.tabs.DiscussionTab.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadBundle;
import org.sagebionetworks.repo.model.discussion.Forum;
import org.sagebionetworks.web.client.DiscussionForumClientAsync;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.ParameterizedToken;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.discussion.DiscussionThreadListWidget;
import org.sagebionetworks.web.client.widget.discussion.DiscussionThreadWidget;
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
	@Mock
	GlobalApplicationState mockGlobalApplicationState;
	@Mock
	PlaceChanger mockPlaceChanger;
	@Mock
	DiscussionThreadWidget mockDiscussionThreadWidget;
	@Mock
	DiscussionThreadBundle mockDiscussionThreadBundle;
	
	DiscussionTab tab;
	private boolean canModerate = false;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		tab = new DiscussionTab(mockView, mockTab, mockSynAlert, mockDiscussionForumClient,
				mockDiscussionThreadListWidget, mockNewDiscussionThreadModal, mockCookies,
				mockAuthController, mockGlobalApplicationState, mockDiscussionThreadWidget);
		when(mockCookies.getCookie(DisplayUtils.SYNAPSE_TEST_WEBSITE_COOKIE_KEY)).thenReturn("not null");
		when(mockAuthController.isLoggedIn()).thenReturn(true);
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
	}

	@Test
	public void testConstruction() {
		verify(mockView).setThreadList(any(Widget.class));
		verify(mockView).setNewThreadModal(any(Widget.class));
		verify(mockView).setPresenter(tab);
		verify(mockView).setAlert(any(Widget.class));
		verify(mockView).setSingleThread(any(Widget.class));
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
		String areaToken = "a=b&c=d";
		tab.configure(entityId, entityName, areaToken, canModerate);

		verify(mockTab).setTabListItemVisible(true);

		ArgumentCaptor<Synapse> captor = ArgumentCaptor.forClass(Synapse.class);
		verify(mockTab).setEntityNameAndPlace(eq(entityName), captor.capture());
		Synapse place = captor.getValue();
		assertEquals(entityId, place.getEntityId());
		assertNull(place.getVersionNumber());
		assertEquals(EntityArea.DISCUSSION, place.getArea());
		assertTrue(place.getAreaToken().contains("a=b"));
		assertTrue(place.getAreaToken().contains("c=d"));

		verify(mockDiscussionForumClient).getForumMetadata(anyString(), any(AsyncCallback.class));
		verify(mockNewDiscussionThreadModal).configure(anyString(), any(Callback.class));
		verify(mockDiscussionThreadListWidget).configure(anyString(), eq(DEFAULT_MODERATOR_MODE));
		verify(mockView).setModeratorModeContainerVisibility(canModerate);
		verify(mockView).setSingleThreadUIVisible(false);
		verify(mockView).setThreadListUIVisible(true);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testConfigureFailure() {
		when(mockForum.getId()).thenReturn("123");
		AsyncMockStubber.callFailureWith(new Exception()).when(mockDiscussionForumClient)
				.getForumMetadata(anyString(), any(AsyncCallback.class));

		String entityId = "syn1"; 
		String entityName = "discussion project test";
		String areaToken = "foo=bar";
		tab.configure(entityId, entityName, areaToken, canModerate);

		verify(mockTab).setTabListItemVisible(true);

		ArgumentCaptor<Synapse> captor = ArgumentCaptor.forClass(Synapse.class);
		verify(mockTab).setEntityNameAndPlace(eq(entityName), captor.capture());
		Synapse place = captor.getValue();
		assertEquals(entityId, place.getEntityId());
		assertNull(place.getVersionNumber());
		assertEquals(EntityArea.DISCUSSION, place.getArea());
		assertEquals(areaToken, place.getAreaToken());

		verify(mockDiscussionForumClient).getForumMetadata(anyString(), any(AsyncCallback.class));
		verify(mockNewDiscussionThreadModal, never()).configure(anyString(), any(Callback.class));
		verify(mockView).setModeratorModeContainerVisibility(canModerate);
		verify(mockSynAlert).handleException(any(Exception.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testConfigureSuccessWithModerator() {
		when(mockForum.getId()).thenReturn("123");
		AsyncMockStubber.callSuccessWith(mockForum).when(mockDiscussionForumClient)
				.getForumMetadata(anyString(), any(AsyncCallback.class));

		String entityId = "syn1"; 
		String entityName = "discussion project test";
		String areaToken = "";
		canModerate = true;
		tab.configure(entityId, entityName, areaToken, canModerate);
		verify(mockDiscussionThreadListWidget).configure(anyString(), eq(DEFAULT_MODERATOR_MODE));
		verify(mockView).setModeratorModeContainerVisibility(canModerate);
	}

	@Test
	public void testNotInTestWebsite() {
		when(mockCookies.getCookie(DisplayUtils.SYNAPSE_TEST_WEBSITE_COOKIE_KEY)).thenReturn(null);
		String entityId = "syn1"; 
		String entityName = "discussion project test";
		String areaToken = "";
		reset(mockTab);
		tab.configure(entityId, entityName, areaToken, canModerate);
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
	public void onCLickNewThreadAnonymousTest() {
		when(mockAuthController.isLoggedIn()).thenReturn(false);
		tab.onClickNewThread();
		verify(mockNewDiscussionThreadModal, never()).show();
		verify(mockGlobalApplicationState).getPlaceChanger();
		verify(mockView).showErrorMessage(anyString());
	}

	@Test
	public void testOnModeratorModeChange() {
		when(mockView.getModeratorMode()).thenReturn(true);
		tab.onModeratorModeChange();
		verify(mockDiscussionThreadListWidget).configure(anyString(), eq(true));
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
		String entityName = "discussion project test";
		String threadId = "007";
		String areaToken = DiscussionTab.THREAD_ID_KEY + "=" + threadId;
		tab.configure(entityId, entityName, areaToken, canModerate);

		verify(mockTab).setTabListItemVisible(true);

		ArgumentCaptor<Synapse> captor = ArgumentCaptor.forClass(Synapse.class);
		verify(mockTab).setEntityNameAndPlace(eq(entityName), captor.capture());
		Synapse place = captor.getValue();
		assertEquals(entityId, place.getEntityId());
		assertNull(place.getVersionNumber());
		assertEquals(EntityArea.DISCUSSION, place.getArea());
		assertEquals(areaToken, place.getAreaToken());
		
		verify(mockDiscussionForumClient).getThread(eq(threadId), any(AsyncCallback.class));
		verify(mockDiscussionThreadWidget).configure(eq(mockDiscussionThreadBundle), eq(canModerate), any(Callback.class));
		verify(mockDiscussionThreadWidget).toggleThread();
		verify(mockDiscussionThreadListWidget, never()).configure(anyString(), anyBoolean());
		verify(mockView).setSingleThreadUIVisible(true);
		verify(mockView).setThreadListUIVisible(false);
	}
	
	@Test
	public void testConfigureSingleThreadFailure() {
		Exception ex = new Exception("error");
		AsyncMockStubber.callFailureWith(ex).when(mockDiscussionForumClient)
				.getThread(anyString(), any(AsyncCallback.class));

		String entityId = "syn1"; 
		String entityName = "discussion project test";
		String threadId = "007";
		String areaToken = DiscussionTab.THREAD_ID_KEY + "=" + threadId;
		tab.configure(entityId, entityName, areaToken, canModerate);

		verify(mockTab).setTabListItemVisible(true);

		ArgumentCaptor<Synapse> captor = ArgumentCaptor.forClass(Synapse.class);
		verify(mockTab).setEntityNameAndPlace(eq(entityName), captor.capture());
		Synapse place = captor.getValue();
		assertEquals(entityId, place.getEntityId());
		assertNull(place.getVersionNumber());
		assertEquals(EntityArea.DISCUSSION, place.getArea());
		assertEquals(areaToken, place.getAreaToken());
		
		verify(mockSynAlert).handleException(ex);
		verify(mockView).setSingleThreadUIVisible(true);
		verify(mockView).setThreadListUIVisible(false);
	}
	
	@Test
	public void testOnClickShowAllThreads() {
		String entityId = "syn1"; 
		String entityName = "discussion project test";
		String threadId = "007";
		String areaToken = DiscussionTab.THREAD_ID_KEY + "=" + threadId;
		tab.configure(entityId, entityName, areaToken, canModerate);
		
		assertEquals(areaToken, tab.getCurrentAreaToken());
		reset(mockTab, mockView);
		
		tab.onClickShowAllThreads();
		
		ArgumentCaptor<Synapse> captor = ArgumentCaptor.forClass(Synapse.class);
		verify(mockTab).setEntityNameAndPlace(eq(entityName), captor.capture());
		Synapse place = captor.getValue();
		assertEquals(entityId, place.getEntityId());
		assertNull(place.getVersionNumber());
		assertEquals(EntityArea.DISCUSSION, place.getArea());
		assertEquals(ParameterizedToken.DEFAULT_TOKEN, place.getAreaToken());
		//push url into history:
		verify(mockTab).showTab();
		//attempts to show full thread list
		verify(mockDiscussionForumClient).getForumMetadata(anyString(), any(AsyncCallback.class));
		verify(mockView).setSingleThreadUIVisible(false);
		verify(mockView).setThreadListUIVisible(true);
	}
}
