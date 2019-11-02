package org.sagebionetworks.web.client.widget.discussion;

import org.sagebionetworks.web.client.utils.Callback;
import com.google.gwt.user.client.ui.Widget;

public interface NewReplyWidgetView {

	public interface Presenter {

		void onSave();

		void onCancel();

		Widget asWidget();

		void onClickNewReply();

	}

	void setPresenter(Presenter newReplyWidget);

	void setAlert(Widget widget);

	void setMarkdownEditor(Widget widget);

	void showSaving();

	void resetButton();

	void showSuccess(String successTitle, String successMessage);

	void setReplyTextBoxVisible(boolean visible);

	void setNewReplyContainerVisible(boolean visible);

	Widget asWidget();

	void showErrorMessage(String error);

	void showConfirmDialog(String restoreTitle, String restoreMessage, Callback yesCallback, Callback noCallback);

	void scrollIntoView();
}
