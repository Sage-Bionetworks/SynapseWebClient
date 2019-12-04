package org.sagebionetworks.web.client.widget.discussion.modal;

import org.sagebionetworks.web.client.utils.Callback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface DiscussionThreadModalView extends IsWidget {

	public interface Presenter {

		public void show();

		public void hide();

		Widget asWidget();

		void onSave();

		void onCancel();
	}

	void setPresenter(Presenter presenter);

	void showDialog();

	void hideDialog();

	String getThreadTitle();

	void clear();

	void setAlert(Widget w);

	void showSuccess(String title, String message);

	void showSaving();

	void resetButton();

	void setThreadTitle(String currentTitle);

	void setModalTitle(String title);

	void setMarkdownEditor(Widget widget);

	void showConfirmDialog(String restoreTitle, String restoreMessage, Callback yesCallback, Callback noCallback);

}
