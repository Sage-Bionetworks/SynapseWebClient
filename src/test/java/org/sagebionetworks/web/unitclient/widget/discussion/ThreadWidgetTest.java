package org.sagebionetworks.web.unitclient.widget.discussion;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
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
	public void testConfigure() {
		verify(mockView).setPresenter(threadWidget);
		// configure
		verify(mockView).clear();
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
