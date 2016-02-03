package org.sagebionetworks.web.client.widget.discussion;

import org.gwtbootstrap3.extras.bootbox.client.callback.AlertCallback;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface ReplyWidgetView extends IsWidget{

	public interface Presenter {

		Widget asWidget();

		void onClickDeleteReply();
	}

	void setPresenter(ReplyWidget presenter);

	void setAuthor(Widget widget);

	void setCreatedOn(String createdOn);

	void setMessage(String message);

	void clear();

	void setAlert(Widget w);

	void setDeleteButtonVisibility(Boolean visible);

	void showDeleteConfirm(String deleteConfirmMessage, AlertCallback callback);
}
