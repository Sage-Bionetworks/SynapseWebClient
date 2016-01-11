package org.sagebionetworks.web.unitclient.widget.discussion;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.web.client.widget.discussion.ReplyWidget;
import org.sagebionetworks.web.client.widget.discussion.ReplyWidgetView;

public class ReplyWidgetTest {
	@Mock
	ReplyWidgetView mockView;

	ReplyWidget replyWidget;

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		replyWidget = new ReplyWidget(mockView);
	}

	@Test
	public void testConstructor() {
		Mockito.verify(mockView).setPresenter(replyWidget);
	}

	@Test
	public void setAsWidgetTest() {
		replyWidget.asWidget();
		Mockito.verify(mockView).setPresenter(replyWidget);
		Mockito.verify(mockView).asWidget();
	}
}
