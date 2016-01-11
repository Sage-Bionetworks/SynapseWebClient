package org.sagebionetworks.web.unitclient.widget.discussion;

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
		Mockito.when(mockGinInjector.createReplyWidget()).thenReturn(mockReplyWidget);
		threadWidget = new ThreadWidget(mockView, mockGinInjector);
	}

	@Test
	public void testConfigure() {
		Mockito.verify(mockView).setPresenter(threadWidget);
		// configure
		Mockito.verify(mockView).clear();
		Mockito.verify(mockView, Mockito.times(2)).addReply((Widget) Mockito.any());
		Mockito.verify(mockGinInjector, Mockito.times(2)).createReplyWidget();
		Mockito.verify(mockReplyWidget, Mockito.times(2)).configure();
	}

	@Test
	public void asWidgetTest() {
		threadWidget.asWidget();
		Mockito.verify(mockView).setPresenter(threadWidget);
		Mockito.verify(mockView).asWidget();
	}
}
