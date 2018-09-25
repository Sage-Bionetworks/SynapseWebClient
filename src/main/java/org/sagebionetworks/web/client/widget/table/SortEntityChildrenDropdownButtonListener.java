package org.sagebionetworks.web.client.widget.table;

import org.sagebionetworks.repo.model.entity.Direction;
import org.sagebionetworks.repo.model.entity.SortBy;

public interface SortEntityChildrenDropdownButtonListener {
	/**
	 * called on sort
	 * @param sortColumnName
	 * @param sortDirection
	 */
	void onSort(SortBy sortColumn, Direction sortDirection);
}
