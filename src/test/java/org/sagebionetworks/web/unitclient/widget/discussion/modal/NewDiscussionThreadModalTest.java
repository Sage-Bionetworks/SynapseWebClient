package org.sagebionetworks.web.unitclient.widget.discussion.modal;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.discussion.CreateDiscussionThread;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadBundle;
import org.sagebionetworks.web.client.DiscussionForumClientAsync;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.discussion.modal.NewDiscussionThreadModal;
import org.sagebionetworks.web.client.widget.discussion.modal.NewDiscussionThreadModalView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public class NewDiscussionThreadModalTest {
	@Mock
	NewDiscussionThreadModalView mockView;
	@Mock
	DiscussionForumClientAsync mockDiscussionForumClient;
	@Mock
	SynapseAlert mockSynAlert;
	@Mock
	CallbackP<Void> mockCallback;
	@Mock
	DiscussionThreadBundle mockDiscussionThreadBundle;
	String forumId = "123";
	NewDiscussionThreadModal modal;

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		modal = new NewDiscussionThreadModal(mockView, mockDiscussionForumClient, mockSynAlert);
		modal.configure(forumId, mockCallback);
	}

	@Test
	public void testConstructor() {
		verify(mockView).setPresenter(modal);
		verify(mockView).setAlert(any(Widget.class));
	}

	@Test
	public void testShowDialog() {
		modal.show();
		verify(mockView).clear();
		verify(mockView).showDialog();
	}

	@Test
	public void testHideDialog() {
		modal.hide();
		verify(mockView).hideDialog();
	}

	@Test
	public void asWidgetTest() {
		modal.asWidget();
		verify(mockView).asWidget();
	}

	@Test
	public void testOnSaveNullTitle() {
		when(mockView.getTitle()).thenReturn(null);
		when(mockView.getMessageMarkdown()).thenReturn("message");
		modal.onSave();
		verify(mockSynAlert).showError("Title cannot be empty.");
		verify(mockView, never()).hideDialog();
		verifyZeroInteractions(mockDiscussionForumClient);
	}

	@Test
	public void testOnSaveEmptyTitle() {
		when(mockView.getTitle()).thenReturn("");
		when(mockView.getMessageMarkdown()).thenReturn("message");
		modal.onSave();
		verify(mockSynAlert).showError("Title cannot be empty.");
		verify(mockView, never()).hideDialog();
		verifyZeroInteractions(mockDiscussionForumClient);
	}

	@Test
	public void testOnSaveNullMessage() {
		when(mockView.getTitle()).thenReturn("title");
		when(mockView.getMessageMarkdown()).thenReturn(null);
		modal.onSave();
		verify(mockSynAlert).showError("Message cannot be empty.");
		verify(mockView, never()).hideDialog();
		verifyZeroInteractions(mockDiscussionForumClient);
	}

	@Test
	public void testOnSaveEmptyMessage() {
		when(mockView.getTitle()).thenReturn("title");
		when(mockView.getMessageMarkdown()).thenReturn("");
		modal.onSave();
		verify(mockSynAlert).showError("Message cannot be empty.");
		verify(mockView, never()).hideDialog();
		verifyZeroInteractions(mockDiscussionForumClient);
	}

	@Test
	public void testOnSaveSuccess() {
		when(mockView.getTitle()).thenReturn("title");
		when(mockView.getMessageMarkdown()).thenReturn("message");
		AsyncMockStubber.callSuccessWith(mockDiscussionThreadBundle)
			.when(mockDiscussionForumClient).createThread(any(CreateDiscussionThread.class),
					any(AsyncCallback.class));
		modal.onSave();
		verify(mockView).hideDialog();
		verify(mockDiscussionForumClient).createThread(any(CreateDiscussionThread.class), any(AsyncCallback.class));
		verify(mockCallback).invoke(null);
	}

	@Test
	public void testOnSaveFailure() {
		when(mockView.getTitle()).thenReturn("title");
		when(mockView.getMessageMarkdown()).thenReturn("message");
		AsyncMockStubber.callFailureWith(new Exception())
			.when(mockDiscussionForumClient).createThread(any(CreateDiscussionThread.class),
					any(AsyncCallback.class));
		modal.onSave();
		verify(mockView).hideDialog();
		verify(mockDiscussionForumClient).createThread(any(CreateDiscussionThread.class), any(AsyncCallback.class));
		verifyZeroInteractions(mockCallback);
	}
}
