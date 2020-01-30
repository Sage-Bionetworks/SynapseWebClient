package org.sagebionetworks.web.client.widget.discussion.modal;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;
import org.sagebionetworks.repo.model.discussion.CreateDiscussionThread;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadBundle;
import org.sagebionetworks.web.client.DiscussionForumClientAsync;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.cache.SessionStorage;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.validation.ValidationResult;
import org.sagebionetworks.web.client.widget.entity.MarkdownEditorWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * A simple modal dialog for adding a new thread.
 */
public class NewDiscussionThreadModal implements DiscussionThreadModalView.Presenter {

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
	private PopupUtilsView popupUtils;
	private String forumId;
	private String titleKey;
	private String messageKey;
	Callback newThreadCallback;

	@Inject
	public NewDiscussionThreadModal(DiscussionThreadModalView view, DiscussionForumClientAsync discussionForumClient, SynapseAlert synAlert, MarkdownEditorWidget markdownEditor, AuthenticationController authController, SessionStorage sessionStorage, PopupUtilsView popupUtils) {
		this.view = view;
		this.discussionForumClient = discussionForumClient;
		fixServiceEntryPoint(discussionForumClient);
		this.synAlert = synAlert;
		this.markdownEditor = markdownEditor;
		this.authController = authController;
		this.storage = sessionStorage;
		this.popupUtils = popupUtils;
		view.setPresenter(this);
		view.setAlert(synAlert.asWidget());
		view.setModalTitle(NEW_THREAD_MODAL_TITLE);
		view.setMarkdownEditor(markdownEditor.asWidget());
	}

	public void configure(String forumId, Callback newThreadCallback) {
		this.forumId = forumId;
		this.newThreadCallback = newThreadCallback;
		this.titleKey = forumId + "_" + authController.getCurrentUserPrincipalId() + "_title";
		this.messageKey = forumId + "_" + authController.getCurrentUserPrincipalId() + "_message";
	}

	@Override
	public void show() {
		view.clear();
		checkForSavedThread();
		view.showDialog();
	}

	private void checkForSavedThread() {
		markdownEditor.hideUploadRelatedCommands();
		markdownEditor.showExternalImageButton();
		if (storage.getItem(titleKey) == null || storage.getItem(messageKey) == null) {
			markdownEditor.configure(DEFAULT_MARKDOWN);
		} else {
			Callback yesCallback = new Callback() {
				@Override
				public void invoke() {
					String title = storage.getItem(titleKey);
					String message = storage.getItem(messageKey);
					view.setThreadTitle(title);
					markdownEditor.configure(message);
					storage.removeItem(titleKey);
					storage.removeItem(messageKey);
				}
			};
			Callback noCallback = new Callback() {
				@Override
				public void invoke() {
					markdownEditor.configure(DEFAULT_MARKDOWN);
					storage.removeItem(titleKey);
					storage.removeItem(messageKey);
				}
			};
			view.showConfirmDialog(RESTORE_TITLE, RESTORE_MESSAGE, yesCallback, noCallback);
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
		result.requiredField("Title", threadTitle).requiredField("Message", messageMarkdown);
		if (!result.isValid()) {
			synAlert.showError(result.getErrorMessage());
			return;
		}
		storage.setItem(titleKey, threadTitle);
		storage.setItem(messageKey, messageMarkdown);
		view.showSaving();
		CreateDiscussionThread toCreate = new CreateDiscussionThread();
		toCreate.setForumId(forumId);
		toCreate.setTitle(threadTitle);
		toCreate.setMessageMarkdown(messageMarkdown);
		discussionForumClient.createThread(toCreate, new AsyncCallback<DiscussionThreadBundle>() {
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
				storage.removeItem(titleKey);
				storage.removeItem(messageKey);
			}

		});
	}

	@Override
	public void onCancel() {
		if (!markdownEditor.getMarkdown().isEmpty()) {
			popupUtils.showConfirmDialog(DisplayConstants.UNSAVED_CHANGES, DisplayConstants.NAVIGATE_AWAY_CONFIRMATION_MESSAGE, () -> {
				view.hideDialog();
			});
		} else {
			view.hideDialog();
		}
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
