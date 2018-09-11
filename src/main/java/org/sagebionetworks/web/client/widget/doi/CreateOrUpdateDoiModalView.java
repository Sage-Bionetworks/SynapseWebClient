package org.sagebionetworks.web.client.widget.doi;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface CreateOrUpdateDoiModalView extends IsWidget {
	
	public void setPresenter(Presenter presenter);
	public interface Presenter {
		void onCreateDoi();
	}
	void setWidgets(Widget authors, Widget titles, Widget publicationYear, Widget selectedResourceTypeGeneral);
	void show();
	void hide();
	void setTitle(String title);
	void setJobTrackingWidget(IsWidget w);
}
