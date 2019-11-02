package org.sagebionetworks.web.client.widget.accessrequirements;

import com.google.gwt.user.client.ui.IsWidget;

public interface SubjectWidgetView extends IsWidget {
	void setPresenter(Presenter presenter);

	void setDeleteVisible(boolean visible);

	void setSubjectRendererWidget(IsWidget w);

	public interface Presenter {
		void onDelete();
	}
}
