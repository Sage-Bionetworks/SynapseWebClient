package org.sagebionetworks.web.client.widget.table;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.entity.Direction;
import org.sagebionetworks.repo.model.entity.SortBy;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.widget.table.modal.fileview.TableType;

import com.google.gwt.user.client.ui.IsWidget;

public interface TableListWidgetView extends IsWidget, SynapseView {

	public enum TableListWidgetViewState {
		/** Loading the initial rows to show. Should NOT be used to indicate "Load More" */
		LOADING,
		EMPTY,
		POPULATED,
		ERROR
	}

	/**
	 * Set the presenter.
	 * 
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);

	/**
	 * Presenter interface
	 */
	public interface Presenter {
		/**
		 * Report when a table is clicked
		 */
		void onTableClicked(EntityHeader header);

		void toggleSort(SortBy sortByColumn);

		void copyIDsToClipboard();
	}

	void setTableType(TableType tableType);

	void clearTableWidgets();

	void addTableListItem(final TableEntityListGroupItem item);

	void setLoadMoreWidget(IsWidget w);

	void setSynAlert(IsWidget w);

	void clearSortUI();

	void setSortUI(SortBy sortBy, Direction dir);

	void setState(TableListWidgetViewState state);

	void copyToClipboard(String ids);

	void setFileCountVisible(boolean visible);
}
