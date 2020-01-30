package org.sagebionetworks.web.client.widget.table.modal.fileview;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * View shows the first step of the wizard
 * 
 * @author Jay
 *
 */
public interface CreateTableViewWizardStep1View extends IsWidget {

	/**
	 * Name of entity chosen by the user
	 * 
	 */
	String getName();

	void setName(String name);

	/**
	 * Add widget to set/get scope.
	 */
	void setScopeWidget(IsWidget scopeWidget);

	void setScopeWidgetVisible(boolean visible);

	void setViewTypeOptionsVisible(boolean visible);

	boolean isFileSelected();

	void setIsFileSelected(boolean selected);

	boolean isTableSelected();

	void setIsTableSelected(boolean selected);

	boolean isFolderSelected();

	void setIsFolderSelected(boolean selected);

	public interface Presenter {
		void updateViewTypeMask();
	}

	void setPresenter(Presenter presenter);

}
