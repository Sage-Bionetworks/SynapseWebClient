package org.sagebionetworks.web.client.widget.discussion;

import org.gwtbootstrap3.extras.bootbox.client.callback.AlertCallback;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface DiscussionThreadWidgetView extends IsWidget{

	public interface Presenter {

		Widget asWidget();

		void onClickNewReply();

		void loadMore();

		void onClickDeleteThread();

		void onClickEditThread();
		
		void onClickThread();
	}

	void setPresenter(DiscussionThreadWidget presenter);

	void addReply(Widget w);

	void clear();

	void setTitle(String title);

	void setSubscribeButtonWidget(Widget widget);
	
	void setMarkdownWidget(Widget widget);

	void setNumberOfReplies(String numberOfReplies, String descriptiveText);

	void setNumberOfViews(String numberOfViews);

	void setLastActivity(String lastActivity);

	void setAuthor(Widget author);

	void setCreatedOn(String createdOn);

	void setNewReplyModal(Widget w);

	void setAlert(Widget w);

	void setLoadMoreButtonVisibility(boolean visible);

	void clearReplies();

	void addActiveAuthor(Widget user);

	boolean isThreadCollapsed();

	boolean isReplyCollapsed();

	void setLoadingRepliesVisible(boolean visible);

	void setDeleteIconVisible(boolean visible);

	void showDeleteConfirm(String deleteConfirmMessage, AlertCallback deleteCallback);

	void setReplyButtonVisible(boolean visible);
	void setCommandsVisible(boolean visible);
	void showErrorMessage(String errorMessage);

	void setEditIconVisible(boolean visible);

	void setEditThreadModal(Widget w);

	void setEditedVisible(Boolean visible);

	void setLoadingMessageVisible(boolean visible);

	void showSuccess(String successTitle, String successMessage);

	void setThreadAuthor(Widget widget);

	void showThreadDetails();

	void hideThreadDetails();

	void showReplyDetails();

	void hideReplyDetails();

	void setThreadLink(String link);
	
	void setRefreshAlert(Widget w);
	void removeRefreshAlert();

	void setButtonContainerWidth(String width);
	void setPinIconVisible(boolean visible);
	void setUnpinIconVisible(boolean visible);
	void setPinnedIconVisible(boolean visible);
	void setIsAuthorModerator(boolean isModerator);
}
