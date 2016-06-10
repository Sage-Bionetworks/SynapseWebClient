package org.sagebionetworks.web.client.widget.docker.modal;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface AddExternalRepoModalView extends IsWidget{

	public interface Presenter {

		void onSave();
		
	}

	void setPresenter(Presenter presenter);

	void showDialog();

	void hideDialog();

	String getRegistryHost();

	String getRepoPath();

	String getDigest();

	String getTag();

	void clear();

	void setAlert(Widget widget);

	void showSuccess(String title, String message);

	void showSaving();

	void resetButton();
}
