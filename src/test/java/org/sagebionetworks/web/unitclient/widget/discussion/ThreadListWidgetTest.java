package org.sagebionetworks.web.unitclient.widget.discussion;
import static org.mockito.Mockito.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.widget.discussion.ThreadListWidget;
import org.sagebionetworks.web.client.widget.discussion.ThreadListWidgetView;
import org.sagebionetworks.web.client.widget.discussion.ThreadWidget;

import com.google.gwt.user.client.ui.Widget;

public class ThreadListWidgetTest {

	@Mock
	ThreadListWidgetView mockView;
	@Mock
	PortalGinInjector mockGinInjector;
	@Mock
	ThreadWidget mockThreadWidget;

	ThreadListWidget discussionListWidget;

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		when(mockGinInjector.createThreadWidget()).thenReturn(mockThreadWidget);
		discussionListWidget = new ThreadListWidget(mockView, mockGinInjector);
	}

	@Test
	public void testConfigure() {
		verify(mockView).setPresenter(discussionListWidget);
		// configure
		verify(mockView).clear();
		verify(mockView, times(2)).addThread(any(Widget.class));
		verify(mockGinInjector, times(2)).createThreadWidget();
		verify(mockThreadWidget, times(2)).configure();
	}

	@Test
	public void asWidgetTest() {
		discussionListWidget.asWidget();
		verify(mockView).asWidget();
	}
}
