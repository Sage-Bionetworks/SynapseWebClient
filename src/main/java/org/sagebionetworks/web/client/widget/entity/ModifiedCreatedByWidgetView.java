package org.sagebionetworks.web.client.widget.entity;

import com.google.gwt.user.client.ui.IsWidget;

public interface ModifiedCreatedByWidgetView extends IsWidget {
	
	public interface Presenter extends IsWidget {
		
	}

	void setCreatedOnText(String string);

	void setModifiedOnText(String string);

	void setModifiedBadge(IsWidget modifiedBadge);

	void setCreatedBadge(IsWidget createdBadge);

	void setVisible(boolean b);
}
