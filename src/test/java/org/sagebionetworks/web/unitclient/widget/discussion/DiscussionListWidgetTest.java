package org.sagebionetworks.web.unitclient.widget.discussion;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.widget.discussion.ThreadListWidget;
import org.sagebionetworks.web.client.widget.discussion.ThreadListWidgetView;
import org.sagebionetworks.web.client.widget.discussion.ThreadWidget;

import com.google.gwt.user.client.ui.Widget;

public class DiscussionListWidgetTest {

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
		Mockito.when(mockGinInjector.createThreadWidget()).thenReturn(mockThreadWidget);
		discussionListWidget = new ThreadListWidget(mockView, mockGinInjector);
	}

	@Test
	public void testConfigure() {
		Mockito.verify(mockView).setPresenter(discussionListWidget);
		// configure
		Mockito.verify(mockView).clear();
		Mockito.verify(mockView, Mockito.times(2)).addThread((Widget) Mockito.any());
		Mockito.verify(mockGinInjector, Mockito.times(2)).createThreadWidget();
		Mockito.verify(mockThreadWidget, Mockito.times(2)).configure();
	}

	@Test
	public void asWidgetTest() {
		discussionListWidget.asWidget();
		Mockito.verify(mockView).setPresenter(discussionListWidget);
		Mockito.verify(mockView).asWidget();
	}
}
