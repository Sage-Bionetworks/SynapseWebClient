package org.sagebionetworks.web.client.widget.entity.tabs;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface TablesTabView extends IsWidget {
	public interface Presenter {
	}

	void setTitle(String title);

	void setTitlebar(Widget w);

	void setTitlebarVisible(boolean visible);

	void setBreadcrumb(Widget w);

	void setBreadcrumbVisible(boolean visible);

	void setTableList(Widget w);

	void setTableListVisible(boolean visible);

	void setTableEntityWidget(Widget w);

	void clearTableEntityWidget();

	void setEntityMetadata(Widget w);

	void setEntityMetadataVisible(boolean visible);

	void setSynapseAlert(Widget w);

	void setModifiedCreatedBy(IsWidget modifiedCreatedBy);

	void setProvenance(IsWidget w);

	void setTableUIVisible(boolean visible);

	void setActionMenu(IsWidget w);

}
