package org.sagebionetworks.web.client.widget.discussion;

import org.sagebionetworks.repo.model.discussion.CreateDiscussionReply;
import org.sagebionetworks.repo.model.discussion.DiscussionReplyBundle;
import org.sagebionetworks.web.client.DiscussionForumClientAsync;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GlobalApplicationState;
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
public class NewReplyWidget implements NewReplyWidgetView.Presenter{

	public static final String DEFAULT_MARKDOWN = "";
	private static final String SUCCESS_TITLE = "Reply created";
	private static final String SUCCESS_MESSAGE = "A reply has been created.";
	private NewReplyWidgetView view;
	private DiscussionForumClientAsync discussionForumClient;
	private SynapseAlert synAlert;
	private MarkdownEditorWidget markdownEditor;
	private AuthenticationController authController;
	private GlobalApplicationState globalApplicationState;
	private Callback newReplyCallback;
	private String threadId;

	@Inject
	public NewReplyWidget(
			NewReplyWidgetView view,
			DiscussionForumClientAsync discussionForumClient,
			SynapseAlert synAlert,
			MarkdownEditorWidget markdownEditor,
			AuthenticationController authController,
			GlobalApplicationState globalApplicationState
			) {
		this.view = view;
		this.discussionForumClient = discussionForumClient;
		this.synAlert = synAlert;
		this.markdownEditor = markdownEditor;
		this.authController = authController;
		this.globalApplicationState = globalApplicationState;
		markdownEditor.hideUploadRelatedCommands();
		markdownEditor.showExternalImageButton();
		view.setPresenter(this);
		view.setAlert(synAlert.asWidget());
		view.setMarkdownEditor(markdownEditor.asWidget());
	}

	public void configure(String threadId, Callback newReplyCallback) {
		this.threadId = threadId;
		this.newReplyCallback = newReplyCallback;
	}

	@Override
	public void onClickNewReply() {
		if (!authController.isLoggedIn()) {
			view.showErrorMessage(DisplayConstants.ERROR_LOGIN_REQUIRED);
			globalApplicationState.getPlaceChanger().goTo(new LoginPlace(LoginPlace.LOGIN_TOKEN));
		} else {
			view.setReplyTextBoxVisible(false);
			markdownEditor.configure(DEFAULT_MARKDOWN);
			view.setNewReplyContainerVisible(true);
			markdownEditor.setMarkdownFocus();
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
		view.showSaving();
		CreateDiscussionReply toCreate = new CreateDiscussionReply();
		toCreate.setThreadId(threadId);
		toCreate.setMessageMarkdown(messageMarkdown);
		discussionForumClient.createReply(toCreate, new AsyncCallback<DiscussionReplyBundle>(){
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
				onCancel();
			}
		});
	}

	@Override
	public void onCancel() {
		view.resetButton();
		view.setReplyTextBoxVisible(true);
		view.setNewReplyContainerVisible(false);
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
