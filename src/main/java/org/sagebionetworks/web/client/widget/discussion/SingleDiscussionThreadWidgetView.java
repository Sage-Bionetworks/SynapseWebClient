package org.sagebionetworks.web.client.widget.discussion;

import org.gwtbootstrap3.extras.bootbox.client.callback.AlertCallback;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface SingleDiscussionThreadWidgetView extends IsWidget{

	public interface Presenter {

		Widget asWidget();

		void onClickNewReply();

		void loadMore();

		void onClickDeleteThread();

		void onClickEditThread();
		
		void onClickThread();
		
		void onClickShowAllReplies();
	}

	void setPresenter(SingleDiscussionThreadWidget presenter);

	void clear();

	void setTitle(String title);

	void setSubscribeButtonWidget(Widget widget);
	
	void setMarkdownWidget(Widget widget);

	void setAuthor(Widget author);

	void setCreatedOn(String createdOn);

	void setAlert(Widget w);

	void setDeleteIconVisible(boolean visible);

	void showDeleteConfirm(String deleteConfirmMessage, AlertCallback deleteCallback);
	
	void setShowAllRepliesButtonVisible(boolean visible);
	
	void setReplyTextBoxVisible(boolean visible);

	void setCommandsVisible(boolean visible);

	void showErrorMessage(String errorMessage);

	void setEditIconVisible(boolean visible);

	void setEditThreadModal(Widget w);

	void setEditedLabelVisible(Boolean visible);

	void setLoadingMessageVisible(boolean visible);
	void setRepliesContainer(IsWidget container);
	void showSuccess(String successTitle, String successMessage);

	void setThreadLink(String link);

	void setRefreshAlert(Widget w);

	void removeRefreshAlert();

	void setPinIconVisible(boolean visible);
	void setUnpinIconVisible(boolean visible);
	void setIsAuthorModerator(boolean isModerator);

	void resetButton();

	void setNewReplyContainerVisible(boolean visible);

	void setMarkdownEditorWidget(Widget widget);

	void showSaving();

	void setReplyContainerVisible(boolean visible);

	void setDeletedThreadVisible(boolean visible);
}
