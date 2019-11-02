package org.sagebionetworks.web.client.widget.discussion.modal;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;
import org.sagebionetworks.repo.model.discussion.DiscussionReplyBundle;
import org.sagebionetworks.repo.model.discussion.UpdateReplyMessage;
import org.sagebionetworks.web.client.DiscussionForumClientAsync;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.validation.ValidationResult;
import org.sagebionetworks.web.client.widget.entity.MarkdownEditorWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * A simple modal dialog for editing a reply.
 */
public class EditReplyModal implements ReplyModalView.Presenter {

	private static final String EDIT_REPLY_MODAL_TITLE = "Edit Reply";
	private static final String SUCCESS_TITLE = "Reply edited";
	private static final String SUCCESS_MESSAGE = "A reply has been edited.";
	private ReplyModalView view;
	private DiscussionForumClientAsync discussionForumClient;
	private SynapseAlert synAlert;
	private MarkdownEditorWidget markdownEditor;
	private PopupUtilsView popupUtils;
	private String replyId;
	private String message;
	Callback editReplyCallback;
	GlobalApplicationState globalAppState;

	@Inject
	public EditReplyModal(ReplyModalView view, DiscussionForumClientAsync discussionForumClient, SynapseAlert synAlert, MarkdownEditorWidget markdownEditor, PopupUtilsView popupUtils, GlobalApplicationState globalAppState) {
		this.view = view;
		this.discussionForumClient = discussionForumClient;
		fixServiceEntryPoint(discussionForumClient);
		this.synAlert = synAlert;
		this.markdownEditor = markdownEditor;
		this.popupUtils = popupUtils;
		this.globalAppState = globalAppState;
		view.setPresenter(this);
		view.setAlert(synAlert.asWidget());
		view.setModalTitle(EDIT_REPLY_MODAL_TITLE);
		view.setMarkdownEditor(markdownEditor.asWidget());
	}

	public void configure(String replyId, String message, Callback editReplyCallback) {
		this.replyId = replyId;
		this.message = message;
		this.editReplyCallback = editReplyCallback;
	}

	@Override
	public void show() {
		view.clear();
		markdownEditor.hideUploadRelatedCommands();
		markdownEditor.showExternalImageButton();
		markdownEditor.configure(message);
		globalAppState.setIsEditing(true);
		view.showDialog();
	}

	@Override
	public void hide() {
		globalAppState.setIsEditing(false);
		view.hideDialog();
	}

	@Override
	public void onCancel() {
		if (!markdownEditor.getMarkdown().equals(message)) {
			popupUtils.showConfirmDialog(DisplayConstants.UNSAVED_CHANGES, DisplayConstants.NAVIGATE_AWAY_CONFIRMATION_MESSAGE, () -> {
				onCancelAfterConfirm();
			});
		} else {
			onCancelAfterConfirm();
		}
	}

	public void onCancelAfterConfirm() {
		hide();
	}

	@Override
	public void onSave() {
		synAlert.clear();
		String messageMarkdown = markdownEditor.getMarkdown();
		ValidationResult result = new ValidationResult();
		result.requiredField("Message", messageMarkdown);
		if (!result.isValid()) {
			synAlert.showError(result.getErrorMessage());
			return;
		}
		view.showSaving();
		UpdateReplyMessage updateReply = new UpdateReplyMessage();
		updateReply.setMessageMarkdown(messageMarkdown);
		discussionForumClient.updateReplyMessage(replyId, updateReply, new AsyncCallback<DiscussionReplyBundle>() {
			@Override
			public void onFailure(Throwable caught) {
				view.resetButton();
				synAlert.handleException(caught);
			}

			@Override
			public void onSuccess(DiscussionReplyBundle result) {
				hide();
				view.showSuccess(SUCCESS_TITLE, SUCCESS_MESSAGE);
				if (editReplyCallback != null) {
					editReplyCallback.invoke();
				}
			}
		});
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
