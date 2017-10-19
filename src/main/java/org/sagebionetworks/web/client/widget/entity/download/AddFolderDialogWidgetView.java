package org.sagebionetworks.web.client.widget.entity.download;

import com.google.gwt.user.client.ui.IsWidget;

public interface AddFolderDialogWidgetView extends IsWidget {

	void setPresenter(Presenter presenter);
	void hide();
	void show();
	void setSynAlert(IsWidget w);
	void setSharingAndDataUseWidget(IsWidget w);
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void updateFolderName(String newFolderName);
		void deleteFolder(boolean skipTrashCan);
	}
}
