package org.sagebionetworks.web.unitclient.widget.discussion;
import static org.mockito.Mockito.*;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.discussion.DiscussionReplyBundle;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.widget.discussion.ReplyWidget;
import org.sagebionetworks.web.client.widget.discussion.ReplyWidgetView;
import org.sagebionetworks.web.client.widget.user.UserBadge;

import com.google.gwt.user.client.ui.Widget;

public class ReplyWidgetTest {
	@Mock
	ReplyWidgetView mockView;
	@Mock
	GWTWrapper mockGwtWrapper;
	@Mock
	UserBadge mockAuthorWidget;

	ReplyWidget replyWidget;

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		replyWidget = new ReplyWidget(mockView, mockAuthorWidget, mockGwtWrapper);
	}

	@Test
	public void testConstructor() {
		verify(mockView).setPresenter(replyWidget);
		verify(mockView).setAuthor(any(Widget.class));
	}

	@Test
	public void testConfigure() {
		DiscussionReplyBundle bundle = createReplyBundle("author", new Date());
		when(mockGwtWrapper.getFormattedDateString(any(Date.class))).thenReturn("today");
		replyWidget.configure(bundle);
		verify(mockAuthorWidget).configure(anyString());
		verify(mockView).setCreatedOn(anyString());
		verify(mockView).setMessage(anyString());
		verify(mockGwtWrapper).getFormattedDateString(any(Date.class));
	}

	@Test
	public void setAsWidgetTest() {
		replyWidget.asWidget();
		verify(mockView).asWidget();
	}

	private DiscussionReplyBundle createReplyBundle(String author, Date createdOn) {
		DiscussionReplyBundle bundle = new DiscussionReplyBundle();
		bundle.setCreatedBy("author");
		bundle.setCreatedOn(createdOn);
		return bundle;
	}
}
