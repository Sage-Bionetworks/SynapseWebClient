package org.sagebionetworks.web.client.widget.entity.file.downloadlist;

import com.google.gwt.user.client.ui.IsWidget;

public interface FileHandleAssociationTableView extends IsWidget {
	void clear();
	void addRow(IsWidget w);
	void showAccessRestrictionsDetectedUI();
}
