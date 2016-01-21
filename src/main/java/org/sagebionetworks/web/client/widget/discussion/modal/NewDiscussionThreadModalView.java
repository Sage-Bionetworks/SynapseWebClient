package org.sagebionetworks.web.client.widget.discussion.modal;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface NewDiscussionThreadModalView extends IsWidget{

	public interface Presenter {
		/**
		 * Show the new thread modal.
		 */
		public void show();

		public void hide();

		Widget asWidget();

		void onSave();
	}

	void setPresenter(Presenter presenter);
	void showDialog();
	void hideDialog();
	String getTitle();
	String getMessageMarkdown();
	void clear();
	void setAlert(Widget w);
	void showSuccess();
	void setSendingRequestVisible(boolean visible);
	void setSaveButtonEnabled(boolean enabled);
	void setCancelButtonEnabled(boolean enabled);

}
