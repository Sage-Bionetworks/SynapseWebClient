package org.sagebionetworks.web.client.widget.table;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.entity.Direction;
import org.sagebionetworks.repo.model.entity.SortBy;
import org.sagebionetworks.web.client.SynapseView;
import com.google.gwt.user.client.ui.IsWidget;

public interface TableListWidgetView extends IsWidget, SynapseView {

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

	void clearTableWidgets();

	void addTableListItem(EntityHeader header);

	void setLoadMoreWidget(IsWidget w);

	void setSynAlert(IsWidget w);

	void clearSortUI();

	void setSortUI(SortBy sortBy, Direction dir);

	void hideLoading();

	void copyToClipboard(String ids);
}
