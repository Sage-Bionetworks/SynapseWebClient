package org.sagebionetworks.web.client.widget.discussion.modal;

import org.sagebionetworks.repo.model.discussion.CreateDiscussionThread;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadBundle;
import org.sagebionetworks.web.client.DiscussionForumClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.validation.ValidationResult;
import org.sagebionetworks.web.client.widget.entity.MarkdownEditorWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;

import com.google.gwt.storage.client.Storage;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
/**
 * A simple modal dialog for adding a new thread.
 */
public class NewDiscussionThreadModal implements DiscussionThreadModalView.Presenter{

	private static final String NEW_THREAD_MODAL_TITLE = "New Thread";
	private static final String SUCCESS_TITLE = "Thread created";
	private static final String SUCCESS_MESSAGE = "A new thread has been created.";
	public static final String DEFAULT_MARKDOWN = "";
	private DiscussionThreadModalView view;
	private DiscussionForumClientAsync discussionForumClient;
	private SynapseAlert synAlert;
	private MarkdownEditorWidget markdownEditor;
	private AuthenticationController authController;
	private String forumId;
	private Storage storage = null;
	private String key;
	Callback newThreadCallback;

	@Inject
	public NewDiscussionThreadModal(
			DiscussionThreadModalView view,
			DiscussionForumClientAsync discussionForumClient,
			SynapseAlert synAlert,
			MarkdownEditorWidget markdownEditor,
			AuthenticationController authController
			) {
		this.view = view;
		this.discussionForumClient = discussionForumClient;
		this.synAlert = synAlert;
		this.markdownEditor = markdownEditor;
		this.authController = authController;
		markdownEditor.hideUploadRelatedCommands();
		markdownEditor.showExternalImageButton();
		view.setPresenter(this);
		view.setAlert(synAlert.asWidget());
		view.setModalTitle(NEW_THREAD_MODAL_TITLE);
		view.setMarkdownEditor(markdownEditor.asWidget());
	}

	public void configure(String forumId, Callback newThreadCallback) {
		this.forumId = forumId;
		this.newThreadCallback = newThreadCallback;
		this.storage = Storage.getSessionStorageIfSupported();
		this.key = forumId + "_" + authController.getCurrentUserPrincipalId();
	}

	@Override
	public void show() {
		view.clear();
		checkForSavedThread();
		view.showDialog();
	}
	
	private void checkForSavedThread() {
		String value = storage.getItem(key);
		if (value == null) {
			markdownEditor.configure(DEFAULT_MARKDOWN);			
		} else {
			String[] stored = value.split(",");
			String title = stored[0].split(":")[1].replace("\"","").trim();
			String message = stored[1].split(":")[1].replace("\"","").replace("}", "").trim();
			view.setThreadTitle(title);
			markdownEditor.configure(message);
		}
	}

	@Override
	public void hide() {
		view.hideDialog();
	}

	@Override
	public void onSave() {
		synAlert.clear();
		String threadTitle = view.getThreadTitle();
		String messageMarkdown = markdownEditor.getMarkdown();
		ValidationResult result = new ValidationResult();
		result.requiredField("Title", threadTitle)
				.requiredField("Message", messageMarkdown);
		if (!result.isValid()) {
			synAlert.showError(result.getErrorMessage());
			return;
		}
		storage.setItem(key, "{\"Title\":\"" + threadTitle + "\", \"Message\":\"" + messageMarkdown + "\"}");
		view.showSaving();
		CreateDiscussionThread toCreate = new CreateDiscussionThread();
		toCreate.setForumId(forumId);
		toCreate.setTitle(threadTitle);
		toCreate.setMessageMarkdown(messageMarkdown);
		discussionForumClient.createThread(toCreate, new AsyncCallback<DiscussionThreadBundle>(){
			@Override
			public void onFailure(Throwable caught) {
				view.resetButton();
				synAlert.handleException(caught);
			}

			@Override
			public void onSuccess(DiscussionThreadBundle result) {
				view.hideDialog();
				view.showSuccess(SUCCESS_TITLE, SUCCESS_MESSAGE);
				if (newThreadCallback != null) {
					newThreadCallback.invoke();
				}
				storage.removeItem(key);
			}

		});
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
