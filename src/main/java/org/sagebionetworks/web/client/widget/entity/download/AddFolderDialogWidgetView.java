package org.sagebionetworks.web.client.widget.entity.download;

import com.google.gwt.user.client.ui.IsWidget;

public interface AddFolderDialogWidgetView extends IsWidget {

	void setPresenter(Presenter presenter);

	void hide();

	void show();

	void setSynAlert(IsWidget w);

	void setSharingAndDataUseWidget(IsWidget w);

	void setSaveEnabled(boolean enabled);

	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void createFolder(String newFolderName);
	}
}
