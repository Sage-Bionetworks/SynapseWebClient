package org.sagebionetworks.web.client.widget.discussion.modal;

import org.sagebionetworks.repo.model.discussion.CreateDiscussionThread;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadBundle;
import org.sagebionetworks.web.client.DiscussionForumClientAsync;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.validation.ValidationResult;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * A simple modal dialog for adding a new thread.
 */
public class NewDiscussionThreadModal implements NewDiscussionThreadModalView.Presenter{

	private NewDiscussionThreadModalView view;
	private DiscussionForumClientAsync discussionForumClient;
	private SynapseAlert synAlert;
	private String forumId;
	Callback newThreadCallback;

	@Inject
	public NewDiscussionThreadModal(
			NewDiscussionThreadModalView view,
			DiscussionForumClientAsync discussionForumClient,
			SynapseAlert synAlert
			) {
		this.view = view;
		this.discussionForumClient = discussionForumClient;
		this.synAlert = synAlert;
		view.setPresenter(this);
		view.setAlert(synAlert.asWidget());
	}

	public void configure(String forumId, Callback newThreadCallback) {
		this.forumId = forumId;
		this.newThreadCallback = newThreadCallback;
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
		String threadTitle = view.getTitle();
		String messageMarkdown = view.getMessageMarkdown();
		ValidationResult result = new ValidationResult();
		result.requiredField("Title", threadTitle)
				.requiredField("Message", messageMarkdown);
		if (!result.isValid()) {
			synAlert.showError(result.getErrorMessage());
			return;
		}
		viewProcessing();
		CreateDiscussionThread toCreate = new CreateDiscussionThread();
		toCreate.setForumId(forumId);
		toCreate.setTitle(threadTitle);
		toCreate.setMessageMarkdown(messageMarkdown);
		discussionForumClient.createThread(toCreate, new AsyncCallback<DiscussionThreadBundle>(){
			@Override
			public void onFailure(Throwable caught) {
				viewProcessed();
				synAlert.handleException(caught);
			}

			@Override
			public void onSuccess(DiscussionThreadBundle result) {
				view.hideDialog();
				view.showSuccess();
				viewProcessed();
				if (newThreadCallback != null) {
					newThreadCallback.invoke();
				}
			}

		});
	}

	public void viewProcessing() {
		view.setSaveButtonEnabled(false);
		view.setCancelButtonEnabled(false);
		view.setSendingRequestVisible(true);
	}

	public void viewProcessed() {
		view.setSaveButtonEnabled(true);
		view.setCancelButtonEnabled(true);
		view.setSendingRequestVisible(false);
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
