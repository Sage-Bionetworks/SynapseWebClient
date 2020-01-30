package org.sagebionetworks.web.client.widget.table.modal.fileview;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * View shows the first step of the wizard
 * 
 * @author Jay
 *
 */
public interface ScopeWidgetView extends IsWidget {

	void setVisible(boolean visible);

	void setEntityListWidget(IsWidget w);

	void setEditableEntityListWidget(IsWidget w);

	void setSynAlert(IsWidget w);

	void showModal();

	void hideModal();

	void setEditButtonVisible(boolean visible);

	void setLoading(boolean loading);

	void setViewTypeOptionsVisible(boolean visible);

	boolean isFileSelected();

	void setIsFileSelected(boolean selected);

	boolean isTableSelected();

	void setIsTableSelected(boolean selected);

	boolean isFolderSelected();

	void setIsFolderSelected(boolean selected);

	public interface Presenter {
		void onSave();

		void onEdit();

		void updateViewTypeMask();
	}

	void setPresenter(Presenter presenter);
}
