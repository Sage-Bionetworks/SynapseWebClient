package org.sagebionetworks.web.unitclient.widget.discussion;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Arrays;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadBundle;
import org.sagebionetworks.web.client.DateTimeUtils;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.utils.TopicUtils;
import org.sagebionetworks.web.client.widget.discussion.DiscussionThreadListItemWidget;
import org.sagebionetworks.web.client.widget.discussion.DiscussionThreadListItemWidgetView;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import com.google.gwt.user.client.ui.Widget;

public class DiscussionThreadListItemWidgetTest {

	@Mock
	DiscussionThreadListItemWidgetView mockView;
	@Mock
	PortalGinInjector mockGinInjector;
	@Mock
	UserBadge mockUserBadge;
	@Mock
	UserBadge mockAuthorWidget;
	@Mock
	PlaceChanger mockPlaceChanger;
	@Mock
	CallbackP<DiscussionThreadBundle> mockThreadIdClickedCallback;
	@Mock
	DiscussionThreadBundle mockThreadBundle;
	@Mock
	DateTimeUtils mockDateTimeUtils;
	DiscussionThreadListItemWidget discussionThreadWidget;
	private String title = "title";
	private Long numberOfViews = 2L;
	public static final String THREAD_ID = "83473";
	public static final String PROJECT_ID = "1111";

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		when(mockGinInjector.getUserBadgeWidget()).thenReturn(mockUserBadge);
		discussionThreadWidget = new DiscussionThreadListItemWidget(mockView, mockAuthorWidget, mockGinInjector, mockDateTimeUtils);
		when(mockThreadBundle.getTitle()).thenReturn(title);
		when(mockThreadBundle.getActiveAuthors()).thenReturn(Arrays.asList("123"));
		when(mockThreadBundle.getNumberOfViews()).thenReturn(numberOfViews);
		when(mockThreadBundle.getProjectId()).thenReturn(PROJECT_ID);
		when(mockThreadBundle.getId()).thenReturn(THREAD_ID);
	}

	@Test
	public void testConstructor() {
		verify(mockView).setPresenter(discussionThreadWidget);
		verify(mockView).setThreadAuthor(any(Widget.class));
	}

	@Test
	public void testConfigure() {
		discussionThreadWidget.configure(mockThreadBundle);
		verify(mockView).setTitle(title);
		verify(mockView).clearActiveAuthors();
		verify(mockView).addActiveAuthor(any(Widget.class));
		verify(mockView).setNumberOfViews("2");
		verify(mockView).setLastActivity(anyString());
		verify(mockGinInjector).getUserBadgeWidget();
		verify(mockDateTimeUtils).getRelativeTime(any(Date.class));
		verify(mockAuthorWidget).configure(anyString());
		verify(mockView).setPinnedIconVisible(false);
		verify(mockView).setThreadUrl(TopicUtils.buildThreadLink(PROJECT_ID, THREAD_ID));
		reset(mockView);
	}

	@Test
	public void testConfigurePinned() {
		when(mockThreadBundle.getIsPinned()).thenReturn(true);
		discussionThreadWidget.configure(mockThreadBundle);
		verify(mockView).setPinnedIconVisible(true);
	}

	@Test
	public void asWidgetTest() {
		discussionThreadWidget.asWidget();
		verify(mockView).asWidget();
	}

	@Test
	public void testOnClickThreadNoCallback() {
		discussionThreadWidget.onClickThread();
		verify(mockThreadIdClickedCallback, never()).invoke(any(DiscussionThreadBundle.class));
	}

	@Test
	public void testOnClickThreadWithCallback() {
		discussionThreadWidget.setThreadIdClickedCallback(mockThreadIdClickedCallback);
		discussionThreadWidget.onClickThread();
		verify(mockThreadIdClickedCallback).invoke(any(DiscussionThreadBundle.class));
	}
}
