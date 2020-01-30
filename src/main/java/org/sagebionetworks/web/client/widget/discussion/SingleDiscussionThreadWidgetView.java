package org.sagebionetworks.web.client.widget.discussion;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface SingleDiscussionThreadWidgetView extends IsWidget {

	public interface Presenter {

		Widget asWidget();

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

	void setShowAllRepliesButtonVisible(boolean visible);

	void setCommandsVisible(boolean visible);

	void showErrorMessage(String errorMessage);

	void setEditIconVisible(boolean visible);

	void setEditThreadModal(Widget w);

	void setEditedLabelVisible(Boolean visible);

	void setLoadingMessageVisible(boolean visible);

	void setRepliesContainer(IsWidget container);

	void showSuccess(String successTitle, String successMessage);

	void setRefreshAlert(Widget w);

	void removeRefreshAlert();

	void setPinIconVisible(boolean visible);

	void setUnpinIconVisible(boolean visible);

	void setIsAuthorModerator(boolean isModerator);

	void setReplyContainersVisible(boolean visible);

	void setDeletedThreadVisible(boolean visible);

	void setReplyListContainerVisible(boolean visible);

	void setRestoreIconVisible(boolean visible);

	void setNewReplyContainer(Widget widget);

	void setSecondNewReplyContainer(Widget widget);

	void setSecondNewReplyContainerVisible(boolean visible);

	void setSubscribersWidget(Widget widget);

	void setSubscribersWidgetContainerVisible(boolean visible);
}
