package org.sagebionetworks.web.client.widget.entity.tabs;

import org.sagebionetworks.repo.model.Entity;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface AdminTabView extends IsWidget {
	public interface Presenter {
	}
	void setEvaluationList(Widget w);
}
