package org.sagebionetworks.web.client.widget.discussion.modal;

import org.sagebionetworks.repo.model.discussion.CreateDiscussionThread;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadBundle;
import org.sagebionetworks.web.client.DiscussionForumClientAsync;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * A simple modal dialog for adding a new thread.
 */
public class NewDiscussionThreadModal implements NewDiscussionThreadModalView.Presenter{
	public static final String EMPTY_TITLE_ERROR = "Title cannot be empty.";
	public static final String EMPTY_MESSAGE_ERROR = "Message cannot be empty.";

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

	@Override
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
		ValidationResult validationResult = validate(threadTitle, messageMarkdown);
		if (!validationResult.isValid()) {
			synAlert.showError(validationResult.getErrorMessage());
			return;
		}
		CreateDiscussionThread toCreate = new CreateDiscussionThread();
		toCreate.setForumId(forumId);
		toCreate.setTitle(threadTitle);
		toCreate.setMessageMarkdown(messageMarkdown);
		discussionForumClient.createThread(toCreate, new AsyncCallback<DiscussionThreadBundle>(){
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}

			@Override
			public void onSuccess(DiscussionThreadBundle result) {
				view.hideDialog();
				view.showSuccess();
				if (newThreadCallback != null) {
					newThreadCallback.invoke();
				}
			}
		});
	}

	public static ValidationResult validate(String threadTitle, String messageMarkdown) {
		String errorMessages = "";
		ValidationResult result = new ValidationResult();
		result.setValidity(true);
		if (threadTitle == null || threadTitle.equals("")) {
			result.setValidity(false);
			errorMessages += EMPTY_TITLE_ERROR;
		}
		if (messageMarkdown == null || messageMarkdown.equals("")) {
			result.setValidity(false);
			if (!errorMessages.equals("")) {
				errorMessages += "\n";
			}
			errorMessages += EMPTY_MESSAGE_ERROR;
		}
		result.setErrorMessage(errorMessages);
		return result;
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
