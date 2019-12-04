package org.sagebionetworks.web.client.widget.entity.file.downloadlist;

import org.sagebionetworks.repo.model.entity.query.SortDirection;
import org.sagebionetworks.web.client.widget.table.v2.results.SortingListener;
import com.google.gwt.user.client.ui.IsWidget;

public interface FileHandleAssociationTableView extends IsWidget {
	void clear();

	void clearRows();

	void addRow(IsWidget w);

	void showAccessRestrictionsDetectedUI();

	void setSortingListener(SortingListener sortingListener);

	void setSort(String headerName, SortDirection sortDir);

	void setScrollBarColumnVisible(boolean visible);
}
