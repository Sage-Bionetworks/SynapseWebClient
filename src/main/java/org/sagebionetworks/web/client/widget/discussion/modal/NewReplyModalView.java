package org.sagebionetworks.web.client.widget.discussion.modal;

import org.sagebionetworks.web.client.utils.Callback;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface NewReplyModalView extends IsWidget{

	public interface Presenter {
		/**
		 * Show the new reply modal.
		 */
		public void show();

		public void hide();

		Widget asWidget();

		void configure(String threadId, Callback newReplyCallback);

		void onSave();
	}

	void setPresenter(Presenter presenter);
	void showDialog();
	void hideDialog();
	String getMessageMarkdown();
	void clear();
	void setAlert(Widget w);
	void showSuccess();

}
