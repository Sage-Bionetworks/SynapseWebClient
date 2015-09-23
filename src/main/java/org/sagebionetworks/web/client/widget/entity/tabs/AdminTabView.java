package org.sagebionetworks.web.client.widget.entity.tabs;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface AdminTabView extends IsWidget {
	public interface Presenter {
	}
	void setEvaluationList(Widget w);
}
