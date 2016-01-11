package org.sagebionetworks.web.unitclient.widget.discussion.modal;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.web.client.DiscussionForumClientAsync;
import org.sagebionetworks.web.client.widget.discussion.modal.NewThreadModal;
import org.sagebionetworks.web.client.widget.discussion.modal.NewThreadModalView;

public class NewThreadModalTest {
	@Mock
	NewThreadModalView mockView;
	@Mock
	DiscussionForumClientAsync mockDiscussionForumClient;
	String forumId;
	NewThreadModal modal;

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		modal = new NewThreadModal(mockView, mockDiscussionForumClient);
	}

	@Test
	public void testConstructor() {
		Mockito.verify(mockView).setPresenter(modal);
	}

	@Test
	public void testShowDialog() {
		modal.show();
		Mockito.verify(mockView).showDialog();
	}

	@Test
	public void testHideDialog() {
		modal.hide();
		Mockito.verify(mockView).hideDialog();
	}

	@Test
	public void asWidgetTest() {
		modal.asWidget();
		Mockito.verify(mockView).setPresenter(modal);
		Mockito.verify(mockView).asWidget();
	}
}
