package org.sagebionetworks.web.client.widget.discussion.modal;

import org.sagebionetworks.repo.model.discussion.DiscussionThreadBundle;
import org.sagebionetworks.web.client.DiscussionForumClientAsync;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.validation.ValidationResult;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.discussion.UpdateThread;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * A simple modal dialog for editing a thread.
 */
public class EditDiscussionThreadModal implements EditDiscussionThreadModalView.Presenter{

	private EditDiscussionThreadModalView view;
	private DiscussionForumClientAsync discussionForumClient;
	private SynapseAlert synAlert;
	private String threadId;
	private String title;
	private String message;
	Callback editThreadCallback;

	@Inject
	public EditDiscussionThreadModal(
			EditDiscussionThreadModalView view,
			DiscussionForumClientAsync discussionForumClient,
			SynapseAlert synAlert
			) {
		this.view = view;
		this.discussionForumClient = discussionForumClient;
		this.synAlert = synAlert;
		view.setPresenter(this);
		view.setAlert(synAlert.asWidget());
	}

	public void configure(String threadId, String currentTitle, String currentMessage, Callback editThreadCallback) {
		this.threadId = threadId;
		this.editThreadCallback = editThreadCallback;
		this.title = currentTitle;
		this.message = currentMessage;
	}

	@Override
	public void show() {
		view.clear();
		view.setThreadTitle(title);
		view.setThreadMessage(message);
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
		view.showSaving();
		UpdateThread updateThread = new UpdateThread();
		updateThread.setTitle(threadTitle);
		updateThread.setMessage(messageMarkdown);
		discussionForumClient.updateThread(threadId, updateThread, new AsyncCallback<DiscussionThreadBundle>(){
			@Override
			public void onFailure(Throwable caught) {
				view.resetButton();
				synAlert.handleException(caught);
			}

			@Override
			public void onSuccess(DiscussionThreadBundle result) {
				view.hideDialog();
				view.showSuccess();
				if (editThreadCallback != null) {
					editThreadCallback.invoke();
				}
			}

		});
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
