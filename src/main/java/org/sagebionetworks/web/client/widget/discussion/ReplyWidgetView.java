package org.sagebionetworks.web.client.widget.discussion;

import org.gwtbootstrap3.extras.bootbox.client.callback.SimpleCallback;
import org.sagebionetworks.web.client.utils.Callback;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface ReplyWidgetView extends IsWidget{

	public interface Presenter {

		Widget asWidget();

		void onClickDeleteReply();

		void onClickEditReply();
		
		void onClickReplyLink();
	}

	void setPresenter(ReplyWidget presenter);

	void setAuthor(Widget widget);

	void setCreatedOn(String createdOn);

	void setMarkdownWidget(Widget widget);

	void clear();

	void setAlert(Widget w);

	void setDeleteIconVisibility(Boolean visible);

	void setEditIconVisible(boolean visible);

	void setEditReplyModal(Widget widget);

	void setEditedVisible(Boolean visible);

	void setLoadingMessageVisible(Boolean visible);

	void setMessageVisible(boolean visible);

	void showSuccess(String title, String message);
	
	void setIsAuthorModerator(boolean isModerator);

	void setCommandsContainerVisible(boolean visible);
	
	void setCopyTextModal(Widget widget);
}
