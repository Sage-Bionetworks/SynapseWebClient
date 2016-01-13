package org.sagebionetworks.web.unitclient.widget.discussion;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadBundle;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.widget.discussion.ReplyWidget;
import org.sagebionetworks.web.client.widget.discussion.ThreadWidget;
import org.sagebionetworks.web.client.widget.discussion.ThreadWidgetView;

import com.google.gwt.user.client.ui.Widget;

public class ThreadWidgetTest {

	@Mock
	ThreadWidgetView mockView;
	@Mock
	PortalGinInjector mockGinInjector;
	@Mock
	ReplyWidget mockReplyWidget;

	ThreadWidget threadWidget;

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		when(mockGinInjector.createReplyWidget()).thenReturn(mockReplyWidget);
		threadWidget = new ThreadWidget(mockView, mockGinInjector);
	}

	@Test
	public void testConstructor() {
		verify(mockView).setPresenter(threadWidget);
	}

	@Test
	public void testConfigure() {
		DiscussionThreadBundle threadBundle = new DiscussionThreadBundle();
		threadBundle.setTitle("title");
		threadBundle.setMessageUrl("messageUrl");
		threadBundle.setActiveAuthors(Arrays.asList("123"));
		threadBundle.setNumberOfReplies(1L);
		threadBundle.setNumberOfViews(2L);
		threadBundle.setLastActivity(new Date());
		threadWidget.configure(threadBundle );
		verify(mockView).clear();
		verify(mockView).setTitle("title");
		verify(mockView).setMessage("messageUrl");
		verify(mockView).setActiveUsers(anyString());
		verify(mockView).setNumberOfReplies("1");
		verify(mockView).setNumberOfViews("2");
		verify(mockView).setLastActivity(anyString());
		// TODO: remove
		verify(mockView, Mockito.times(2)).addReply(any(Widget.class));
		verify(mockGinInjector, times(2)).createReplyWidget();
		verify(mockReplyWidget, times(2)).configure();
	}

	@Test
	public void asWidgetTest() {
		threadWidget.asWidget();
		verify(mockView).asWidget();
	}
}
