package org.sagebionetworks.web.unitclient.widget.header;

import static junit.framework.Assert.*;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.*;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.repo.model.discussion.DiscussionFilter;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadBundle;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadOrder;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.DiscussionForumClientAsync;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.cache.ClientCache;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.Home;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.SynapseForumPlace;
import org.sagebionetworks.web.client.place.Trash;
import org.sagebionetworks.web.client.place.users.RegisterAccount;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.entity.FavoriteWidget;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.client.widget.header.HeaderView;
import org.sagebionetworks.web.client.widget.header.StuAnnouncementWidget;
import org.sagebionetworks.web.client.widget.header.StuAnnouncementWidgetView;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.datepicker.client.CalendarUtil;

public class StuAnnouncementWidgetTest {

	StuAnnouncementWidget widget;
	@Mock
	StuAnnouncementWidgetView mockView;
	@Mock
	GlobalApplicationState mockGlobalApplicationState;
	@Mock
	DiscussionForumClientAsync mockDiscussionForumClient;
	@Mock
	SynapseJSNIUtils mockSynapseJSNIUtils;
	@Mock
	ClientCache mockClientCache;
	@Mock
	PlaceChanger mockPlaceChanger;
	@Mock
	CookieProvider mockCookies;
	@Mock
	StuAnnouncementWidget mockStuAnnouncementWidget;
	@Mock
	PaginatedResults<DiscussionThreadBundle> mockThreadBundlePage;
	List<DiscussionThreadBundle> discussionThreadBundleList;
	String forumId = "628";
	String stuUserId = "4444";
	String announcementThreadId = "9876";
	String announcementTitle = "New feature available now!";
	String stuAnnouncementsProjectId = "syn0001";
	@Mock
	DiscussionThreadBundle mockDiscussionThreadBundle;
	
	@Before
	public void setup(){
		MockitoAnnotations.initMocks(this);
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		widget = new StuAnnouncementWidget(mockView, mockSynapseJSNIUtils, mockDiscussionForumClient, mockGlobalApplicationState, mockClientCache, mockCookies);
		discussionThreadBundleList = new ArrayList<DiscussionThreadBundle>();
		AsyncMockStubber.callSuccessWith(mockThreadBundlePage)
			.when(mockDiscussionForumClient).getThreadsForForum(anyString(), anyLong(),
					anyLong(), any(DiscussionThreadOrder.class), anyBoolean(),
					any(DiscussionFilter.class), any(AsyncCallback.class));
		when(mockThreadBundlePage.getResults()).thenReturn(discussionThreadBundleList);
		when(mockGlobalApplicationState.getSynapseProperty(StuAnnouncementWidget.STU_ANNOUNCEMENTS_FORUM_ID_KEY)).thenReturn(forumId);
		when(mockGlobalApplicationState.getSynapseProperty(StuAnnouncementWidget.STU_USER_ID_KEY)).thenReturn(stuUserId);
		when(mockGlobalApplicationState.getSynapseProperty(StuAnnouncementWidget.STU_ANNOUNCEMENTS_PROJECT_ID_KEY)).thenReturn(stuAnnouncementsProjectId);
		
		when(mockDiscussionThreadBundle.getModifiedOn()).thenReturn(new Date());
		when(mockDiscussionThreadBundle.getCreatedBy()).thenReturn(stuUserId);
		when(mockDiscussionThreadBundle.getId()).thenReturn(announcementThreadId);
		when(mockDiscussionThreadBundle.getTitle()).thenReturn(announcementTitle);
		when(mockClientCache.contains(anyString())).thenReturn(false);
		when(mockCookies.getCookie(eq(DisplayUtils.SYNAPSE_TEST_WEBSITE_COOKIE_KEY))).thenReturn("true");
	}

	@Test
	public void testConstructor() {
		verify(mockView).setPresenter(widget);
		verify(mockView).hide();
	}

	@Test
	public void testAsWidget(){
		widget.asWidget();
		verify(mockView).asWidget();
	}

