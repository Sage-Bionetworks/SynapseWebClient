package org.sagebionetworks.web.client.widget.discussion.modal;

import org.sagebionetworks.repo.model.discussion.DiscussionReplyBundle;
import org.sagebionetworks.repo.model.discussion.UpdateReplyMessage;
import org.sagebionetworks.web.client.DiscussionForumClientAsync;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.validation.ValidationResult;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * A simple modal dialog for editing a reply.
 */
public class EditReplyModal implements EditReplyModalView.Presenter{

	private EditReplyModalView view;
	private DiscussionForumClientAsync discussionForumClient;
	private SynapseAlert synAlert;
	private String replyId;
	private String message;
	Callback editReplyCallback;

	@Inject
	public EditReplyModal(
			EditReplyModalView view,
			DiscussionForumClientAsync discussionForumClient,
			SynapseAlert synAlert
			) {
		this.view = view;
		this.discussionForumClient = discussionForumClient;
		this.synAlert = synAlert;
		view.setPresenter(this);
		view.setAlert(synAlert.asWidget());
	}

	public void configure(String replyId, String message, Callback editReplyCallback) {
		this.replyId = replyId;
		this.message = message;
		this.editReplyCallback = editReplyCallback;
	}

	@Override
	public void show() {
		view.clear();
		view.setMessage(message);
		view.showDialog();
	}

	@Override
	public void hide() {
		view.hideDialog();
	}

	@Override
	public void onSave() {
		synAlert.clear();
		String messageMarkdown = view.getMessageMarkdown();
		ValidationResult result = new ValidationResult();
		result.requiredField("Message", messageMarkdown);
		if (!result.isValid()) {
			synAlert.showError(result.getErrorMessage());
			return;
		}
		view.showSaving();
		UpdateReplyMessage updateReply = new UpdateReplyMessage();
		updateReply.setMessageMarkdown(messageMarkdown);
		discussionForumClient.updateReplyMessage(replyId, updateReply, new AsyncCallback<DiscussionReplyBundle>(){
			@Override
			public void onFailure(Throwable caught) {
				view.resetButton();
				synAlert.handleException(caught);
			}

			@Override
			public void onSuccess(DiscussionReplyBundle result) {
				view.hideDialog();
				view.showSuccess();
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
