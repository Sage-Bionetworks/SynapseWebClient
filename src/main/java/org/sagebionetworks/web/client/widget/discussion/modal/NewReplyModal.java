package org.sagebionetworks.web.client.widget.discussion.modal;

import org.sagebionetworks.repo.model.discussion.CreateDiscussionReply;
import org.sagebionetworks.repo.model.discussion.DiscussionReplyBundle;
import org.sagebionetworks.web.client.DiscussionForumClientAsync;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.validation.ValidationResult;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * A simple modal dialog for adding a new reply.
 */
public class NewReplyModal implements NewReplyModalView.Presenter{

	private NewReplyModalView view;
	private DiscussionForumClientAsync discussionForumClient;
	private SynapseAlert synAlert;
	private String threadId;
	Callback newReplyCallback;

	@Inject
	public NewReplyModal(
			NewReplyModalView view,
			DiscussionForumClientAsync discussionForumClient,
			SynapseAlert synAlert
			) {
		this.view = view;
		this.discussionForumClient = discussionForumClient;
		this.synAlert = synAlert;
		view.setPresenter(this);
		view.setAlert(synAlert.asWidget());
	}

	public void configure(String threadId, Callback newReplyCallback) {
		this.threadId = threadId;
		this.newReplyCallback = newReplyCallback;
	}

	@Override
	public void show() {
		view.clear();
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
		viewProcessing();
		CreateDiscussionReply toCreate = new CreateDiscussionReply();
		toCreate.setThreadId(threadId);
		toCreate.setMessageMarkdown(messageMarkdown);
		discussionForumClient.createReply(toCreate, new AsyncCallback<DiscussionReplyBundle>(){
			@Override
			public void onFailure(Throwable caught) {
				viewProcessed();
				synAlert.handleException(caught);
			}

			@Override
			public void onSuccess(DiscussionReplyBundle result) {
				view.hideDialog();
				view.showSuccess();
				viewProcessed();
				if (newReplyCallback != null) {
					newReplyCallback.invoke();
				}
			}
		});
	}

	public void viewProcessed() {
		view.setSaveButtonEnabled(true);
		view.setCancelButtonEnabled(true);
		view.setSendingRequestVisible(false);
	}

	public void viewProcessing() {
		view.setSaveButtonEnabled(false);
		view.setCancelButtonEnabled(false);
		view.setSendingRequestVisible(true);
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
