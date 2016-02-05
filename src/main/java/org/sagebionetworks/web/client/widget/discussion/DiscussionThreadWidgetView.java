package org.sagebionetworks.web.client.widget.discussion;

import org.gwtbootstrap3.extras.bootbox.client.callback.AlertCallback;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface DiscussionThreadWidgetView extends IsWidget{

	public interface Presenter {

		Widget asWidget();

		void toggleReplies();

		void toggleThread();

		void onClickNewReply();

		void loadMore();

		void onClickDeleteThread();

		void onClickEditThread();
	}

	void setPresenter(DiscussionThreadWidget presenter);

	void addReply(Widget w);

	void clear();

	void setTitle(String title);

	void setMessage(String message);

	void setNumberOfReplies(String numberOfReplies);

	void setNumberOfViews(String numberOfViews);

	void setLastActivity(String lastActivity);

	void setAuthor(Widget author);

	void setCreatedOn(String createdOn);

	void toggleThread();

	void toggleReplies();

	void setNewReplyModal(Widget w);

	void setAlert(Widget w);

	void setLoadMoreButtonVisibility(boolean visible);

	void setShowRepliesVisibility(boolean visible);

	void showReplyDetails();

	void clearReplies();

	void addActiveAuthor(Widget user);

	boolean isThreadCollapsed();

	void setThreadUpIconVisible(boolean visible);

	void setThreadDownIconVisible(boolean visible);

	void setReplyUpIconVisible(boolean visible);

	void setReplyDownIconVisible(boolean visible);

	boolean isReplyCollapsed();

	void setLoadingRepliesVisible(boolean visible);

	void setDeleteButtonVisible(boolean visible);

	void setTitleAsDeleted();

	void showDeleteConfirm(String deleteConfirmMessage, AlertCallback deleteCallback);

	void setReplyButtonVisible(boolean visible);

	void showErrorMessage(String errorMessage);

	void setEditIconVisible(boolean visible);

	void setEditThreadModal(Widget w);

	void setEditedVisible(Boolean visible);

	void setLoadingMessageVisible(boolean visible);
}
