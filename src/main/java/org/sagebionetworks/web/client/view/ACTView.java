package org.sagebionetworks.web.client.view;

import java.util.List;

import org.sagebionetworks.web.client.SynapsePresenter;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.widget.search.PaginationEntry;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface ACTView extends IsWidget, SynapseView {
	void setPresenter(Presenter presenter);
	void setUserPickerWidget(Widget w);
	void clearRows();
	void addRow(Widget w);
	void setStates(List<String> states);
	
	void setSynAlert(Widget w);
	void updatePagination(List<PaginationEntry> entries);
	void setSelectedStateText(String state);
	void setSelectedUserBadge(Widget w);
	void setSelectedUserBadgeVisible(boolean visible);
	public interface Presenter extends SynapsePresenter {
		void onClearUserFilter();
		void onClearStateFilter();
		
		void loadData(Long offset);
		void onStateSelected(String state);
	}
}
