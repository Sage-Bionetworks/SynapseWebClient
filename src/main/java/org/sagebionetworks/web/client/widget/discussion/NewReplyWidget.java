package org.sagebionetworks.web.client.widget.discussion;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;
import org.sagebionetworks.repo.model.discussion.CreateDiscussionReply;
import org.sagebionetworks.repo.model.discussion.DiscussionReplyBundle;
import org.sagebionetworks.web.client.DiscussionForumClientAsync;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.cache.SessionStorage;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.validation.ValidationResult;
import org.sagebionetworks.web.client.widget.entity.MarkdownEditorWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * A simple dialog for creating a reply.
 */
public class NewReplyWidget implements NewReplyWidgetView.Presenter {

	public static final String DEFAULT_MARKDOWN = "";
	private static final String SUCCESS_TITLE = "Reply created";
	private static final String SUCCESS_MESSAGE = "A reply has been created.";
	public static final String RESTORE_TITLE = "Restore draft?";
	public static final String RESTORE_MESSAGE = "Would you like to continue writing where you left off?";
	private NewReplyWidgetView view;
	private DiscussionForumClientAsync discussionForumClient;
	private SynapseAlert synAlert;
	private MarkdownEditorWidget markdownEditor;
	private AuthenticationController authController;
	private GlobalApplicationState globalApplicationState;
	private SessionStorage storage;
	private Callback newReplyCallback;
	private String threadId;
	private String key;
	private PopupUtilsView popupUtils;

	@Inject
	public NewReplyWidget(NewReplyWidgetView view, DiscussionForumClientAsync discussionForumClient, SynapseAlert synAlert, MarkdownEditorWidget markdownEditor, AuthenticationController authController, GlobalApplicationState globalApplicationState, SessionStorage sessionStorage, PopupUtilsView popupUtils) {
		this.view = view;
		this.discussionForumClient = discussionForumClient;
		fixServiceEntryPoint(discussionForumClient);
		this.synAlert = synAlert;
		this.markdownEditor = markdownEditor;
		this.authController = authController;
		this.globalApplicationState = globalApplicationState;
		this.storage = sessionStorage;
		this.popupUtils = popupUtils;
		view.setPresenter(this);
		view.setAlert(synAlert.asWidget());
		view.setMarkdownEditor(markdownEditor.asWidget());
	}

	public void configure(String threadId, Callback newReplyCallback) {
		this.threadId = threadId;
		this.newReplyCallback = newReplyCallback;
		this.key = threadId + "_" + authController.getCurrentUserPrincipalId() + "_reply";
		reset();
	}

	public void reset() {
		onCancelAfterConfirm();
	}

	@Override
	public void onClickNewReply() {
		if (!authController.isLoggedIn()) {
			view.showErrorMessage(DisplayConstants.ERROR_LOGIN_REQUIRED);
			globalApplicationState.getPlaceChanger().goTo(new LoginPlace(LoginPlace.LOGIN_TOKEN));
		} else {
			globalApplicationState.setIsEditing(true);
			view.setReplyTextBoxVisible(false);
			checkForSavedReply();
			view.setNewReplyContainerVisible(true);
			markdownEditor.setMarkdownFocus();
			view.scrollIntoView();
		}
	}

	private void checkForSavedReply() {
		markdownEditor.hideUploadRelatedCommands();
		markdownEditor.showExternalImageButton();
		if (storage.getItem(key) == null) {
			markdownEditor.configure(DEFAULT_MARKDOWN);
		} else {
			Callback yesCallback = new Callback() {
				@Override
				public void invoke() {
					String reply = storage.getItem(key);
					markdownEditor.configure(reply);
					storage.removeItem(key);
				}
			};
			Callback noCallback = new Callback() {
				@Override
				public void invoke() {
					markdownEditor.configure(DEFAULT_MARKDOWN);
					storage.removeItem(key);
				}
			};
			view.showConfirmDialog(RESTORE_TITLE, RESTORE_MESSAGE, yesCallback, noCallback);
		}
	}

	@Override
	public void onSave() {
		synAlert.clear();
		String messageMarkdown = markdownEditor.getMarkdown();
		ValidationResult result = new ValidationResult();
		result.requiredField("Message", messageMarkdown);
		if (!result.isValid()) {
			synAlert.showError(result.getErrorMessage());
			return;
		}
		storage.setItem(key, messageMarkdown);
		view.showSaving();
		CreateDiscussionReply toCreate = new CreateDiscussionReply();
		toCreate.setThreadId(threadId);
		toCreate.setMessageMarkdown(messageMarkdown);
		discussionForumClient.createReply(toCreate, new AsyncCallback<DiscussionReplyBundle>() {
			@Override
			public void onFailure(Throwable caught) {
				view.resetButton();
				synAlert.handleException(caught);
			}

			@Override
			public void onSuccess(DiscussionReplyBundle result) {
				view.showSuccess(SUCCESS_TITLE, SUCCESS_MESSAGE);
				if (newReplyCallback != null) {
					newReplyCallback.invoke();
				}
				reset();
				storage.removeItem(key);
			}
		});
	}

	@Override
	public void onCancel() {
		if (!markdownEditor.getMarkdown().isEmpty()) {
			popupUtils.showConfirmDialog(DisplayConstants.UNSAVED_CHANGES, DisplayConstants.NAVIGATE_AWAY_CONFIRMATION_MESSAGE, () -> {
				onCancelAfterConfirm();
			});
		} else {
			onCancelAfterConfirm();
		}
	}

	public void onCancelAfterConfirm() {
		globalApplicationState.setIsEditing(false);
		view.resetButton();
		view.setReplyTextBoxVisible(true);
		view.setNewReplyContainerVisible(false);
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
