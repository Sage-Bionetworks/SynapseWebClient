package org.sagebionetworks.web.client.widget.docker.modal;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface AddExternalRepoModalView extends IsWidget {

	public interface Presenter {

		void onSave();

	}

	void setPresenter(Presenter presenter);

	void showDialog();

	void hideDialog();

	String getRepoName();

	void clear();

	void setAlert(Widget widget);

	void showSuccess(String title, String message);

	void showSaving();

	void resetButton();

	void setModalTitle(String title);
}
