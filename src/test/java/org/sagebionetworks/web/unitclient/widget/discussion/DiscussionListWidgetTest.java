package org.sagebionetworks.web.unitclient.widget.discussion;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.widget.discussion.DiscussionListWidget;
import org.sagebionetworks.web.client.widget.discussion.DiscussionListWidgetView;
import org.sagebionetworks.web.client.widget.discussion.ThreadWidget;

import com.google.gwt.user.client.ui.Widget;

public class DiscussionListWidgetTest {

	@Mock
	DiscussionListWidgetView mockView;
	@Mock
	PortalGinInjector mockGinInjector;
	@Mock
	ThreadWidget mockThreadWidget;

	DiscussionListWidget discussionListWidget;

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		Mockito.when(mockGinInjector.createThreadWidget()).thenReturn(mockThreadWidget);
		discussionListWidget = new DiscussionListWidget(mockView, mockGinInjector);
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
