package org.sagebionetworks.web.client.widget.entity.tabs;

import org.sagebionetworks.repo.model.Entity;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface TablesTabView extends IsWidget {
	public interface Presenter {
	}
	void setTitlebar(Widget w);
	void setBreadcrumb(Widget w);
	void setTableList(Widget w);
	void setTableEntityWidget(Widget w);
	void setEntityMetadata(Widget w);
	void setActionMenu(Widget w);
	void configureModifiedAndCreatedWidget(Entity entity);
}
