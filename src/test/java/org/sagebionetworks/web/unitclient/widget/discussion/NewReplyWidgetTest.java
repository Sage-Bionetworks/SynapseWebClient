package org.sagebionetworks.web.unitclient.widget.discussion;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.discussion.CreateDiscussionReply;
import org.sagebionetworks.repo.model.discussion.DiscussionReplyBundle;
import org.sagebionetworks.web.client.DiscussionForumClientAsync;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.discussion.NewReplyWidget;
import org.sagebionetworks.web.client.widget.discussion.NewReplyWidgetView;
import org.sagebionetworks.web.client.widget.entity.MarkdownEditorWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public class NewReplyWidgetTest {
	private NewReplyWidget newReplyWidget;
	@Mock
	private NewReplyWidgetView mockView;
	@Mock
	private DiscussionForumClientAsync mockDiscussionForumClient;
	@Mock
	private SynapseAlert mockSynAlert;
	@Mock
	private MarkdownEditorWidget mockMarkdownEditor;
	@Mock
	private AuthenticationController mockAuthController;
	@Mock
	private GlobalApplicationState mockGlobalApplicationState;
	@Mock
	private PlaceChanger mockPlaceChanger;
	@Mock
	private DiscussionReplyBundle mockDiscussionReplyBundle;
	@Mock
	Callback mockCallback;

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		newReplyWidget = new NewReplyWidget(mockView, mockDiscussionForumClient, mockSynAlert,
				mockMarkdownEditor, mockAuthController, mockGlobalApplicationState);
		when(mockAuthController.isLoggedIn()).thenReturn(true);
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		AsyncMockStubber.callSuccessWith(mockDiscussionReplyBundle)
				.when(mockDiscussionForumClient).getReply(anyString(), any(AsyncCallback.class));
		newReplyWidget.configure("1", mockCallback);
	}

	@Test
	public void testOnClickNewReply() {
		newReplyWidget.onClickNewReply();
		verify(mockMarkdownEditor).configure(anyString());
		verify(mockView).setReplyTextBoxVisible(false);
		verify(mockView).setNewReplyContainerVisible(true);
		verify(mockMarkdownEditor).setMarkdownFocus();
	}

	@Test
	public void testOnClickNewReplyAnonymous() {
		when(mockAuthController.isLoggedIn()).thenReturn(false);
		newReplyWidget.onClickNewReply();
		verify(mockMarkdownEditor, never()).configure(anyString());
		verify(mockGlobalApplicationState).getPlaceChanger();
		verify(mockView).showErrorMessage(anyString());
		verify(mockView, never()).setReplyTextBoxVisible(false);
		verify(mockView, never()).setNewReplyContainerVisible(true);
		verify(mockMarkdownEditor, never()).setMarkdownFocus();
	}


	@Test
	public void testOnClickCancel() {
		newReplyWidget.onCancel();
		verify(mockView).resetButton();
		verify(mockView).setReplyTextBoxVisible(true);
		verify(mockView).setNewReplyContainerVisible(false);
	}

	@Test
	public void testOnClickSaveInvalidArgument() {
		when(mockMarkdownEditor.getMarkdown()).thenReturn("");
		newReplyWidget.onSave();
		verify(mockSynAlert).clear();
		verify(mockMarkdownEditor).getMarkdown();
		verify(mockSynAlert).showError(anyString());
		verifyZeroInteractions(mockDiscussionForumClient);
	}

	@Test
	public void testOnClickSaveSuccess() {
		when(mockMarkdownEditor.getMarkdown()).thenReturn("message");
		AsyncMockStubber.callSuccessWith(mockDiscussionReplyBundle)
			.when(mockDiscussionForumClient).createReply(any(CreateDiscussionReply.class),
					any(AsyncCallback.class));
		newReplyWidget.onSave();
		verify(mockSynAlert, atLeastOnce()).clear();
		verify(mockView).showSaving();
		verify(mockView).showSuccess(anyString(), anyString());
		verify(mockDiscussionForumClient).createReply(any(CreateDiscussionReply.class), any(AsyncCallback.class));
		verify(mockView).resetButton();
		verify(mockView).setReplyTextBoxVisible(true);
		verify(mockView).setNewReplyContainerVisible(false);
		verify(mockCallback).invoke();
	}

	@Test
	public void testOnSaveFailure() {
		when(mockMarkdownEditor.getMarkdown()).thenReturn("message");
		Exception exception = new Exception();
		AsyncMockStubber.callFailureWith(exception)
			.when(mockDiscussionForumClient).createReply(any(CreateDiscussionReply.class),
					any(AsyncCallback.class));
		newReplyWidget.onSave();
		verify(mockSynAlert).clear();
		verify(mockView).showSaving();
		verify(mockDiscussionForumClient).createReply(any(CreateDiscussionReply.class), any(AsyncCallback.class));
		verifyZeroInteractions(mockCallback);
		verify(mockSynAlert).handleException(exception);
		verify(mockView).resetButton();
	}

	@Test
	public void testConstructor() {
		verify(mockView).setAlert(any(Widget.class));
		verify(mockView).setMarkdownEditor(any(Widget.class));
		verify(mockView).setPresenter(newReplyWidget);
		verify(mockMarkdownEditor).showExternalImageButton();
		verify(mockMarkdownEditor).hideUploadRelatedCommands();
	}

}
