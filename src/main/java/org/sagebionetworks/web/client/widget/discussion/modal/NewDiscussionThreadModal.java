package org.sagebionetworks.web.client.widget.discussion.modal;

import org.sagebionetworks.repo.model.discussion.CreateDiscussionThread;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadBundle;
import org.sagebionetworks.web.client.DiscussionForumClientAsync;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.cache.SessionStorage;
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
	public static final String RESTORE_TITLE = "Restore draft?";
	public static final String RESTORE_MESSAGE = "Would you like to continue writing where you left off?"; 
	public static final String DEFAULT_MARKDOWN = "";
	private DiscussionThreadModalView view;
	private DiscussionForumClientAsync discussionForumClient;
	private SynapseAlert synAlert;
	private MarkdownEditorWidget markdownEditor;
	private AuthenticationController authController;
	private SessionStorage storage;
	private String forumId;
	private String key;
	Callback newThreadCallback;

	@Inject
	public NewDiscussionThreadModal(
			DiscussionThreadModalView view,
			DiscussionForumClientAsync discussionForumClient,
			SynapseAlert synAlert,
			MarkdownEditorWidget markdownEditor,
			AuthenticationController authController,
			SessionStorage sessionStorage
			) {
		this.view = view;
		this.discussionForumClient = discussionForumClient;
		this.synAlert = synAlert;
		this.markdownEditor = markdownEditor;
		this.authController = authController;
		this.storage = sessionStorage;
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
		this.key = forumId + "_" + authController.getCurrentUserPrincipalId();
	}

	@Override
	public void show() {
		view.clear();
		checkForSavedThread();
		view.showDialog();
	}
	
	private void checkForSavedThread() {
		if (storage.getItem(key + "_title") == null || storage.getItem(key + "_message") == null) {
			markdownEditor.configure(DEFAULT_MARKDOWN);			
		} else {
			Callback yesCallback = new Callback() {
				@Override
				public void invoke() {
					String title = storage.getItem(key + "_title");
					String message = storage.getItem(key + "_message");
					view.setThreadTitle(title);
					markdownEditor.configure(message);
					storage.removeItem(key + "_title");
					storage.removeItem(key + "_message");
				}
			};
			Callback noCallback = new Callback() {
				@Override
				public void invoke() {
					markdownEditor.configure(DEFAULT_MARKDOWN);	
					storage.removeItem(key + "_title");
					storage.removeItem(key + "_message");
				}
			};
			DisplayUtils.showConfirmDialog(RESTORE_TITLE, RESTORE_MESSAGE, yesCallback, noCallback);
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
		storage.setItem(key + "_title", threadTitle);
		storage.setItem(key + "_message", messageMarkdown);
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
				storage.removeItem(key + "_title");
				storage.removeItem(key + "_message");
			}

		});
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