	@Test
	public void testInitStuAnnouncement() {
		discussionThreadBundleList.add(mockDiscussionThreadBundle);
		widget.initStuAnnouncement();
		verify(mockDiscussionForumClient).getThreadsForForum(anyString(), anyLong(),
				anyLong(), any(DiscussionThreadOrder.class), anyBoolean(),
				any(DiscussionFilter.class), any(AsyncCallback.class));
		verify(mockView).show(announcementTitle);
	}
	
	@Test
	public void testInitStuAnnouncementInCache() {
		when(mockClientCache.contains(anyString())).thenReturn(true);
		discussionThreadBundleList.add(mockDiscussionThreadBundle);
		widget.initStuAnnouncement();
		verify(mockDiscussionForumClient).getThreadsForForum(anyString(), anyLong(),
				anyLong(), any(DiscussionThreadOrder.class), anyBoolean(),
				any(DiscussionFilter.class), any(AsyncCallback.class));
		verify(mockView, never()).show(anyString());
	}
	
	@Test
	public void testInitStuAnnouncementNotStu() {
		when(mockDiscussionThreadBundle.getCreatedBy()).thenReturn("different-user-id");
		discussionThreadBundleList.add(mockDiscussionThreadBundle);
		widget.initStuAnnouncement();
		verify(mockDiscussionForumClient).getThreadsForForum(anyString(), anyLong(),
				anyLong(), any(DiscussionThreadOrder.class), anyBoolean(),
				any(DiscussionFilter.class), any(AsyncCallback.class));
		verify(mockView, never()).show(anyString());
	}
	
	@Test
	public void testInitStuAnnouncementTooOld() {
		Date monthAgo = new Date();
		CalendarUtil.addMonthsToDate(monthAgo, -1);
		when(mockDiscussionThreadBundle.getModifiedOn()).thenReturn(monthAgo);
		discussionThreadBundleList.add(mockDiscussionThreadBundle);
		widget.initStuAnnouncement();
		verify(mockDiscussionForumClient).getThreadsForForum(anyString(), anyLong(),
				anyLong(), any(DiscussionThreadOrder.class), anyBoolean(),
				any(DiscussionFilter.class), any(AsyncCallback.class));
		verify(mockView, never()).show(anyString());
	}
	
	@Test
	public void testInitStuAnnouncementRPCFailure() {
		String errorMessage = "I have distressing news...";
		
		AsyncMockStubber.callFailureWith(new Exception(errorMessage))
		.when(mockDiscussionForumClient).getThreadsForForum(anyString(), anyLong(),
				anyLong(), any(DiscussionThreadOrder.class), anyBoolean(),
				any(DiscussionFilter.class), any(AsyncCallback.class));
	
		discussionThreadBundleList.add(mockDiscussionThreadBundle);
		widget.initStuAnnouncement();
		verify(mockDiscussionForumClient).getThreadsForForum(anyString(), anyLong(),
				anyLong(), any(DiscussionThreadOrder.class), anyBoolean(),
				any(DiscussionFilter.class), any(AsyncCallback.class));
		verify(mockView, never()).show(anyString());
		verify(mockSynapseJSNIUtils).consoleError(errorMessage);
	}
	
	@Test
	public void testClickAnnouncement() {
		widget.onClickAnnouncement();
		ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
		verify(mockClientCache).put(stringCaptor.capture(), eq(Boolean.TRUE.toString()), anyLong());
		String capturedKey = stringCaptor.getValue();
		assertTrue(capturedKey.startsWith(StuAnnouncementWidget.STU_ANNOUNCEMENT_CLICKED_PREFIX_KEY));
		verify(mockView, times(2)).hide();
		verify(mockPlaceChanger).goTo(any(Synapse.class));
	}
	
	@Test
	public void testOnDismiss() {
		widget.onDismiss();
		ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
		verify(mockClientCache).put(stringCaptor.capture(), eq(Boolean.TRUE.toString()), anyLong());
		String capturedKey = stringCaptor.getValue();
		assertTrue(capturedKey.startsWith(StuAnnouncementWidget.STU_ANNOUNCEMENT_CLICKED_PREFIX_KEY));
		verify(mockView, times(2)).hide();
		verifyZeroInteractions(mockPlaceChanger);
	}

}
