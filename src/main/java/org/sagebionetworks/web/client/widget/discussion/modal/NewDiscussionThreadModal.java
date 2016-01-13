package org.sagebionetworks.web.client.widget.discussion.modal;

import org.sagebionetworks.repo.model.discussion.CreateDiscussionThread;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadBundle;
import org.sagebionetworks.web.client.DiscussionForumClientAsync;
import org.sagebionetworks.web.client.utils.CallbackP;
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
	CallbackP<Void> newThreadCallback;

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
	public void configure(String forumId, CallbackP<Void> newThreadCallback) {
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
		String threadTitle = view.getTitle();
		String messageMarkdown = view.getMessageMarkdown();
		if (!isValidTitle(threadTitle)) {
			synAlert.showError("Title cannot be empty.");
			return;
		}
		if (!isValidMessage(messageMarkdown)) {
			synAlert.showError("Message cannot be empty.");
			return;
		}
		view.hideDialog();
		CreateDiscussionThread toCreate = new CreateDiscussionThread();
		toCreate.setForumId(forumId);
		toCreate.setTitle(threadTitle);
		toCreate.setMessageMarkdown(messageMarkdown);
		discussionForumClient.createThread(toCreate, new AsyncCallback<DiscussionThreadBundle>(){
			@Override
			public void onFailure(Throwable caught) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onSuccess(DiscussionThreadBundle result) {
				if (newThreadCallback != null) {
					newThreadCallback.invoke(null);
				}
			}
		});
	}

	private boolean isValidMessage(String messageMarkdown) {
		return (messageMarkdown != null && !messageMarkdown.equals(""));
	}

	private boolean isValidTitle(String threadTitle) {
		return (threadTitle != null && !threadTitle.equals(""));
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
