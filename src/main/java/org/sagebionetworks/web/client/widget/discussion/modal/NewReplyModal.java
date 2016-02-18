package org.sagebionetworks.web.client.widget.discussion.modal;

import org.sagebionetworks.repo.model.discussion.CreateDiscussionReply;
import org.sagebionetworks.repo.model.discussion.DiscussionReplyBundle;
import org.sagebionetworks.web.client.DiscussionForumClientAsync;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.validation.ValidationResult;
import org.sagebionetworks.web.client.widget.entity.MarkdownEditorWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * A simple modal dialog for adding a new reply.
 */
public class NewReplyModal implements ReplyModalView.Presenter{

	private static final String NEW_REPLY_MODAL_TITLE = "New Reply";
	private static final String SUCCESS_TITLE = "Reply created";
	private static final String SUCCESS_MESSAGE = "A new reply has been created.";
	public static final String DEFAULT_MARKDOWN = "";
	private ReplyModalView view;
	private DiscussionForumClientAsync discussionForumClient;
	private SynapseAlert synAlert;
	private MarkdownEditorWidget markdownEditor;
	private String threadId;
	Callback newReplyCallback;

	@Inject
	public NewReplyModal(
			ReplyModalView view,
			DiscussionForumClientAsync discussionForumClient,
			SynapseAlert synAlert,
			MarkdownEditorWidget markdownEditor
			) {
		this.view = view;
		this.discussionForumClient = discussionForumClient;
		this.synAlert = synAlert;
		this.markdownEditor = markdownEditor;
		markdownEditor.hideUploadRelatedCommands();
		view.setPresenter(this);
		view.setAlert(synAlert.asWidget());
		view.setModalTitle(NEW_REPLY_MODAL_TITLE);
		view.setMarkdownEditor(markdownEditor.asWidget());
	}

	public void configure(String threadId, Callback newReplyCallback) {
		this.threadId = threadId;
		this.newReplyCallback = newReplyCallback;
	}

	@Override
	public void show() {
		view.clear();
		markdownEditor.configure(DEFAULT_MARKDOWN);
		view.showDialog();
	}

	@Override
	public void hide() {
		view.hideDialog();
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
		CreateDiscussionReply toCreate = new CreateDiscussionReply();
		toCreate.setThreadId(threadId);
		toCreate.setMessageMarkdown(messageMarkdown);
		discussionForumClient.createReply(toCreate, new AsyncCallback<DiscussionReplyBundle>(){
			@Override
			public void onFailure(Throwable caught) {
				view.resetButton();
				synAlert.handleException(caught);
			}

			@Override
			public void onSuccess(DiscussionReplyBundle result) {
				view.hideDialog();
				view.showSuccess(SUCCESS_TITLE, SUCCESS_MESSAGE);
				if (newReplyCallback != null) {
					newReplyCallback.invoke();
				}
			}
		});
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
