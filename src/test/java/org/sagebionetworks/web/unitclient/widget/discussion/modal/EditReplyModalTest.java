package org.sagebionetworks.web.unitclient.widget.discussion.modal;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.discussion.DiscussionReplyBundle;
import org.sagebionetworks.repo.model.discussion.UpdateReplyMessage;
import org.sagebionetworks.web.client.DiscussionForumClientAsync;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.discussion.modal.EditReplyModal;
import org.sagebionetworks.web.client.widget.discussion.modal.ReplyModalView;
import org.sagebionetworks.web.client.widget.entity.MarkdownEditorWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

public class EditReplyModalTest {

  @Mock
  ReplyModalView mockView;

  @Mock
  DiscussionForumClientAsync mockDiscussionForumClient;

  @Mock
  SynapseAlert mockSynAlert;

  @Mock
  Callback mockCallback;

  @Mock
  DiscussionReplyBundle mockDiscussionReplyBundle;

  @Mock
  MarkdownEditorWidget mockMarkdownEditor;

  @Mock
  PopupUtilsView mockPopupUtilsView;

  @Mock
  GlobalApplicationState mockGlobalAppState;

  @Captor
  ArgumentCaptor<Callback> callbackCaptor;

  String replyId = "123";
  String message = "message";
  EditReplyModal modal;

  @Before
  public void before() {
    MockitoAnnotations.initMocks(this);
    modal =
      new EditReplyModal(
        mockView,
        mockDiscussionForumClient,
        mockSynAlert,
        mockMarkdownEditor,
        mockPopupUtilsView,
        mockGlobalAppState
      );
    modal.configure(replyId, message, mockCallback);
    when(mockMarkdownEditor.getMarkdown()).thenReturn(message);
  }

  @Test
  public void testConstructor() {
    verify(mockView).setPresenter(modal);
    verify(mockView).setAlert(any());
    verify(mockView).setModalTitle(anyString());
    verify(mockView).setMarkdownEditor(any());
  }

  @Test
  public void testShowDialog() {
    modal.show();
    verify(mockView).clear();
    verify(mockMarkdownEditor).configure(anyString());
    verify(mockMarkdownEditor).hideUploadRelatedCommands();
    verify(mockMarkdownEditor).showExternalImageButton();
    verify(mockView).showDialog();
    verify(mockGlobalAppState).setIsEditing(true);
  }

  @Test
  public void testHideDialog() {
    modal.hide();
    verify(mockView).hideDialog();
    verify(mockGlobalAppState).setIsEditing(false);
  }

  @Test
  public void asWidgetTest() {
    modal.asWidget();
    verify(mockView).asWidget();
  }

  @Test
  public void testOnSaveInvalidArgument() {
    when(mockMarkdownEditor.getMarkdown()).thenReturn("");
    modal.onSave();
    verify(mockSynAlert).clear();
    verify(mockSynAlert).showError(anyString());
    verify(mockView, never()).hideDialog();
    verifyZeroInteractions(mockDiscussionForumClient);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testOnSaveSuccess() {
    when(mockMarkdownEditor.getMarkdown()).thenReturn("message");
    AsyncMockStubber
      .callSuccessWith(mockDiscussionReplyBundle)
      .when(mockDiscussionForumClient)
      .updateReplyMessage(
        anyString(),
        any(UpdateReplyMessage.class),
        any(AsyncCallback.class)
      );
    modal.onSave();
    verify(mockSynAlert).clear();
    verify(mockView).showSaving();
    verify(mockView).hideDialog();
    verify(mockView).showSuccess(anyString(), anyString());
    verify(mockDiscussionForumClient)
      .updateReplyMessage(
        anyString(),
        any(UpdateReplyMessage.class),
        any(AsyncCallback.class)
      );
    verify(mockCallback).invoke();
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testOnSaveFailure() {
    when(mockMarkdownEditor.getMarkdown()).thenReturn("message");
    AsyncMockStubber
      .callFailureWith(new Exception())
      .when(mockDiscussionForumClient)
      .updateReplyMessage(
        anyString(),
        any(UpdateReplyMessage.class),
        any(AsyncCallback.class)
      );

    modal.onSave();
    verify(mockSynAlert).clear();
    verify(mockView).showSaving();
    verify(mockDiscussionForumClient)
      .updateReplyMessage(
        anyString(),
        any(UpdateReplyMessage.class),
        any(AsyncCallback.class)
      );
    verifyZeroInteractions(mockCallback);
    verify(mockSynAlert).handleException(any(Throwable.class));
    verify(mockView).resetButton();
  }

  @Test
  public void testOnClickCancel() {
    modal.onCancel();

    // since no changes were made, verify confirmation dialog was not shown
    verify(mockPopupUtilsView, never())
      .showConfirmDialog(
        eq(DisplayConstants.UNSAVED_CHANGES),
        eq(DisplayConstants.NAVIGATE_AWAY_CONFIRMATION_MESSAGE),
        callbackCaptor.capture()
      );
    verify(mockView).hideDialog();
  }

  @Test
  public void testOnClickCancelWithUnsavedChanges() {
    when(mockMarkdownEditor.getMarkdown()).thenReturn("unsaved changes");
    modal.onCancel();

    // since no changes were made, verify confirmation dialog was not shown
    verify(mockPopupUtilsView)
      .showConfirmDialog(
        eq(DisplayConstants.UNSAVED_CHANGES),
        eq(DisplayConstants.NAVIGATE_AWAY_CONFIRMATION_MESSAGE),
        callbackCaptor.capture()
      );
    verify(mockView, never()).hideDialog();
    // simulate user confirmed
    callbackCaptor.getValue().invoke();

    verify(mockView).hideDialog();
  }
}
