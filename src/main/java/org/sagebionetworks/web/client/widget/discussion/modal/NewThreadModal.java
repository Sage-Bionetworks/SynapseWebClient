package org.sagebionetworks.web.client.widget.discussion.modal;

import org.sagebionetworks.repo.model.discussion.CreateDiscussionThread;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadBundle;
import org.sagebionetworks.web.client.DiscussionForumClientAsync;
import org.sagebionetworks.web.client.utils.CallbackP;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * A simple modal dialog for adding a new thread.
 */
public class NewThreadModal implements NewThreadModalView.Presenter{

	private NewThreadModalView view;
	private DiscussionForumClientAsync discussionForumClient;
	private String forumId;
	CallbackP<Void> newThreadCallback;

	@Inject
	public NewThreadModal(
			NewThreadModalView view,
			DiscussionForumClientAsync discussionForumClient
			) {
		this.view = view;
		this.discussionForumClient = discussionForumClient;
		view.setPresenter(this);
	}

	@Override
	public void configure(String forumId, CallbackP<Void> newThreadCallback) {
		this.forumId = forumId;
		this.newThreadCallback = newThreadCallback;
	}

	@Override
	public void show() {
		view.showDialog();
	}

	@Override
	public void hide() {
		view.hideDialog();
	}

	@Override
	public void onSave(String threadTitle, String messageMarkdown) {
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

	@Override
	public void onCancel() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
