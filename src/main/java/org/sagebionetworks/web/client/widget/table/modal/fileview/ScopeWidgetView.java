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
	void setEntityListWidget(IsWidget entityListWidget);
	void setEditableEntityListWidget(IsWidget entityListWidget);
	void showEditScopeModal();
	
	public interface Presenter {
		void onSave();
	}
	void setPresenter(Presenter presenter);
}
