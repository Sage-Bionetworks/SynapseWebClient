package org.sagebionetworks.web.unitclient.widget.discussion.modal;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadBundle;
import org.sagebionetworks.web.client.DiscussionForumClientAsync;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.discussion.modal.EditDiscussionThreadModal;
import org.sagebionetworks.web.client.widget.discussion.modal.DiscussionThreadModalView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.discussion.UpdateThread;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public class EditDiscussionThreadModalTest {
	@Mock
	DiscussionThreadModalView mockView;
	@Mock
	DiscussionForumClientAsync mockDiscussionForumClient;
	@Mock
	SynapseAlert mockSynAlert;
	@Mock
	Callback mockCallback;
	@Mock
	DiscussionThreadBundle mockDiscussionThreadBundle;
	String threadId = "123";
	String title = "title";
	String message = "message";
	EditDiscussionThreadModal modal;

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		modal = new EditDiscussionThreadModal(mockView, mockDiscussionForumClient, mockSynAlert);
		modal.configure(threadId, title, message, mockCallback);
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
		verify(mockView).setThreadTitle(title);
		verify(mockView).setThreadMessage(message);
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
	public void testOnSaveInvalidArgument() {
		when(mockView.getThreadTitle()).thenReturn(null);
		when(mockView.getMessageMarkdown()).thenReturn("message");
		modal.onSave();
		verify(mockSynAlert).clear();
		verify(mockSynAlert).showError(anyString());
		verify(mockView, never()).hideDialog();
		verifyZeroInteractions(mockDiscussionForumClient);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testOnSaveSuccess() {
		when(mockView.getThreadTitle()).thenReturn("title");
		when(mockView.getMessageMarkdown()).thenReturn("message");
		AsyncMockStubber.callSuccessWith(mockDiscussionThreadBundle)
			.when(mockDiscussionForumClient).updateThread(eq(threadId), any(UpdateThread.class),
					any(AsyncCallback.class));
		modal.onSave();
		verify(mockSynAlert).clear();
		verify(mockView).showSaving();
		verify(mockView).hideDialog();
		verify(mockView).showSuccess(anyString(), anyString());
		verify(mockDiscussionForumClient).updateThread(eq(threadId), any(UpdateThread.class),
				any(AsyncCallback.class));
		verify(mockCallback).invoke();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testOnSaveFailure() {
		when(mockView.getThreadTitle()).thenReturn("title");
		when(mockView.getMessageMarkdown()).thenReturn("message");
		AsyncMockStubber.callFailureWith(new Exception())
			.when(mockDiscussionForumClient).updateThread(eq(threadId), any(UpdateThread.class),
					any(AsyncCallback.class));
		modal.onSave();
		verify(mockSynAlert).clear();
		verify(mockView).showSaving();
		verify(mockDiscussionForumClient).updateThread(eq(threadId), any(UpdateThread.class),
				any(AsyncCallback.class));
		verifyZeroInteractions(mockCallback);
		verify(mockView).resetButton();
		verify(mockSynAlert).handleException(any(Throwable.class));
	}
}
