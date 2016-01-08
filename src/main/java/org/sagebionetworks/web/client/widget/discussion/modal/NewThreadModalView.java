package org.sagebionetworks.web.client.widget.discussion.modal;

import com.google.gwt.user.client.ui.IsWidget;

public interface NewThreadModalView extends IsWidget{

	public interface Presenter {
		/**
		 * Show the new thread modal.
		 */
		public void show();

		public void onSave();

		public void onCancel();

		public void hide();
	}

	void setPresenter(Presenter presenter);
	void showDialog();
	void hideDialog();

}
